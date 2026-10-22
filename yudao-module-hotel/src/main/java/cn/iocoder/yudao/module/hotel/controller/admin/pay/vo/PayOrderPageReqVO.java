package cn.iocoder.yudao.module.hotel.controller.admin.pay.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 支付单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class PayOrderPageReqVO extends PageParam {

    @Schema(description = "支付单号")
    private String payOrderNo;

    @Schema(description = "业务订单编号")
    private Long bizOrderId;

    @Schema(description = "支付渠道")
    private String channel;

    @Schema(description = "状态")
    private Integer status;
}
