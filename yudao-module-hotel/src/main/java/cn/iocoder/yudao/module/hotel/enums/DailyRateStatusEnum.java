package cn.iocoder.yudao.module.hotel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 房价日历状态枚举
 */
@Getter
@AllArgsConstructor
public enum DailyRateStatusEnum {

    OPEN(10, "开房"),
    CLOSED(20, "关房");

    private final Integer status;
    private final String name;
}
