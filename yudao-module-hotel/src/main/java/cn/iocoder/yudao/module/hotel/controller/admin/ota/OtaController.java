package cn.iocoder.yudao.module.hotel.controller.admin.ota;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.ota.vo.OtaConfigPageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.ota.vo.OtaConfigRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.ota.vo.OtaConfigSaveReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.ota.vo.OtaOrderPageReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.ota.OtaOrderDO;
import cn.iocoder.yudao.module.hotel.service.ota.OtaServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - OTA 直连")
@RestController
@RequestMapping("/hotel/ota")
@Validated
public class OtaController {

    @Resource
    private OtaServiceImpl otaService;

    @GetMapping("/config/page")
    @Operation(summary = "OTA 配置分页")
    @PreAuthorize("@ss.hasPermission('hotel:ota:query')")
    public CommonResult<PageResult<OtaConfigRespVO>> getConfigPage(@Valid OtaConfigPageReqVO reqVO) {
        return success(otaService.getConfigPage(reqVO));
    }

    @PostMapping("/config/create")
    @Operation(summary = "创建 OTA 配置")
    @PreAuthorize("@ss.hasPermission('hotel:ota:create')")
    public CommonResult<Long> createConfig(@Valid @RequestBody OtaConfigSaveReqVO reqVO) {
        return success(otaService.createConfig(reqVO));
    }

    @PutMapping("/config/update")
    @Operation(summary = "修改 OTA 配置")
    @PreAuthorize("@ss.hasPermission('hotel:ota:update')")
    public CommonResult<Boolean> updateConfig(@Valid @RequestBody OtaConfigSaveReqVO reqVO) {
        otaService.updateConfig(reqVO);
        return success(true);
    }

    @PostMapping("/sync-order")
    @Operation(summary = "同步 OTA 订单")
    @PreAuthorize("@ss.hasPermission('hotel:ota:update')")
    public CommonResult<Integer> syncOrder(@RequestParam("configId") Long configId) {
        return success(otaService.syncOrder(configId));
    }

    @PostMapping("/push-rate")
    @Operation(summary = "推送房价库存")
    @PreAuthorize("@ss.hasPermission('hotel:ota:update')")
    public CommonResult<Boolean> pushRate(@RequestParam("configId") Long configId) {
        return success(otaService.pushRate(configId));
    }

    @GetMapping("/order/page")
    @Operation(summary = "OTA 订单映射分页")
    @PreAuthorize("@ss.hasPermission('hotel:ota:query')")
    public CommonResult<PageResult<OtaOrderDO>> getOrderPage(@Valid OtaOrderPageReqVO reqVO) {
        return success(otaService.getOrderPage(reqVO));
    }

}
