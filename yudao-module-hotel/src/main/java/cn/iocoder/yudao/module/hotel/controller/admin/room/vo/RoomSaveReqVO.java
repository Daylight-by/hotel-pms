package cn.iocoder.yudao.module.hotel.controller.admin.room.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 房间新增/修改 Request VO")
@Data
public class RoomSaveReqVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "房间号", requiredMode = Schema.RequiredMode.REQUIRED, example = "301")
    @NotBlank(message = "房间号不能为空")
    private String roomNo;

    @Schema(description = "房型编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请选择房型")
    private Long roomTypeId;

    @Schema(description = "楼层", example = "3")
    private Integer floor;

    @Schema(description = "面积", example = "30")
    private Integer area;

    @Schema(description = "售价", requiredMode = Schema.RequiredMode.REQUIRED, example = "398.00")
    @NotNull(message = "售价不能为空")
    @DecimalMin(value = "0.01", message = "售价必须大于 0")
    private BigDecimal price;

    @Schema(description = "房态", example = "10")
    private Integer status;

    @Schema(description = "锁定状态：0 正常 1 维修锁定 2 预留锁定", example = "0")
    private Integer lockStatus;

    @Schema(description = "排序", example = "0")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;
}
