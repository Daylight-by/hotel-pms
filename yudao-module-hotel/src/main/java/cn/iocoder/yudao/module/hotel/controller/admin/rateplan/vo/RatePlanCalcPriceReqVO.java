package cn.iocoder.yudao.module.hotel.controller.admin.rateplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = "管理后台 - 房价计划试算 Request VO")
@Data
public class RatePlanCalcPriceReqVO {

    @Schema(description = "房价计划编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "请选择房价计划")
    private Long ratePlanId;

    @Schema(description = "房型编号（为空则取房价计划绑定的房型）", example = "1")
    private Long roomTypeId;

    @Schema(description = "开始日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-10-22")
    @NotNull(message = "请选择开始日期")
    @DateTimeFormat(iso = DATE)
    private LocalDate beginDate;

    @Schema(description = "结束日期（含）", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-10-24")
    @NotNull(message = "请选择结束日期")
    @DateTimeFormat(iso = DATE)
    private LocalDate endDate;
}
