package cn.iocoder.yudao.module.hotel.controller.admin.cleaning.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 清洁任务状态流转 Request VO")
@Data
public class CleaningTaskStatusReqVO {

    @Schema(description = "目标状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    @NotNull(message = "目标状态不能为空")
    private Integer status;

    @Schema(description = "备注")
    private String remark;
}
