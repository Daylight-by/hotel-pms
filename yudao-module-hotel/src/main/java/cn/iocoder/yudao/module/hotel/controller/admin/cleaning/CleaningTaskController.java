package cn.iocoder.yudao.module.hotel.controller.admin.cleaning;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.cleaning.vo.CleaningTaskPageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.cleaning.vo.CleaningTaskRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.cleaning.vo.CleaningTaskSaveReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.cleaning.vo.CleaningTaskStatusReqVO;
import cn.iocoder.yudao.module.hotel.service.cleaning.CleaningTaskServiceImpl;
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

@Tag(name = "管理后台 - 清洁任务")
@RestController
@RequestMapping("/hotel/cleaning-task")
@Validated
public class CleaningTaskController {

    @Resource
    private CleaningTaskServiceImpl cleaningTaskService;

    @PostMapping("/create")
    @Operation(summary = "创建清洁任务")
    @PreAuthorize("@ss.hasPermission('hotel:cleaning:create')")
    public CommonResult<Long> createTask(@Valid @RequestBody CleaningTaskSaveReqVO createReqVO) {
        return success(cleaningTaskService.createTask(createReqVO));
    }

    @PutMapping("/update-status")
    @Operation(summary = "流转清洁任务状态")
    @Parameter(name = "id", description = "任务编号", required = true)
    @PreAuthorize("@ss.hasPermission('hotel:cleaning:update')")
    public CommonResult<Boolean> updateStatus(@RequestParam("id") Long id,
                                               @Valid @RequestBody CleaningTaskStatusReqVO reqVO) {
        cleaningTaskService.updateStatus(id, reqVO);
        return success(true);
    }

    @PutMapping("/assign")
    @Operation(summary = "指派保洁人员")
    @Parameter(name = "id", description = "任务编号", required = true)
    @PreAuthorize("@ss.hasPermission('hotel:cleaning:update')")
    public CommonResult<Boolean> assign(@RequestParam("id") Long id,
                                        @RequestParam(value = "assigneeId", required = false) Long assigneeId) {
        cleaningTaskService.assign(id, assigneeId);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除清洁任务")
    @Parameter(name = "id", description = "任务编号", required = true)
    @PreAuthorize("@ss.hasPermission('hotel:cleaning:delete')")
    public CommonResult<Boolean> deleteTask(@RequestParam("id") Long id) {
        cleaningTaskService.deleteTask(id);
        return success(true);
    }

    @GetMapping("/page")
    @Operation(summary = "获得清洁任务分页")
    @PreAuthorize("@ss.hasPermission('hotel:cleaning:query')")
    public CommonResult<PageResult<CleaningTaskRespVO>> getTaskPage(@Valid CleaningTaskPageReqVO pageReqVO) {
        return success(cleaningTaskService.getTaskPage(pageReqVO));
    }

    @GetMapping("/list-open")
    @Operation(summary = "待处理清洁任务列表")
    @PreAuthorize("@ss.hasPermission('hotel:cleaning:query')")
    public CommonResult<List<CleaningTaskRespVO>> getOpenTasks() {
        return success(cleaningTaskService.getOpenTasks());
    }
}
