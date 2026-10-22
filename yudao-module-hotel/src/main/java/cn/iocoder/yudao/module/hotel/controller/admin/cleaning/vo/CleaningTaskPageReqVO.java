package cn.iocoder.yudao.module.hotel.controller.admin.cleaning.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = "管理后台 - 清洁任务分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class CleaningTaskPageReqVO extends PageParam {

    @Schema(description = "房间编号")
    private Long roomId;

    @Schema(description = "任务状态")
    private Integer status;

    @Schema(description = "任务类型")
    private Integer type;

    @Schema(description = "计划日期")
    @DateTimeFormat(iso = DATE)
    private LocalDate planDate;
}
