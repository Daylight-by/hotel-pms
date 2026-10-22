package cn.iocoder.yudao.module.hotel.controller.admin.pricing.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 定价回测 Response VO")
@Data
public class PriceBacktestRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "回测名称", example = "10 月历史回测")
    private String name;

    @Schema(description = "房型编号", example = "1")
    private Long roomTypeId;

    @Schema(description = "房型名称", example = "单人间")
    private String roomTypeName;

    @Schema(description = "区间起", example = "2026-09-21")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate beginDate;

    @Schema(description = "区间止", example = "2026-10-20")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate endDate;

    @Schema(description = "按原价的 RevPAR", example = "168.00")
    private BigDecimal actualRevpar;

    @Schema(description = "按建议价的 RevPAR", example = "182.40")
    private BigDecimal modelRevpar;

    @Schema(description = "提升比例（%）", example = "8.57")
    private BigDecimal upliftRate;

    @Schema(description = "需求弹性假设说明（答辩必须如实说明）")
    private String assumption;

    @Schema(description = "逐日明细")
    private List<Day> days;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @Schema(description = "逐日回测明细")
    @Data
    public static class Day {

        @Schema(description = "日期", example = "2026-09-21")
        @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
        private LocalDate bizDate;

        @Schema(description = "原价（当日日历价）", example = "300.00")
        private BigDecimal basePrice;

        @Schema(description = "建议价", example = "330.00")
        private BigDecimal suggestedPrice;

        @Schema(description = "实际出租率（0-1）", example = "0.720")
        private BigDecimal occupancy;

        @Schema(description = "按原价的 RevPAR", example = "216.00")
        private BigDecimal actualRevpar;

        @Schema(description = "按建议价的 RevPAR", example = "231.00")
        private BigDecimal modelRevpar;

        @Schema(description = "该日出租率是否为兜底估算值", example = "false")
        private Boolean occupancyEstimated;
    }
}
