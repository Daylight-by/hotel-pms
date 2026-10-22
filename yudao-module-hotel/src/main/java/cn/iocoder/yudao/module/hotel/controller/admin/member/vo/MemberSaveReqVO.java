package cn.iocoder.yudao.module.hotel.controller.admin.member.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "管理后台 - 会员创建/修改 Request VO")
@Data
public class MemberSaveReqVO {

    @Schema(description = "编号（修改时必填）")
    private Long id;

    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "姓名不能为空")
    private String name;

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "手机号不能为空")
    private String mobile;

    @Schema(description = "密码（留空则免密登录）")
    private String password;

    @Schema(description = "等级：10 普通 20 银卡 30 金卡 40 铂金")
    private Integer level;

    @Schema(description = "状态：0 正常 1 冻结")
    private Integer status;

    @Schema(description = "关联住客编号")
    private Long guestId;
}
