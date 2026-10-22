package cn.iocoder.yudao.module.hotel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 客人类别枚举
 */
@Getter
@AllArgsConstructor
public enum GuestLevelEnum {

    FIT(0, "散客"),
    MEMBER(1, "会员"),
    COMPANY(2, "协议单位"),
    AGENT(3, "中介");

    private final Integer level;
    private final String name;
}
