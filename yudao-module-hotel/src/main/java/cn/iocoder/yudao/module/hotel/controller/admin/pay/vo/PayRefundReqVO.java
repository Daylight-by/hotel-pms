package cn.iocoder.yudao.module.hotel.controller.admin.pay.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 支付退款 Request VO")
@Data
public class PayRefundReqVO {

    @Schema(description = "支付单编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "支付单编号不能为空")
    private Long payOrderId;

    @Schema(description = "退款金额（为空则全额退款）")
    private BigDecimal amount;

    @Schema(description = "退款原因")
    private String reason;
}
