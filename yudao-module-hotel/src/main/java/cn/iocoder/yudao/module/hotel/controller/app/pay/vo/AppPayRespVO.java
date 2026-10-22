package cn.iocoder.yudao.module.hotel.controller.app.pay.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Schema(description = "用户 App - 创建支付单 Response VO")
@Data
public class AppPayRespVO {

    @Schema(description = "支付单号")
    private String payOrderNo;

    @Schema(description = "订单编号")
    private Long orderId;

    @Schema(description = "支付金额")
    private BigDecimal amount;

    @Schema(description = "支付渠道")
    private String channel;

    @Schema(description = "支付单状态")
    private Integer status;

    @Schema(description = "支付参数（Mock 渠道为演示参数）")
    private Map<String, String> payParams;

    @Schema(description = "是否开启模拟支付")
    private Boolean mockEnabled;

    @Schema(description = "支付截止时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expireTime;
}
