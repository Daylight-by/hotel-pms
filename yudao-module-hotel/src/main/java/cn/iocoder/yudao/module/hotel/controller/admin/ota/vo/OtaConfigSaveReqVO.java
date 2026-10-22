package cn.iocoder.yudao.module.hotel.controller.admin.ota.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OtaConfigSaveReqVO {

    private Long id;

    @NotNull(message = "渠道不能为空")
    private Long channelId;

    @NotBlank(message = "OTA 编码不能为空")
    private String otaCode;

    private String appKey;
    private String appSecret;
    private String apiUrl;
    private Boolean pushRate;
    private Boolean pullOrder;
    private Integer syncInterval;
    private Boolean mockEnabled;
    private Integer status;
    private String remark;

}
