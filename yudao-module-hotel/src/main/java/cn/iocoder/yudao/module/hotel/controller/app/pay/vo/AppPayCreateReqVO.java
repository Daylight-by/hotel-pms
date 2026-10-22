package cn.iocoder.yudao.module.hotel.controller.app.pay.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "用户 App - 创建支付单 Request VO")
@Data
public class AppPayCreateReqVO {

    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "订单编号不能为空")
    private Long orderId;

    @Schema(description = "支付渠道：wxpay / alipay / mock", example = "wxpay")
    private String channel;
}
