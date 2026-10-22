package cn.iocoder.yudao.module.hotel.controller.admin.ar.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ArSettlementRespVO {

    private Long id;
    private Long storeId;
    private String settlementNo;
    private Long arAccountId;
    private String arAccountName;
    private LocalDate beginDate;
    private LocalDate endDate;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainAmount;
    private Integer status;
    private LocalDateTime settleTime;
    private String remark;
    private LocalDateTime createTime;

}
