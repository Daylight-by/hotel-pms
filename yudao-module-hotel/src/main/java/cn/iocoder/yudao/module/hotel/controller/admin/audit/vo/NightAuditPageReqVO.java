package cn.iocoder.yudao.module.hotel.controller.admin.audit.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = "管理后台 - 夜审分页 Request VO")
@Data
public class NightAuditPageReqVO extends cn.iocoder.yudao.framework.common.pojo.PageParam {

    @Schema(description = "营业日")
    @DateTimeFormat(iso = DATE)
    private LocalDate bizDate;

    @Schema(description = "状态：10 进行中 20 成功 30 失败")
    private Integer status;
}
