package cn.iocoder.yudao.module.hotel.controller.admin.guest.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 住客分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class GuestPageReqVO extends PageParam {

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "手机号")
    private String mobile;
}
