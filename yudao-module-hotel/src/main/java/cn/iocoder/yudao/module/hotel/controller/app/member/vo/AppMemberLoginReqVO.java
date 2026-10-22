package cn.iocoder.yudao.module.hotel.controller.app.member.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "用户 App - 会员登录 Request VO")
@Data
public class AppMemberLoginReqVO {

    @Schema(description = "手机号", requiredMode = Schema.RequiredMode.REQUIRED, example = "13900006666")
    @NotBlank(message = "手机号不能为空")
    private String mobile;

    @Schema(description = "短信验证码（演示环境固定 123456）", example = "123456")
    private String code;

    @Schema(description = "密码（设置过密码的会员可用密码登录）")
    private String password;
}
