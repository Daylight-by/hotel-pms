package cn.iocoder.yudao.module.hotel.controller.admin.invoiceApply.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 发票申请 Response VO")
@Data
public class InvoiceApplyRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "申请单号")
    private String applyNo;

    @Schema(description = "订单编号")
    private Long orderId;

    @Schema(description = "会员编号")
    private Long memberId;

    @Schema(description = "发票抬头")
    private String title;

    @Schema(description = "税号")
    private String taxNo;

    @Schema(description = "接收邮箱")
    private String email;

    @Schema(description = "类型")
    private Integer type;

    @Schema(description = "类型名称")
    private String typeName;

    @Schema(description = "开票金额")
    private BigDecimal amount;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "关联发票编号")
    private Long invoiceId;

    @Schema(description = "驳回原因")
    private String rejectReason;

    @Schema(description = "处理时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime handleTime;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
