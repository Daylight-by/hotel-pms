package cn.iocoder.yudao.module.hotel.dal.dataobject.pay;

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
 * 酒店在线支付单 DO
 */
@TableName("hotel_pay_order")
@KeySequence("hotel_pay_order_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayOrderDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 门店编号（0 表示未分配） */
    private Long storeId;

    /** 本站支付单号 */
    private String payOrderNo;

    /** 业务订单编号（hotel_order.id） */
    private Long bizOrderId;

    /** 业务类型，见 PayBizTypeEnum */
    private Integer bizType;

    /** 支付金额（元） */
    private BigDecimal amount;

    /** 支付渠道：wxpay/alipay/mock */
    private String channel;

    /** 第三方交易号 */
    private String channelTradeNo;

    /** 状态，见 PayOrderStatusEnum */
    private Integer status;

    /** 支付单过期时间 */
    private LocalDateTime expireTime;

    /** 支付成功时间 */
    private LocalDateTime payTime;

    /** 已退金额 */
    private BigDecimal refundAmount;

    /** 回调原始报文 */
    private String notifyData;
}
