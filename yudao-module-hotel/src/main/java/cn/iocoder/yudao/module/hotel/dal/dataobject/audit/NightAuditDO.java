package cn.iocoder.yudao.module.hotel.dal.dataobject.audit;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 夜审记录 DO
 */
@TableName("hotel_night_audit")
@KeySequence("hotel_night_audit_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NightAuditDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 门店编号（0 表示未分配） */
    private Long storeId;

    /** 被审计的营业日 */
    private LocalDate bizDate;

    /** 状态，见 NightAuditStatusEnum */
    private Integer status;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 过账房费合计 */
    private BigDecimal roomChargeTotal;

    /** 过账房数 */
    private Integer postedRoomCount;

    /** 过账明细数 */
    private Integer postedItemCount;

    /** 失败原因 */
    private String failReason;

    /** 操作人 */
    private Long operatorId;
}
