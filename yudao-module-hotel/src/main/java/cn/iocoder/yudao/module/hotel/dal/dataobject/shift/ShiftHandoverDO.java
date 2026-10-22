package cn.iocoder.yudao.module.hotel.dal.dataobject.shift;

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
 * 交班记录 DO
 */
@TableName("hotel_shift_handover")
@KeySequence("hotel_shift_handover_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftHandoverDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 门店编号（0 表示未分配） */
    private Long storeId;

    /** 班次号 SH + yyyyMMdd + 两位序号 */
    private String shiftNo;

    /** 营业日 */
    private LocalDate bizDate;

    /** 开班时间 */
    private LocalDateTime startTime;

    /** 交班时间 */
    private LocalDateTime endTime;

    /** 收银员 */
    private Long operatorId;

    /** 现金 */
    private BigDecimal cashAmount;

    /** 银行卡 */
    private BigDecimal cardAmount;

    /** 微信 */
    private BigDecimal wechatAmount;

    /** 支付宝 */
    private BigDecimal alipayAmount;

    /** 其他 */
    private BigDecimal otherAmount;

    /** 合计 */
    private BigDecimal totalAmount;

    /** 状态，见 ShiftStatusEnum */
    private Integer status;

    /** 交接备注 */
    private String remark;
}
