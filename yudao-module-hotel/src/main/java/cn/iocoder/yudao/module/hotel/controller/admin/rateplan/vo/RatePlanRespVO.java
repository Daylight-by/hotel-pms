package cn.iocoder.yudao.module.hotel.controller.admin.rateplan.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 房价计划 Response VO")
@Data
public class RatePlanRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "名称", example = "直销 9 折含双早")
    private String name;

    @Schema(description = "编码", example = "DIRECT90")
    private String code;

    @Schema(description = "渠道编号", example = "1")
    private Long channelId;

    @Schema(description = "渠道名称", example = "直销")
    private String channelName;

    @Schema(description = "适用房型编号", example = "1")
    private Long roomTypeId;

    @Schema(description = "适用房型名称", example = "单人间")
    private String roomTypeName;

    @Schema(description = "价格规则", example = "20")
    private Integer priceRuleType;

    @Schema(description = "价格规则名称", example = "日历价折扣")
    private String priceRuleTypeName;

    @Schema(description = "规则值", example = "90.00")
    private BigDecimal priceRuleValue;

    @Schema(description = "早餐：0 不含 1 单早 2 双早", example = "2")
    private Integer breakfast;

    @Schema(description = "最少入住晚数", example = "1")
    private Integer minNights;

    @Schema(description = "最多入住晚数", example = "30")
    private Integer maxNights;

    @Schema(description = "最少提前预订小时数", example = "24")
    private Integer minAdvanceHours;

    @Schema(description = "取消政策", example = "10")
    private Integer cancelPolicy;

    @Schema(description = "取消政策名称", example = "免费取消")
    private String cancelPolicyName;

    @Schema(description = "免费取消截止小时数", example = "24")
    private Integer cancelFreeHours;

    @Schema(description = "是否需预付", example = "false")
    private Boolean needDeposit;

    @Schema(description = "预付比例（%）", example = "100.00")
    private BigDecimal depositRate;

    @Schema(description = "状态：0 开启 1 关闭", example = "0")
    private Integer status;

    @Schema(description = "状态名称", example = "开启")
    private String statusName;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
