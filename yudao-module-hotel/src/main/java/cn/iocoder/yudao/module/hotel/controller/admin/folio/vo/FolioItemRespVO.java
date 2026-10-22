package cn.iocoder.yudao.module.hotel.controller.admin.folio.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 账务明细 Response VO")
@Data
public class FolioItemRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "账页编号")
    private Long folioId;

    @Schema(description = "订单编号")
    private Long orderId;

    @Schema(description = "营业日")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate businessDate;

    @Schema(description = "项目类型")
    private Integer itemType;

    @Schema(description = "项目类型名称")
    private String itemTypeName;

    @Schema(description = "项目名称")
    private String itemName;

    @Schema(description = "方向：1 借方 2 贷方")
    private Integer direction;

    @Schema(description = "方向名称")
    private String directionName;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "单价")
    private BigDecimal unitPrice;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "支付方式")
    private Integer paymentMethod;

    @Schema(description = "支付方式名称")
    private String paymentMethodName;

    @Schema(description = "状态：10 有效 20 已冲销 30 已转出")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "冲销关联明细号")
    private Long voidId;

    @Schema(description = "转出目标账页号")
    private Long transferToFolioId;

    @Schema(description = "操作人编号")
    private Long operatorId;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
