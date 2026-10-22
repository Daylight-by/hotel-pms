package cn.iocoder.yudao.module.hotel.service.pay.client;

import cn.iocoder.yudao.module.hotel.dal.dataobject.pay.PayOrderDO;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付渠道客户端抽象。
 *
 * <p>本项目没有企业主体，拿不到真实支付商户号，因此默认使用 {@link MockPayClient} 演示；
 * {@link WxPayClient} / {@link AliPayClient} 保留真实接入的骨架与参数位。</p>
 */
public interface PayClient {

    /** 渠道编码：wxpay / alipay / mock */
    String getChannel();

    /** 渠道名称 */
    String getName();

    /**
     * 创建支付参数（前端调起支付用）。
     */
    Map<String, String> createPayParams(PayOrderDO payOrder);

    /**
     * 解析支付回调报文。返回 null 表示报文无法识别。
     */
    PayNotifyResult parseNotify(String body, Map<String, String> params);

    /**
     * 校验回调签名。Mock 渠道恒为 true。
     */
    default boolean verifySign(String body, Map<String, String> headers) {
        return true;
    }

    /**
     * 申请退款。
     */
    default boolean refund(PayOrderDO payOrder, BigDecimal amount, String reason) {
        return true;
    }
}
