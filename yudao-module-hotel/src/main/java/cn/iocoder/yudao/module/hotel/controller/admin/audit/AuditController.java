package cn.iocoder.yudao.module.hotel.controller.admin.audit;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.audit.vo.BusinessDateRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.audit.vo.NightAuditPageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.audit.vo.NightAuditRespVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.audit.BusinessDateDO;
import cn.iocoder.yudao.module.hotel.enums.BusinessDateStatusEnum;
import cn.iocoder.yudao.module.hotel.service.audit.BusinessDateService;
import cn.iocoder.yudao.module.hotel.service.audit.NightAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 酒店夜审与营业日")
@RestController
@RequestMapping("/hotel/audit")
@Validated
public class AuditController {

    @Resource
    private BusinessDateService businessDateService;
    @Resource
    private NightAuditService nightAuditService;

    @GetMapping("/business-date")
    @Operation(summary = "获得当前营业日")
    @PreAuthorize("@ss.hasPermission('hotel:audit:query')")
    public CommonResult<BusinessDateRespVO> getBusinessDate() {
        BusinessDateDO current = businessDateService.getCurrentBusinessDateDO();
        BusinessDateRespVO vo = new BusinessDateRespVO();
        if (current != null) {
            vo.setBizDate(current.getBizDate());
            vo.setStatus(current.getStatus());
            vo.setStatusName(Arrays.stream(BusinessDateStatusEnum.values())
                    .filter(e -> Objects.equals(e.getStatus(), current.getStatus()))
                    .map(BusinessDateStatusEnum::getName).findFirst().orElse(""));
            vo.setAuditTime(current.getAuditTime());
        }
        vo.setToday(LocalDate.now());
        return success(vo);
    }

    @PostMapping("/run")
    @Operation(summary = "执行夜审")
    @PreAuthorize("@ss.hasPermission('hotel:audit:audit')")
    public CommonResult<NightAuditRespVO> run() {
        Long id = nightAuditService.runNightAudit();
        return success(nightAuditService.getNightAudit(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得夜审记录分页")
    @PreAuthorize("@ss.hasPermission('hotel:audit:query')")
    public CommonResult<PageResult<NightAuditRespVO>> page(@Valid NightAuditPageReqVO reqVO) {
        return success(nightAuditService.getNightAuditPage(reqVO));
    }

    @GetMapping("/last")
    @Operation(summary = "最近一次成功的夜审")
    @PreAuthorize("@ss.hasPermission('hotel:audit:query')")
    public CommonResult<NightAuditRespVO> last() {
        return success(nightAuditService.getLastNightAudit());
    }
}
