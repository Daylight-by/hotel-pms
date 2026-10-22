package cn.iocoder.yudao.module.hotel.controller.admin.folio.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - 账页冲销 Request VO")
@Data
public class FolioVoidReqVO {

    @Schema(description = "明细编号（也可通过 query 参数 id 传入）")
    private Long itemId;

    @Schema(description = "冲销原因")
    private String reason;
}
