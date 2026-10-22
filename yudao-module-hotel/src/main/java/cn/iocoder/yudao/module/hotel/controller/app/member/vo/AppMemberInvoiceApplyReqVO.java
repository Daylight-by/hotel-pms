package cn.iocoder.yudao.module.hotel.controller.app.member.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "用户 App - 发票申请 Request VO")
@Data
public class AppMemberInvoiceApplyReqVO {

    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "订单编号不能为空")
    private Long orderId;

    @Schema(description = "发票抬头", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "发票抬头不能为空")
    private String title;

    @Schema(description = "税号")
    private String taxNo;

    @Schema(description = "接收邮箱")
    private String email;

    @Schema(description = "类型：10 增值税普通 20 增值税专用 30 电子发票", example = "30")
    private Integer type;
}
