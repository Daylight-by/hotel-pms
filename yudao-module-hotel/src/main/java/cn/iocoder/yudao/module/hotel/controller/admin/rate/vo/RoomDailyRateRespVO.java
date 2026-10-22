package cn.iocoder.yudao.module.hotel.controller.admin.rate.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 房价日历 Response VO")
@Data
public class RoomDailyRateRespVO {

    @Schema(description = "编号（未维护的日期为空）")
    private Long id;

    @Schema(description = "房型编号")
    private Long roomTypeId;

    @Schema(description = "房型名称")
    private String roomTypeName;

    @Schema(description = "业务日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate bizDate;

    @Schema(description = "当日房价")
    private BigDecimal price;

    @Schema(description = "总库存")
    private Integer totalInventory;

    @Schema(description = "已售房量")
    private Integer soldInventory;

    @Schema(description = "剩余房量 = 总库存 - 已售")
    private Integer remaining;

    @Schema(description = "状态：10 开房 20 关房")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
