package cn.iocoder.yudao.module.hotel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 会员积分变动类型：10 消费获得 / 20 兑换使用 / 30 手动调整 / 40 退款扣回
 */
@Getter
@AllArgsConstructor
public enum PointsTypeEnum {

    EARN(10, "消费获得"),
    EXCHANGE(20, "兑换使用"),
    ADJUST(30, "手动调整"),
    REFUND_BACK(40, "退款扣回");

    private final Integer type;
    private final String name;

    public static String nameOf(Integer type) {
        for (PointsTypeEnum value : values()) {
            if (value.getType().equals(type)) {
                return value.getName();
            }
        }
        return "未知";
    }
}
