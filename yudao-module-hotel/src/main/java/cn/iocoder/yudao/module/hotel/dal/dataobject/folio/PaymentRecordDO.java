package cn.iocoder.yudao.module.hotel.dal.dataobject.folio;

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
import java.time.LocalDateTime;

/**
 * 支付流水 DO
 */
@TableName("hotel_payment_record")
@KeySequence("hotel_payment_record_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRecordDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 门店编号（0 表示未分配） */
    private Long storeId;

    /** 账页编号 */
    private Long folioId;

    /** 订单编号 */
    private Long orderId;

    /** 外部交易号 */
    private String tradeNo;

    /** 金额（退款为负） */
    private BigDecimal amount;

    /** 支付方式，见 PaymentMethodEnum */
    private Integer method;

    /** 类型，见 PaymentTypeEnum */
    private Integer type;

    /** 状态，见 PaymentRecordStatusEnum */
    private Integer status;

    /** 支付时间 */
    private LocalDateTime payTime;

    /** 操作人 */
    private Long operatorId;

    /** 备注 */
    private String remark;
}
