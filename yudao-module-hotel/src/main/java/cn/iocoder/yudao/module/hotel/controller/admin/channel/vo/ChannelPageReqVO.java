package cn.iocoder.yudao.module.hotel.controller.admin.channel.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 渠道分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class ChannelPageReqVO extends PageParam {

    @Schema(description = "渠道名称", example = "直销")
    private String name;

    @Schema(description = "渠道编码", example = "DIRECT")
    private String code;

    @Schema(description = "类型：10 直销 20 OTA 30 协议 40 会员 50 门店", example = "10")
    private Integer type;

    @Schema(description = "状态：0 开启 1 关闭", example = "0")
    private Integer status;
}
