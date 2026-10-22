package cn.iocoder.yudao.module.hotel.controller.app.pay.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 模拟支付成功 Request VO")
@Data
public class AppMockPayReqVO {

    @Schema(description = "支付单号（也可通过 query 传递）")
    private String payOrderNo;
}
