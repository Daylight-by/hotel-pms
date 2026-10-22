package cn.iocoder.yudao.module.hotel.controller.admin.report;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.module.hotel.controller.admin.report.vo.DailyReportExcelVO;
import cn.iocoder.yudao.module.hotel.controller.admin.report.vo.MonthlyReportExcelVO;
import cn.iocoder.yudao.module.hotel.service.report.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY;

@Tag(name = "管理后台 - 酒店经营报表")
@RestController
@RequestMapping("/hotel/statistics")
@Validated
public class ReportController {

    @Resource
    private ReportService reportService;

    @GetMapping("/daily-report")
    @Operation(summary = "经营日报")
    @PreAuthorize("@ss.hasPermission('hotel:statistics:query')")
    public CommonResult<Map<String, Object>> dailyReport(
            @RequestParam("bizDate") @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY) LocalDate bizDate) {
        return success(reportService.dailyReport(bizDate));
    }

    @GetMapping("/monthly-report")
    @Operation(summary = "经营月报")
    @PreAuthorize("@ss.hasPermission('hotel:statistics:query')")
    public CommonResult<Map<String, Object>> monthlyReport(@RequestParam("month") String month) {
        return success(reportService.monthlyReport(month));
    }

    @GetMapping("/export")
    @Operation(summary = "报表导出（type=daily 日报 / monthly 月报）")
    @PreAuthorize("@ss.hasPermission('hotel:statistics:export')")
    public void export(@Valid @RequestParam("beginDate") @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY) LocalDate beginDate,
                       @Valid @RequestParam("endDate") @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY) LocalDate endDate,
                       @RequestParam("type") String type,
                       HttpServletResponse response) throws IOException {
        if (endDate.isBefore(beginDate)) {
            LocalDate tmp = beginDate;
            beginDate = endDate;
            endDate = tmp;
        }
        if ("monthly".equalsIgnoreCase(type)) {
            List<MonthlyReportExcelVO> rows = reportService.exportMonthlyRows(beginDate, endDate);
            ExcelUtils.write(response, "酒店经营月报.xlsx", "经营月报", MonthlyReportExcelVO.class, rows);
        } else {
            List<DailyReportExcelVO> rows = reportService.exportDailyRows(beginDate, endDate);
            ExcelUtils.write(response, "酒店经营日报.xlsx", "经营日报", DailyReportExcelVO.class, rows);
        }
    }
}
