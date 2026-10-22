package cn.iocoder.yudao.module.hotel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 夜审状态枚举
 */
@Getter
@AllArgsConstructor
public enum NightAuditStatusEnum {

    PROCESSING(10, "进行中"),
    SUCCESS(20, "成功"),
    FAILED(30, "失败");

    private final Integer status;
    private final String name;
}
