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
 * OTA 订单映射 DO
 */
@TableName("hotel_ota_order")
@KeySequence("hotel_ota_order_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtaOrderDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long storeId;
    private String otaCode;
    private String otaOrderNo;
    private Long orderId;
    /** 10 待处理 20 已建单 30 已确认 40 已取消 50 失败 */
    private Integer syncStatus;
    private String rawData;
    private String failReason;
    private LocalDateTime syncTime;

}
