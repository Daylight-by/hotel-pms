package cn.iocoder.yudao.module.hotel.service.roomtype;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hotel.controller.admin.roomtype.vo.RoomTypePageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.roomtype.vo.RoomTypeSaveReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.roomtype.RoomTypeDO;
import cn.iocoder.yudao.module.hotel.dal.mysql.roomtype.RoomTypeMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.room.RoomMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hotel.enums.ErrorCodeConstants.*;

/**
 * 房型 Service 实现
 */
@Service
@Validated
@Slf4j
public class RoomTypeServiceImpl {

    @Resource
    private RoomTypeMapper roomTypeMapper;
    @Resource
    private RoomMapper roomMapper;

    public Long createRoomType(RoomTypeSaveReqVO reqVO) {
        validateCodeUnique(null, reqVO.getCode());
        RoomTypeDO roomType = BeanUtils.toBean(reqVO, RoomTypeDO.class);
        if (roomType.getStatus() == null) {
            roomType.setStatus(CommonStatusEnum.ENABLE.getStatus());
        }
        if (roomType.getSort() == null) {
            roomType.setSort(0);
        }
        roomTypeMapper.insert(roomType);
        return roomType.getId();
    }

    public void updateRoomType(RoomTypeSaveReqVO reqVO) {
        validateExists(reqVO.getId());
        validateCodeUnique(reqVO.getId(), reqVO.getCode());
        RoomTypeDO update = BeanUtils.toBean(reqVO, RoomTypeDO.class);
        roomTypeMapper.updateById(update);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteRoomType(Long id) {
        validateExists(id);
        Long roomCount = roomMapper.selectCountByRoomTypeId(id);
        if (roomCount != null && roomCount > 0) {
            throw exception(ROOM_TYPE_HAS_ROOM);
        }
        roomTypeMapper.deleteById(id);
    }

    public RoomTypeDO getRoomType(Long id) {
        return roomTypeMapper.selectById(id);
    }

    public PageResult<RoomTypeDO> getRoomTypePage(RoomTypePageReqVO reqVO) {
        return roomTypeMapper.selectPage(reqVO);
    }

    public List<RoomTypeDO> getRoomTypeList() {
        return roomTypeMapper.selectListByStatus(null);
    }

    public List<RoomTypeDO> getEnabledRoomTypeList() {
        return roomTypeMapper.selectListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

    private void validateExists(Long id) {
        if (id == null || roomTypeMapper.selectById(id) == null) {
            throw exception(ROOM_TYPE_NOT_EXISTS);
        }
    }

    private void validateCodeUnique(Long id, String code) {
        if (code == null) {
            return;
        }
        RoomTypeDO exists = roomTypeMapper.selectByCode(code.trim());
        if (exists != null && !Objects.equals(exists.getId(), id)) {
            throw exception(ROOM_TYPE_CODE_DUPLICATE);
        }
    }
}
