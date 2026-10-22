package cn.iocoder.yudao.module.hotel.controller.app.booking.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "用户 App - 提交预订 Request VO")
@Data
public class AppBookingReqVO {

    @Schema(description = "占位号", requiredMode = Schema.RequiredMode.REQUIRED, example = "8f1c2a5e9d7b4c6e")
    @NotBlank(message = "占位号不能为空")
    private String holdNo;

    @Schema(description = "联系人姓名", example = "张三")
    @Size(max = 50, message = "联系人姓名过长")
    private String contactName;

    @Schema(description = "联系手机", example = "13900001111")
    @NotBlank(message = "请填写联系手机")
    private String contactMobile;

    @Schema(description = "入住人姓名（为空取联系人）", example = "张三")
    private String guestName;

    @Schema(description = "入住人手机（为空取联系人手机）", example = "13900001111")
    private String guestMobile;

    @Schema(description = "预计到店时间", example = "18:00")
    private String arrivalTime;

    @Schema(description = "备注", example = "需要无烟房")
    @Size(max = 200, message = "备注过长")
    private String remark;
}
