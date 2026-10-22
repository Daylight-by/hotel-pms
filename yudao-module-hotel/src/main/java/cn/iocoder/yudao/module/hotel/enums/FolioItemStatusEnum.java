package cn.iocoder.yudao.module.hotel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 账务明细状态枚举
 */
@Getter
@AllArgsConstructor
public enum FolioItemStatusEnum {

    VALID(10, "有效"),
    VOIDED(20, "已冲销"),
    TRANSFERRED(30, "已转出");

    private final Integer status;
    private final String name;
}
