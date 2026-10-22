package cn.iocoder.yudao.module.hotel.controller.app.booking.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = "用户 App - 创建预订占位 Request VO")
@Data
public class AppHoldReqVO {

    @Schema(description = "房型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "请选择房型")
    private Long roomTypeId;

    @Schema(description = "房价计划编号（可选）", example = "1")
    private Long ratePlanId;

    @Schema(description = "入住日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-10-22")
    @NotNull(message = "请选择入住日期")
    @DateTimeFormat(iso = DATE)
    private LocalDate checkInDate;

    @Schema(description = "离店日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-10-24")
    @NotNull(message = "请选择离店日期")
    @DateTimeFormat(iso = DATE)
    private LocalDate checkOutDate;

    @Schema(description = "占房数", example = "1")
    @Min(value = 1, message = "占房数至少为 1")
    private Integer roomCount;
}
