package cn.iocoder.yudao.module.hotel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付业务类型：10 房费预付 / 20 押金 / 30 储值充值
 */
@Getter
@AllArgsConstructor
public enum PayBizTypeEnum {

    ROOM_FEE(10, "房费预付"),
    DEPOSIT(20, "押金"),
    RECHARGE(30, "储值充值");

    private final Integer type;
    private final String name;

    public static String nameOf(Integer type) {
        for (PayBizTypeEnum value : values()) {
            if (value.getType().equals(type)) {
                return value.getName();
            }
        }
        return "未知";
    }
}
