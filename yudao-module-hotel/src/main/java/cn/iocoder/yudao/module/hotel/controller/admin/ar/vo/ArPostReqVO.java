package cn.iocoder.yudao.module.hotel.controller.admin.ar.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ArPostReqVO {

    @NotNull(message = "协议单位不能为空")
    private Long arAccountId;

    @NotNull(message = "挂账金额不能为空")
    private BigDecimal amount;

    private Long orderId;
    private Long folioId;
    private String remark;

}
