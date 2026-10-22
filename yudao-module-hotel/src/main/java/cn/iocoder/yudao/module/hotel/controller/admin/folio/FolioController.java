package cn.iocoder.yudao.module.hotel.controller.admin.folio;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.folio.vo.*;
import cn.iocoder.yudao.module.hotel.dal.dataobject.folio.FolioDO;
import cn.iocoder.yudao.module.hotel.dal.mysql.folio.FolioMapper;
import cn.iocoder.yudao.module.hotel.service.folio.FolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 酒店账务账页")
@RestController
@RequestMapping("/hotel/folio")
@Validated
public class FolioController {

    @Resource
    private FolioService folioService;
    @Resource
    private FolioMapper folioMapper;

    @GetMapping("/get")
    @Operation(summary = "获得账页（含明细）")
    @Parameter(name = "id", description = "账页编号", required = true)
    @PreAuthorize("@ss.hasPermission('hotel:folio:query')")
    public CommonResult<FolioRespVO> get(@RequestParam("id") Long id) {
        return success(folioService.getFolio(id));
    }

    @GetMapping("/get-by-order")
    @Operation(summary = "按订单获得主账页（含明细）")
    @Parameter(name = "orderId", description = "订单编号", required = true)
    @PreAuthorize("@ss.hasPermission('hotel:folio:query')")
    public CommonResult<FolioRespVO> getByOrder(@RequestParam("orderId") Long orderId) {
        return success(folioService.getFolioByOrder(orderId));
    }

    @GetMapping("/page")
    @Operation(summary = "获得账页分页")
    @PreAuthorize("@ss.hasPermission('hotel:folio:query')")
    public CommonResult<PageResult<FolioRespVO>> page(@Valid FolioPageReqVO reqVO) {
        PageResult<FolioDO> page = folioMapper.selectPage(reqVO);
        List<FolioRespVO> list = new ArrayList<>();
        page.getList().forEach(folio -> list.add(folioService.getFolio(folio.getId())));
        return success(new PageResult<>(list, page.getTotal()));
    }

    @PostMapping("/create")
    @Operation(summary = "开账页")
    @PreAuthorize("@ss.hasPermission('hotel:folio:create')")
    public CommonResult<Long> create(@Valid @RequestBody FolioCreateReqVO reqVO) {
        return success(folioService.createFolio(reqVO));
    }

    @PostMapping("/post-charge")
    @Operation(summary = "入账消费")
    @PreAuthorize("@ss.hasPermission('hotel:folio:create')")
    public CommonResult<Long> postCharge(@Valid @RequestBody FolioPostReqVO reqVO) {
        return success(folioService.postCharge(reqVO));
    }

    @PostMapping("/post-payment")
    @Operation(summary = "收款")
    @PreAuthorize("@ss.hasPermission('hotel:folio:create')")
    public CommonResult<Long> postPayment(@Valid @RequestBody FolioPaymentReqVO reqVO) {
        return success(folioService.postPayment(reqVO));
    }

    @PutMapping("/void-item")
    @Operation(summary = "冲销明细")
    @Parameter(name = "id", description = "明细编号", required = true)
    @PreAuthorize("@ss.hasPermission('hotel:folio:update')")
    public CommonResult<Long> voidItem(@RequestParam(value = "id", required = false) Long id,
                                       @RequestBody(required = false) FolioVoidReqVO reqVO) {
        Long itemId = id != null ? id : (reqVO == null ? null : reqVO.getItemId());
        String reason = reqVO == null ? null : reqVO.getReason();
        return success(folioService.voidItem(itemId, reason));
    }

    @PutMapping("/transfer-item")
    @Operation(summary = "转账（把明细转到其它账页）")
    @PreAuthorize("@ss.hasPermission('hotel:folio:update')")
    public CommonResult<Long> transferItem(@Valid @RequestBody FolioTransferReqVO reqVO) {
        return success(folioService.transferItem(reqVO.getItemId(), reqVO.getTargetFolioId()));
    }

    @PostMapping("/split")
    @Operation(summary = "拆账页")
    @PreAuthorize("@ss.hasPermission('hotel:folio:create')")
    public CommonResult<Long> split(@Valid @RequestBody FolioSplitReqVO reqVO) {
        return success(folioService.splitFolio(reqVO.getFolioId(), reqVO.getItemIds(), reqVO.getRemark()));
    }

    @PutMapping("/settle")
    @Operation(summary = "结账")
    @Parameter(name = "id", description = "账页编号", required = true)
    @PreAuthorize("@ss.hasPermission('hotel:folio:update')")
    public CommonResult<Long> settle(@RequestParam("id") Long id) {
        return success(folioService.settleFolio(id));
    }

    @GetMapping("/item-page")
    @Operation(summary = "获得账务明细分页")
    @PreAuthorize("@ss.hasPermission('hotel:folio:query')")
    public CommonResult<PageResult<FolioItemRespVO>> itemPage(@Valid FolioItemPageReqVO reqVO) {
        return success(folioService.getFolioItemPage(reqVO));
    }
}
