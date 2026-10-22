package cn.iocoder.yudao.module.hotel.controller.admin.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 订单 Response VO")
@Data
public class OrderRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "住客编号")
    private Long guestId;

    @Schema(description = "住客姓名")
    private String guestName;

    @Schema(description = "住客手机号")
    private String guestMobile;

    @Schema(description = "房间编号")
    private Long roomId;

    @Schema(description = "房间号")
    private String roomNo;

    @Schema(description = "房型编号")
    private Long roomTypeId;

    @Schema(description = "房型名称")
    private String roomTypeName;

    @Schema(description = "订单类型：10 全日房 20 钟点房")
    private Integer orderType;

    @Schema(description = "订单类型名称")
    private String orderTypeName;

    @Schema(description = "房价计划编号")
    private Long ratePlanId;

    @Schema(description = "房价计划名称")
    private String ratePlanName;

    @Schema(description = "渠道编号")
    private Long channelId;

    @Schema(description = "渠道名称")
    private String channelName;

    @Schema(description = "会员编号")
    private Long memberId;

    @Schema(description = "所属营业日")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate businessDate;

    @Schema(description = "入住日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate checkInDate;

    @Schema(description = "离店日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate checkOutDate;

    @Schema(description = "计划入住时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime checkInTime;

    @Schema(description = "计划离店时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime checkOutTime;

    @Schema(description = "晚数")
    private Integer nights;

    @Schema(description = "成人数")
    private Integer adultCount;

    @Schema(description = "儿童数")
    private Integer childCount;

    @Schema(description = "原订单编号（续住/换房时关联）")
    private Long parentOrderId;

    @Schema(description = "订单金额")
    private BigDecimal amount;

    @Schema(description = "押金")
    private BigDecimal deposit;

    @Schema(description = "已收金额")
    private BigDecimal paidAmount;

    @Schema(description = "订单状态")
    private Integer status;

    @Schema(description = "订单状态名称")
    private String statusName;

    @Schema(description = "支付状态")
    private Integer paymentStatus;

    @Schema(description = "支付状态名称")
    private String paymentStatusName;

    @Schema(description = "支付方式")
    private Integer paymentMethod;

    @Schema(description = "支付方式名称")
    private String paymentMethodName;

    @Schema(description = "发票号")
    private String invoiceNo;

    @Schema(description = "是否需要发票")
    private Boolean invoiceRequested;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "实际入住时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime actualCheckInTime;

    @Schema(description = "实际离店时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime actualCheckOutTime;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
