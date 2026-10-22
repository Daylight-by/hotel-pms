package cn.iocoder.yudao.module.hotel.controller.admin.lock;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.lock.vo.LockPageReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.lock.LockCardLogDO;
import cn.iocoder.yudao.module.hotel.service.lock.LockServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 门锁发卡")
@RestController
@RequestMapping("/hotel/lock")
@Validated
public class LockController {

    @Resource
    private LockServiceImpl lockService;

    @PostMapping("/issue")
    @Operation(summary = "发卡")
    @PreAuthorize("@ss.hasPermission('hotel:lock:create')")
    public CommonResult<Long> issue(@RequestParam("orderId") Long orderId) {
        return success(lockService.issueByOrderId(orderId));
    }

    @PostMapping("/reissue")
    @Operation(summary = "补卡")
    @PreAuthorize("@ss.hasPermission('hotel:lock:create')")
    public CommonResult<Long> reissue(@RequestParam("cardLogId") Long cardLogId) {
        return success(lockService.reissue(cardLogId));
    }

    @PostMapping("/cancel")
    @Operation(summary = "销卡")
    @PreAuthorize("@ss.hasPermission('hotel:lock:update')")
    public CommonResult<Long> cancel(@RequestParam("orderId") Long orderId) {
        return success(lockService.cancelByOrderId(orderId));
    }

    @PostMapping("/extend")
    @Operation(summary = "续卡")
    @PreAuthorize("@ss.hasPermission('hotel:lock:update')")
    public CommonResult<Long> extend(@RequestParam("orderId") Long orderId,
                                     @RequestParam(value = "newCheckOutDate", required = false)
                                     @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate newCheckOutDate) {
        return success(lockService.extendByOrderId(orderId, newCheckOutDate));
    }

    @GetMapping("/page")
    @Operation(summary = "发卡日志分页")
    @PreAuthorize("@ss.hasPermission('hotel:lock:query')")
    public CommonResult<PageResult<LockCardLogDO>> getPage(@Valid LockPageReqVO reqVO) {
        return success(lockService.getPage(reqVO));
    }

}
