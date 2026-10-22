package cn.iocoder.yudao.module.hotel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 营业日状态枚举
 */
@Getter
@AllArgsConstructor
public enum BusinessDateStatusEnum {

    PROCESSING(10, "进行中"),
    POSTED(20, "已过账待审核"),
    CLOSED(30, "已结账");

    private final Integer status;
    private final String name;
}
