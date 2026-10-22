package cn.iocoder.yudao.module.hotel.controller.admin.rateplan.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 房价计划分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class RatePlanPageReqVO extends PageParam {

    @Schema(description = "名称", example = "直销 9 折")
    private String name;

    @Schema(description = "编码", example = "DIRECT90")
    private String code;

    @Schema(description = "渠道编号", example = "1")
    private Long channelId;

    @Schema(description = "房型编号", example = "1")
    private Long roomTypeId;

    @Schema(description = "状态：0 开启 1 关闭", example = "0")
    private Integer status;
}
