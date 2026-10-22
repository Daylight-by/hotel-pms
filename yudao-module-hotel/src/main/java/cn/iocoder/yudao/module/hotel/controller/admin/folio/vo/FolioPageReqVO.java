package cn.iocoder.yudao.module.hotel.controller.admin.folio.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Schema(description = "管理后台 - 账页分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class FolioPageReqVO extends PageParam {

    @Schema(description = "账页号")
    private String folioNo;

    @Schema(description = "订单编号")
    private Long orderId;

    @Schema(description = "住客编号")
    private Long guestId;

    @Schema(description = "状态：10 未结 20 部分结账 30 已结账 40 已转出 50 已取消")
    private Integer status;

    @Schema(description = "账页类型：10 主账页 20 分账页")
    private Integer type;
}
