package cn.iocoder.yudao.module.hotel.controller.admin.roomtype;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hotel.controller.admin.roomtype.vo.RoomTypePageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.roomtype.vo.RoomTypeRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.roomtype.vo.RoomTypeSaveReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.roomtype.RoomTypeDO;
import cn.iocoder.yudao.module.hotel.service.roomtype.RoomTypeServiceImpl;
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

@Tag(name = "管理后台 - 酒店房型")
@RestController
@RequestMapping("/hotel/room-type")
@Validated
public class RoomTypeController {

    @Resource
    private RoomTypeServiceImpl roomTypeService;

    @PostMapping("/create")
    @Operation(summary = "创建房型")
    @PreAuthorize("@ss.hasPermission('hotel:room-type:create')")
    public CommonResult<Long> createRoomType(@Valid @RequestBody RoomTypeSaveReqVO createReqVO) {
        return success(roomTypeService.createRoomType(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改房型")
    @PreAuthorize("@ss.hasPermission('hotel:room-type:update')")
    public CommonResult<Boolean> updateRoomType(@Valid @RequestBody RoomTypeSaveReqVO updateReqVO) {
        roomTypeService.updateRoomType(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除房型")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('hotel:room-type:delete')")
    public CommonResult<Boolean> deleteRoomType(@RequestParam("id") Long id) {
        roomTypeService.deleteRoomType(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得房型")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('hotel:room-type:query')")
    public CommonResult<RoomTypeRespVO> getRoomType(@RequestParam("id") Long id) {
        RoomTypeDO roomType = roomTypeService.getRoomType(id);
        return success(BeanUtils.toBean(roomType, RoomTypeRespVO.class));
    }

    @GetMapping("/page")
    @Operation(summary = "获得房型分页")
    @PreAuthorize("@ss.hasPermission('hotel:room-type:query')")
    public CommonResult<PageResult<RoomTypeRespVO>> getRoomTypePage(@Valid RoomTypePageReqVO pageReqVO) {
        PageResult<RoomTypeDO> pageResult = roomTypeService.getRoomTypePage(pageReqVO);
        return success(BeanUtils.toBean(pageResult, RoomTypeRespVO.class));
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "获得房型精简列表")
    public CommonResult<List<RoomTypeRespVO>> getSimpleRoomTypeList() {
        List<RoomTypeDO> list = roomTypeService.getEnabledRoomTypeList();
        return success(BeanUtils.toBean(list, RoomTypeRespVO.class));
    }
}
