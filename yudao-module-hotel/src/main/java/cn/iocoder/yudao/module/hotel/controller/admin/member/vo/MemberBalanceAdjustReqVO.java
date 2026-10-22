package cn.iocoder.yudao.module.hotel.controller.admin.member.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 会员储值调整 Request VO")
@Data
public class MemberBalanceAdjustReqVO {

    @Schema(description = "会员编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "会员编号不能为空")
    private Long memberId;

    @Schema(description = "金额变动（正充值负扣减）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "金额不能为空")
    private BigDecimal amount;

    @Schema(description = "备注")
    private String remark;
}
