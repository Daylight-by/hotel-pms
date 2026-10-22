package cn.iocoder.yudao.module.hotel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付状态枚举
 */
@Getter
@AllArgsConstructor
public enum PaymentStatusEnum {

    UNPAID(10, "未支付"),
    PARTIAL(20, "部分支付"),
    PAID(30, "已支付"),
    REFUNDED(40, "已退款");

    private final Integer status;
    private final String name;
}
