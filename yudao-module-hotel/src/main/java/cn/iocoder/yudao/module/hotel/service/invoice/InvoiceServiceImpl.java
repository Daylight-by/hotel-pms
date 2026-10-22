package cn.iocoder.yudao.module.hotel.service.invoice;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.invoice.vo.InvoicePageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.invoice.vo.InvoiceRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.invoice.vo.InvoiceSaveReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.folio.FolioDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.guest.GuestDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.invoice.InvoiceDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.order.OrderDO;
import cn.iocoder.yudao.module.hotel.dal.mysql.folio.FolioMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.guest.GuestMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.invoice.InvoiceMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.order.OrderMapper;
import cn.iocoder.yudao.module.hotel.enums.InvoiceStatusEnum;
import cn.iocoder.yudao.module.hotel.enums.InvoiceTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.hotel.enums.ErrorCodeConstants.*;

/**
 * 发票 Service 实现
 */
@Service
@Validated
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

    private static final DateTimeFormatter INVOICE_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Resource
    private InvoiceMapper invoiceMapper;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private GuestMapper guestMapper;
    @Resource
    private FolioMapper folioMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long issueInvoice(InvoiceSaveReqVO reqVO) {
        OrderDO order = null;
        if (reqVO.getOrderId() != null) {
            order = orderMapper.selectById(reqVO.getOrderId());
            if (order == null) {
                throw exception(ORDER_NOT_EXISTS);
            }
        }
        Long guestId = reqVO.getGuestId() != null ? reqVO.getGuestId()
                : (order == null ? null : order.getGuestId());
        if (guestId == null) {
            throw exception(GUEST_NOT_EXISTS);
        }
        BigDecimal amount = reqVO.getAmount();
        BigDecimal issuedTotal = invoiceMapper.selectListByOrderId(reqVO.getOrderId(),
                        InvoiceStatusEnum.ISSUED.getStatus()).stream()
                .map(i -> i.getAmount() == null ? BigDecimal.ZERO : i.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal allowed = resolveAllowedAmount(order);
        if (amount.add(issuedTotal).compareTo(allowed) > 0) {
            throw exception(ERROR_INVOICE_AMOUNT_INVALID);
        }

        InvoiceDO invoice = new InvoiceDO();
        invoice.setInvoiceNo(reqVO.getInvoiceNo() != null && !reqVO.getInvoiceNo().trim().isEmpty()
                ? reqVO.getInvoiceNo().trim() : generateInvoiceNo());
        invoice.setOrderId(reqVO.getOrderId());
        invoice.setGuestId(guestId);
        invoice.setTitle(reqVO.getTitle());
        invoice.setTaxNo(reqVO.getTaxNo());
        invoice.setType(reqVO.getType() == null ? InvoiceTypeEnum.NORMAL.getType() : reqVO.getType());
        invoice.setAmount(amount);
        invoice.setStatus(InvoiceStatusEnum.ISSUED.getStatus());
        invoice.setIssueTime(LocalDateTime.now());
        invoice.setOperatorId(getLoginUserId());
        invoice.setRemark(reqVO.getRemark());
        invoiceMapper.insert(invoice);

        if (order != null) {
            OrderDO update = new OrderDO();
            update.setId(order.getId());
            update.setInvoiceNo(invoice.getInvoiceNo());
            update.setInvoiceRequested(true);
            orderMapper.updateById(update);
        }
        return invoice.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long voidInvoice(Long id, String reason) {
        InvoiceDO invoice = id == null ? null : invoiceMapper.selectById(id);
        if (invoice == null) {
            throw exception(ERROR_INVOICE_ALREADY_VOID);
        }
        if (Objects.equals(invoice.getStatus(), InvoiceStatusEnum.VOIDED.getStatus())) {
            throw exception(ERROR_INVOICE_ALREADY_VOID);
        }
        InvoiceDO update = new InvoiceDO();
        update.setId(id);
        update.setStatus(InvoiceStatusEnum.VOIDED.getStatus());
        update.setVoidTime(LocalDateTime.now());
        update.setRemark(reason == null ? invoice.getRemark() : reason);
        invoiceMapper.updateById(update);

        if (invoice.getOrderId() != null) {
            OrderDO orderUpdate = new OrderDO();
            orderUpdate.setId(invoice.getOrderId());
            orderUpdate.setInvoiceNo("");
            orderUpdate.setInvoiceRequested(false);
            orderMapper.updateById(orderUpdate);
        }
        return id;
    }

    @Override
    public PageResult<InvoiceRespVO> getInvoicePage(InvoicePageReqVO reqVO) {
        PageResult<InvoiceDO> page = invoiceMapper.selectPage(reqVO);
        if (page.getList().isEmpty()) {
            return new PageResult<>(new ArrayList<>(), page.getTotal());
        }
        Set<Long> orderIds = page.getList().stream().map(InvoiceDO::getOrderId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, OrderDO> orderMap = orderIds.isEmpty() ? Collections.emptyMap()
                : orderMapper.selectBatchIds(orderIds).stream()
                .collect(Collectors.toMap(OrderDO::getId, o -> o, (a, b) -> a));
        Set<Long> guestIds = page.getList().stream().map(InvoiceDO::getGuestId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, GuestDO> guestMap = guestIds.isEmpty() ? Collections.emptyMap()
                : guestMapper.selectBatchIds(guestIds).stream()
                .collect(Collectors.toMap(GuestDO::getId, g -> g, (a, b) -> a));

        List<InvoiceRespVO> list = new ArrayList<>(page.getList().size());
        for (InvoiceDO invoice : page.getList()) {
            InvoiceRespVO vo = new InvoiceRespVO();
            vo.setId(invoice.getId());
            vo.setInvoiceNo(invoice.getInvoiceNo());
            vo.setOrderId(invoice.getOrderId());
            vo.setGuestId(invoice.getGuestId());
            vo.setTitle(invoice.getTitle());
            vo.setTaxNo(invoice.getTaxNo());
            vo.setType(invoice.getType());
            vo.setTypeName(Arrays.stream(InvoiceTypeEnum.values())
                    .filter(e -> Objects.equals(e.getType(), invoice.getType()))
                    .map(InvoiceTypeEnum::getName).findFirst().orElse(""));
            vo.setAmount(invoice.getAmount());
            vo.setStatus(invoice.getStatus());
            vo.setStatusName(Arrays.stream(InvoiceStatusEnum.values())
                    .filter(e -> Objects.equals(e.getStatus(), invoice.getStatus()))
                    .map(InvoiceStatusEnum::getName).findFirst().orElse(""));
            vo.setIssueTime(invoice.getIssueTime());
            vo.setVoidTime(invoice.getVoidTime());
            vo.setRemark(invoice.getRemark());
            vo.setCreateTime(invoice.getCreateTime());
            OrderDO order = invoice.getOrderId() == null ? null : orderMap.get(invoice.getOrderId());
            vo.setOrderNo(order == null ? null : order.getOrderNo());
            GuestDO guest = invoice.getGuestId() == null ? null : guestMap.get(invoice.getGuestId());
            vo.setGuestName(guest == null ? null : guest.getName());
            list.add(vo);
        }
        return new PageResult<>(list, page.getTotal());
    }

    /**
     * 可开票额度：优先取订单账页消费合计，没有账页时取订单金额
     */
    private BigDecimal resolveAllowedAmount(OrderDO order) {
        if (order == null) {
            return BigDecimal.ZERO;
        }
        FolioDO folio = folioMapper.selectMainByOrderId(order.getId());
        if (folio != null && folio.getTotalCharge() != null
                && folio.getTotalCharge().compareTo(BigDecimal.ZERO) > 0) {
            return folio.getTotalCharge();
        }
        return order.getAmount() == null ? BigDecimal.ZERO : order.getAmount();
    }

    private String generateInvoiceNo() {
        return "FP" + LocalDateTime.now().format(INVOICE_NO_FORMAT)
                + String.format("%03d", new Random().nextInt(1000));
    }
}
