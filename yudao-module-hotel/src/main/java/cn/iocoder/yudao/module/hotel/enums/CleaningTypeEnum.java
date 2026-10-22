package cn.iocoder.yudao.module.hotel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 清洁任务类型
 */
@Getter
@AllArgsConstructor
public enum CleaningTypeEnum {

    CHECKOUT(10, "退房清洁"),
    DAILY(20, "日常清洁"),
    MAINTENANCE(30, "维修后清洁");

    private final Integer type;
    private final String name;
}
