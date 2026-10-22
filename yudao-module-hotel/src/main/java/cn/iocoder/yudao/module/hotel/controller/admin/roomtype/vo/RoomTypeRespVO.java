package cn.iocoder.yudao.module.hotel.controller.admin.roomtype.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 房型 Response VO")
@Data
public class RoomTypeRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "房型名称")
    private String name;

    @Schema(description = "房型编码")
    private String code;

    @Schema(description = "基础价格")
    private BigDecimal basePrice;

    @Schema(description = "钟点房价格（元/时段）")
    private BigDecimal hourlyPrice;

    @Schema(description = "钟点房时长（小时）")
    private Integer hourlyHours;

    @Schema(description = "是否开放钟点房")
    private Boolean hourlyEnabled;

    @Schema(description = "建议押金")
    private BigDecimal deposit;

    @Schema(description = "最大入住人数")
    private Integer maxOccupancy;

    @Schema(description = "面积")
    private Integer area;

    @Schema(description = "设施")
    private String amenities;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "房间数量")
    private Long roomCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
