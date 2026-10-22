package cn.iocoder.yudao.module.hotel.controller.admin.invoice.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 发票 Response VO")
@Data
public class InvoiceRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "发票号码")
    private String invoiceNo;

    @Schema(description = "订单编号")
    private Long orderId;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "住客编号")
    private Long guestId;

    @Schema(description = "住客姓名")
    private String guestName;

    @Schema(description = "发票抬头")
    private String title;

    @Schema(description = "税号")
    private String taxNo;

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

    @Schema(description = "开票时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime issueTime;

    @Schema(description = "红冲时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime voidTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
