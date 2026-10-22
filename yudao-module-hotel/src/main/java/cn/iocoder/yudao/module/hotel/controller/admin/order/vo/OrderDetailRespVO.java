package cn.iocoder.yudao.module.hotel.controller.admin.order.vo;

import cn.iocoder.yudao.module.hotel.controller.admin.folio.vo.FolioRespVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 订单详情 Response VO（订单 + 账页 + 换房记录）")
@Data
public class OrderDetailRespVO {

    @Schema(description = "订单")
    private OrderRespVO order;

    @Schema(description = "主账页（含明细）")
    private FolioRespVO folio;

    @Schema(description = "同订单的全部账页")
    private List<FolioRespVO> folios;

    @Schema(description = "换房记录")
    private List<RoomChangeLogRespVO> roomChanges;
}
