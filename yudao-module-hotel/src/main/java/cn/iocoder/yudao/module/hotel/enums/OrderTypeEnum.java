package cn.iocoder.yudao.module.hotel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单类型枚举
 */
@Getter
@AllArgsConstructor
public enum OrderTypeEnum {

    FULL_DAY(10, "全日房"),
    HOURLY(20, "钟点房");

    private final Integer type;
    private final String name;
}
