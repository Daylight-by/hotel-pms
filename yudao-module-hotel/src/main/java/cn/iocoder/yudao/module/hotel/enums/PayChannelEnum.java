package cn.iocoder.yudao.module.hotel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 在线支付渠道。
 *
 * <p>本项目没有企业主体，拿不到真实商户号，答辩使用 MOCK 渠道演示。</p>
 */
@Getter
@AllArgsConstructor
public enum PayChannelEnum {

    WXPAY("wxpay", "微信支付", PaymentMethodEnum.WECHAT.getMethod()),
    ALIPAY("alipay", "支付宝", PaymentMethodEnum.ALIPAY.getMethod()),
    WX_LITE("wx_lite", "微信小程序支付", PaymentMethodEnum.WECHAT.getMethod()),
    MOCK("mock", "模拟支付", PaymentMethodEnum.ONLINE.getMethod());

    /** 渠道编码（落库） */
    private final String code;
    /** 渠道名称 */
    private final String name;
    /** 对应的收银方式 */
    private final Integer paymentMethod;

    public static PayChannelEnum of(String code) {
        if (code == null) {
            return null;
        }
        for (PayChannelEnum value : values()) {
            if (value.getCode().equalsIgnoreCase(code)) {
                return value;
            }
        }
        return null;
    }
}
