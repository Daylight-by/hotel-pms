package cn.iocoder.yudao.module.hotel.controller.admin.roomtype.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 房型新增/修改 Request VO")
@Data
public class RoomTypeSaveReqVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "房型名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "豪华双床房")
    @NotBlank(message = "房型名称不能为空")
    @Size(max = 100, message = "房型名称不能超过 100 个字符")
    private String name;

    @Schema(description = "房型编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "DOUBLE")
    @NotBlank(message = "房型编码不能为空")
    private String code;

    @Schema(description = "基础价格", requiredMode = Schema.RequiredMode.REQUIRED, example = "398.00")
    @NotNull(message = "基础价格不能为空")
    @DecimalMin(value = "0.01", message = "基础价格必须大于 0")
    private BigDecimal basePrice;

    @Schema(description = "钟点房价格（元/时段）", example = "168.00")
    private BigDecimal hourlyPrice;

    @Schema(description = "钟点房时长（小时）", example = "3")
    private Integer hourlyHours;

    @Schema(description = "是否开放钟点房", example = "true")
    private Boolean hourlyEnabled;

    @Schema(description = "建议押金", example = "200.00")
    private BigDecimal deposit;

    @Schema(description = "最大入住人数", example = "2")
    private Integer maxOccupancy;

    @Schema(description = "面积（平方米）", example = "30")
    private Integer area;

    @Schema(description = "设施，逗号分隔", example = "WiFi,空调,电视")
    private String amenities;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "排序", example = "1")
    private Integer sort;
}
