package cn.iocoder.yudao.module.hotel.controller.admin.rate.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = "管理后台 - 房价日历批量维护 Request VO")
@Data
public class RoomDailyRateBatchSaveReqVO {

    @Schema(description = "房型编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请选择房型")
    private Long roomTypeId;

    @Schema(description = "开始日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请选择开始日期")
    @DateTimeFormat(iso = DATE)
    private LocalDate beginDate;

    @Schema(description = "结束日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请选择结束日期")
    @DateTimeFormat(iso = DATE)
    private LocalDate endDate;

    @Schema(description = "当日房价", requiredMode = Schema.RequiredMode.REQUIRED, example = "398.00")
    @NotNull(message = "房价不能为空")
    @DecimalMin(value = "0.00", message = "房价不能为负数")
    private BigDecimal price;

    @Schema(description = "总库存（可售房量）", example = "5")
    @Min(value = 0, message = "库存不能为负数")
    private Integer totalInventory;

    @Schema(description = "状态：10 开房 20 关房", example = "10")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
