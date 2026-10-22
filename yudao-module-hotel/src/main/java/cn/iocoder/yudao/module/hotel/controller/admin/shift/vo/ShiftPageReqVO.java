package cn.iocoder.yudao.module.hotel.controller.admin.shift.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = "管理后台 - 交班分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ShiftPageReqVO extends PageParam {

    @Schema(description = "状态：10 进行中 20 已交班")
    private Integer status;

    @Schema(description = "营业日起")
    @DateTimeFormat(iso = DATE)
    private LocalDate beginDate;

    @Schema(description = "营业日止")
    @DateTimeFormat(iso = DATE)
    private LocalDate endDate;
}
