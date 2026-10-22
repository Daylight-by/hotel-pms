package cn.iocoder.yudao.module.hotel.dal.dataobject.lock;

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
 * 门锁发卡日志 DO
 */
@TableName("hotel_lock_card_log")
@KeySequence("hotel_lock_card_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LockCardLogDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long storeId;
    private Long orderId;
    private Long roomId;
    private String roomNo;
    private String cardNo;
    /** 10 发卡 20 补卡 30 销卡 40 续卡 */
    private Integer actionType;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    /** 10 待处理 20 成功 30 失败 */
    private Integer status;
    private String failReason;
    private LocalDateTime handleTime;

}
