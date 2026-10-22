package cn.iocoder.yudao.module.hotel.controller.app.booking.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "用户 App - 创建预订占位 Response VO")
@Data
public class AppHoldRespVO {

    @Schema(description = "占位号（提交订单时回传）", example = "8f1c2a5e9d7b4c6e")
    private String holdNo;

    @Schema(description = "占位过期时间（15 分钟）")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime expireTime;

    @Schema(description = "房型编号", example = "1")
    private Long roomTypeId;

    @Schema(description = "房型名称", example = "单人间")
    private String roomTypeName;

    @Schema(description = "房价计划编号", example = "1")
    private Long ratePlanId;

    @Schema(description = "晚数", example = "2")
    private Integer nights;

    @Schema(description = "占房数", example = "1")
    private Integer roomCount;

    @Schema(description = "预估总额", example = "540.00")
    private BigDecimal totalAmount;
}
