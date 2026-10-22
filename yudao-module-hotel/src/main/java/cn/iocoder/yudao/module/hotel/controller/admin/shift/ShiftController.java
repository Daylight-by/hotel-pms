package cn.iocoder.yudao.module.hotel.controller.admin.shift;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.shift.vo.ShiftCloseReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.shift.vo.ShiftHandoverRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.shift.vo.ShiftOpenReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.shift.vo.ShiftPageReqVO;
import cn.iocoder.yudao.module.hotel.service.shift.ShiftService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 酒店交班")
@RestController
@RequestMapping("/hotel/shift")
@Validated
public class ShiftController {

    @Resource
    private ShiftService shiftService;

    @PostMapping("/open")
    @Operation(summary = "开班")
    @PreAuthorize("@ss.hasPermission('hotel:shift:create')")
    public CommonResult<Long> open(@RequestBody(required = false) ShiftOpenReqVO reqVO) {
        return success(shiftService.openShift(reqVO == null ? new ShiftOpenReqVO() : reqVO));
    }

    @PutMapping("/close")
    @Operation(summary = "交班")
    @Parameter(name = "id", description = "班次编号（不传取当前班次）")
    @PreAuthorize("@ss.hasPermission('hotel:shift:update')")
    public CommonResult<Long> close(@RequestParam(value = "id", required = false) Long id,
                                    @RequestBody(required = false) ShiftCloseReqVO reqVO) {
        ShiftCloseReqVO req = reqVO == null ? new ShiftCloseReqVO() : reqVO;
        if (id != null) {
            req.setId(id);
        }
        return success(shiftService.closeShift(req));
    }

    @GetMapping("/current")
    @Operation(summary = "当前班次")
    @PreAuthorize("@ss.hasPermission('hotel:shift:query')")
    public CommonResult<ShiftHandoverRespVO> current() {
        return success(shiftService.getCurrentShift());
    }

    @GetMapping("/page")
    @Operation(summary = "获得交班记录分页")
    @PreAuthorize("@ss.hasPermission('hotel:shift:query')")
    public CommonResult<PageResult<ShiftHandoverRespVO>> page(@Valid ShiftPageReqVO reqVO) {
        return success(shiftService.getShiftPage(reqVO));
    }
}
