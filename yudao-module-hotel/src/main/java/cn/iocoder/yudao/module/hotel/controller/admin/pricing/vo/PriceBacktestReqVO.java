package cn.iocoder.yudao.module.hotel.controller.admin.pricing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = "管理后台 - 定价回测 Request VO")
@Data
public class PriceBacktestReqVO {

    @Schema(description = "回测名称（为空自动生成）", example = "10 月历史回测")
    private String name;

    @Schema(description = "房型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "请选择房型")
    private Long roomTypeId;

    @Schema(description = "回测区间起", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-09-21")
    @NotNull(message = "请选择开始日期")
    @DateTimeFormat(iso = DATE)
    private LocalDate beginDate;

    @Schema(description = "回测区间止", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-10-20")
    @NotNull(message = "请选择结束日期")
    @DateTimeFormat(iso = DATE)
    private LocalDate endDate;
}
