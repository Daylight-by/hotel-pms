package cn.iocoder.yudao.module.hotel.dal.dataobject.ota;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * OTA 渠道对接配置 DO
 */
@TableName("hotel_ota_config")
@KeySequence("hotel_ota_config_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtaConfigDO extends TenantBaseDO {

    @TableId
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
    /** 0 启用 1 停用 */
    private Integer status;
    private LocalDateTime lastSyncTime;
    private String remark;

}
