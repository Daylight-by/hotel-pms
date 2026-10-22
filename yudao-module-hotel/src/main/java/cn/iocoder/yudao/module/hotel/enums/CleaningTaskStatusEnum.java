package cn.iocoder.yudao.module.hotel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 清洁任务状态：待处理 → 清洁中 → 已完成 → 已验收（验收后房间才恢复空闲）
 */
@Getter
@AllArgsConstructor
public enum CleaningTaskStatusEnum {

    PENDING(10, "待处理"),
    PROCESSING(20, "清洁中"),
    FINISHED(30, "已完成"),
    VERIFIED(40, "已验收");

    private final Integer status;
    private final String name;
}
