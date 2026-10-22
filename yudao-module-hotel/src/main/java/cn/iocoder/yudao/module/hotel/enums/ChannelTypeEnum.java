package cn.iocoder.yudao.module.hotel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 渠道类型枚举（T1）
 */
@Getter
@AllArgsConstructor
public enum ChannelTypeEnum {

    DIRECT(10, "直销"),
    OTA(20, "OTA"),
    CORP(30, "协议"),
    MEMBER(40, "会员"),
    STORE(50, "门店");

    private final Integer type;
    private final String name;
}
