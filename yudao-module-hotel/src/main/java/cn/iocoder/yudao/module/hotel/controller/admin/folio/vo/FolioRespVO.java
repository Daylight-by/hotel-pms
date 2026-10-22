package cn.iocoder.yudao.module.hotel.controller.admin.folio.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - 账页 Response VO")
@Data
public class FolioRespVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "账页号")
    private String folioNo;

    @Schema(description = "订单编号")
    private Long orderId;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "住客编号")
    private Long guestId;

    @Schema(description = "住客姓名")
    private String guestName;

    @Schema(description = "房间编号")
    private Long roomId;

    @Schema(description = "房间号")
    private String roomNo;

    @Schema(description = "账页类型：10 主账页 20 分账页")
    private Integer type;

    @Schema(description = "状态：10 未结 20 部分结账 30 已结账 40 已转出 50 已取消")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "消费合计")
    private BigDecimal totalCharge;

    @Schema(description = "收款合计")
    private BigDecimal totalPayment;

    @Schema(description = "余额（正为应收）")
    private BigDecimal balance;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "明细")
    private List<FolioItemRespVO> items;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
