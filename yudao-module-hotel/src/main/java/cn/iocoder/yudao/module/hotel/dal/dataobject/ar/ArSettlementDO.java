package cn.iocoder.yudao.module.hotel.dal.dataobject.ar;

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
 * 协议单位结算单 DO
 */
@TableName("hotel_ar_settlement")
@KeySequence("hotel_ar_settlement_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArSettlementDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long storeId;
    private String settlementNo;
    private Long arAccountId;
    private LocalDate beginDate;
    private LocalDate endDate;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    /** 10 待结算 20 部分收款 30 已结清 */
    private Integer status;
    private LocalDateTime settleTime;
    private String remark;

}
