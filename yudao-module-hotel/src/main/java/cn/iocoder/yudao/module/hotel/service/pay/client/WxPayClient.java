package cn.iocoder.yudao.module.hotel.service.pay.client;

import cn.iocoder.yudao.module.hotel.dal.dataobject.pay.PayOrderDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 微信支付客户端（真实接入骨架）。
 *
 * <p>需要商户号、API 证书与可公网访问的回调地址，当前仅保留参数位；
 * 未配置商户号时由 Mock 渠道代替演示。</p>
 */
@Component
@Slf4j
public class WxPayClient implements PayClient {

    public static final String CHANNEL = "wxpay";

    @Value("${hotel.pay.wxpay.app-id:}")
    private String appId;
    @Value("${hotel.pay.wxpay.mch-id:}")
    private String mchId;
    @Value("${hotel.pay.wxpay.api-v3-key:}")
    private String apiV3Key;
    @Value("${hotel.pay.notify-url:http://localhost:48082/app-api/hotel/pay/notify/wxpay}")
    private String notifyUrl;

    @Override
    public String getChannel() {
        return CHANNEL;
    }

    @Override
    public String getName() {
        return "微信支付";
    }

    @Override
    public Map<String, String> createPayParams(PayOrderDO payOrder) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("channel", CHANNEL);
        params.put("appId", appId);
        params.put("mchId", mchId);
        params.put("outTradeNo", payOrder.getPayOrderNo());
        params.put("notifyUrl", notifyUrl);
        params.put("amount", payOrder.getAmount() == null ? "0" : payOrder.getAmount().toPlainString());
        if (appId == null || appId.isEmpty() || mchId == null || mchId.isEmpty()) {
            params.put("tip", "未配置微信商户号，请使用 mock 渠道演示");
        }
        return params;
    }

    @Override
    public PayNotifyResult parseNotify(String body, Map<String, String> params) {
        PayNotifyResult result = new PayNotifyResult();
        result.setSuccess(params != null && "SUCCESS".equalsIgnoreCase(params.get("trade_state")));
        result.setPayOrderNo(params == null ? null : params.get("out_trade_no"));
        result.setChannelTradeNo(params == null ? null : params.get("transaction_id"));
        result.setRaw(body);
        return result;
    }

    @Override
    public boolean verifySign(String body, Map<String, String> headers) {
        // 真实接入需按微信支付 APIv3 用平台证书验签；未配置密钥时放行仅供联调
        return apiV3Key == null || apiV3Key.isEmpty() || body != null;
    }
}
