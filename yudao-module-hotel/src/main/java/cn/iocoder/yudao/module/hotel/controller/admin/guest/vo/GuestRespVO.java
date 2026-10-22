package cn.iocoder.yudao.module.hotel.controller.admin.guest.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 住客 Response VO")
@Data
public class GuestRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "手机号")
    private String mobile;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "性别")
    private Integer sex;

    @Schema(description = "客人类别：0 散客 1 会员 2 协议单位 3 中介")
    private Integer guestLevel;

    @Schema(description = "客人类别名称")
    private String guestLevelName;

    @Schema(description = "所属单位")
    private String company;

    @Schema(description = "联系地址")
    private String address;

    @Schema(description = "出生日期")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate birthday;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "累计订单数")
    private Long orderCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
