package cn.iocoder.yudao.module.hotel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 房态枚举
 */
@Getter
@AllArgsConstructor
public enum RoomStatusEnum {

    AVAILABLE(10, "空闲"),
    OCCUPIED(20, "已入住"),
    CLEANING(30, "待清洁"),
    MAINTENANCE(40, "维修中");

    private final Integer status;
    private final String name;
}
