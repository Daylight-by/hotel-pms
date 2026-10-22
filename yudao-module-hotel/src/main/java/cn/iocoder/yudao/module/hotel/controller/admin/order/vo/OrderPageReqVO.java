package cn.iocoder.yudao.module.hotel.controller.admin.order.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = "管理后台 - 订单分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderPageReqVO extends PageParam {

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "住客编号")
    private Long guestId;

    @Schema(description = "房间编号")
    private Long roomId;

    @Schema(description = "订单状态")
    private Integer status;

    @Schema(description = "支付状态")
    private Integer paymentStatus;

    @Schema(description = "订单类型：10 全日房 20 钟点房")
    private Integer orderType;

    @Schema(description = "入住日期起")
    @DateTimeFormat(iso = DATE)
    private LocalDate checkInDateStart;

    @Schema(description = "离店日期止")
    @DateTimeFormat(iso = DATE)
    private LocalDate checkOutDateEnd;
}
