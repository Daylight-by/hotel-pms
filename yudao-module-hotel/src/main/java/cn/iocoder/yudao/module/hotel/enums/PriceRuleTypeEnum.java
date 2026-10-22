package cn.iocoder.yudao.module.hotel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 房价计划价格规则枚举（T1）
 *
 * <p>算价规则见 {@code RatePlanServiceImpl#applyRule}：
 * 10 按日历价 / 20 日历价折扣 / 30 日历价减额 / 40 固定价</p>
 */
@Getter
@AllArgsConstructor
public enum PriceRuleTypeEnum {

    CALENDAR(10, "按日历价"),
    DISCOUNT(20, "日历价折扣"),
    REDUCE(30, "日历价减额"),
    FIXED(40, "固定价");

    private final Integer type;
    private final String name;
}
