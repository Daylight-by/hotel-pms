package cn.iocoder.yudao.module.hotel.controller.admin.ar.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Data
@EqualsAndHashCode(callSuper = true)
public class ArLedgerPageReqVO extends PageParam {

    private Long arAccountId;
    private Long orderId;
    private Integer type;
    private Integer status;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate beginDate;

    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY)
    private LocalDate endDate;

}
