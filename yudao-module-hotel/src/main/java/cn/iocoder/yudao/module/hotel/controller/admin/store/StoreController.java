package cn.iocoder.yudao.module.hotel.controller.admin.store;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.store.vo.StorePageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.store.vo.StoreRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.store.vo.StoreSaveReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.store.StoreDO;
import cn.iocoder.yudao.module.hotel.service.store.StoreServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 酒店门店")
@RestController
@RequestMapping("/hotel/store")
@Validated
public class StoreController {

    @Resource
    private StoreServiceImpl storeService;

    @PostMapping("/create")
    @Operation(summary = "创建门店")
    @PreAuthorize("@ss.hasPermission('hotel:store:create')")
    public CommonResult<Long> createStore(@Valid @RequestBody StoreSaveReqVO reqVO) {
        return success(storeService.createStore(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改门店")
    @PreAuthorize("@ss.hasPermission('hotel:store:update')")
    public CommonResult<Boolean> updateStore(@Valid @RequestBody StoreSaveReqVO reqVO) {
        storeService.updateStore(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除门店")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('hotel:store:delete')")
    public CommonResult<Boolean> deleteStore(@RequestParam("id") Long id) {
        storeService.deleteStore(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得门店")
    @PreAuthorize("@ss.hasPermission('hotel:store:query')")
    public CommonResult<StoreDO> getStore(@RequestParam("id") Long id) {
        return success(storeService.getStore(id));
    }

    @GetMapping("/list")
    @Operation(summary = "获得门店列表")
    @PreAuthorize("@ss.hasPermission('hotel:store:query')")
    public CommonResult<List<StoreRespVO>> getStoreList() {
        return success(storeService.getStoreList());
    }

    @GetMapping("/page")
    @Operation(summary = "获得门店分页")
    @PreAuthorize("@ss.hasPermission('hotel:store:query')")
    public CommonResult<PageResult<StoreRespVO>> getStorePage(@Valid StorePageReqVO reqVO) {
        return success(storeService.getStorePage(reqVO));
    }

}
