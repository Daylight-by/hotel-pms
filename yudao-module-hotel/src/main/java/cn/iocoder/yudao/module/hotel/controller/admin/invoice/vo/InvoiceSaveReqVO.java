package cn.iocoder.yudao.module.hotel.controller.admin.invoice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 发票开具 Request VO")
@Data
public class InvoiceSaveReqVO {

    @Schema(description = "订单编号")
    private Long orderId;

    @Schema(description = "住客编号（不传则取订单住客）")
    private Long guestId;

    @Schema(description = "发票号码（不传自动生成）")
    private String invoiceNo;

    @Schema(description = "发票抬头", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "发票抬头不能为空")
    private String title;

    @Schema(description = "税号")
    private String taxNo;

    @Schema(description = "类型：10 增值税普通发票 20 增值税专用发票 30 电子发票", example = "10")
    private Integer type;

    @Schema(description = "开票金额", requiredMode = Schema.RequiredMode.REQUIRED, example = "398.00")
    @NotNull(message = "开票金额不能为空")
    @DecimalMin(value = "0.01", message = "开票金额必须大于 0")
    private BigDecimal amount;

    @Schema(description = "备注")
    private String remark;
}
