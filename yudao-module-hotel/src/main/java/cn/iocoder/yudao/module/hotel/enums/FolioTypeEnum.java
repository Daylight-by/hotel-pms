package cn.iocoder.yudao.module.hotel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 账页类型枚举
 */
@Getter
@AllArgsConstructor
public enum FolioTypeEnum {

    MAIN(10, "主账页"),
    SPLIT(20, "分账页");

    private final Integer type;
    private final String name;
}
