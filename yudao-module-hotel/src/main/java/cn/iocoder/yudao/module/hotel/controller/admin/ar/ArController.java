package cn.iocoder.yudao.module.hotel.controller.admin.ar;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.ar.vo.*;
import cn.iocoder.yudao.module.hotel.service.ar.ArServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 协议单位 AR")
@RestController
@RequestMapping("/hotel/ar")
@Validated
public class ArController {

    @Resource
    private ArServiceImpl arService;

    @PostMapping("/account/create")
    @Operation(summary = "创建协议单位")
    @PreAuthorize("@ss.hasPermission('hotel:ar:create')")
    public CommonResult<Long> createAccount(@Valid @RequestBody ArAccountSaveReqVO reqVO) {
        return success(arService.createArAccount(reqVO));
    }

    @PutMapping("/account/update")
    @Operation(summary = "修改协议单位")
    @PreAuthorize("@ss.hasPermission('hotel:ar:update')")
    public CommonResult<Boolean> updateAccount(@Valid @RequestBody ArAccountSaveReqVO reqVO) {
        arService.updateArAccount(reqVO);
        return success(true);
    }

    @GetMapping("/account/page")
    @Operation(summary = "协议单位分页")
    @PreAuthorize("@ss.hasPermission('hotel:ar:query')")
    public CommonResult<PageResult<ArAccountRespVO>> getAccountPage(@Valid ArAccountPageReqVO reqVO) {
        return success(arService.getArAccountPage(reqVO));
    }

    @GetMapping("/account/list")
    @Operation(summary = "协议单位列表")
    @PreAuthorize("@ss.hasPermission('hotel:ar:query')")
    public CommonResult<List<ArAccountRespVO>> getAccountList() {
        return success(arService.getArAccountList());
    }

    @PostMapping("/post")
    @Operation(summary = "挂账")
    @PreAuthorize("@ss.hasPermission('hotel:ar:create')")
    public CommonResult<Long> post(@Valid @RequestBody ArPostReqVO reqVO) {
        return success(arService.postToAr(reqVO));
    }

    @PostMapping("/settlement/generate")
    @Operation(summary = "生成结算单")
    @PreAuthorize("@ss.hasPermission('hotel:ar:create')")
    public CommonResult<Long> generateSettlement(@Valid @RequestBody ArSettlementGenerateReqVO reqVO) {
        return success(arService.generateSettlement(reqVO));
    }

    @PostMapping("/settlement/receive")
    @Operation(summary = "结算收款")
    @PreAuthorize("@ss.hasPermission('hotel:ar:update')")
    public CommonResult<Boolean> receiveSettlement(@Valid @RequestBody ArSettlementReceiveReqVO reqVO) {
        arService.receiveSettlement(reqVO);
        return success(true);
    }

    @GetMapping("/ledger/page")
    @Operation(summary = "挂账明细分页")
    @PreAuthorize("@ss.hasPermission('hotel:ar:query')")
    public CommonResult<PageResult<ArLedgerRespVO>> getLedgerPage(@Valid ArLedgerPageReqVO reqVO) {
        return success(arService.getArLedgerPage(reqVO));
    }

    @GetMapping("/settlement/page")
    @Operation(summary = "结算单分页")
    @PreAuthorize("@ss.hasPermission('hotel:ar:query')")
    public CommonResult<PageResult<ArSettlementRespVO>> getSettlementPage(
            @Valid ArSettlementPageReqVO reqVO) {
        return success(arService.getArSettlementPage(reqVO));
    }

}
