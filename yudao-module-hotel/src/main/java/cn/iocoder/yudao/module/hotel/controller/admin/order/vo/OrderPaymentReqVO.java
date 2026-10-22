package cn.iocoder.yudao.module.hotel.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 订单支付/退款 Request VO")
@Data
public class OrderPaymentReqVO {

    @Schema(description = "支付状态", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "支付状态不能为空")
    private Integer paymentStatus;

    @Schema(description = "支付方式")
    private Integer paymentMethod;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "发票号")
    private String invoiceNo;

    @Schema(description = "是否需要发票")
    private Boolean invoiceRequested;

    @Schema(description = "退款原因")
    private String reason;
}
