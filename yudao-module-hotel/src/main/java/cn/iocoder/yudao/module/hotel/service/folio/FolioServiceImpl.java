package cn.iocoder.yudao.module.hotel.service.folio;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.folio.vo.*;
import cn.iocoder.yudao.module.hotel.dal.dataobject.folio.FolioDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.folio.FolioItemDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.folio.PaymentRecordDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.guest.GuestDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.order.OrderDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.room.RoomDO;
import cn.iocoder.yudao.module.hotel.dal.mysql.folio.FolioItemMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.folio.FolioMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.folio.PaymentRecordMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.guest.GuestMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.order.OrderMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.room.RoomMapper;
import cn.iocoder.yudao.module.hotel.enums.*;
import cn.iocoder.yudao.module.hotel.service.audit.BusinessDateService;
import cn.iocoder.yudao.module.hotel.service.operationlog.annotation.HotelOperationLog;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.hotel.enums.ErrorCodeConstants.*;

/**
 * 账务账页 Service 实现
 */
@Service
@Validated
@Slf4j
public class FolioServiceImpl implements FolioService {

    /** 参与合计的有效状态：有效 + 已冲销（冲销通过反向明细体现，原始凭证仍保留在合计中） */
    private static final List<Integer> TOTAL_STATUSES = List.of(
            FolioItemStatusEnum.VALID.getStatus(), FolioItemStatusEnum.VOIDED.getStatus());

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private FolioMapper folioMapper;
    @Resource
    private FolioItemMapper folioItemMapper;
    @Resource
    private PaymentRecordMapper paymentRecordMapper;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private GuestMapper guestMapper;
    @Resource
    private RoomMapper roomMapper;
    @Resource
    private BusinessDateService businessDateService;
    @Resource
    private AdminUserApi adminUserApi;

    // ============================================================ 账页

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFolio(FolioCreateReqVO reqVO) {
        FolioDO folio = new FolioDO();
        folio.setFolioNo(generateFolioNo());
        folio.setOrderId(reqVO.getOrderId());
        folio.setGuestId(reqVO.getGuestId());
        folio.setRoomId(reqVO.getRoomId());
        folio.setType(reqVO.getType() == null ? FolioTypeEnum.MAIN.getType() : reqVO.getType());
        folio.setStatus(FolioStatusEnum.UNSETTLED.getStatus());
        folio.setTotalCharge(BigDecimal.ZERO);
        folio.setTotalPayment(BigDecimal.ZERO);
        folio.setBalance(BigDecimal.ZERO);
        folio.setRemark(reqVO.getRemark());
        folioMapper.insert(folio);
        return folio.getId();
    }

    @Override
    public Long ensureFolioForOrder(Long orderId, Long guestId, Long roomId) {
        if (orderId == null) {
            return null;
        }
        FolioDO exists = folioMapper.selectMainByOrderId(orderId);
        if (exists != null) {
            return exists.getId();
        }
        FolioCreateReqVO reqVO = new FolioCreateReqVO();
        reqVO.setOrderId(orderId);
        reqVO.setGuestId(guestId);
        reqVO.setRoomId(roomId);
        reqVO.setType(FolioTypeEnum.MAIN.getType());
        reqVO.setRemark("订单自动开账");
        return createFolio(reqVO);
    }

    @Override
    public FolioRespVO getFolio(Long id) {
        FolioDO folio = validateFolio(id);
        return buildFolioRespVO(folio, true);
    }

    @Override
    public FolioRespVO getFolioByOrder(Long orderId) {
        FolioDO folio = folioMapper.selectMainByOrderId(orderId);
        if (folio == null) {
            List<FolioDO> list = folioMapper.selectListByOrderId(orderId);
            folio = list.isEmpty() ? null : list.get(0);
        }
        if (folio == null) {
            throw exception(ERROR_FOLIO_NOT_EXIST);
        }
        return buildFolioRespVO(folio, true);
    }

    // ============================================================ 入账 / 收款

    @Override
    @Transactional(rollbackFor = Exception.class)
    @HotelOperationLog(module = "folio", operation = "入账")
    public Long postCharge(FolioPostReqVO reqVO) {
        FolioDO folio = validateFolioOperable(reqVO.getFolioId());
        BigDecimal amount = resolveAmount(reqVO.getQuantity(), reqVO.getUnitPrice(), reqVO.getAmount());
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(ERROR_PAYMENT_AMOUNT_INVALID);
        }
        Integer itemType = reqVO.getItemType() == null ? FolioItemTypeEnum.OTHER.getType() : reqVO.getItemType();
        String itemName = reqVO.getItemName();
        if (itemName == null || itemName.trim().isEmpty()) {
            itemName = itemTypeName(itemType);
        }

        FolioItemDO item = new FolioItemDO();
        item.setFolioId(folio.getId());
        item.setOrderId(folio.getOrderId());
        item.setBusinessDate(reqVO.getBusinessDate() != null
                ? reqVO.getBusinessDate() : businessDateService.getCurrentBusinessDate());
        item.setItemType(itemType);
        item.setItemName(itemName);
        item.setDirection(FolioDirectionEnum.DEBIT.getDirection());
        item.setQuantity(reqVO.getQuantity() == null ? 1 : reqVO.getQuantity());
        item.setUnitPrice(reqVO.getUnitPrice() == null ? amount : reqVO.getUnitPrice());
        item.setAmount(amount);
        item.setStatus(FolioItemStatusEnum.VALID.getStatus());
        item.setOperatorId(getLoginUserId());
        item.setRemark(reqVO.getRemark());
        folioItemMapper.insert(item);

        recalculate(folio.getId());
        return item.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @HotelOperationLog(module = "folio", operation = "收款")
    public Long postPayment(FolioPaymentReqVO reqVO) {
        FolioDO folio = validateFolioOperable(reqVO.getFolioId());
        BigDecimal amount = reqVO.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) {
            throw exception(ERROR_PAYMENT_AMOUNT_INVALID);
        }
        boolean refund = amount.compareTo(BigDecimal.ZERO) < 0;
        Integer method = reqVO.getPaymentMethod() == null ? PaymentMethodEnum.CASH.getMethod() : reqVO.getPaymentMethod();
        LocalDateTime now = LocalDateTime.now();

        FolioItemDO item = new FolioItemDO();
        item.setFolioId(folio.getId());
        item.setOrderId(folio.getOrderId());
        item.setBusinessDate(businessDateService.getCurrentBusinessDate());
        item.setItemType(refund ? FolioItemTypeEnum.REFUND.getType() : FolioItemTypeEnum.OTHER.getType());
        item.setItemName((refund ? "退款-" : "收款-") + paymentMethodName(method));
        item.setDirection(FolioDirectionEnum.CREDIT.getDirection());
        item.setQuantity(1);
        item.setUnitPrice(amount);
        item.setAmount(amount);
        item.setPaymentMethod(method);
        item.setStatus(FolioItemStatusEnum.VALID.getStatus());
        item.setOperatorId(getLoginUserId());
        item.setRemark(reqVO.getRemark());
        folioItemMapper.insert(item);

        PaymentRecordDO record = new PaymentRecordDO();
        record.setFolioId(folio.getId());
        record.setOrderId(folio.getOrderId());
        record.setTradeNo(reqVO.getTradeNo());
        record.setAmount(amount);
        record.setMethod(method);
        record.setType(refund ? PaymentTypeEnum.REFUND.getType() : PaymentTypeEnum.INCOME.getType());
        record.setStatus(PaymentRecordStatusEnum.SUCCESS.getStatus());
        record.setPayTime(now);
        record.setOperatorId(getLoginUserId());
        record.setRemark(reqVO.getRemark());
        paymentRecordMapper.insert(record);

        recalculate(folio.getId());
        syncOrderPayment(folio.getOrderId());
        return item.getId();
    }

    // ============================================================ 冲销 / 转账 / 拆账

    @Override
    @Transactional(rollbackFor = Exception.class)
    @HotelOperationLog(module = "folio", operation = "冲销")
    public Long voidItem(Long itemId, String reason) {
        FolioItemDO item = folioItemMapper.selectById(itemId);
        if (item == null) {
            throw exception(ERROR_FOLIO_ITEM_NOT_EXIST);
        }
        if (!Objects.equals(item.getStatus(), FolioItemStatusEnum.VALID.getStatus())) {
            throw exception(ERROR_FOLIO_ITEM_ALREADY_VOID);
        }
        FolioDO folio = validateFolioOperable(item.getFolioId());

        FolioItemDO reversal = new FolioItemDO();
        reversal.setFolioId(item.getFolioId());
        reversal.setOrderId(item.getOrderId());
        reversal.setBusinessDate(businessDateService.getCurrentBusinessDate());
        reversal.setItemType(item.getItemType());
        reversal.setItemName("冲销：" + item.getItemName());
        reversal.setDirection(FolioDirectionEnum.CREDIT.getDirection());
        reversal.setQuantity(item.getQuantity());
        reversal.setUnitPrice(item.getUnitPrice() == null ? null : item.getUnitPrice().negate());
        reversal.setAmount(item.getAmount() == null ? BigDecimal.ZERO : item.getAmount().negate());
        reversal.setPaymentMethod(item.getPaymentMethod());
        reversal.setStatus(FolioItemStatusEnum.VALID.getStatus());
        reversal.setOperatorId(getLoginUserId());
        reversal.setRemark(reason);
        folioItemMapper.insert(reversal);

        FolioItemDO update = new FolioItemDO();
        update.setId(item.getId());
        update.setStatus(FolioItemStatusEnum.VOIDED.getStatus());
        update.setVoidId(reversal.getId());
        folioItemMapper.updateById(update);

        recalculate(folio.getId());
        return reversal.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long transferItem(Long itemId, Long targetFolioId) {
        FolioItemDO item = folioItemMapper.selectById(itemId);
        if (item == null) {
            throw exception(ERROR_FOLIO_ITEM_NOT_EXIST);
        }
        if (!Objects.equals(item.getStatus(), FolioItemStatusEnum.VALID.getStatus())) {
            throw exception(ERROR_FOLIO_ITEM_ALREADY_VOID);
        }
        if (Objects.equals(item.getFolioId(), targetFolioId)) {
            throw exception(ERROR_FOLIO_NOT_EXIST);
        }
        FolioDO source = validateFolioOperable(item.getFolioId());
        FolioDO target = validateFolioOperable(targetFolioId);

        FolioItemDO copy = new FolioItemDO();
        copy.setFolioId(target.getId());
        copy.setOrderId(item.getOrderId());
        copy.setBusinessDate(item.getBusinessDate());
        copy.setItemType(item.getItemType());
        copy.setItemName(item.getItemName());
        copy.setDirection(item.getDirection());
        copy.setQuantity(item.getQuantity());
        copy.setUnitPrice(item.getUnitPrice());
        copy.setAmount(item.getAmount());
        copy.setPaymentMethod(item.getPaymentMethod());
        copy.setStatus(FolioItemStatusEnum.VALID.getStatus());
        copy.setOperatorId(getLoginUserId());
        copy.setRemark("由账页 " + source.getFolioNo() + " 转入");
        folioItemMapper.insert(copy);

        FolioItemDO update = new FolioItemDO();
        update.setId(item.getId());
        update.setStatus(FolioItemStatusEnum.TRANSFERRED.getStatus());
        update.setTransferToFolioId(target.getId());
        folioItemMapper.updateById(update);

        recalculate(source.getId());
        recalculate(target.getId());
        return copy.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long splitFolio(Long folioId, List<Long> itemIds, String remark) {
        FolioDO source = validateFolioOperable(folioId);
        if (itemIds == null || itemIds.isEmpty()) {
            throw exception(ERROR_FOLIO_ITEM_NOT_EXIST);
        }
        FolioCreateReqVO createReqVO = new FolioCreateReqVO();
        createReqVO.setOrderId(source.getOrderId());
        createReqVO.setGuestId(source.getGuestId());
        createReqVO.setRoomId(source.getRoomId());
        createReqVO.setType(FolioTypeEnum.SPLIT.getType());
        createReqVO.setRemark(remark == null ? "由账页 " + source.getFolioNo() + " 拆出" : remark);
        Long targetId = createFolio(createReqVO);

        for (Long itemId : itemIds) {
            FolioItemDO item = folioItemMapper.selectById(itemId);
            if (item == null || !Objects.equals(item.getFolioId(), folioId)
                    || !Objects.equals(item.getStatus(), FolioItemStatusEnum.VALID.getStatus())) {
                continue;
            }
            transferItem(itemId, targetId);
        }
        return targetId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long settleFolio(Long folioId) {
        FolioDO folio = validateFolio(folioId);
        if (Objects.equals(folio.getStatus(), FolioStatusEnum.SETTLED.getStatus())) {
            throw exception(ERROR_FOLIO_ALREADY_SETTLED);
        }
        recalculate(folioId);
        FolioDO latest = folioMapper.selectById(folioId);
        BigDecimal balance = latest.getBalance() == null ? BigDecimal.ZERO : latest.getBalance();
        if (balance.compareTo(BigDecimal.ZERO) != 0) {
            throw exception(ERROR_FOLIO_BALANCE_NOT_ZERO);
        }
        folioMapper.updateStatus(folioId, FolioStatusEnum.SETTLED.getStatus());
        return folioId;
    }

    @Override
    public PageResult<FolioItemRespVO> getFolioItemPage(FolioItemPageReqVO reqVO) {
        PageResult<FolioItemDO> page = folioItemMapper.selectPage(reqVO);
        return new PageResult<>(buildItemRespList(page.getList()), page.getTotal());
    }

    @Override
    public void recalculate(Long folioId) {
        if (folioId == null) {
            return;
        }
        List<FolioItemDO> items = folioItemMapper.selectListByFolioId(folioId);
        BigDecimal charge = BigDecimal.ZERO;
        BigDecimal payment = BigDecimal.ZERO;
        for (FolioItemDO item : items) {
            if (!TOTAL_STATUSES.contains(item.getStatus())) {
                continue;
            }
            BigDecimal amount = item.getAmount() == null ? BigDecimal.ZERO : item.getAmount();
            if (Objects.equals(item.getDirection(), FolioDirectionEnum.DEBIT.getDirection())) {
                charge = charge.add(amount);
            } else {
                payment = payment.add(amount);
            }
        }
        folioMapper.updateTotals(folioId, charge, payment);
    }

    @Override
    public BigDecimal getBalance(Long folioId) {
        FolioDO folio = folioMapper.selectById(folioId);
        return folio == null || folio.getBalance() == null ? BigDecimal.ZERO : folio.getBalance();
    }

    // ============================================================ 私有方法

    private BigDecimal resolveAmount(Integer quantity, BigDecimal unitPrice, BigDecimal amount) {
        if (amount != null) {
            return amount;
        }
        int qty = quantity == null ? 1 : quantity;
        BigDecimal price = unitPrice == null ? BigDecimal.ZERO : unitPrice;
        return price.multiply(BigDecimal.valueOf(qty));
    }

    private FolioDO validateFolio(Long id) {
        FolioDO folio = id == null ? null : folioMapper.selectById(id);
        if (folio == null) {
            throw exception(ERROR_FOLIO_NOT_EXIST);
        }
        return folio;
    }

    private FolioDO validateFolioOperable(Long id) {
        FolioDO folio = validateFolio(id);
        if (Objects.equals(folio.getStatus(), FolioStatusEnum.SETTLED.getStatus())
                || Objects.equals(folio.getStatus(), FolioStatusEnum.CANCELLED.getStatus())) {
            throw exception(ERROR_FOLIO_ALREADY_SETTLED);
        }
        return folio;
    }

    private String generateFolioNo() {
        String prefix = "F" + LocalDate.now().format(DATE_FORMAT);
        String max = folioMapper.selectMaxFolioNoByPrefix(prefix);
        int seq = 1;
        if (max != null && max.length() > prefix.length()) {
            try {
                seq = Integer.parseInt(max.substring(prefix.length())) + 1;
            } catch (NumberFormatException ignored) {
                seq = 1;
            }
        }
        return prefix + String.format("%06d", seq);
    }

    private FolioRespVO buildFolioRespVO(FolioDO folio, boolean withItems) {
        FolioRespVO vo = new FolioRespVO();
        vo.setId(folio.getId());
        vo.setFolioNo(folio.getFolioNo());
        vo.setOrderId(folio.getOrderId());
        vo.setGuestId(folio.getGuestId());
        vo.setRoomId(folio.getRoomId());
        vo.setType(folio.getType());
        vo.setStatus(folio.getStatus());
        vo.setStatusName(folioStatusName(folio.getStatus()));
        vo.setTotalCharge(folio.getTotalCharge());
        vo.setTotalPayment(folio.getTotalPayment());
        vo.setBalance(folio.getBalance());
        vo.setRemark(folio.getRemark());
        vo.setCreateTime(folio.getCreateTime());

        if (folio.getGuestId() != null) {
            GuestDO guest = guestMapper.selectById(folio.getGuestId());
            vo.setGuestName(guest == null ? null : guest.getName());
        }
        if (folio.getRoomId() != null) {
            RoomDO room = roomMapper.selectById(folio.getRoomId());
            vo.setRoomNo(room == null ? null : room.getRoomNo());
        }
        if (folio.getOrderId() != null) {
            OrderDO order = orderMapper.selectById(folio.getOrderId());
            vo.setOrderNo(order == null ? null : order.getOrderNo());
        }
        if (withItems) {
            vo.setItems(buildItemRespList(folioItemMapper.selectListByFolioId(folio.getId())));
        }
        return vo;
    }

    private List<FolioItemRespVO> buildItemRespList(List<FolioItemDO> items) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> operatorIds = items.stream().map(FolioItemDO::getOperatorId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, AdminUserRespDTO> userMap = operatorIds.isEmpty() ? Collections.emptyMap()
                : adminUserApi.getUserMap(operatorIds);
        List<FolioItemRespVO> list = new ArrayList<>(items.size());
        for (FolioItemDO item : items) {
            FolioItemRespVO vo = new FolioItemRespVO();
            vo.setId(item.getId());
            vo.setFolioId(item.getFolioId());
            vo.setOrderId(item.getOrderId());
            vo.setBusinessDate(item.getBusinessDate());
            vo.setItemType(item.getItemType());
            vo.setItemTypeName(itemTypeName(item.getItemType()));
            vo.setItemName(item.getItemName());
            vo.setDirection(item.getDirection());
            vo.setDirectionName(directionName(item.getDirection()));
            vo.setQuantity(item.getQuantity());
            vo.setUnitPrice(item.getUnitPrice());
            vo.setAmount(item.getAmount());
            vo.setPaymentMethod(item.getPaymentMethod());
            vo.setPaymentMethodName(paymentMethodName(item.getPaymentMethod()));
            vo.setStatus(item.getStatus());
            vo.setStatusName(itemStatusName(item.getStatus()));
            vo.setVoidId(item.getVoidId());
            vo.setTransferToFolioId(item.getTransferToFolioId());
            vo.setOperatorId(item.getOperatorId());
            AdminUserRespDTO user = item.getOperatorId() == null ? null : userMap.get(item.getOperatorId());
            vo.setOperatorName(user == null ? null : user.getNickname());
            vo.setRemark(item.getRemark());
            vo.setCreateTime(item.getCreateTime());
            list.add(vo);
        }
        return list;
    }

    /**
     * 回写订单已收金额与支付状态
     */
    private void syncOrderPayment(Long orderId) {
        if (orderId == null) {
            return;
        }
        OrderDO order = orderMapper.selectById(orderId);
        if (order == null) {
            return;
        }
        List<PaymentRecordDO> records = paymentRecordMapper.selectListByOrderId(orderId);
        BigDecimal paid = records.stream()
                .filter(r -> Objects.equals(r.getStatus(), PaymentRecordStatusEnum.SUCCESS.getStatus()))
                .map(r -> r.getAmount() == null ? BigDecimal.ZERO : r.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal amount = order.getAmount() == null ? BigDecimal.ZERO : order.getAmount();
        Integer paymentStatus;
        if (paid.compareTo(amount) >= 0 && amount.compareTo(BigDecimal.ZERO) > 0) {
            paymentStatus = PaymentStatusEnum.PAID.getStatus();
        } else if (paid.compareTo(BigDecimal.ZERO) > 0) {
            paymentStatus = PaymentStatusEnum.PARTIAL.getStatus();
        } else if (paid.compareTo(BigDecimal.ZERO) < 0) {
            paymentStatus = PaymentStatusEnum.REFUNDED.getStatus();
        } else {
            paymentStatus = PaymentStatusEnum.UNPAID.getStatus();
        }
        OrderDO update = new OrderDO();
        update.setId(orderId);
        update.setPaidAmount(paid);
        update.setPaymentStatus(paymentStatus);
        orderMapper.updateById(update);
    }

    private String folioStatusName(Integer status) {
        return Arrays.stream(FolioStatusEnum.values()).filter(e -> Objects.equals(e.getStatus(), status))
                .map(FolioStatusEnum::getName).findFirst().orElse("");
    }

    private String itemTypeName(Integer type) {
        return Arrays.stream(FolioItemTypeEnum.values()).filter(e -> Objects.equals(e.getType(), type))
                .map(FolioItemTypeEnum::getName).findFirst().orElse("");
    }

    private String directionName(Integer direction) {
        return Arrays.stream(FolioDirectionEnum.values()).filter(e -> Objects.equals(e.getDirection(), direction))
                .map(FolioDirectionEnum::getName).findFirst().orElse("");
    }

    private String itemStatusName(Integer status) {
        return Arrays.stream(FolioItemStatusEnum.values()).filter(e -> Objects.equals(e.getStatus(), status))
                .map(FolioItemStatusEnum::getName).findFirst().orElse("");
    }

    private String paymentMethodName(Integer method) {
        if (method == null) {
            return "";
        }
        return Arrays.stream(PaymentMethodEnum.values()).filter(e -> Objects.equals(e.getMethod(), method))
                .map(PaymentMethodEnum::getName).findFirst().orElse("");
    }
}
