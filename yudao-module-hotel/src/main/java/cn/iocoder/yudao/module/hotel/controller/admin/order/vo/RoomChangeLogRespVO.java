package cn.iocoder.yudao.module.hotel.controller.admin.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 换房记录 Response VO")
@Data
public class RoomChangeLogRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "订单编号")
    private Long orderId;

    @Schema(description = "原房间编号")
    private Long fromRoomId;

    @Schema(description = "原房间号")
    private String fromRoomNo;

    @Schema(description = "新房间编号")
    private Long toRoomId;

    @Schema(description = "新房间号")
    private String toRoomNo;

    @Schema(description = "换房时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime changeTime;

    @Schema(description = "换房原因")
    private String reason;
}
