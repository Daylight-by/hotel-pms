package cn.iocoder.yudao.module.hotel.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 订单取消 Request VO")
@Data
public class OrderCancelReqVO {

    @Schema(description = "订单编号")
    private Long id;

    @Schema(description = "取消原因")
    private String reason;

    @Schema(description = "违约金（入账到账页，可为空）")
    private BigDecimal penalty;
}
