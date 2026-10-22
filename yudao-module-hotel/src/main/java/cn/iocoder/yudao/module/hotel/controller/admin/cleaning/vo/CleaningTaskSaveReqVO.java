package cn.iocoder.yudao.module.hotel.controller.admin.cleaning.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = "管理后台 - 清洁任务新增 Request VO")
@Data
public class CleaningTaskSaveReqVO {

    @Schema(description = "房间编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请选择房间")
    private Long roomId;

    @Schema(description = "任务类型", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请选择任务类型")
    private Integer type;

    @Schema(description = "保洁人员编号")
    private Long assigneeId;

    @Schema(description = "计划日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请选择计划日期")
    @DateTimeFormat(iso = DATE)
    private LocalDate planDate;

    @Schema(description = "备注")
    private String remark;
}
