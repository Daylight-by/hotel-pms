package cn.iocoder.yudao.module.hotel.controller.admin.member.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 会员积分调整 Request VO")
@Data
public class MemberPointsAdjustReqVO {

    @Schema(description = "会员编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "会员编号不能为空")
    private Long memberId;

    @Schema(description = "积分变动（正加负减）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "积分不能为空")
    private Integer points;

    @Schema(description = "备注")
    private String remark;
}
