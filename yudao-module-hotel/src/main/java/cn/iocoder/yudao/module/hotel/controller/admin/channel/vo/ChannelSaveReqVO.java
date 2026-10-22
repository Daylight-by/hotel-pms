package cn.iocoder.yudao.module.hotel.controller.admin.channel.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 渠道新增/修改 Request VO")
@Data
public class ChannelSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "渠道名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "直销")
    @NotBlank(message = "渠道名称不能为空")
    @Size(max = 100, message = "渠道名称长度不能超过 100")
    private String name;

    @Schema(description = "渠道编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "DIRECT")
    @NotBlank(message = "渠道编码不能为空")
    @Size(max = 50, message = "渠道编码长度不能超过 50")
    private String code;

    @Schema(description = "类型：10 直销 20 OTA 30 协议 40 会员 50 门店", requiredMode = Schema.RequiredMode.REQUIRED, example = "10")
    @NotNull(message = "渠道类型不能为空")
    private Integer type;

    @Schema(description = "佣金比例（%）", example = "15.00")
    @DecimalMin(value = "0.00", message = "佣金比例不能为负数")
    private BigDecimal commissionRate;

    @Schema(description = "状态：0 开启 1 关闭", example = "0")
    private Integer status;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;
}
