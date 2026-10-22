package cn.iocoder.yudao.module.hotel.controller.admin.shift.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 开班 Request VO")
@Data
public class ShiftOpenReqVO {

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "收银员编号（不传取当前登录用户）")
    private Long operatorId;
}
