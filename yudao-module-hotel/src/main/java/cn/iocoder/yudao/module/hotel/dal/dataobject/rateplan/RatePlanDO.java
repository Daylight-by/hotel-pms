package cn.iocoder.yudao.module.hotel.dal.dataobject.rateplan;

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

/**
 * 房价计划 DO（T1）
 */
@TableName("hotel_rate_plan")
@KeySequence("hotel_rate_plan_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RatePlanDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 门店编号（0 表示未分配） */
    private Long storeId;

    /** 名称 */
    private String name;

    /** 编码 */
    private String code;

    /** 渠道编号 */
    private Long channelId;

    /** 适用房型（空 = 全部房型） */
    private Long roomTypeId;

    /** 价格规则，见 PriceRuleTypeEnum */
    private Integer priceRuleType;

    /** 规则值：折扣率(90=9折)/减额/固定价 */
    private BigDecimal priceRuleValue;

    /** 早餐：0 不含 1 单早 2 双早 */
    private Integer breakfast;

    /** 最少入住晚数 */
    private Integer minNights;

    /** 最多入住晚数 */
    private Integer maxNights;

    /** 最少提前预订小时数 */
    private Integer minAdvanceHours;

    /** 取消政策，见 CancelPolicyEnum */
    private Integer cancelPolicy;

    /** 免费取消截止小时数 */
    private Integer cancelFreeHours;

    /** 是否需预付 */
    private Boolean needDeposit;

    /** 预付比例（%） */
    private BigDecimal depositRate;

    /** 状态：0 开启 1 关闭 */
    private Integer status;

    /** 排序 */
    private Integer sort;

    /** 备注 */
    private String remark;
}
