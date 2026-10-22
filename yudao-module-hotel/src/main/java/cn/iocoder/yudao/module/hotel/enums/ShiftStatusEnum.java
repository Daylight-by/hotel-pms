package cn.iocoder.yudao.module.hotel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 交班状态枚举
 */
@Getter
@AllArgsConstructor
public enum ShiftStatusEnum {

    PROCESSING(10, "进行中"),
    CLOSED(20, "已交班");

    private final Integer status;
    private final String name;
}
