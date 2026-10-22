package cn.iocoder.yudao.module.hotel.controller.admin.guest.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Schema(description = "管理后台 - 住客新增/修改 Request VO")
@Data
public class GuestSaveReqVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "李明月")
    @NotBlank(message = "姓名不能为空")
    private String name;

    @Schema(description = "手机号", example = "13900000001")
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "邮箱")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "性别")
    private Integer sex;

    @Schema(description = "客人类别：0 散客 1 会员 2 协议单位 3 中介", example = "0")
    private Integer guestLevel;

    @Schema(description = "所属单位", example = "余姚某某科技有限公司")
    private String company;

    @Schema(description = "联系地址")
    private String address;

    @Schema(description = "出生日期")
    private LocalDate birthday;

    @Schema(description = "备注")
    private String remark;
}
