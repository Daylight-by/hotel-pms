package cn.iocoder.yudao.module.hotel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 预订占位状态枚举（T1）
 */
@Getter
@AllArgsConstructor
public enum HoldStatusEnum {

    HOLDING(10, "占用中"),
    CONVERTED(20, "已转化"),
    RELEASED(30, "已释放"),
    EXPIRED(40, "已过期");

    private final Integer status;
    private final String name;
}
