package cn.iocoder.yudao.module.hotel.controller.admin.shift.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 交班 Request VO")
@Data
public class ShiftCloseReqVO {

    @Schema(description = "班次编号（不传则取当前进行中的班次）")
    private Long id;

    @Schema(description = "交接备注")
    private String remark;

    @Schema(description = "实点现金（仅记录，不参与统计）")
    private BigDecimal actualCash;
}
