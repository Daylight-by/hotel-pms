package cn.iocoder.yudao.module.hotel.controller.admin.invoice.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 发票分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class InvoicePageReqVO extends PageParam {

    @Schema(description = "发票号码")
    private String invoiceNo;

    @Schema(description = "发票抬头")
    private String title;

    @Schema(description = "订单编号")
    private Long orderId;

    @Schema(description = "状态：10 已开具 20 已红冲")
    private Integer status;

    @Schema(description = "类型：10 普通 20 专用 30 电子")
    private Integer type;
}
