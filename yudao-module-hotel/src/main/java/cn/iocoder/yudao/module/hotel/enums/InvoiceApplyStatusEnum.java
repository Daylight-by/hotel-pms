package cn.iocoder.yudao.module.hotel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 电子发票申请状态：10 待开具 / 20 已开具 / 30 已驳回
 */
@Getter
@AllArgsConstructor
public enum InvoiceApplyStatusEnum {

    PENDING(10, "待开具"),
    ISSUED(20, "已开具"),
    REJECTED(30, "已驳回");

    private final Integer status;
    private final String name;

    public static String nameOf(Integer status) {
        for (InvoiceApplyStatusEnum value : values()) {
            if (value.getStatus().equals(status)) {
                return value.getName();
            }
        }
        return "未知";
    }
}
