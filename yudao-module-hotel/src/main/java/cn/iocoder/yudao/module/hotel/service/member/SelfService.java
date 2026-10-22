package cn.iocoder.yudao.module.hotel.service.member;

import cn.iocoder.yudao.module.hotel.dal.dataobject.member.MemberDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.order.OrderDO;
import cn.iocoder.yudao.module.hotel.dal.mysql.order.OrderMapper;
import cn.iocoder.yudao.module.hotel.enums.OrderStatusEnum;
import cn.iocoder.yudao.module.hotel.enums.PaymentStatusEnum;
import cn.iocoder.yudao.module.hotel.service.audit.BusinessDateService;
import cn.iocoder.yudao.module.hotel.service.order.OrderServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hotel.enums.ErrorCodeConstants.ORDER_NOT_EXISTS;
import static cn.iocoder.yudao.module.hotel.enums.HotelT2ErrorCodeConstants.*;

/**
 * 自助服务：会员自助入住 / 自助退房 / 积分累计。
 */
@Service
@Validated
@Slf4j
public class SelfService {

    @Resource
    private OrderMapper orderMapper;
    @Resource
    private OrderServiceImpl orderService;
    @Resource
    private MemberService memberService;
    @Resource
    private BusinessDateService businessDateService;

    /**
     * 自助入住：仅限「已确认 + 已支付 + 当天（或未到店的当日）订单」，转 30 并置房态已入住。
     */
    @Transactional(rollbackFor = Exception.class)
    public void selfCheckIn(Long memberId, Long orderId) {
        MemberDO member = memberService.validateMember(memberId);
        OrderDO order = orderId == null ? null : orderMapper.selectById(orderId);
        if (order == null) {
            throw exception(ORDER_NOT_EXISTS);
        }
        if (!Objects.equals(order.getStatus(), OrderStatusEnum.CONFIRMED.getStatus())) {
            throw exception(ERROR_SELF_CHECK_IN_NOT_ALLOWED);
        }
        if (!Objects.equals(order.getPaymentStatus(), PaymentStatusEnum.PAID.getStatus())) {
            throw exception(ERROR_SELF_CHECK_IN_NOT_ALLOWED);
        }
        LocalDate today = LocalDate.now();
        LocalDate businessDate = businessDateService.getCurrentBusinessDate();
        boolean dateMatched = Objects.equals(order.getCheckInDate(), today)
                || Objects.equals(order.getCheckInDate(), businessDate);
        if (!dateMatched) {
            throw exception(ERROR_SELF_CHECK_IN_NOT_ALLOWED);
        }
        orderService.checkIn(order.getId());

        // 会员自助下单时可能未绑定会员，入住时补绑，便于后续积分与订单归属
        if (order.getMemberId() == null) {
            OrderDO update = new OrderDO();
            update.setId(order.getId());
            update.setMemberId(member.getId());
            orderMapper.updateById(update);
        }
    }

    /**
     * 自助退房：转 40、房态转待清洁、生成清洁任务，并按实付金额累计积分。
     */
    @Transactional(rollbackFor = Exception.class)
    public void selfCheckOut(Long memberId, Long orderId) {
        MemberDO member = memberService.validateMember(memberId);
        OrderDO order = orderId == null ? null : orderMapper.selectById(orderId);
        if (order == null) {
            throw exception(ORDER_NOT_EXISTS);
        }
        if (!Objects.equals(order.getStatus(), OrderStatusEnum.CHECKED_IN.getStatus())) {
            throw exception(ERROR_SELF_CHECK_OUT_NOT_ALLOWED);
        }
        orderService.checkOut(order.getId());

        OrderDO latest = orderMapper.selectById(order.getId());
        Long pointsMemberId = latest.getMemberId() != null ? latest.getMemberId() : member.getId();
        BigDecimal paid = latest.getPaidAmount() == null ? BigDecimal.ZERO : latest.getPaidAmount();
        int points = memberService.accruePointsForOrder(pointsMemberId, latest.getId(), paid);
        log.info("[selfCheckOut] 订单 {} 会员 {} 累计积分 {}", latest.getOrderNo(), pointsMemberId, points);
    }
}
