package cn.iocoder.yudao.module.hotel.controller.admin.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - 订单续住 Request VO")
@Data
public class OrderExtendReqVO {

    @Schema(description = "订单编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请选择订单")
    private Long id;

    @Schema(description = "续住晚数", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "请输入续住晚数")
    @Min(value = 1, message = "续住晚数至少 1 晚")
    private Integer nights;

    @Schema(description = "备注")
    private String remark;
}
