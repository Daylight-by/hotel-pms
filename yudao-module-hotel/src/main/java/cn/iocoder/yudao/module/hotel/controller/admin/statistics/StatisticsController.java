package cn.iocoder.yudao.module.hotel.controller.admin.statistics;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.hotel.service.statistics.StatisticsServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Tag(name = "管理后台 - 酒店统计")
@RestController
@RequestMapping("/hotel/statistics")
public class StatisticsController {

    @Resource
    private StatisticsServiceImpl statisticsService;

    @GetMapping("/overview")
    @Operation(summary = "经营概览")
    @PreAuthorize("@ss.hasPermission('hotel:statistics:query')")
    public CommonResult<Map<String, Object>> overview() {
        return success(statisticsService.overview());
    }

    @GetMapping("/summary")
    @Operation(summary = "核心经营指标（OCC / ADR / RevPAR）")
    @PreAuthorize("@ss.hasPermission('hotel:statistics:query')")
    public CommonResult<Map<String, Object>> summary(
            @RequestParam(value = "beginDate", required = false) @DateTimeFormat(iso = DATE) LocalDate beginDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DATE) LocalDate endDate) {
        return success(statisticsService.summary(beginDate, endDate));
    }

    @GetMapping("/trend")
    @Operation(summary = "收入与入住率趋势（支持 beginDate/endDate/type=day|month，兼容 days 参数）")
    @PreAuthorize("@ss.hasPermission('hotel:statistics:query')")
    public CommonResult<List<Map<String, Object>>> trend(
            @RequestParam(value = "beginDate", required = false) @DateTimeFormat(iso = DATE) LocalDate beginDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DATE) LocalDate endDate,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "days", defaultValue = "14") int days) {
        if (beginDate == null && endDate == null) {
            return success(statisticsService.trend(days));
        }
        return success(statisticsService.trendRange(beginDate, endDate, type));
    }

    @GetMapping("/source")
    @Operation(summary = "客源结构与收款结构")
    @PreAuthorize("@ss.hasPermission('hotel:statistics:query')")
    public CommonResult<Map<String, Object>> source(
            @RequestParam(value = "beginDate", required = false) @DateTimeFormat(iso = DATE) LocalDate beginDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DATE) LocalDate endDate) {
        return success(statisticsService.source(beginDate, endDate));
    }

    @GetMapping("/payment-share")
    @Operation(summary = "支付方式占比")
    @PreAuthorize("@ss.hasPermission('hotel:statistics:query')")
    public CommonResult<List<Map<String, Object>>> paymentShare() {
        return success(statisticsService.paymentShare());
    }
}
