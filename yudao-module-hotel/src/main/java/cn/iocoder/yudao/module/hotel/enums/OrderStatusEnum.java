package cn.iocoder.yudao.module.hotel.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单状态枚举
 *
 * <p>P1 状态机扩容（T1）：10 待支付 / 11 待确认 / 20 已确认 / 30 已入住 /
 * 40 已退房 / 50 已取消 / 60 已超时关闭。常量名沿用 P0，避免改动既有引用。</p>
 */
@Getter
@AllArgsConstructor
public enum OrderStatusEnum {

    /** 10：线上下单后待支付（P0 时代语义为「待确认」，P1 起改为「待支付」） */
    PENDING(10, "待支付"),
    /** 11：待支付已完成但需人工确认（如线下转账、协议单位挂账） */
    WAIT_CONFIRM(11, "待确认"),
    CONFIRMED(20, "已确认"),
    CHECKED_IN(30, "已入住"),
    CHECKED_OUT(40, "已退房"),
    CANCELLED(50, "已取消"),
    /** 60：待支付超时未付款，由定时任务关单 */
    TIMEOUT_CLOSED(60, "已超时关闭");

    private final Integer status;
    private final String name;
}
