package cn.iocoder.yudao.module.hotel.controller.admin.order.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "管理后台 - 钟点房开单 Request VO")
@Data
public class OrderHourlyCreateReqVO {

    @Schema(description = "住客编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请选择住客")
    private Long guestId;

    @Schema(description = "房间编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请选择房间")
    private Long roomId;

    @Schema(description = "入住时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-10-21 10:00:00")
    @NotNull(message = "请选择入住时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime checkInTime;

    @Schema(description = "离店时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-10-21 13:00:00")
    @NotNull(message = "请选择离店时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime checkOutTime;

    @Schema(description = "成人数")
    private Integer adultCount;

    @Schema(description = "儿童数")
    private Integer childCount;

    @Schema(description = "押金")
    private BigDecimal deposit;

    @Schema(description = "备注")
    private String remark;
}
