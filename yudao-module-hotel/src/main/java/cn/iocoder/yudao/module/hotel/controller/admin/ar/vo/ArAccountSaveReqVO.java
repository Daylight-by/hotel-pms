package cn.iocoder.yudao.module.hotel.controller.admin.ar.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Schema(description = "管理后台 - 协议单位新增/修改 Request VO")
@Data
public class ArAccountSaveReqVO {

    private Long id;

    @Schema(description = "单位名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "单位名称不能为空")
    private String name;

    @Schema(description = "单位编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "单位编码不能为空")
    private String code;

    private String taxNo;
    private String contactName;
    private String contactMobile;
    private BigDecimal creditLimit;
    private Integer settleCycle;
    private Integer status;
    private String remark;

}
