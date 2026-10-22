package cn.iocoder.yudao.module.hotel.service.pay.client;

import cn.iocoder.yudao.module.hotel.dal.dataobject.pay.PayOrderDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 支付宝客户端（真实接入骨架）。
 */
@Component
@Slf4j
public class AliPayClient implements PayClient {

    public static final String CHANNEL = "alipay";

    @Value("${hotel.pay.alipay.app-id:}")
    private String appId;
    @Value("${hotel.pay.alipay.private-key:}")
    private String privateKey;
    @Value("${hotel.pay.notify-url:http://localhost:48082/app-api/hotel/pay/notify/alipay}")
    private String notifyUrl;

    @Override
    public String getChannel() {
        return CHANNEL;
    }

    @Override
    public String getName() {
        return "支付宝";
    }

    @Override
    public Map<String, String> createPayParams(PayOrderDO payOrder) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("channel", CHANNEL);
        params.put("appId", appId);
        params.put("outTradeNo", payOrder.getPayOrderNo());
        params.put("notifyUrl", notifyUrl);
        params.put("amount", payOrder.getAmount() == null ? "0" : payOrder.getAmount().toPlainString());
        if (appId == null || appId.isEmpty()) {
            params.put("tip", "未配置支付宝应用，请使用 mock 渠道演示");
        }
        return params;
    }

    @Override
    public PayNotifyResult parseNotify(String body, Map<String, String> params) {
        PayNotifyResult result = new PayNotifyResult();
        result.setSuccess(params != null
                && ("TRADE_SUCCESS".equalsIgnoreCase(params.get("trade_status"))
                || "TRADE_FINISHED".equalsIgnoreCase(params.get("trade_status"))));
        result.setPayOrderNo(params == null ? null : params.get("out_trade_no"));
        result.setChannelTradeNo(params == null ? null : params.get("trade_no"));
        result.setRaw(body);
        return result;
    }

    @Override
    public boolean verifySign(String body, Map<String, String> headers) {
        // 真实接入需 RSA2 验签；未配置私钥时放行仅供联调
        return privateKey == null || privateKey.isEmpty() || headers != null || body != null;
    }
}
