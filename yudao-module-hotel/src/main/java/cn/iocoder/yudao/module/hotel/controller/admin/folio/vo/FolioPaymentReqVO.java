package cn.iocoder.yudao.module.hotel.controller.admin.folio.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 账页收款 Request VO")
@Data
public class FolioPaymentReqVO {

    @Schema(description = "账页编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请选择账页")
    private Long folioId;

    @Schema(description = "收款金额（退款传负数）", requiredMode = Schema.RequiredMode.REQUIRED, example = "100.00")
    @NotNull(message = "请输入金额")
    private BigDecimal amount;

    @Schema(description = "支付方式：1 现金 2 银行卡 3 微信 4 支付宝 5 在线支付", requiredMode = Schema.RequiredMode.REQUIRED, example = "3")
    @NotNull(message = "请选择支付方式")
    private Integer paymentMethod;

    @Schema(description = "外部交易号")
    private String tradeNo;

    @Schema(description = "备注")
    private String remark;
}
