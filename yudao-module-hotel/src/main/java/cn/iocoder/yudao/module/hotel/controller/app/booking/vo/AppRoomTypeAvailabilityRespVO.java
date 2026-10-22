package cn.iocoder.yudao.module.hotel.controller.app.booking.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "用户 App - 可售房型 Response VO")
@Data
public class AppRoomTypeAvailabilityRespVO {

    @Schema(description = "房型编号", example = "1")
    private Long roomTypeId;

    @Schema(description = "房型名称", example = "单人间")
    private String roomTypeName;

    @Schema(description = "最低可售价（含税/间夜）", example = "270.00")
    private BigDecimal price;

    @Schema(description = "剩余房量（区间内最小可售）", example = "4")
    private Integer availableCount;

    @Schema(description = "最低价对应的房价计划编号", example = "1")
    private Long ratePlanId;

    @Schema(description = "房价计划名称", example = "直销 9 折·含双早")
    private String ratePlanName;

    @Schema(description = "早餐：0 不含 1 单早 2 双早", example = "2")
    private Integer breakfast;

    @Schema(description = "最大入住人数", example = "2")
    private Integer maxOccupancy;

    @Schema(description = "房型基础价", example = "268.00")
    private BigDecimal basePrice;

    @Schema(description = "逐日价格明细")
    private List<DayPrice> dailyPrices;

    @Schema(description = "逐日价格")
    @Data
    public static class DayPrice {

        @Schema(description = "日期", example = "2026-10-22")
        @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
        private LocalDate bizDate;

        @Schema(description = "售价", example = "270.00")
        private BigDecimal price;

        @Schema(description = "剩余房量", example = "4")
        private Integer remaining;
    }
}
