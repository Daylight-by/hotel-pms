package cn.iocoder.yudao.module.hotel.controller.admin.invoice;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.invoice.vo.InvoicePageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.invoice.vo.InvoiceRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.invoice.vo.InvoiceSaveReqVO;
import cn.iocoder.yudao.module.hotel.service.invoice.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 酒店发票")
@RestController
@RequestMapping("/hotel/invoice")
@Validated
public class InvoiceController {

    @Resource
    private InvoiceService invoiceService;

    @PostMapping("/issue")
    @Operation(summary = "开票")
    @PreAuthorize("@ss.hasPermission('hotel:invoice:create')")
    public CommonResult<Long> issue(@Valid @RequestBody InvoiceSaveReqVO reqVO) {
        return success(invoiceService.issueInvoice(reqVO));
    }

    @PutMapping("/void")
    @Operation(summary = "红冲发票")
    @Parameter(name = "id", description = "发票编号", required = true)
    @PreAuthorize("@ss.hasPermission('hotel:invoice:update')")
    public CommonResult<Long> voidInvoice(@RequestParam("id") Long id,
                                          @RequestParam(value = "reason", required = false) String reason) {
        return success(invoiceService.voidInvoice(id, reason));
    }

    @GetMapping("/page")
    @Operation(summary = "获得发票分页")
    @PreAuthorize("@ss.hasPermission('hotel:invoice:query')")
    public CommonResult<PageResult<InvoiceRespVO>> page(@Valid InvoicePageReqVO reqVO) {
        return success(invoiceService.getInvoicePage(reqVO));
    }
}
