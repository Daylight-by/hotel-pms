package cn.iocoder.yudao.module.hotel.controller.admin.shift.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 交班记录 Response VO")
@Data
public class ShiftHandoverRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "班次号")
    private String shiftNo;

    @Schema(description = "营业日")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate bizDate;

    @Schema(description = "开班时间")
    private LocalDateTime startTime;

    @Schema(description = "交班时间")
    private LocalDateTime endTime;

    @Schema(description = "收银员编号")
    private Long operatorId;

    @Schema(description = "收银员姓名")
    private String operatorName;

    @Schema(description = "现金")
    private BigDecimal cashAmount;

    @Schema(description = "银行卡")
    private BigDecimal cardAmount;

    @Schema(description = "微信")
    private BigDecimal wechatAmount;

    @Schema(description = "支付宝")
    private BigDecimal alipayAmount;

    @Schema(description = "其他")
    private BigDecimal otherAmount;

    @Schema(description = "合计")
    private BigDecimal totalAmount;

    @Schema(description = "状态：10 进行中 20 已交班")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "交接备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
