package cn.iocoder.yudao.module.hotel.controller.app.pay.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "用户 App - 支付结果 Response VO")
@Data
public class AppPayResultRespVO {

    @Schema(description = "支付单号")
    private String payOrderNo;

    @Schema(description = "订单编号")
    private Long orderId;

    @Schema(description = "支付金额")
    private BigDecimal amount;

    @Schema(description = "支付渠道")
    private String channel;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "支付时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime payTime;

    @Schema(description = "第三方交易号")
    private String channelTradeNo;
}
