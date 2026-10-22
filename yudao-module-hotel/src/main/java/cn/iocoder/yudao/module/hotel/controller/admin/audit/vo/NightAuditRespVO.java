package cn.iocoder.yudao.module.hotel.controller.admin.audit.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 夜审记录 Response VO")
@Data
public class NightAuditRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "被审计的营业日")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate bizDate;

    @Schema(description = "状态：10 进行中 20 成功 30 失败")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "耗时（毫秒）")
    private Long durationMs;

    @Schema(description = "过账房费合计")
    private BigDecimal roomChargeTotal;

    @Schema(description = "过账房数")
    private Integer postedRoomCount;

    @Schema(description = "过账明细数")
    private Integer postedItemCount;

    @Schema(description = "失败原因")
    private String failReason;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
