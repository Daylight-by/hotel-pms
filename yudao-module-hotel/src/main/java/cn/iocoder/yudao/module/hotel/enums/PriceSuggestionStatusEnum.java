package cn.iocoder.yudao.module.hotel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 定价建议状态枚举（T1）
 */
@Getter
@AllArgsConstructor
public enum PriceSuggestionStatusEnum {

    SUGGESTING(10, "建议中"),
    ADOPTED(20, "已采纳"),
    IGNORED(30, "已忽略");

    private final Integer status;
    private final String name;
}
