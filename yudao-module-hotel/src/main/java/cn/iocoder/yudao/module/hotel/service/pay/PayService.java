package cn.iocoder.yudao.module.hotel.service.pay;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.folio.vo.FolioPaymentReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.folio.vo.FolioPostReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.folio.vo.FolioRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.pay.vo.PayOrderPageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.pay.vo.PayOrderRespVO;
import cn.iocoder.yudao.module.hotel.controller.app.pay.vo.AppPayRespVO;
import cn.iocoder.yudao.module.hotel.controller.app.pay.vo.AppPayResultRespVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.member.MemberDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.order.OrderDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.pay.PayOrderDO;
import cn.iocoder.yudao.module.hotel.dal.mysql.order.OrderMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.pay.PayOrderMapper;
import cn.iocoder.yudao.module.hotel.enums.*;
import cn.iocoder.yudao.module.hotel.service.folio.FolioService;
import cn.iocoder.yudao.module.hotel.service.member.MemberService;
import cn.iocoder.yudao.module.hotel.service.pay.client.*;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hotel.enums.ErrorCodeConstants.ORDER_NOT_EXISTS;
import static cn.iocoder.yudao.module.hotel.enums.HotelT2ErrorCodeConstants.*;

/**
 * 轻量在线支付 Service（不依赖 yudao-module-pay）。
 *
 * <p>核心设计：</p>
 * <ol>
 *   <li>抽象 {@link PayClient}，提供 Mock / 微信 / 支付宝三个实现；演示环境默认 Mock；</li>
 *   <li>回调幂等：先用 {@code channel_trade_no} 唯一键去重，再以「status=10 → 20」条件更新，</li>
 *       只有更新成功（影响行数 = 1）的那次回调才入账，重复回调直接返回；</li>
 *   <li>支付成功：写账页贷方（收款）、回写订单 paid_amount、订单转已确认。</li>
 * </ol>
 */
@Service
@Validated
@Slf4j
public class PayService {

    private static final DateTimeFormatter PAY_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Resource
    private PayOrderMapper payOrderMapper;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private FolioService folioService;
    @Resource
    private MemberService memberService;
    @Resource
    private MockPayClient mockPayClient;
    @Resource
    private WxPayClient wxPayClient;
    @Resource
    private AliPayClient aliPayClient;

    @Value("${hotel.pay.mock-enabled:true}")
    private boolean mockEnabled;
    @Value("${hotel.pay.timeout-minutes:30}")
    private int timeoutMinutes;

    public boolean isMockEnabled() {
        return mockEnabled;
    }

    // ============================================================ 下单 / 查询

    /**
     * 为订单创建支付单（房费预付）。
     */
    @Transactional(rollbackFor = Exception.class)
    public AppPayRespVO createPayOrder(Long orderId, String channelCode) {
        OrderDO order = orderId == null ? null : orderMapper.selectById(orderId);
        if (order == null) {
            throw exception(ORDER_NOT_EXISTS);
        }
        if (Objects.equals(order.getStatus(), OrderStatusEnum.CANCELLED.getStatus())
                || Objects.equals(order.getStatus(), OrderStatusEnum.TIMEOUT_CLOSED.getStatus())
                || Objects.equals(order.getStatus(), OrderStatusEnum.CHECKED_OUT.getStatus())) {
            throw exception(ERROR_PAY_ORDER_STATUS_INVALID);
        }
        BigDecimal paid = order.getPaidAmount() == null ? BigDecimal.ZERO : order.getPaidAmount();
        BigDecimal payable = (order.getAmount() == null ? BigDecimal.ZERO : order.getAmount()).subtract(paid);
        if (payable.compareTo(BigDecimal.ZERO) <= 0) {
            throw exception(ERROR_PAY_ORDER_STATUS_INVALID);
        }
        PayChannelEnum channel = PayChannelEnum.of(channelCode == null ? "mock" : channelCode);
        if (channel == null) {
            throw exception(ERROR_PAY_CHANNEL_NOT_SUPPORT);
        }
        PayOrderDO payOrder = new PayOrderDO();
        payOrder.setPayOrderNo(generatePayOrderNo());
        payOrder.setBizOrderId(order.getId());
        payOrder.setBizType(PayBizTypeEnum.ROOM_FEE.getType());
        payOrder.setAmount(payable.setScale(2, RoundingMode.HALF_UP));
        payOrder.setChannel(channel.getCode());
        payOrder.setStatus(PayOrderStatusEnum.PENDING.getStatus());
        payOrder.setExpireTime(LocalDateTime.now().plusMinutes(timeoutMinutes));
        payOrder.setRefundAmount(BigDecimal.ZERO);
        payOrderMapper.insert(payOrder);

        PayClient client = resolveClient(channel);
        AppPayRespVO resp = new AppPayRespVO();
        resp.setPayOrderNo(payOrder.getPayOrderNo());
        resp.setOrderId(order.getId());
        resp.setAmount(payOrder.getAmount());
        resp.setChannel(payOrder.getChannel());
        resp.setStatus(payOrder.getStatus());
        resp.setPayParams(client.createPayParams(payOrder));
        resp.setMockEnabled(mockEnabled);
        resp.setExpireTime(payOrder.getExpireTime());
        return resp;
    }

    public PayOrderDO getPayOrderByNo(String payOrderNo) {
        PayOrderDO payOrder = payOrderNo == null ? null : payOrderMapper.selectByPayOrderNo(payOrderNo);
        if (payOrder == null) {
            throw exception(ERROR_PAY_ORDER_NOT_EXIST);
        }
        return payOrder;
    }

    public AppPayResultRespVO getPayResult(String payOrderNo) {
        PayOrderDO payOrder = getPayOrderByNo(payOrderNo);
        AppPayResultRespVO resp = new AppPayResultRespVO();
        resp.setPayOrderNo(payOrder.getPayOrderNo());
        resp.setOrderId(payOrder.getBizOrderId());
        resp.setAmount(payOrder.getAmount());
        resp.setChannel(payOrder.getChannel());
        resp.setStatus(payOrder.getStatus());
        resp.setStatusName(PayOrderStatusEnum.nameOf(payOrder.getStatus()));
        resp.setPayTime(payOrder.getPayTime());
        resp.setChannelTradeNo(payOrder.getChannelTradeNo());
        return resp;
    }

    // ============================================================ 支付成功（Mock / 回调）

    /**
     * 一键模拟支付成功（答辩演示用）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void mockSuccess(String payOrderNo) {
        if (!mockEnabled) {
            throw exception(ERROR_PAY_MOCK_DISABLED);
        }
        PayOrderDO payOrder = getPayOrderByNo(payOrderNo);
        if (Objects.equals(payOrder.getStatus(), PayOrderStatusEnum.PAID.getStatus())) {
            // 幂等：重复点击直接成功返回
            return;
        }
        if (!Objects.equals(payOrder.getStatus(), PayOrderStatusEnum.PENDING.getStatus())) {
            throw exception(ERROR_PAY_ORDER_STATUS_INVALID);
        }
        markPaid(payOrder, "MOCK" + payOrder.getPayOrderNo(), "{\"source\":\"mock-success\"}");
    }

    /**
     * 处理第三方支付回调，返回 true 表示已成功处理（可回复 success）。
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean handleNotify(String channelCode, String body, Map<String, String> params,
                                Map<String, String> headers) {
        PayChannelEnum channel = PayChannelEnum.of(channelCode);
        if (channel == null) {
            throw exception(ERROR_PAY_CHANNEL_NOT_SUPPORT);
        }
        PayClient client = resolveRealClient(channel);
        if (!client.verifySign(body, headers)) {
            throw exception(ERROR_PAY_NOTIFY_INVALID);
        }
        PayNotifyResult result = client.parseNotify(body, params);
        if (result == null || result.getPayOrderNo() == null) {
            throw exception(ERROR_PAY_NOTIFY_INVALID);
        }
        PayOrderDO payOrder = getPayOrderByNo(result.getPayOrderNo());
        if (!Boolean.TRUE.equals(result.getSuccess())) {
            log.warn("[handleNotify] 支付单 {} 回调非成功状态：{}", result.getPayOrderNo(), body);
            return false;
        }
        if (result.getAmount() != null
                && result.getAmount().compareTo(payOrder.getAmount()) != 0) {
            throw exception(ERROR_PAY_AMOUNT_MISMATCH);
        }
        if (Objects.equals(payOrder.getStatus(), PayOrderStatusEnum.PAID.getStatus())) {
            // 幂等：重复回调只入账一次
            return true;
        }
        markPaid(payOrder, result.getChannelTradeNo(), result.getRaw());
        return true;
    }

    /**
     * 支付成功入账：条件更新支付单 + 写账页贷方 + 回写订单。
     *
     * <p>幂等关键：{@code UPDATE ... WHERE id=? AND status=10}，只有影响行数为 1 才继续入账。</p>
     */
    @Transactional(rollbackFor = Exception.class)
    public void markPaid(PayOrderDO payOrder, String channelTradeNo, String raw) {
        if (channelTradeNo != null) {
            PayOrderDO exists = payOrderMapper.selectByChannelTradeNo(channelTradeNo);
            if (exists != null && !Objects.equals(exists.getId(), payOrder.getId())) {
                log.warn("[markPaid] 第三方交易号 {} 已被支付单 {} 使用，忽略重复回调", channelTradeNo, exists.getId());
                return;
            }
        }
        LocalDateTime now = LocalDateTime.now();
        PayOrderDO update = new PayOrderDO();
        update.setStatus(PayOrderStatusEnum.PAID.getStatus());
        update.setPayTime(now);
        update.setChannelTradeNo(channelTradeNo);
        update.setNotifyData(truncate(raw, 1900));
        int rows = payOrderMapper.update(update, new LambdaUpdateWrapper<PayOrderDO>()
                .eq(PayOrderDO::getId, payOrder.getId())
                .eq(PayOrderDO::getStatus, PayOrderStatusEnum.PENDING.getStatus()));
        if (rows == 0) {
            log.info("[markPaid] 支付单 {} 已被其它回调处理，跳过重复入账", payOrder.getPayOrderNo());
            return;
        }

        OrderDO order = orderMapper.selectById(payOrder.getBizOrderId());
        if (order == null) {
            log.warn("[markPaid] 支付单 {} 关联订单 {} 不存在", payOrder.getPayOrderNo(), payOrder.getBizOrderId());
            return;
        }
        Long folioId = folioService.ensureFolioForOrder(order.getId(), order.getGuestId(), order.getRoomId());
        // 首次线上支付时补记房费消费，保证账页「消费 - 收款 = 0」
        FolioRespVO folio = folioService.getFolio(folioId);
        if (folio.getTotalCharge() == null || folio.getTotalCharge().compareTo(BigDecimal.ZERO) <= 0) {
            FolioPostReqVO charge = new FolioPostReqVO();
            charge.setFolioId(folioId);
            charge.setItemType(FolioItemTypeEnum.ROOM.getType());
            charge.setItemName("房费（线上预付）");
            charge.setQuantity(1);
            charge.setUnitPrice(payOrder.getAmount());
            charge.setAmount(payOrder.getAmount());
            charge.setRemark("支付单 " + payOrder.getPayOrderNo());
            folioService.postCharge(charge);
        }
        PaymentMethodEnum method = resolvePaymentMethod(payOrder.getChannel());
        FolioPaymentReqVO payment = new FolioPaymentReqVO();
        payment.setFolioId(folioId);
        payment.setAmount(payOrder.getAmount());
        payment.setPaymentMethod(method.getMethod());
        payment.setTradeNo(payOrder.getPayOrderNo());
        payment.setRemark("在线支付-" + method.getName());
        folioService.postPayment(payment);

        BigDecimal paid = (order.getPaidAmount() == null ? BigDecimal.ZERO : order.getPaidAmount())
                .add(payOrder.getAmount());
        OrderDO orderUpdate = new OrderDO();
        orderUpdate.setId(order.getId());
        orderUpdate.setPaidAmount(paid);
        orderUpdate.setPaymentMethod(method.getMethod());
        orderUpdate.setPaymentStatus(paid.compareTo(order.getAmount() == null ? BigDecimal.ZERO : order.getAmount()) >= 0
                ? PaymentStatusEnum.PAID.getStatus() : PaymentStatusEnum.PARTIAL.getStatus());
        if (Objects.equals(order.getStatus(), OrderStatusEnum.PENDING.getStatus())
                || Objects.equals(order.getStatus(), OrderStatusEnum.WAIT_CONFIRM.getStatus())) {
            orderUpdate.setStatus(OrderStatusEnum.CONFIRMED.getStatus());
        }
        orderMapper.updateById(orderUpdate);
    }

    // ============================================================ 退款

    @Transactional(rollbackFor = Exception.class)
    public void refund(Long payOrderId, BigDecimal amount, String reason) {
        PayOrderDO payOrder = payOrderId == null ? null : payOrderMapper.selectById(payOrderId);
        if (payOrder == null) {
            throw exception(ERROR_PAY_ORDER_NOT_EXIST);
        }
        if (!Objects.equals(payOrder.getStatus(), PayOrderStatusEnum.PAID.getStatus())
                && !Objects.equals(payOrder.getStatus(), PayOrderStatusEnum.REFUNDED.getStatus())) {
            throw exception(ERROR_PAY_ORDER_STATUS_INVALID);
        }
        BigDecimal refunded = payOrder.getRefundAmount() == null ? BigDecimal.ZERO : payOrder.getRefundAmount();
        BigDecimal refundAmount = amount == null ? payOrder.getAmount().subtract(refunded) : amount;
        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0
                || refunded.add(refundAmount).compareTo(payOrder.getAmount()) > 0) {
            throw exception(ERROR_PAY_REFUND_EXCEED);
        }
        PayClient client = resolveClient(PayChannelEnum.of(payOrder.getChannel()));
        client.refund(payOrder, refundAmount, reason);

        BigDecimal totalRefunded = refunded.add(refundAmount);
        PayOrderDO update = new PayOrderDO();
        update.setId(payOrder.getId());
        update.setRefundAmount(totalRefunded);
        update.setStatus(totalRefunded.compareTo(payOrder.getAmount()) >= 0
                ? PayOrderStatusEnum.REFUNDED.getStatus() : PayOrderStatusEnum.PAID.getStatus());
        payOrderMapper.updateById(update);

        OrderDO order = orderMapper.selectById(payOrder.getBizOrderId());
        if (order == null) {
            return;
        }
        Long folioId = folioService.ensureFolioForOrder(order.getId(), order.getGuestId(), order.getRoomId());
        FolioPaymentReqVO payment = new FolioPaymentReqVO();
        payment.setFolioId(folioId);
        payment.setAmount(refundAmount.negate());
        payment.setPaymentMethod(resolvePaymentMethod(payOrder.getChannel()).getMethod());
        payment.setTradeNo(payOrder.getPayOrderNo() + "-R");
        payment.setRemark("在线退款" + (reason == null ? "" : "：" + reason));
        folioService.postPayment(payment);

        BigDecimal paid = (order.getPaidAmount() == null ? BigDecimal.ZERO : order.getPaidAmount())
                .subtract(refundAmount);
        if (paid.compareTo(BigDecimal.ZERO) < 0) {
            paid = BigDecimal.ZERO;
        }
        OrderDO orderUpdate = new OrderDO();
        orderUpdate.setId(order.getId());
        orderUpdate.setPaidAmount(paid);
        orderUpdate.setPaymentStatus(paid.compareTo(BigDecimal.ZERO) == 0
                ? PaymentStatusEnum.REFUNDED.getStatus() : PaymentStatusEnum.PARTIAL.getStatus());
        orderMapper.updateById(orderUpdate);

        // 退款按积分规则扣回（类型 40）
        Long memberId = order.getMemberId();
        if (memberId != null) {
            MemberDO member = memberService.validateMember(memberId);
            int points = refundAmount.setScale(0, RoundingMode.DOWN).intValue();
            if (points > 0 && member.getPoints() != null && member.getPoints() > 0) {
                int deduct = Math.min(points, member.getPoints());
                memberService.adjustPoints(memberId, -deduct, PointsTypeEnum.REFUND_BACK.getType(),
                        order.getId(), "退款扣回积分");
            }
        }
    }

    // ============================================================ 管理端分页

    public PageResult<PayOrderRespVO> getPayOrderPage(PayOrderPageReqVO reqVO) {
        PageResult<PayOrderDO> page = payOrderMapper.selectPage(reqVO);
        List<PayOrderRespVO> list = page.getList().stream().map(this::buildResp).collect(Collectors.toList());
        return new PageResult<>(list, page.getTotal());
    }

    private PayOrderRespVO buildResp(PayOrderDO payOrder) {
        PayOrderRespVO resp = new PayOrderRespVO();
        resp.setId(payOrder.getId());
        resp.setPayOrderNo(payOrder.getPayOrderNo());
        resp.setBizOrderId(payOrder.getBizOrderId());
        resp.setBizType(payOrder.getBizType());
        resp.setBizTypeName(PayBizTypeEnum.nameOf(payOrder.getBizType()));
        resp.setAmount(payOrder.getAmount());
        resp.setChannel(payOrder.getChannel());
        PayChannelEnum channel = PayChannelEnum.of(payOrder.getChannel());
        resp.setChannelName(channel == null ? payOrder.getChannel() : channel.getName());
        resp.setChannelTradeNo(payOrder.getChannelTradeNo());
        resp.setStatus(payOrder.getStatus());
        resp.setStatusName(PayOrderStatusEnum.nameOf(payOrder.getStatus()));
        resp.setExpireTime(payOrder.getExpireTime());
        resp.setPayTime(payOrder.getPayTime());
        resp.setRefundAmount(payOrder.getRefundAmount());
        resp.setCreateTime(payOrder.getCreateTime());
        OrderDO order = orderMapper.selectById(payOrder.getBizOrderId());
        if (order != null) {
            resp.setOrderNo(order.getOrderNo());
        }
        return resp;
    }

    // ============================================================ 工具方法

    private PayClient resolveClient(PayChannelEnum channel) {
        if (mockEnabled) {
            return mockPayClient;
        }
        return resolveRealClient(channel);
    }

    private PayClient resolveRealClient(PayChannelEnum channel) {
        if (channel == null) {
            return mockPayClient;
        }
        switch (channel) {
            case WXPAY:
            case WX_LITE:
                return wxPayClient;
            case ALIPAY:
                return aliPayClient;
            default:
                return mockPayClient;
        }
    }

    private PaymentMethodEnum resolvePaymentMethod(String channelCode) {
        PayChannelEnum channel = PayChannelEnum.of(channelCode);
        if (channel == null) {
            return PaymentMethodEnum.ONLINE;
        }
        switch (channel) {
            case WXPAY:
            case WX_LITE:
                return PaymentMethodEnum.WECHAT;
            case ALIPAY:
                return PaymentMethodEnum.ALIPAY;
            default:
                return PaymentMethodEnum.ONLINE;
        }
    }

    private String generatePayOrderNo() {
        return "P" + LocalDateTime.now().format(PAY_NO_FORMAT)
                + String.format("%04d", new Random().nextInt(10000));
    }

    private String truncate(String text, int max) {
        if (text == null) {
            return null;
        }
        return text.length() <= max ? text : text.substring(0, max);
    }
}
