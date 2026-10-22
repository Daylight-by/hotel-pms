package cn.iocoder.yudao.module.hotel.dal.dataobject.rate;

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
 * 房价日历 DO（房型 × 日期）
 */
@TableName("hotel_room_daily_rate")
@KeySequence("hotel_room_daily_rate_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDailyRateDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 门店编号（0 表示未分配） */
    private Long storeId;

    /** 房型编号 */
    private Long roomTypeId;

    /** 业务日期 */
    private LocalDate bizDate;

    /** 当日房价 */
    private BigDecimal price;

    /** 总库存（可售房量） */
    private Integer totalInventory;

    /** 已售房量 */
    private Integer soldInventory;

    /** 状态，见 DailyRateStatusEnum：10 开房 20 关房 */
    private Integer status;

    /** 备注 */
    private String remark;
}
