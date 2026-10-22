package cn.iocoder.yudao.module.hotel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付流水状态枚举
 */
@Getter
@AllArgsConstructor
public enum PaymentRecordStatusEnum {

    SUCCESS(10, "成功"),
    REFUNDED(20, "已退款"),
    FAILED(30, "失败");

    private final Integer status;
    private final String name;
}
