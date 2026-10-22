package cn.iocoder.yudao.module.hotel.controller.admin.operationlog;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.operationlog.vo.OperationLogPageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.operationlog.vo.OperationLogRespVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.operationlog.OperationLogDO;
import cn.iocoder.yudao.module.hotel.service.operationlog.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 酒店操作审计")
@RestController
@RequestMapping("/hotel/operation-log")
@Validated
public class OperationLogController {

    @Resource
    private OperationLogService operationLogService;

    @GetMapping("/page")
    @Operation(summary = "操作审计日志分页")
    @PreAuthorize("@ss.hasPermission('hotel:log:query')")
    public CommonResult<PageResult<OperationLogRespVO>> page(@Valid OperationLogPageReqVO pageReqVO) {
        PageResult<OperationLogDO> pageResult = operationLogService.getOperationLogPage(pageReqVO);
        return success(new PageResult<>(pageResult.getList().stream()
                .map(operationLogService::toRespVO).toList(), pageResult.getTotal()));
    }
}
