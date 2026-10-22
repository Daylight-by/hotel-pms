package cn.iocoder.yudao.module.hotel.controller.app.member.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "用户 App - 会员登录 Response VO")
@Data
public class AppMemberLoginRespVO {

    @Schema(description = "访问令牌")
    private String accessToken;

    @Schema(description = "过期时间（时间戳，毫秒）")
    private Long expiresTime;

    @Schema(description = "会员编号")
    private Long memberId;

    @Schema(description = "会员卡号")
    private String memberNo;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "等级")
    private Integer level;

    @Schema(description = "等级名称")
    private String levelName;

    @Schema(description = "积分")
    private Integer points;

    @Schema(description = "储值余额")
    private BigDecimal balance;

    @Schema(description = "注册时间")
    private LocalDateTime registerTime;
}
