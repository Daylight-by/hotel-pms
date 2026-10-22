package cn.iocoder.yudao.module.hotel.service.room;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hotel.controller.admin.room.vo.RoomPageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.room.vo.RoomRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.room.vo.RoomSaveReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.room.RoomDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.roomtype.RoomTypeDO;
import cn.iocoder.yudao.module.hotel.dal.mysql.cleaning.CleaningTaskMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.guest.GuestMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.order.OrderMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.room.RoomMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.roomtype.RoomTypeMapper;
import cn.iocoder.yudao.module.hotel.enums.CleaningTaskStatusEnum;
import cn.iocoder.yudao.module.hotel.enums.OrderStatusEnum;
import cn.iocoder.yudao.module.hotel.enums.RoomStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hotel.enums.ErrorCodeConstants.*;

/**
 * 房间 Service 实现
 */
@Service
@Validated
@Slf4j
public class RoomServiceImpl {

    @Resource
    private RoomMapper roomMapper;
    @Resource
    private RoomTypeMapper roomTypeMapper;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private GuestMapper guestMapper;
    @Resource
    private CleaningTaskMapper cleaningTaskMapper;

    public Long createRoom(RoomSaveReqVO reqVO) {
        validateRoomType(reqVO.getRoomTypeId());
        validateRoomNoUnique(null, reqVO.getRoomNo());
        RoomDO room = BeanUtils.toBean(reqVO, RoomDO.class);
        if (room.getStatus() == null) {
            room.setStatus(RoomStatusEnum.AVAILABLE.getStatus());
        }
        if (room.getFloor() == null) {
            room.setFloor(parseFloor(room.getRoomNo()));
        }
        roomMapper.insert(room);
        return room.getId();
    }

    public void updateRoom(RoomSaveReqVO reqVO) {
        validateExists(reqVO.getId());
        validateRoomType(reqVO.getRoomTypeId());
        validateRoomNoUnique(reqVO.getId(), reqVO.getRoomNo());
        roomMapper.updateById(BeanUtils.toBean(reqVO, RoomDO.class));
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteRoom(Long id) {
        validateExists(id);
        Long activeOrders = orderMapper.selectCountByRoomIdAndStatusIn(id,
                List.of(OrderStatusEnum.PENDING.getStatus(), OrderStatusEnum.CONFIRMED.getStatus(),
                        OrderStatusEnum.CHECKED_IN.getStatus()));
        if (activeOrders != null && activeOrders > 0) {
            throw exception(ROOM_HAS_ACTIVE_ORDER);
        }
        roomMapper.deleteById(id);
    }

    public void updateRoomStatus(Long id, Integer status) {
        validateExists(id);
        if (Objects.equals(status, RoomStatusEnum.AVAILABLE.getStatus())) {
            Long open = cleaningTaskMapper.selectCountByRoomIdAndStatusIn(id,
                    List.of(CleaningTaskStatusEnum.PENDING.getStatus(), CleaningTaskStatusEnum.PROCESSING.getStatus()));
            if (open != null && open > 0) {
                throw exception(ROOM_HAS_OPEN_CLEANING);
            }
        }
        RoomDO update = new RoomDO();
        update.setId(id);
        update.setStatus(status);
        roomMapper.updateById(update);
    }

    public RoomDO getRoom(Long id) {
        return roomMapper.selectById(id);
    }

    public RoomRespVO getRoomVO(Long id) {
        RoomDO room = roomMapper.selectById(id);
        if (room == null) {
            return null;
        }
        return buildVOList(List.of(room)).get(0);
    }

    public PageResult<RoomDO> getRoomPage(RoomPageReqVO reqVO) {
        return roomMapper.selectPage(reqVO);
    }

    public PageResult<RoomRespVO> getRoomVOPage(RoomPageReqVO reqVO) {
        PageResult<RoomDO> page = roomMapper.selectPage(reqVO);
        List<RoomRespVO> list = buildVOList(page.getList());
        return new PageResult<>(list, page.getTotal());
    }

    public List<RoomRespVO> getRoomList() {
        return buildVOList(roomMapper.selectListAll());
    }

    public List<RoomDO> getAvailableRooms() {
        return roomMapper.selectListByStatus(RoomStatusEnum.AVAILABLE.getStatus());
    }

    /**
     * 房态看板：按状态分组 + 汇总
     */
    public Map<String, Object> getBoard() {
        List<RoomRespVO> rooms = buildVOList(roomMapper.selectListAll());
        Map<Integer, Long> counts = rooms.stream()
                .collect(Collectors.groupingBy(r -> r.getStatus() == null ? 0 : r.getStatus(), Collectors.counting()));
        Map<String, Object> board = new LinkedHashMap<>();
        board.put("total", rooms.size());
        board.put("rooms", rooms);
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        statusCounts.put("available", counts.getOrDefault(RoomStatusEnum.AVAILABLE.getStatus(), 0L));
        statusCounts.put("occupied", counts.getOrDefault(RoomStatusEnum.OCCUPIED.getStatus(), 0L));
        statusCounts.put("cleaning", counts.getOrDefault(RoomStatusEnum.CLEANING.getStatus(), 0L));
        statusCounts.put("maintenance", counts.getOrDefault(RoomStatusEnum.MAINTENANCE.getStatus(), 0L));
        board.put("counts", statusCounts);
        return board;
    }

    private List<RoomRespVO> buildVOList(List<RoomDO> rooms) {
        if (rooms == null || rooms.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> typeIds = rooms.stream().map(RoomDO::getRoomTypeId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, RoomTypeDO> typeMap = typeIds.isEmpty() ? Collections.emptyMap()
                : roomTypeMapper.selectBatchIds(typeIds).stream()
                .collect(Collectors.toMap(RoomTypeDO::getId, Function.identity(), (a, b) -> a));

        Map<Long, String> guestMap = new HashMap<>();
        Set<Long> roomIds = rooms.stream().map(RoomDO::getId).collect(Collectors.toSet());
        List<cn.iocoder.yudao.module.hotel.dal.dataobject.order.OrderDO> checkedIn =
                orderMapper.selectListByRoomIdsAndStatus(roomIds, OrderStatusEnum.CHECKED_IN.getStatus());
        if (!checkedIn.isEmpty()) {
            Set<Long> guestIds = checkedIn.stream()
                    .map(cn.iocoder.yudao.module.hotel.dal.dataobject.order.OrderDO::getGuestId)
                    .filter(Objects::nonNull).collect(Collectors.toSet());
            Map<Long, String> guestNames = guestIds.isEmpty() ? Collections.emptyMap()
                    : guestMapper.selectBatchIds(guestIds).stream()
                    .collect(Collectors.toMap(cn.iocoder.yudao.module.hotel.dal.dataobject.guest.GuestDO::getId,
                            cn.iocoder.yudao.module.hotel.dal.dataobject.guest.GuestDO::getName, (a, b) -> a));
            checkedIn.forEach(o -> guestMap.put(o.getRoomId(), guestNames.get(o.getGuestId())));
        }

        return rooms.stream().map(room -> {
            RoomRespVO vo = BeanUtils.toBean(room, RoomRespVO.class);
            RoomTypeDO type = typeMap.get(room.getRoomTypeId());
            vo.setRoomTypeName(type == null ? null : type.getName());
            vo.setStatusName(statusName(room.getStatus()));
            vo.setLockStatusName(lockStatusName(room.getLockStatus()));
            vo.setCurrentGuest(guestMap.get(room.getId()));
            return vo;
        }).collect(Collectors.toList());
    }

    private String statusName(Integer status) {
        return Arrays.stream(RoomStatusEnum.values())
                .filter(e -> Objects.equals(e.getStatus(), status))
                .map(RoomStatusEnum::getName)
                .findFirst().orElse("");
    }

    private String lockStatusName(Integer lockStatus) {
        if (lockStatus == null) {
            return cn.iocoder.yudao.module.hotel.enums.RoomLockStatusEnum.NORMAL.getName();
        }
        return Arrays.stream(cn.iocoder.yudao.module.hotel.enums.RoomLockStatusEnum.values())
                .filter(e -> Objects.equals(e.getStatus(), lockStatus))
                .map(cn.iocoder.yudao.module.hotel.enums.RoomLockStatusEnum::getName)
                .findFirst().orElse("");
    }

    /**
     * 锁定 / 解锁房间（维修锁定、预留锁定）
     */
    public void updateLockStatus(Long id, Integer lockStatus) {
        validateExists(id);
        RoomDO update = new RoomDO();
        update.setId(id);
        update.setLockStatus(lockStatus == null ? 0 : lockStatus);
        roomMapper.updateById(update);
    }

    private void validateExists(Long id) {
        if (id == null || roomMapper.selectById(id) == null) {
            throw exception(ROOM_NOT_EXISTS);
        }
    }

    private void validateRoomType(Long roomTypeId) {
        if (roomTypeId == null || roomTypeMapper.selectById(roomTypeId) == null) {
            throw exception(ROOM_TYPE_NOT_EXISTS);
        }
    }

    private void validateRoomNoUnique(Long id, String roomNo) {
        if (roomNo == null) {
            return;
        }
        RoomDO exists = roomMapper.selectByRoomNo(roomNo.trim());
        if (exists != null && !Objects.equals(exists.getId(), id)) {
            throw exception(ROOM_NO_DUPLICATE);
        }
    }

    private Integer parseFloor(String roomNo) {
        String digits = roomNo == null ? "" : roomNo.replaceAll("\\D", "");
        if (digits.length() >= 3) {
            return Integer.parseInt(digits.substring(0, 1));
        }
        return 0;
    }
}
