package cn.iocoder.yudao.module.hotel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 账页状态枚举
 */
@Getter
@AllArgsConstructor
public enum FolioStatusEnum {

    UNSETTLED(10, "未结"),
    PARTIAL(20, "部分结账"),
    SETTLED(30, "已结账"),
    TRANSFERRED(40, "已转出"),
    CANCELLED(50, "已取消");

    private final Integer status;
    private final String name;
}
