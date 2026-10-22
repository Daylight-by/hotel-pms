package cn.iocoder.yudao.module.hotel.service.shift;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.shift.vo.ShiftCloseReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.shift.vo.ShiftHandoverRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.shift.vo.ShiftOpenReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.shift.vo.ShiftPageReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.folio.PaymentRecordDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.shift.ShiftHandoverDO;
import cn.iocoder.yudao.module.hotel.dal.mysql.folio.PaymentRecordMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.shift.ShiftHandoverMapper;
import cn.iocoder.yudao.module.hotel.enums.PaymentMethodEnum;
import cn.iocoder.yudao.module.hotel.enums.PaymentRecordStatusEnum;
import cn.iocoder.yudao.module.hotel.enums.PaymentTypeEnum;
import cn.iocoder.yudao.module.hotel.enums.ShiftStatusEnum;
import cn.iocoder.yudao.module.hotel.service.audit.BusinessDateService;
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
import static cn.iocoder.yudao.module.hotel.enums.ErrorCodeConstants.ERROR_SHIFT_ALREADY_OPEN;
import static cn.iocoder.yudao.module.hotel.enums.ErrorCodeConstants.ERROR_SHIFT_NOT_OPEN;

/**
 * 交班 Service 实现
 *
 * <p>统计口径：pay_time ∈ [start_time, end_time) 且 type=收款、status=成功 的支付流水，按支付方式分组。</p>
 */
@Service
@Validated
@Slf4j
public class ShiftServiceImpl implements ShiftService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Resource
    private ShiftHandoverMapper shiftMapper;
    @Resource
    private PaymentRecordMapper paymentRecordMapper;
    @Resource
    private BusinessDateService businessDateService;
    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long openShift(ShiftOpenReqVO reqVO) {
        ShiftHandoverDO running = shiftMapper.selectByStatus(ShiftStatusEnum.PROCESSING.getStatus());
        if (running != null) {
            throw exception(ERROR_SHIFT_ALREADY_OPEN);
        }
        ShiftHandoverDO shift = new ShiftHandoverDO();
        shift.setShiftNo(generateShiftNo());
        shift.setBizDate(businessDateService.getCurrentBusinessDate());
        shift.setStartTime(LocalDateTime.now());
        shift.setOperatorId(reqVO.getOperatorId() != null ? reqVO.getOperatorId() : getLoginUserId());
        shift.setCashAmount(BigDecimal.ZERO);
        shift.setCardAmount(BigDecimal.ZERO);
        shift.setWechatAmount(BigDecimal.ZERO);
        shift.setAlipayAmount(BigDecimal.ZERO);
        shift.setOtherAmount(BigDecimal.ZERO);
        shift.setTotalAmount(BigDecimal.ZERO);
        shift.setStatus(ShiftStatusEnum.PROCESSING.getStatus());
        shift.setRemark(reqVO.getRemark());
        shiftMapper.insert(shift);
        return shift.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long closeShift(ShiftCloseReqVO reqVO) {
        ShiftHandoverDO shift = reqVO.getId() != null ? shiftMapper.selectById(reqVO.getId())
                : shiftMapper.selectByStatus(ShiftStatusEnum.PROCESSING.getStatus());
        if (shift == null) {
            throw exception(ERROR_SHIFT_NOT_OPEN);
        }
        if (Objects.equals(shift.getStatus(), ShiftStatusEnum.CLOSED.getStatus())) {
            throw exception(ERROR_SHIFT_NOT_OPEN);
        }
        LocalDateTime endTime = LocalDateTime.now();
        Map<Integer, BigDecimal> amounts = sumPayments(shift.getStartTime(), endTime);

        ShiftHandoverDO update = new ShiftHandoverDO();
        update.setId(shift.getId());
        update.setEndTime(endTime);
        update.setCashAmount(amounts.getOrDefault(PaymentMethodEnum.CASH.getMethod(), BigDecimal.ZERO));
        update.setCardAmount(amounts.getOrDefault(PaymentMethodEnum.CARD.getMethod(), BigDecimal.ZERO));
        update.setWechatAmount(amounts.getOrDefault(PaymentMethodEnum.WECHAT.getMethod(), BigDecimal.ZERO));
        update.setAlipayAmount(amounts.getOrDefault(PaymentMethodEnum.ALIPAY.getMethod(), BigDecimal.ZERO));
        BigDecimal other = BigDecimal.ZERO;
        for (Map.Entry<Integer, BigDecimal> entry : amounts.entrySet()) {
            Integer method = entry.getKey();
            if (!Objects.equals(method, PaymentMethodEnum.CASH.getMethod())
                    && !Objects.equals(method, PaymentMethodEnum.CARD.getMethod())
                    && !Objects.equals(method, PaymentMethodEnum.WECHAT.getMethod())
                    && !Objects.equals(method, PaymentMethodEnum.ALIPAY.getMethod())) {
                other = other.add(entry.getValue());
            }
        }
        update.setOtherAmount(other);
        BigDecimal total = update.getCashAmount().add(update.getCardAmount())
                .add(update.getWechatAmount()).add(update.getAlipayAmount()).add(other);
        update.setTotalAmount(total);
        update.setStatus(ShiftStatusEnum.CLOSED.getStatus());
        if (reqVO.getRemark() != null) {
            update.setRemark(reqVO.getRemark());
        }
        shiftMapper.updateById(update);
        return shift.getId();
    }

    @Override
    public ShiftHandoverRespVO getCurrentShift() {
        ShiftHandoverDO shift = shiftMapper.selectByStatus(ShiftStatusEnum.PROCESSING.getStatus());
        return shift == null ? null : buildRespVOList(List.of(shift)).get(0);
    }

    @Override
    public PageResult<ShiftHandoverRespVO> getShiftPage(ShiftPageReqVO reqVO) {
        PageResult<ShiftHandoverDO> page = shiftMapper.selectPage(reqVO);
        return new PageResult<>(buildRespVOList(page.getList()), page.getTotal());
    }

    private Map<Integer, BigDecimal> sumPayments(LocalDateTime begin, LocalDateTime end) {
        List<PaymentRecordDO> records = paymentRecordMapper.selectIncomeBetween(begin, end,
                PaymentTypeEnum.INCOME.getType(), PaymentRecordStatusEnum.SUCCESS.getStatus());
        Map<Integer, BigDecimal> result = new HashMap<>();
        for (PaymentRecordDO record : records) {
            Integer method = record.getMethod() == null ? PaymentMethodEnum.CASH.getMethod() : record.getMethod();
            BigDecimal amount = record.getAmount() == null ? BigDecimal.ZERO : record.getAmount();
            result.merge(method, amount, BigDecimal::add);
        }
        return result;
    }

    private String generateShiftNo() {
        String prefix = "SH" + LocalDate.now().format(DATE_FORMAT);
        String max = shiftMapper.selectMaxShiftNoByPrefix(prefix);
        int seq = 1;
        if (max != null && max.length() > prefix.length()) {
            try {
                seq = Integer.parseInt(max.substring(prefix.length())) + 1;
            } catch (NumberFormatException ignored) {
                seq = 1;
            }
        }
        return prefix + String.format("%02d", seq);
    }

    private List<ShiftHandoverRespVO> buildRespVOList(List<ShiftHandoverDO> shifts) {
        if (shifts == null || shifts.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> operatorIds = shifts.stream().map(ShiftHandoverDO::getOperatorId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, AdminUserRespDTO> userMap = operatorIds.isEmpty() ? Collections.emptyMap()
                : adminUserApi.getUserMap(operatorIds);
        List<ShiftHandoverRespVO> list = new ArrayList<>(shifts.size());
        for (ShiftHandoverDO shift : shifts) {
            ShiftHandoverRespVO vo = new ShiftHandoverRespVO();
            vo.setId(shift.getId());
            vo.setShiftNo(shift.getShiftNo());
            vo.setBizDate(shift.getBizDate());
            vo.setStartTime(shift.getStartTime());
            vo.setEndTime(shift.getEndTime());
            vo.setOperatorId(shift.getOperatorId());
            AdminUserRespDTO user = shift.getOperatorId() == null ? null : userMap.get(shift.getOperatorId());
            vo.setOperatorName(user == null ? null : user.getNickname());
            vo.setCashAmount(shift.getCashAmount());
            vo.setCardAmount(shift.getCardAmount());
            vo.setWechatAmount(shift.getWechatAmount());
            vo.setAlipayAmount(shift.getAlipayAmount());
            vo.setOtherAmount(shift.getOtherAmount());
            vo.setTotalAmount(shift.getTotalAmount());
            vo.setStatus(shift.getStatus());
            vo.setStatusName(Arrays.stream(ShiftStatusEnum.values())
                    .filter(e -> Objects.equals(e.getStatus(), shift.getStatus()))
                    .map(ShiftStatusEnum::getName).findFirst().orElse(""));
            vo.setRemark(shift.getRemark());
            vo.setCreateTime(shift.getCreateTime());
            list.add(vo);
        }
        return list;
    }
}
