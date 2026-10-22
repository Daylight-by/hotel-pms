package cn.iocoder.yudao.module.hotel.dal.dataobject.invoice;

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
 * 发票 DO
 */
@TableName("hotel_invoice")
@KeySequence("hotel_invoice_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 门店编号（0 表示未分配） */
    private Long storeId;

    /** 发票号码 */
    private String invoiceNo;

    /** 订单编号 */
    private Long orderId;

    /** 住客编号 */
    private Long guestId;

    /** 发票抬头 */
    private String title;

    /** 税号 */
    private String taxNo;

    /** 类型，见 InvoiceTypeEnum */
    private Integer type;

    /** 开票金额 */
    private BigDecimal amount;

    /** 状态，见 InvoiceStatusEnum */
    private Integer status;

    /** 开票时间 */
    private LocalDateTime issueTime;

    /** 红冲时间 */
    private LocalDateTime voidTime;

    /** 操作人 */
    private Long operatorId;

    /** 备注 */
    private String remark;
}
