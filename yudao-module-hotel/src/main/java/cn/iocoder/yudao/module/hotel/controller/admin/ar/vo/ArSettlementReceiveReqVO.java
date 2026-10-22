package cn.iocoder.yudao.module.hotel.controller.admin.ar.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ArSettlementReceiveReqVO {

    @NotNull(message = "结算单不能为空")
    private Long settlementId;

    @NotNull(message = "收款金额不能为空")
    private BigDecimal amount;

    /** 收款方式：1 现金 2 银行卡 3 微信 4 支付宝 5 在线 6 挂账 */
    private Integer paymentMethod;

}
