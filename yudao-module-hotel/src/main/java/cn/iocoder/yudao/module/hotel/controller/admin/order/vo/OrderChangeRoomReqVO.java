package cn.iocoder.yudao.module.hotel.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 订单换房 Request VO")
@Data
public class OrderChangeRoomReqVO {

    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请选择订单")
    private Long id;

    @Schema(description = "目标房间编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请选择目标房间")
    private Long toRoomId;

    @Schema(description = "换房原因")
    private String reason;
}
