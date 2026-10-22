package cn.iocoder.yudao.module.hotel.controller.admin.ar.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ArAccountRespVO {

    private Long id;
    private Long storeId;
    private String storeName;
    private String name;
    private String code;
    private String taxNo;
    private String contactName;
    private String contactMobile;
    private BigDecimal creditLimit;
    private BigDecimal usedAmount;
    private BigDecimal availableAmount;
    private Integer settleCycle;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;

}
