package cn.iocoder.yudao.module.hotel.controller.admin.member.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 会员积分流水 Response VO")
@Data
public class MemberPointsLogRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "会员编号")
    private Long memberId;

    @Schema(description = "类型")
    private Integer type;

    @Schema(description = "类型名称")
    private String typeName;

    @Schema(description = "积分变动")
    private Integer points;

    @Schema(description = "变动后积分")
    private Integer balance;

    @Schema(description = "关联订单")
    private Long orderId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
