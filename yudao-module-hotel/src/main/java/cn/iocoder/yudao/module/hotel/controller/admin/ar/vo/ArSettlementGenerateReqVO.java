package cn.iocoder.yudao.module.hotel.controller.admin.ar.vo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ArSettlementGenerateReqVO {

    @NotNull(message = "协议单位不能为空")
    private Long arAccountId;

    @NotNull(message = "账期起不能为空")
    private LocalDate beginDate;

    @NotNull(message = "账期止不能为空")
    private LocalDate endDate;

    private String remark;

}
