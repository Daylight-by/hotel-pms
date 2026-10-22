package cn.iocoder.yudao.module.hotel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 发票类型枚举
 */
@Getter
@AllArgsConstructor
public enum InvoiceTypeEnum {

    NORMAL(10, "增值税普通发票"),
    SPECIAL(20, "增值税专用发票"),
    ELECTRONIC(30, "电子发票");

    private final Integer type;
    private final String name;
}
