package cn.iocoder.yudao.module.hotel.controller.admin.invoiceApply.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 发票申请分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class InvoiceApplyPageReqVO extends PageParam {

    @Schema(description = "申请单号")
    private String applyNo;

    @Schema(description = "订单编号")
    private Long orderId;

    @Schema(description = "会员编号")
    private Long memberId;

    @Schema(description = "状态")
    private Integer status;
}
