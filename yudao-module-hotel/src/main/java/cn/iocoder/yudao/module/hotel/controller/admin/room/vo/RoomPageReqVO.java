package cn.iocoder.yudao.module.hotel.controller.admin.room.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 房间分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class RoomPageReqVO extends PageParam {

    @Schema(description = "房间号")
    private String roomNo;

    @Schema(description = "房型编号")
    private Long roomTypeId;

    @Schema(description = "楼层")
    private Integer floor;

    @Schema(description = "房态")
    private Integer status;
}
