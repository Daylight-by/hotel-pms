package cn.iocoder.yudao.module.hotel.controller.admin.folio.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = "管理后台 - 账务明细分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class FolioItemPageReqVO extends PageParam {

    @Schema(description = "账页编号")
    private Long folioId;

    @Schema(description = "订单编号")
    private Long orderId;

    @Schema(description = "项目类型")
    private Integer itemType;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "营业日起")
    @DateTimeFormat(iso = DATE)
    private LocalDate beginDate;

    @Schema(description = "营业日止")
    @DateTimeFormat(iso = DATE)
    private LocalDate endDate;
}
