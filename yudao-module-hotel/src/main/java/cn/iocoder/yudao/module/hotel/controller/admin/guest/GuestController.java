package cn.iocoder.yudao.module.hotel.controller.admin.guest;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hotel.controller.admin.guest.vo.GuestPageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.guest.vo.GuestRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.guest.vo.GuestSaveReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.guest.GuestDO;
import cn.iocoder.yudao.module.hotel.service.guest.GuestServiceImpl;
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

@Tag(name = "管理后台 - 酒店住客")
@RestController
@RequestMapping("/hotel/guest")
@Validated
public class GuestController {

    @Resource
    private GuestServiceImpl guestService;

    @PostMapping("/create")
    @Operation(summary = "创建住客")
    @PreAuthorize("@ss.hasPermission('hotel:guest:create')")
    public CommonResult<Long> createGuest(@Valid @RequestBody GuestSaveReqVO createReqVO) {
        return success(guestService.createGuest(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改住客")
    @PreAuthorize("@ss.hasPermission('hotel:guest:update')")
    public CommonResult<Boolean> updateGuest(@Valid @RequestBody GuestSaveReqVO updateReqVO) {
        guestService.updateGuest(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除住客")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('hotel:guest:delete')")
    public CommonResult<Boolean> deleteGuest(@RequestParam("id") Long id) {
        guestService.deleteGuest(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得住客")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('hotel:guest:query')")
    public CommonResult<GuestRespVO> getGuest(@RequestParam("id") Long id) {
        GuestRespVO vo = BeanUtils.toBean(guestService.getGuest(id), GuestRespVO.class);
        decorate(vo);
        return success(vo);
    }

    @GetMapping("/page")
    @Operation(summary = "获得住客分页")
    @PreAuthorize("@ss.hasPermission('hotel:guest:query')")
    public CommonResult<PageResult<GuestRespVO>> getGuestPage(@Valid GuestPageReqVO pageReqVO) {
        PageResult<GuestDO> page = guestService.getGuestPage(pageReqVO);
        PageResult<GuestRespVO> result = BeanUtils.toBean(page, GuestRespVO.class);
        result.getList().forEach(this::decorate);
        return success(result);
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "获得住客精简列表")
    public CommonResult<List<GuestRespVO>> getSimpleGuestList() {
        List<GuestRespVO> list = BeanUtils.toBean(guestService.getGuestList(), GuestRespVO.class);
        list.forEach(this::decorate);
        return success(list);
    }

    private void decorate(GuestRespVO vo) {
        if (vo == null) {
            return;
        }
        vo.setGuestLevelName(java.util.Arrays.stream(cn.iocoder.yudao.module.hotel.enums.GuestLevelEnum.values())
                .filter(e -> java.util.Objects.equals(e.getLevel(), vo.getGuestLevel()))
                .map(cn.iocoder.yudao.module.hotel.enums.GuestLevelEnum::getName)
                .findFirst().orElse("散客"));
    }
}
