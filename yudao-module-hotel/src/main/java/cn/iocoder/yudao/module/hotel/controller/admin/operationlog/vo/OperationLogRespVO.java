package cn.iocoder.yudao.module.hotel.controller.admin.operationlog.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

@Schema(description = "管理后台 - 操作审计日志 Response VO")
@Data
public class OperationLogRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "门店编号")
    private Long storeId;

    @Schema(description = "模块", example = "folio")
    private String module;

    @Schema(description = "业务单据号", example = "F20261021001")
    private String bizId;

    @Schema(description = "操作", example = "冲销")
    private String operation;

    @Schema(description = "变更前（JSON）")
    private String beforeData;

    @Schema(description = "变更后（JSON）")
    private String afterData;

    @Schema(description = "操作人")
    private Long operatorId;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "IP")
    private String ip;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND, timezone = "GMT+8")
    private LocalDateTime createTime;
}
