package cn.iocoder.yudao.module.hotel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 房间锁定状态枚举
 */
@Getter
@AllArgsConstructor
public enum RoomLockStatusEnum {

    NORMAL(0, "正常"),
    MAINTENANCE(1, "维修锁定"),
    RESERVED(2, "预留锁定");

    private final Integer status;
    private final String name;
}
