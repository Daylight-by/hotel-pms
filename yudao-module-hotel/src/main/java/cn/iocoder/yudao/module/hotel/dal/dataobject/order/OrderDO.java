package cn.iocoder.yudao.module.hotel.dal.dataobject.order;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单 DO
 */
@TableName("hotel_order")
@KeySequence("hotel_order_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 门店编号（0 表示未分配） */
    private Long storeId;

    /** 订单号 */
    private String orderNo;

    /** 住客编号 */
    private Long guestId;

    /** 房间编号 */
    private Long roomId;

    /** 房型编号（冗余，便于统计） */
    private Long roomTypeId;

    /** 订单类型，见 OrderTypeEnum：10 全日房 20 钟点房 */
    private Integer orderType;

    /** 房价计划编号（T1 预订引擎写入） */
    private Long ratePlanId;

    /** 渠道编号（T1 预订引擎写入） */
    private Long channelId;

    /** 会员编号（T2 会员体系写入） */
    private Long memberId;

    /** 所属营业日（入账归属日） */
    private LocalDate businessDate;

    /** 入住日期 */
    private LocalDate checkInDate;

    /** 离店日期 */
    private LocalDate checkOutDate;

    /** 计划入住时间（钟点房精确到时段） */
    private LocalDateTime checkInTime;

    /** 计划离店时间（钟点房精确到时段） */
    private LocalDateTime checkOutTime;

    /** 晚数 */
    private Integer nights;

    /** 成人数 */
    private Integer adultCount;

    /** 儿童数 */
    private Integer childCount;

    /** 原订单号（续住/换房时关联） */
    private Long parentOrderId;

    /** 订单金额 */
    private BigDecimal amount;

    /** 押金 */
    private BigDecimal deposit;

    /** 已收金额 */
    private BigDecimal paidAmount;

    /** 订单状态，见 OrderStatusEnum */
    private Integer status;

    /** 支付状态，见 PaymentStatusEnum */
    private Integer paymentStatus;

    /** 支付方式，见 PaymentMethodEnum */
    private Integer paymentMethod;

    /** 发票号 */
    private String invoiceNo;

    /** 是否需要发票 */
    private Boolean invoiceRequested;

    /** 备注 */
    private String remark;

    /** 实际入住时间 */
    private LocalDateTime actualCheckInTime;

    /** 实际离店时间 */
    private LocalDateTime actualCheckOutTime;
}
