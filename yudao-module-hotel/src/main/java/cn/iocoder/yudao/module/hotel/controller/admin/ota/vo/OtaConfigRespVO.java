package cn.iocoder.yudao.module.hotel.controller.admin.ota.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OtaConfigRespVO {

    private Long id;
    private Long storeId;
    private Long channelId;
    private String otaCode;
    private String appKey;
    private String appSecret;
    private String apiUrl;
    private Boolean pushRate;
    private Boolean pullOrder;
    private Integer syncInterval;
    private Boolean mockEnabled;
    private Integer status;
    private LocalDateTime lastSyncTime;
    private String remark;
    private LocalDateTime createTime;

}
