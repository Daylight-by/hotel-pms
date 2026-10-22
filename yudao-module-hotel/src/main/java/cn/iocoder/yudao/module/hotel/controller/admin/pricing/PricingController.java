package cn.iocoder.yudao.module.hotel.controller.admin.pricing;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.pricing.vo.PriceBacktestPageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.pricing.vo.PriceBacktestReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.pricing.vo.PriceBacktestRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.pricing.vo.PriceGenerateReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.pricing.vo.PriceSuggestionRespVO;
import cn.iocoder.yudao.module.hotel.service.pricing.PricingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Tag(name = "管理后台 - 智能定价")
@RestController
@RequestMapping("/hotel/pricing")
@Validated
public class PricingController {

    @Resource
    private PricingService pricingService;

    @PostMapping("/generate")
    @Operation(summary = "生成定价建议")
    @PreAuthorize("@ss.hasPermission('hotel:pricing:create')")
    public CommonResult<Integer> generate(@Valid @RequestBody PriceGenerateReqVO reqVO) {
        return success(pricingService.generateSuggestions(reqVO));
    }

    @GetMapping("/list")
    @Operation(summary = "查询定价建议")
    @Parameter(name = "roomTypeId", description = "房型编号", required = true)
    @Parameter(name = "beginDate", description = "开始日期", required = true)
    @Parameter(name = "endDate", description = "结束日期", required = true)
    @PreAuthorize("@ss.hasPermission('hotel:pricing:query')")
    public CommonResult<List<PriceSuggestionRespVO>> list(
            @RequestParam("roomTypeId") Long roomTypeId,
            @RequestParam("beginDate") @DateTimeFormat(iso = DATE) LocalDate beginDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DATE) LocalDate endDate) {
        return success(pricingService.listSuggestions(roomTypeId, beginDate, endDate));
    }

    @PutMapping("/adopt")
    @Operation(summary = "采纳定价建议（写回房价日历）")
    @PreAuthorize("@ss.hasPermission('hotel:pricing:update')")
    public CommonResult<Boolean> adopt(@RequestParam("id") Long id,
                                       @RequestParam(value = "price", required = false) BigDecimal price) {
        pricingService.adopt(id, price);
        return success(true);
    }

    @PutMapping("/ignore")
    @Operation(summary = "忽略定价建议")
    @PreAuthorize("@ss.hasPermission('hotel:pricing:update')")
    public CommonResult<Boolean> ignore(@RequestParam("id") Long id) {
        pricingService.ignore(id);
        return success(true);
    }

    @PostMapping("/backtest")
    @Operation(summary = "定价回测：原价 vs 建议价的 RevPAR")
    @PreAuthorize("@ss.hasPermission('hotel:pricing:query')")
    public CommonResult<PriceBacktestRespVO> backtest(@Valid @RequestBody PriceBacktestReqVO reqVO) {
        return success(pricingService.runBacktest(reqVO));
    }

    @GetMapping("/backtest/page")
    @Operation(summary = "定价回测分页")
    @PreAuthorize("@ss.hasPermission('hotel:pricing:query')")
    public CommonResult<PageResult<PriceBacktestRespVO>> backtestPage(@Valid PriceBacktestPageReqVO reqVO) {
        return success(pricingService.getBacktestPage(reqVO));
    }
}
