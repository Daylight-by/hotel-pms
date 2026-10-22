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

/**
 * 协议单位挂账明细 DO
 */
@TableName("hotel_ar_ledger")
@KeySequence("hotel_ar_ledger_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArLedgerDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long storeId;
    private Long arAccountId;
    private Long orderId;
    private Long folioId;
    /** 正数挂账，负数冲销 */
    private BigDecimal amount;
    /** 10 挂账 20 冲销 30 结算 */
    private Integer type;
    private LocalDate bizDate;
    private Long settlementId;
    /** 10 未结 20 已结 */
    private Integer status;
    private Long operatorId;
    private String remark;

}
