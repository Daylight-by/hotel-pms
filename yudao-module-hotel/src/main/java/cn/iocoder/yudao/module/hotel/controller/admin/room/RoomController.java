package cn.iocoder.yudao.module.hotel.controller.admin.room;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hotel.controller.admin.room.vo.RoomPageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.room.vo.RoomRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.room.vo.RoomSaveReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.room.RoomDO;
import cn.iocoder.yudao.module.hotel.service.room.RoomServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 酒店房间")
@RestController
@RequestMapping("/hotel/room")
@Validated
public class RoomController {

    @Resource
    private RoomServiceImpl roomService;

    @PostMapping("/create")
    @Operation(summary = "创建房间")
    @PreAuthorize("@ss.hasPermission('hotel:room:create')")
    public CommonResult<Long> createRoom(@Valid @RequestBody RoomSaveReqVO createReqVO) {
        return success(roomService.createRoom(createReqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改房间")
    @PreAuthorize("@ss.hasPermission('hotel:room:update')")
    public CommonResult<Boolean> updateRoom(@Valid @RequestBody RoomSaveReqVO updateReqVO) {
        roomService.updateRoom(updateReqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除房间")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('hotel:room:delete')")
    public CommonResult<Boolean> deleteRoom(@RequestParam("id") Long id) {
        roomService.deleteRoom(id);
        return success(true);
    }

    @PutMapping("/update-status")
    @Operation(summary = "调整房态")
    @PreAuthorize("@ss.hasPermission('hotel:room:update')")
    public CommonResult<Boolean> updateRoomStatus(@RequestParam("id") Long id,
                                                  @RequestParam("status") Integer status) {
        roomService.updateRoomStatus(id, status);
        return success(true);
    }

    @PutMapping("/update-lock-status")
    @Operation(summary = "锁定 / 解锁房间")
    @PreAuthorize("@ss.hasPermission('hotel:room:update')")
    public CommonResult<Boolean> updateLockStatus(@RequestParam("id") Long id,
                                                  @RequestParam("lockStatus") Integer lockStatus) {
        roomService.updateLockStatus(id, lockStatus);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得房间")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('hotel:room:query')")
    public CommonResult<RoomRespVO> getRoom(@RequestParam("id") Long id) {
        return success(roomService.getRoomVO(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得房间分页")
    @PreAuthorize("@ss.hasPermission('hotel:room:query')")
    public CommonResult<PageResult<RoomRespVO>> getRoomPage(@Valid RoomPageReqVO pageReqVO) {
        return success(roomService.getRoomVOPage(pageReqVO));
    }

    @GetMapping("/list-all-simple")
    @Operation(summary = "获得房间精简列表")
    public CommonResult<java.util.List<RoomRespVO>> getSimpleRoomList() {
        return success(roomService.getRoomList());
    }

    @GetMapping("/board")
    @Operation(summary = "房态看板")
    @PreAuthorize("@ss.hasPermission('hotel:room:query')")
    public CommonResult<Map<String, Object>> getBoard() {
        return success(roomService.getBoard());
    }
}
