package cn.iocoder.yudao.module.hotel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 账务明细项目类型枚举
 */
@Getter
@AllArgsConstructor
public enum FolioItemTypeEnum {

    ROOM(10, "房费"),
    FOOD(20, "餐饮"),
    MINIBAR(30, "迷你吧"),
    LAUNDRY(40, "洗衣"),
    COMPENSATION(50, "赔偿"),
    DEPOSIT(60, "押金"),
    OTHER(70, "其他"),
    REFUND(80, "退款");

    private final Integer type;
    private final String name;
}
