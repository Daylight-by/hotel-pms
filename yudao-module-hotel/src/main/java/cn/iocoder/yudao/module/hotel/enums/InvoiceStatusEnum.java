package cn.iocoder.yudao.module.hotel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 发票状态枚举
 */
@Getter
@AllArgsConstructor
public enum InvoiceStatusEnum {

    ISSUED(10, "已开具"),
    VOIDED(20, "已红冲");

    private final Integer status;
    private final String name;
}
