package cn.iocoder.yudao.module.hotel.controller.admin.store.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 门店分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class StorePageReqVO extends PageParam {

    @Schema(description = "门店名称")
    private String name;

    @Schema(description = "门店编码")
    private String code;

    @Schema(description = "状态：0 营业 1 停业")
    private Integer status;

}
