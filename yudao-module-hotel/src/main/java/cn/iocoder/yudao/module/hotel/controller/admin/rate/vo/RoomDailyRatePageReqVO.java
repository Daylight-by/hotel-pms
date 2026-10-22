package cn.iocoder.yudao.module.hotel.controller.admin.rate.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = "管理后台 - 房价日历分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class RoomDailyRatePageReqVO extends PageParam {

    @Schema(description = "房型编号")
    private Long roomTypeId;

    @Schema(description = "开始日期")
    @DateTimeFormat(iso = DATE)
    private LocalDate beginDate;

    @Schema(description = "结束日期")
    @DateTimeFormat(iso = DATE)
    private LocalDate endDate;

    @Schema(description = "状态：10 开房 20 关房")
    private Integer status;
}
