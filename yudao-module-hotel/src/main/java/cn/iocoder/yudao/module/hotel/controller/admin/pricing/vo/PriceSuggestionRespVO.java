package cn.iocoder.yudao.module.hotel.controller.admin.pricing.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 定价建议 Response VO")
@Data
public class PriceSuggestionRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "房型编号", example = "1")
    private Long roomTypeId;

    @Schema(description = "房型名称", example = "单人间")
    private String roomTypeName;

    @Schema(description = "业务日期", example = "2026-10-24")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate bizDate;

    @Schema(description = "基线价（日历价）", example = "300.00")
    private BigDecimal basePrice;

    @Schema(description = "建议售价", example = "345.00")
    private BigDecimal suggestedPrice;

    @Schema(description = "出租率系数", example = "1.150")
    private BigDecimal occFactor;

    @Schema(description = "提前期系数", example = "1.000")
    private BigDecimal advanceFactor;

    @Schema(description = "周末系数", example = "1.200")
    private BigDecimal weekendFactor;

    @Schema(description = "历史同期出租率（0-1）", example = "0.780")
    private BigDecimal histOccupancy;

    @Schema(description = "库存进度（已售/总量）", example = "0.600")
    private BigDecimal bookingPace;

    @Schema(description = "提前天数", example = "5")
    private Integer leadDays;

    @Schema(description = "建议理由（人话）", example = "历史同期出租率 78%，属旺季；距入住 5 天已售 60%，建议上调 15%")
    private String reason;

    @Schema(description = "状态：10 建议中 20 已采纳 30 已忽略", example = "10")
    private Integer status;

    @Schema(description = "状态名称", example = "建议中")
    private String statusName;

    @Schema(description = "实际采纳价", example = "340.00")
    private BigDecimal adoptedPrice;

    @Schema(description = "生成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
