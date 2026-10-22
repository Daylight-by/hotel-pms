package cn.iocoder.yudao.module.hotel.controller.admin.demo;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.hotel.service.demo.DemoDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 酒店演示数据")
@RestController
@RequestMapping("/hotel/demo")
@Validated
public class DemoController {

    @Resource
    private DemoDataService demoDataService;

    @PostMapping("/generate")
    @Operation(summary = "生成演示数据（6 个月约 400-600 笔订单）")
    @PreAuthorize("@ss.hasPermission('hotel:demo:generate')")
    public CommonResult<Map<String, Object>> generate(@RequestParam(value = "months", defaultValue = "6") Integer months) {
        return success(demoDataService.generate(months));
    }

    @PostMapping("/clean")
    @Operation(summary = "清理演示数据（幂等）")
    @PreAuthorize("@ss.hasPermission('hotel:demo:clean')")
    public CommonResult<Map<String, Object>> clean() {
        return success(demoDataService.clean());
    }

    @GetMapping("/status")
    @Operation(summary = "演示数据状态")
    @PreAuthorize("@ss.hasPermission('hotel:demo:query')")
    public CommonResult<Map<String, Object>> status() {
        return success(demoDataService.status());
    }
}
