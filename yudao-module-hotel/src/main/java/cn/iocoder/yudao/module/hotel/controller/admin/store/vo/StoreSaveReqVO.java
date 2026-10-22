package cn.iocoder.yudao.module.hotel.controller.admin.store.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "管理后台 - 门店新增/修改 Request VO")
@Data
public class StoreSaveReqVO {

    @Schema(description = "编号")
    private Long id;

    @Schema(description = "门店名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "门店名称不能为空")
    private String name;

    @Schema(description = "门店编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "门店编码不能为空")
    private String code;

    private String province;
    private String city;
    private String district;
    private String address;
    private String phone;
    private String legalPerson;
    private String psbCode;
    private String checkInTime;
    private String checkOutTime;
    private Integer status;
    private Integer sort;
    private String remark;

}
