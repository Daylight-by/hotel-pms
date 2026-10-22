package cn.iocoder.yudao.module.hotel.controller.admin.member.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 会员 Response VO")
@Data
public class MemberRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "会员卡号")
    private String memberNo;

    @Schema(description = "关联住客编号")
    private Long guestId;

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

    @Schema(description = "累计间夜")
    private Integer totalNights;

    @Schema(description = "累计消费")
    private BigDecimal totalAmount;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "注册时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime registerTime;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
