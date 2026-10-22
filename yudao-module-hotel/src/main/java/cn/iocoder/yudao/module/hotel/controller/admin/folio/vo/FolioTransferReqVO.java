package cn.iocoder.yudao.module.hotel.controller.admin.folio.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - 账页转账 Request VO")
@Data
public class FolioTransferReqVO {

    @Schema(description = "明细编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请选择明细")
    private Long itemId;

    @Schema(description = "目标账页编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请选择目标账页")
    private Long targetFolioId;
}
