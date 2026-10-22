package cn.iocoder.yudao.module.hotel.service.pay.client;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付回调解析结果
 */
@Data
public class PayNotifyResult {

    /** 是否支付成功 */
    private Boolean success;

    /** 本站支付单号 */
    private String payOrderNo;

    /** 第三方交易号 */
    private String channelTradeNo;

    /** 第三方实付金额 */
    private BigDecimal amount;

    /** 原始报文（截断后入库） */
    private String raw;
}
