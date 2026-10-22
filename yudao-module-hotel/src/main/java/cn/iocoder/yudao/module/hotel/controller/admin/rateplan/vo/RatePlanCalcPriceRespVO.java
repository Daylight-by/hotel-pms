package cn.iocoder.yudao.module.hotel.controller.admin.rateplan.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "管理后台 - 房价计划试算 Response VO")
@Data
public class RatePlanCalcPriceRespVO {

    @Schema(description = "房价计划编号", example = "1")
    private Long ratePlanId;

    @Schema(description = "房价计划名称", example = "直销 9 折含双早")
    private String ratePlanName;

    @Schema(description = "房型编号", example = "1")
    private Long roomTypeId;

    @Schema(description = "房型名称", example = "单人间")
    private String roomTypeName;

    @Schema(description = "晚数", example = "3")
    private Integer nights;

    @Schema(description = "总价（Σ 每日价）", example = "810.00")
    private BigDecimal totalAmount;

    @Schema(description = "逐日价格明细")
    private List<DailyPrice> dailyPrices;

    @Schema(description = "逐日价格")
    @Data
    public static class DailyPrice {

        @Schema(description = "日期", example = "2026-10-22")
        @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
        private LocalDate bizDate;

        @Schema(description = "基线价（日历价）", example = "300.00")
        private BigDecimal basePrice;

        @Schema(description = "本房价计划售价", example = "270.00")
        private BigDecimal price;

        @Schema(description = "当日剩余房量", example = "4")
        private Integer remaining;
    }
}
