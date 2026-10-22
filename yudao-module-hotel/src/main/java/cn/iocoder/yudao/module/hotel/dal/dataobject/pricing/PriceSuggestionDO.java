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
 * 智能定价建议 DO（T1）
 */
@TableName("hotel_price_suggestion")
@KeySequence("hotel_price_suggestion_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceSuggestionDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 门店编号（0 表示未分配） */
    private Long storeId;

    /** 房型编号 */
    private Long roomTypeId;

    /** 业务日期 */
    private LocalDate bizDate;

    /** 基线价（日历价） */
    private BigDecimal basePrice;

    /** 建议售价 */
    private BigDecimal suggestedPrice;

    /** 出租率系数 */
    private BigDecimal occFactor;

    /** 提前期系数 */
    private BigDecimal advanceFactor;

    /** 周末/节假日系数 */
    private BigDecimal weekendFactor;

    /** 历史同期出租率（0-1） */
    private BigDecimal histOccupancy;

    /** 库存进度（已售/总量） */
    private BigDecimal bookingPace;

    /** 提前天数 */
    private Integer leadDays;

    /** 建议理由（人话） */
    private String reason;

    /** 状态，见 PriceSuggestionStatusEnum */
    private Integer status;

    /** 实际采纳价 */
    private BigDecimal adoptedPrice;
}
