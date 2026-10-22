package cn.iocoder.yudao.module.hotel.controller.admin.folio.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 账页创建 Request VO")
@Data
public class FolioCreateReqVO {

    @Schema(description = "订单编号")
    private Long orderId;

    @Schema(description = "住客编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请选择住客")
    private Long guestId;

    @Schema(description = "房间编号")
    private Long roomId;

    @Schema(description = "账页类型：10 主账页 20 分账页")
    private Integer type;

    @Schema(description = "备注")
    private String remark;
}
