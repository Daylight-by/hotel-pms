package cn.iocoder.yudao.module.hotel.controller.admin.psb;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.psb.vo.PsbPageReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.psb.PsbUploadLogDO;
import cn.iocoder.yudao.module.hotel.service.psb.PsbServiceImpl;
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
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 公安上传")
@RestController
@RequestMapping("/hotel/psb")
@Validated
public class PsbController {

    @Resource
    private PsbServiceImpl psbService;

    @GetMapping("/page")
    @Operation(summary = "公安上传日志分页")
    @PreAuthorize("@ss.hasPermission('hotel:psb:query')")
    public CommonResult<PageResult<PsbUploadLogDO>> getPage(@Valid PsbPageReqVO reqVO) {
        return success(psbService.getPage(reqVO));
    }

    @PostMapping("/retry")
    @Operation(summary = "重试上传")
    @Parameter(name = "id", description = "日志编号", required = true)
    @PreAuthorize("@ss.hasPermission('hotel:psb:update')")
    public CommonResult<Boolean> retry(@RequestParam("id") Long id) {
        psbService.retry(id);
        return success(true);
    }

    @GetMapping("/stat")
    @Operation(summary = "公安上传统计")
    @PreAuthorize("@ss.hasPermission('hotel:psb:query')")
    public CommonResult<Map<String, Object>> stat(
            @RequestParam(value = "storeId", required = false) Long storeId,
            @RequestParam(value = "beginDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate beginDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        return success(psbService.getStat(storeId, beginDate, endDate));
    }

}
