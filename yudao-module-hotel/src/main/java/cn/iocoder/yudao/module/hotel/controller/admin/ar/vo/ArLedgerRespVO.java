package cn.iocoder.yudao.module.hotel.controller.admin.ar.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ArLedgerRespVO {

    private Long id;
    private Long storeId;
    private Long arAccountId;
    private String arAccountName;
    private Long orderId;
    private Long folioId;
    private BigDecimal amount;
    private Integer type;
    private LocalDate bizDate;
    private Long settlementId;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;

}
