package cn.iocoder.yudao.module.hotel.controller.admin.room.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 房间 Response VO")
@Data
public class RoomRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "房间号")
    private String roomNo;

    @Schema(description = "房型编号")
    private Long roomTypeId;

    @Schema(description = "房型名称")
    private String roomTypeName;

    @Schema(description = "楼层")
    private Integer floor;

    @Schema(description = "面积")
    private Integer area;

    @Schema(description = "售价")
    private BigDecimal price;

    @Schema(description = "房态")
    private Integer status;

    @Schema(description = "房态名称")
    private String statusName;

    @Schema(description = "锁定状态：0 正常 1 维修锁定 2 预留锁定")
    private Integer lockStatus;

    @Schema(description = "锁定状态名称")
    private String lockStatusName;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "当前住客")
    private String currentGuest;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
