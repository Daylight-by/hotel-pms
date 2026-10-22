package cn.iocoder.yudao.module.hotel.controller.admin.folio.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 拆账页 Request VO")
@Data
public class FolioSplitReqVO {

    @Schema(description = "原账页编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请选择账页")
    private Long folioId;

    @Schema(description = "要拆出的明细编号列表", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "请选择要拆出的明细")
    private List<Long> itemIds;

    @Schema(description = "备注")
    private String remark;
}
