package cn.iocoder.yudao.module.hotel.controller.admin.pricing.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 定价回测分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class PriceBacktestPageReqVO extends PageParam {

    @Schema(description = "回测名称", example = "10 月")
    private String name;

    @Schema(description = "房型编号", example = "1")
    private Long roomTypeId;
}
