package cn.iocoder.yudao.module.hotel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付流水类型枚举
 */
@Getter
@AllArgsConstructor
public enum PaymentTypeEnum {

    INCOME(10, "收款"),
    REFUND(20, "退款");

    private final Integer type;
    private final String name;
}
