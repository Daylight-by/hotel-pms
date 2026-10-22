package cn.iocoder.yudao.module.hotel.controller.admin.invoiceApply;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.invoiceApply.vo.InvoiceApplyHandleReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.invoiceApply.vo.InvoiceApplyPageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.invoiceApply.vo.InvoiceApplyRespVO;
import cn.iocoder.yudao.module.hotel.service.invoiceApply.InvoiceApplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 酒店电子发票申请")
@RestController
@RequestMapping("/hotel/invoice-apply")
@Validated
public class InvoiceApplyController {

    @Resource
    private InvoiceApplyService invoiceApplyService;

    @GetMapping("/page")
    @Operation(summary = "发票申请分页")
    @PreAuthorize("@ss.hasPermission('hotel:invoice-apply:query')")
    public CommonResult<PageResult<InvoiceApplyRespVO>> getPage(@Valid InvoiceApplyPageReqVO reqVO) {
        return success(invoiceApplyService.getPage(reqVO));
    }

    @PutMapping("/handle")
    @Operation(summary = "处理发票申请（20 开具 / 30 驳回）")
    @PreAuthorize("@ss.hasPermission('hotel:invoice-apply:update')")
    public CommonResult<Boolean> handle(@RequestParam(value = "id", required = false) Long id,
                                        @RequestBody(required = false) InvoiceApplyHandleReqVO reqVO) {
        Long applyId = id != null ? id : (reqVO == null ? null : reqVO.getId());
        Integer status = reqVO == null ? null : reqVO.getStatus();
        invoiceApplyService.handle(applyId, status, reqVO == null ? null : reqVO.getRejectReason());
        return success(true);
    }
}
