package cn.iocoder.yudao.module.hotel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 取消政策枚举（T1）
 */
@Getter
@AllArgsConstructor
public enum CancelPolicyEnum {

    FREE(10, "免费取消"),
    HOURS_BEFORE(20, "入住前 N 小时可免费取消"),
    NONE_REFUNDABLE(30, "不可取消");

    private final Integer policy;
    private final String name;
}
