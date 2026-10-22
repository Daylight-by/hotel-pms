package cn.iocoder.yudao.module.hotel.controller.admin.rate.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "管理后台 - 房态盘 Response VO（房型 × 日期）")
@Data
public class RateCalendarBoardRespVO {

    @Schema(description = "房型编号")
    private Long roomTypeId;

    @Schema(description = "房型名称")
    private String roomTypeName;

    @Schema(description = "房型基础价")
    private BigDecimal basePrice;

    @Schema(description = "房间总数")
    private Long roomCount;

    @Schema(description = "每日数据")
    private List<Day> days;

    @Schema(description = "房态盘单元格")
    @Data
    public static class Day {

        @Schema(description = "日期")
        @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
        private LocalDate bizDate;

        @Schema(description = "房价")
        private BigDecimal price;

        @Schema(description = "总库存")
        private Integer totalInventory;

        @Schema(description = "已售")
        private Integer soldInventory;

        @Schema(description = "剩余")
        private Integer remaining;

        @Schema(description = "状态：10 开房 20 关房")
        private Integer status;
    }
}
