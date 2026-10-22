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
 * 电子发票申请 DO
 */
@TableName("hotel_invoice_apply")
@KeySequence("hotel_invoice_apply_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceApplyDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 门店编号（0 表示未分配） */
    private Long storeId;

    /** 申请单号 */
    private String applyNo;

    /** 订单编号 */
    private Long orderId;

    /** 会员编号（H5 自助申请） */
    private Long memberId;

    /** 发票抬头 */
    private String title;

    /** 税号 */
    private String taxNo;

    /** 接收邮箱 */
    private String email;

    /** 类型，见 InvoiceTypeEnum */
    private Integer type;

    /** 开票金额 */
    private BigDecimal amount;

    /** 状态，见 InvoiceApplyStatusEnum */
    private Integer status;

    /** 开具后关联的发票编号 */
    private Long invoiceId;

    /** 驳回原因 */
    private String rejectReason;

    /** 处理时间 */
    private LocalDateTime handleTime;
}
