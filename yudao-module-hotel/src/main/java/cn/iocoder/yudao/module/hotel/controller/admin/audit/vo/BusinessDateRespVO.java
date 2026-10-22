package cn.iocoder.yudao.module.hotel.controller.admin.audit.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 当前营业日 Response VO")
@Data
public class BusinessDateRespVO {

    @Schema(description = "当前营业日")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate bizDate;

    @Schema(description = "状态：10 进行中 20 已过账待审核 30 已结账")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "上次夜审完成时间")
    private LocalDateTime auditTime;

    @Schema(description = "自然日（用于前端提示营业日与自然日的差异）")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate today;
}
