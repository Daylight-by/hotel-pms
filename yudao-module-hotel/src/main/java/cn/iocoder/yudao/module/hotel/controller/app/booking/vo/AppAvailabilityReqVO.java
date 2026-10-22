package cn.iocoder.yudao.module.hotel.controller.app.booking.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = "用户 App - 可售房型查询 Request VO")
@Data
public class AppAvailabilityReqVO {

    @Schema(description = "入住日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-10-22")
    @NotNull(message = "请选择入住日期")
    @DateTimeFormat(iso = DATE)
    private LocalDate checkInDate;

    @Schema(description = "离店日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-10-24")
    @NotNull(message = "请选择离店日期")
    @DateTimeFormat(iso = DATE)
    private LocalDate checkOutDate;

    @Schema(description = "成人数", example = "2")
    private Integer adultCount;

    @Schema(description = "渠道编号（为空取直销渠道）", example = "1")
    private Long channelId;
}
