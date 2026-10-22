package cn.iocoder.yudao.module.hotel.controller.admin.rateplan;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.rateplan.vo.RatePlanCalcPriceReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.rateplan.vo.RatePlanCalcPriceRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.rateplan.vo.RatePlanPageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.rateplan.vo.RatePlanRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.rateplan.vo.RatePlanSaveReqVO;
import cn.iocoder.yudao.module.hotel.service.rateplan.RatePlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 房价计划")
@RestController
@RequestMapping("/hotel/rate-plan")
@Validated
public class RatePlanController {

    @Resource
    private RatePlanService ratePlanService;

    @PostMapping("/create")
    @Operation(summary = "创建房价计划")
    @PreAuthorize("@ss.hasPermission('hotel:rate-plan:create')")
    public CommonResult<Long> createRatePlan(@Valid @RequestBody RatePlanSaveReqVO reqVO) {
        return success(ratePlanService.createRatePlan(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新房价计划")
    @PreAuthorize("@ss.hasPermission('hotel:rate-plan:update')")
    public CommonResult<Boolean> updateRatePlan(@Valid @RequestBody RatePlanSaveReqVO reqVO) {
        ratePlanService.updateRatePlan(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除房价计划")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('hotel:rate-plan:delete')")
    public CommonResult<Boolean> deleteRatePlan(@RequestParam("id") Long id) {
        ratePlanService.deleteRatePlan(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取房价计划")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('hotel:rate-plan:query')")
    public CommonResult<RatePlanRespVO> getRatePlan(@RequestParam("id") Long id) {
        return success(ratePlanService.getRatePlan(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获取房价计划分页")
    @PreAuthorize("@ss.hasPermission('hotel:rate-plan:query')")
    public CommonResult<PageResult<RatePlanRespVO>> getRatePlanPage(@Valid RatePlanPageReqVO reqVO) {
        return success(ratePlanService.getRatePlanPage(reqVO));
    }

    @PostMapping("/calc-price")
    @Operation(summary = "试算房价计划每日售价")
    @PreAuthorize("@ss.hasPermission('hotel:rate-plan:query')")
    public CommonResult<RatePlanCalcPriceRespVO> calcPrice(@Valid @RequestBody RatePlanCalcPriceReqVO reqVO) {
        return success(ratePlanService.calcPrice(reqVO));
    }
}
