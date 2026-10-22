package cn.iocoder.yudao.module.hotel.controller.admin.folio.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Schema(description = "管理后台 - 账页入账（消费）Request VO")
@Data
public class FolioPostReqVO {

    @Schema(description = "账页编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "请选择账页")
    private Long folioId;

    @Schema(description = "项目类型：10 房费 20 餐饮 30 迷你吧 40 洗衣 50 赔偿 60 押金 70 其他 80 退款", example = "20")
    @NotNull(message = "请选择项目类型")
    private Integer itemType;

    @Schema(description = "项目名称", example = "餐饮")
    private String itemName;

    @Schema(description = "数量", example = "1")
    private Integer quantity;

    @Schema(description = "单价", example = "88.00")
    private BigDecimal unitPrice;

    @Schema(description = "金额（不传则按 数量 × 单价 计算）", example = "88.00")
    private BigDecimal amount;

    @Schema(description = "营业日（不传取当前营业日）")
    @DateTimeFormat(iso = DATE)
    private LocalDate businessDate;

    @Schema(description = "备注")
    private String remark;
}
