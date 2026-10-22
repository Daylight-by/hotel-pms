package cn.iocoder.yudao.module.hotel.dal.dataobject.demo;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 演示数据批次 DO
 *
 * <p>记录 T4 演示数据生成器每次生成的批次，供 {@code demo/clean} 精确清理本包产生的数据，
 * 不触碰其它 agent 的数据。</p>
 */
@TableName("hotel_demo_seed")
@KeySequence("hotel_demo_seed_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemoSeedDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 门店编号（0 表示未分配） */
    private Long storeId;

    /** 批次号 */
    private String batchNo;

    /** 生成月数 */
    private Integer months;

    /** 数据窗口起 */
    private LocalDate startDate;

    /** 数据窗口止 */
    private LocalDate endDate;

    /** 订单数 */
    private Integer orderCount;

    /** 住客数 */
    private Integer guestCount;

    /** 房价日历行数 */
    private Integer rateCount;

    /** 状态：10 进行中 20 已清理 */
    private Integer status;

    /** 备注 */
    private String remark;
}
