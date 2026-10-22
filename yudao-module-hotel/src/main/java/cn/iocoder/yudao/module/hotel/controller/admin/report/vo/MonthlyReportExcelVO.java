package cn.iocoder.yudao.module.hotel.controller.admin.report.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 经营月报导出行
 */
@Data
public class MonthlyReportExcelVO {

    @ExcelProperty("月份")
    private String month;

    @ExcelProperty("营业天数")
    private Integer days;

    @ExcelProperty("出租率(%)")
    private Double occ;

    @ExcelProperty("平均房价 ADR(元)")
    private Double adr;

    @ExcelProperty("RevPAR(元)")
    private Double revpar;

    @ExcelProperty("房费收入(元)")
    private BigDecimal roomRevenue;

    @ExcelProperty("其他收入(元)")
    private BigDecimal otherRevenue;

    @ExcelProperty("总营收(元)")
    private BigDecimal totalRevenue;
}
