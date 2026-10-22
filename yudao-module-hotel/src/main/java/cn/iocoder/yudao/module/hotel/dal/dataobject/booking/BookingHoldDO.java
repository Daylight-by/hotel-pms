package cn.iocoder.yudao.module.hotel.dal.dataobject.booking;

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
import java.time.LocalDateTime;

/**
 * 预订占位 DO（T1）：同一 holdNo 在区间内每天一行
 */
@TableName("hotel_booking_hold")
@KeySequence("hotel_booking_hold_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingHoldDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 门店编号（0 表示未分配） */
    private Long storeId;

    /** 占位号（UUID） */
    private String holdNo;

    /** 房型编号 */
    private Long roomTypeId;

    /** 日期 */
    private LocalDate bizDate;

    /** 占房数 */
    private Integer roomCount;

    /** 转化后的订单编号 */
    private Long orderId;

    /** 状态，见 HoldStatusEnum */
    private Integer status;

    /** 过期时间 */
    private LocalDateTime expireTime;
}
