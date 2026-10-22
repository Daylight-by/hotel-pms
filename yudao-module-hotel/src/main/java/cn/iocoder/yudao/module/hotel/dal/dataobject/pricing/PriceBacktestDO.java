package cn.iocoder.yudao.module.hotel.dal.dataobject.pricing;

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
 * 定价回测 DO（T1）
 */
@TableName("hotel_price_backtest")
@KeySequence("hotel_price_backtest_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceBacktestDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 门店编号（0 表示未分配） */
    private Long storeId;

    /** 回测名称 */
    private String name;

    /** 房型编号（空 = 全部房型） */
    private Long roomTypeId;

    /** 区间起 */
    private LocalDate beginDate;

    /** 区间止 */
    private LocalDate endDate;

    /** 按原价的 RevPAR */
    private BigDecimal actualRevpar;

    /** 按建议价的 RevPAR */
    private BigDecimal modelRevpar;

    /** 提升比例（%） */
    private BigDecimal upliftRate;

    /** 逐日明细 JSON */
    private String detail;
}
