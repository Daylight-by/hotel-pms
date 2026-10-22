package cn.iocoder.yudao.module.hotel.controller.admin.cleaning.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 清洁任务 Response VO")
@Data
public class CleaningTaskRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "房间编号")
    private Long roomId;

    @Schema(description = "房间号")
    private String roomNo;

    @Schema(description = "楼层")
    private Integer floor;

    @Schema(description = "任务类型")
    private Integer type;

    @Schema(description = "任务类型名称")
    private String typeName;

    @Schema(description = "任务状态")
    private Integer status;

    @Schema(description = "任务状态名称")
    private String statusName;

    @Schema(description = "保洁人员编号")
    private Long assigneeId;

    @Schema(description = "保洁人员")
    private String assigneeName;

    @Schema(description = "计划日期")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate planDate;

    @Schema(description = "完成时间")
    private LocalDateTime finishTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
