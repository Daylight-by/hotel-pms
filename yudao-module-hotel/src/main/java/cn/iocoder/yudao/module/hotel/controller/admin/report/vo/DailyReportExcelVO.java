package cn.iocoder.yudao.module.hotel.controller.admin.report.vo;

import cn.idev.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 经营日报导出行
 */
@Data
public class DailyReportExcelVO {

    @ExcelProperty("营业日")
    private String bizDate;

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

    @ExcelProperty("在住间夜")
    private Long soldNights;

    @ExcelProperty("可售间夜")
    private Long availableNights;

    @ExcelProperty("离店间夜")
    private Long departureNights;

    @ExcelProperty("到店数")
    private Integer arrivals;

    @ExcelProperty("离店数")
    private Integer departures;

    @ExcelProperty("欠账余额(元)")
    private BigDecimal arBalance;
}
