package cn.iocoder.yudao.module.hotel.controller.app.booking.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "用户 App - 提交预订 Response VO")
@Data
public class AppBookingRespVO {

    @Schema(description = "订单编号", example = "1")
    private Long orderId;

    @Schema(description = "订单号", example = "HT20261021183012001")
    private String orderNo;

    @Schema(description = "订单金额（应付）", example = "540.00")
    private BigDecimal amount;

    @Schema(description = "订单状态：10 待支付", example = "10")
    private Integer status;

    @Schema(description = "订单状态名称", example = "待支付")
    private String statusName;

    @Schema(description = "入住日期", example = "2026-10-22")
    private LocalDate checkInDate;

    @Schema(description = "离店日期", example = "2026-10-24")
    private LocalDate checkOutDate;

    @Schema(description = "晚数", example = "2")
    private Integer nights;

    @Schema(description = "占位号", example = "8f1c2a5e9d7b4c6e")
    private String holdNo;
}
