package cn.iocoder.yudao.module.hotel.controller.admin.rateplan.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 房价计划新增/修改 Request VO")
@Data
public class RatePlanSaveReqVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "直销 9 折含双早")
    @NotBlank(message = "房价计划名称不能为空")
    @Size(max = 100, message = "名称长度不能超过 100")
    private String name;

    @Schema(description = "编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "DIRECT90")
    @NotBlank(message = "房价计划编码不能为空")
    @Size(max = 50, message = "编码长度不能超过 50")
    private String code;

    @Schema(description = "渠道编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "请选择渠道")
    private Long channelId;

    @Schema(description = "适用房型（空 = 全部房型）", example = "1")
    private Long roomTypeId;

    @Schema(description = "价格规则：10 按日历价 20 折扣 30 减额 40 固定价", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    @NotNull(message = "请选择价格规则")
    private Integer priceRuleType;

    @Schema(description = "规则值：折扣率(90=9折)/减额/固定价", example = "90.00")
    @DecimalMin(value = "0.00", message = "规则值不能为负数")
    private BigDecimal priceRuleValue;

    @Schema(description = "早餐：0 不含 1 单早 2 双早", example = "2")
    private Integer breakfast;

    @Schema(description = "最少入住晚数", example = "1")
    @Min(value = 1, message = "最少入住晚数至少为 1")
    private Integer minNights;

    @Schema(description = "最多入住晚数", example = "30")
    private Integer maxNights;

    @Schema(description = "最少提前预订小时数", example = "24")
    private Integer minAdvanceHours;

    @Schema(description = "取消政策：10 免费取消 20 入住前N小时 30 不可取消", example = "10")
    private Integer cancelPolicy;

    @Schema(description = "免费取消截止小时数", example = "24")
    private Integer cancelFreeHours;

    @Schema(description = "是否需预付", example = "false")
    private Boolean needDeposit;

    @Schema(description = "预付比例（%）", example = "100.00")
    private BigDecimal depositRate;

    @Schema(description = "状态：0 开启 1 关闭", example = "0")
    private Integer status;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过 500")
    private String remark;
}
