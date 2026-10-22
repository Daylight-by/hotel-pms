package cn.iocoder.yudao.module.hotel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付单状态：10 待支付 / 20 已支付 / 30 已关闭 / 40 已退款 / 50 退款中
 */
@Getter
@AllArgsConstructor
public enum PayOrderStatusEnum {

    PENDING(10, "待支付"),
    PAID(20, "已支付"),
    CLOSED(30, "已关闭"),
    REFUNDED(40, "已退款"),
    REFUNDING(50, "退款中");

    private final Integer status;
    private final String name;

    public static String nameOf(Integer status) {
        for (PayOrderStatusEnum value : values()) {
            if (value.getStatus().equals(status)) {
                return value.getName();
            }
        }
        return "未知";
    }
}
