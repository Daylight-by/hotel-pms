package cn.iocoder.yudao.module.hotel.controller.admin.invoiceApply.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 发票申请处理 Request VO")
@Data
public class InvoiceApplyHandleReqVO {

    @Schema(description = "申请编号（也可通过 query 传递）")
    private Long id;

    @Schema(description = "目标状态：20 开具 / 30 驳回", example = "20")
    private Integer status;

    @Schema(description = "驳回原因")
    private String rejectReason;
}
