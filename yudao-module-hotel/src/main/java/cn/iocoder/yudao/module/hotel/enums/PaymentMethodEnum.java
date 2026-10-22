package cn.iocoder.yudao.module.hotel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付方式枚举
 */
@Getter
@AllArgsConstructor
public enum PaymentMethodEnum {

    CASH(1, "现金"),
    CARD(2, "银行卡"),
    WECHAT(3, "微信"),
    ALIPAY(4, "支付宝"),
    ONLINE(5, "在线支付");

    private final Integer method;
    private final String name;
}
