package cn.iocoder.yudao.module.hotel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 会员等级：10 普通 / 20 银卡 / 30 金卡 / 40 铂金
 */
@Getter
@AllArgsConstructor
public enum MemberLevelEnum {

    NORMAL(10, "普通"),
    SILVER(20, "银卡"),
    GOLD(30, "金卡"),
    PLATINUM(40, "铂金");

    private final Integer level;
    private final String name;

    public static String nameOf(Integer level) {
        for (MemberLevelEnum value : values()) {
            if (value.getLevel().equals(level)) {
                return value.getName();
            }
        }
        return "未知";
    }
}
