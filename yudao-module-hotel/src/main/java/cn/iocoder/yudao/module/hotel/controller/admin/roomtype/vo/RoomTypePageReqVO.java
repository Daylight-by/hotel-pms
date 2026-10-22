package cn.iocoder.yudao.module.hotel.controller.admin.roomtype.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 房型分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class RoomTypePageReqVO extends PageParam {

    @Schema(description = "房型名称")
    private String name;

    @Schema(description = "房型编码")
    private String code;

    @Schema(description = "状态")
    private Integer status;
}
