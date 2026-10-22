package cn.iocoder.yudao.module.hotel.controller.admin.pay.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 支付单 Response VO")
@Data
public class PayOrderRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "支付单号")
    private String payOrderNo;

    @Schema(description = "业务订单编号")
    private Long bizOrderId;

    @Schema(description = "业务订单号")
    private String orderNo;

    @Schema(description = "业务类型")
    private Integer bizType;

    @Schema(description = "业务类型名称")
    private String bizTypeName;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "支付渠道")
    private String channel;

    @Schema(description = "渠道名称")
    private String channelName;

    @Schema(description = "第三方交易号")
    private String channelTradeNo;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expireTime;

    @Schema(description = "支付时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime payTime;

    @Schema(description = "已退金额")
    private BigDecimal refundAmount;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
