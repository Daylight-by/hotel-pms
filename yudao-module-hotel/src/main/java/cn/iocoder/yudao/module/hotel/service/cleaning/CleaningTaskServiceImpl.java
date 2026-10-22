package cn.iocoder.yudao.module.hotel.service.cleaning;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.cleaning.vo.CleaningTaskPageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.cleaning.vo.CleaningTaskRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.cleaning.vo.CleaningTaskSaveReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.cleaning.vo.CleaningTaskStatusReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.cleaning.CleaningTaskDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.room.RoomDO;
import cn.iocoder.yudao.module.hotel.dal.mysql.cleaning.CleaningTaskMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.room.RoomMapper;
import cn.iocoder.yudao.module.hotel.enums.CleaningTaskStatusEnum;
import cn.iocoder.yudao.module.hotel.enums.CleaningTypeEnum;
import cn.iocoder.yudao.module.hotel.enums.RoomStatusEnum;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.dal.mysql.user.AdminUserMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hotel.enums.ErrorCodeConstants.*;

/**
 * 清洁任务 Service：清洁任务与房态联动
 */
@Service
@Validated
@Slf4j
public class CleaningTaskServiceImpl {

    @Resource
    private CleaningTaskMapper cleaningTaskMapper;
    @Resource
    private RoomMapper roomMapper;
    @Resource
    private AdminUserMapper adminUserMapper;

    public Long createTask(CleaningTaskSaveReqVO reqVO) {
        RoomDO room = roomMapper.selectById(reqVO.getRoomId());
        if (room == null) {
            throw exception(ROOM_NOT_EXISTS);
        }
        CleaningTaskDO task = new CleaningTaskDO();
        task.setRoomId(reqVO.getRoomId());
        task.setType(reqVO.getType());
        task.setStatus(CleaningTaskStatusEnum.PENDING.getStatus());
        task.setAssigneeId(reqVO.getAssigneeId());
        task.setPlanDate(reqVO.getPlanDate());
        task.setRemark(reqVO.getRemark());
        cleaningTaskMapper.insert(task);

        if (Objects.equals(room.getStatus(), RoomStatusEnum.AVAILABLE.getStatus())) {
            RoomDO update = new RoomDO();
            update.setId(room.getId());
            update.setStatus(RoomStatusEnum.CLEANING.getStatus());
            roomMapper.updateById(update);
        }
        return task.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, CleaningTaskStatusReqVO reqVO) {
        CleaningTaskDO task = validateExists(id);
        Integer target = reqVO.getStatus();
        Integer current = task.getStatus();
        if (Objects.equals(current, target)) {
            return;
        }
        validateTransition(current, target);

        CleaningTaskDO update = new CleaningTaskDO();
        update.setId(id);
        update.setStatus(target);
        if (reqVO.getRemark() != null) {
            update.setRemark(reqVO.getRemark());
        }
        RoomDO roomUpdate = new RoomDO();
        roomUpdate.setId(task.getRoomId());

        if (Objects.equals(target, CleaningTaskStatusEnum.PROCESSING.getStatus())) {
            roomUpdate.setStatus(RoomStatusEnum.CLEANING.getStatus());
        } else if (Objects.equals(target, CleaningTaskStatusEnum.FINISHED.getStatus())) {
            update.setFinishTime(LocalDateTime.now());
        } else if (Objects.equals(target, CleaningTaskStatusEnum.VERIFIED.getStatus())) {
            update.setFinishTime(task.getFinishTime() == null ? LocalDateTime.now() : task.getFinishTime());
            roomUpdate.setStatus(RoomStatusEnum.AVAILABLE.getStatus());
        } else if (Objects.equals(target, CleaningTaskStatusEnum.PENDING.getStatus())) {
            roomUpdate.setStatus(RoomStatusEnum.CLEANING.getStatus());
        }
        cleaningTaskMapper.updateById(update);
        if (roomUpdate.getStatus() != null) {
            roomMapper.updateById(roomUpdate);
        }
    }

    public void assign(Long id, Long assigneeId) {
        validateExists(id);
        CleaningTaskDO update = new CleaningTaskDO();
        update.setId(id);
        update.setAssigneeId(assigneeId);
        cleaningTaskMapper.updateById(update);
    }

    public void deleteTask(Long id) {
        CleaningTaskDO task = validateExists(id);
        if (!Objects.equals(task.getStatus(), CleaningTaskStatusEnum.PENDING.getStatus())) {
            throw exception(CLEANING_DELETE_NOT_ALLOW);
        }
        cleaningTaskMapper.deleteById(id);
    }

    public PageResult<CleaningTaskRespVO> getTaskPage(CleaningTaskPageReqVO reqVO) {
        PageResult<CleaningTaskDO> page = cleaningTaskMapper.selectPage(reqVO);
        return new PageResult<>(buildVOList(page.getList()), page.getTotal());
    }

    public List<CleaningTaskRespVO> getOpenTasks() {
        return buildVOList(cleaningTaskMapper.selectListByStatusIn(List.of(
                CleaningTaskStatusEnum.PENDING.getStatus(), CleaningTaskStatusEnum.PROCESSING.getStatus())));
    }

    public Long countOpenTasks() {
        return cleaningTaskMapper.selectCountByStatusIn(List.of(
                CleaningTaskStatusEnum.PENDING.getStatus(), CleaningTaskStatusEnum.PROCESSING.getStatus()));
    }

    private List<CleaningTaskRespVO> buildVOList(List<CleaningTaskDO> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> roomIds = tasks.stream().map(CleaningTaskDO::getRoomId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, RoomDO> roomMap = roomIds.isEmpty() ? Collections.emptyMap()
                : roomMapper.selectBatchIds(roomIds).stream()
                .collect(Collectors.toMap(RoomDO::getId, Function.identity(), (a, b) -> a));
        Set<Long> userIds = tasks.stream().map(CleaningTaskDO::getAssigneeId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, AdminUserDO> userMap = userIds.isEmpty() ? Collections.emptyMap()
                : adminUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(AdminUserDO::getId, Function.identity(), (a, b) -> a));

        return tasks.stream().map(task -> {
            CleaningTaskRespVO vo = new CleaningTaskRespVO();
            org.springframework.beans.BeanUtils.copyProperties(task, vo);
            RoomDO room = roomMap.get(task.getRoomId());
            if (room != null) {
                vo.setRoomNo(room.getRoomNo());
                vo.setFloor(room.getFloor());
            }
            AdminUserDO user = userMap.get(task.getAssigneeId());
            if (user != null) {
                vo.setAssigneeName(user.getNickname());
            }
            vo.setTypeName(Arrays.stream(CleaningTypeEnum.values())
                    .filter(e -> Objects.equals(e.getType(), task.getType()))
                    .map(CleaningTypeEnum::getName).findFirst().orElse(""));
            vo.setStatusName(Arrays.stream(CleaningTaskStatusEnum.values())
                    .filter(e -> Objects.equals(e.getStatus(), task.getStatus()))
                    .map(CleaningTaskStatusEnum::getName).findFirst().orElse(""));
            return vo;
        }).collect(Collectors.toList());
    }

    private CleaningTaskDO validateExists(Long id) {
        CleaningTaskDO task = id == null ? null : cleaningTaskMapper.selectById(id);
        if (task == null) {
            throw exception(CLEANING_TASK_NOT_EXISTS);
        }
        return task;
    }

    private void validateTransition(Integer current, Integer target) {
        boolean allowed;
        if (Objects.equals(target, CleaningTaskStatusEnum.PROCESSING.getStatus())) {
            allowed = Objects.equals(current, CleaningTaskStatusEnum.PENDING.getStatus());
        } else if (Objects.equals(target, CleaningTaskStatusEnum.FINISHED.getStatus())) {
            allowed = Objects.equals(current, CleaningTaskStatusEnum.PROCESSING.getStatus())
                    || Objects.equals(current, CleaningTaskStatusEnum.PENDING.getStatus());
        } else if (Objects.equals(target, CleaningTaskStatusEnum.VERIFIED.getStatus())) {
            allowed = Objects.equals(current, CleaningTaskStatusEnum.FINISHED.getStatus());
        } else if (Objects.equals(target, CleaningTaskStatusEnum.PENDING.getStatus())) {
            allowed = true;
        } else {
            allowed = false;
        }
        if (!allowed) {
            throw exception(CLEANING_STATUS_NOT_ALLOW);
        }
    }
}
