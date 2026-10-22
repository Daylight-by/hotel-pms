package cn.iocoder.yudao.module.hotel.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = "管理后台 - 订单新增/修改 Request VO")
@Data
public class OrderSaveReqVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "住客编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请选择住客")
    private Long guestId;

    @Schema(description = "房间编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请选择房间")
    private Long roomId;

    @Schema(description = "入住日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请选择入住日期")
    @DateTimeFormat(iso = DATE)
    private LocalDate checkInDate;

    @Schema(description = "离店日期", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请选择离店日期")
    @DateTimeFormat(iso = DATE)
    private LocalDate checkOutDate;

    @Schema(description = "成人数", example = "1")
    private Integer adultCount;

    @Schema(description = "儿童数", example = "0")
    private Integer childCount;

    @Schema(description = "押金", example = "200.00")
    private BigDecimal deposit;

    @Schema(description = "原订单编号（续住/换房时关联）")
    private Long parentOrderId;

    @Schema(description = "备注")
    private String remark;
}
