package cn.iocoder.yudao.module.hotel.service.pay.client;

import cn.iocoder.yudao.module.hotel.dal.dataobject.pay.PayOrderDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 模拟支付客户端（答辩演示用）。
 *
 * <p>没有真实商户号时，前端拿到的是模拟支付参数，
 * 调用 POST /app-api/hotel/pay/mock-success 即视为支付成功。</p>
 */
@Component
@Slf4j
public class MockPayClient implements PayClient {

    public static final String CHANNEL = "mock";

    @Override
    public String getChannel() {
        return CHANNEL;
    }

    @Override
    public String getName() {
        return "模拟支付";
    }

    @Override
    public Map<String, String> createPayParams(PayOrderDO payOrder) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("mock", "true");
        params.put("payOrderNo", payOrder.getPayOrderNo());
        params.put("amount", payOrder.getAmount() == null ? "0" : payOrder.getAmount().toPlainString());
        params.put("tip", "演示环境未接入真实商户号，请调用 /app-api/hotel/pay/mock-success 模拟支付成功");
        return params;
    }

    @Override
    public PayNotifyResult parseNotify(String body, Map<String, String> params) {
        PayNotifyResult result = new PayNotifyResult();
        result.setSuccess(Boolean.TRUE);
        result.setPayOrderNo(params == null ? null : params.get("payOrderNo"));
        result.setChannelTradeNo(params == null ? null : params.get("channelTradeNo"));
        result.setAmount(params == null || params.get("amount") == null
                ? null : new BigDecimal(params.get("amount")));
        result.setRaw(body);
        return result;
    }

    @Override
    public boolean refund(PayOrderDO payOrder, BigDecimal amount, String reason) {
        log.info("[refund][MOCK] 支付单 {} 退款 {} 元，原因：{}", payOrder.getPayOrderNo(), amount, reason);
        return true;
    }
}
