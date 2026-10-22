package cn.iocoder.yudao.module.hotel.controller.admin.channel.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 渠道 Response VO")
@Data
public class ChannelRespVO {

    @Schema(description = "编号", example = "1")
    private Long id;

    @Schema(description = "渠道名称", example = "直销")
    private String name;

    @Schema(description = "渠道编码", example = "DIRECT")
    private String code;

    @Schema(description = "类型", example = "10")
    private Integer type;

    @Schema(description = "类型名称", example = "直销")
    private String typeName;

    @Schema(description = "佣金比例（%）", example = "15.00")
    private BigDecimal commissionRate;

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
