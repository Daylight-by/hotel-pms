package cn.iocoder.yudao.module.hotel.controller.admin.dashboard;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.hotel.service.dashboard.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 酒店经营数据大屏")
@RestController
@RequestMapping("/hotel/dashboard")
@Validated
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    @GetMapping("/overview")
    @Operation(summary = "大屏总览（今日指标/房态/趋势/预抵预离/待办）")
    @PreAuthorize("@ss.hasPermission('hotel:dashboard:query')")
    public CommonResult<Map<String, Object>> overview() {
        return success(dashboardService.overview());
    }
}
