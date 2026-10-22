package cn.iocoder.yudao.module.hotel.dal.mysql.room;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.controller.admin.room.vo.RoomPageReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.room.RoomDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface RoomMapper extends BaseMapperX<RoomDO> {

    default PageResult<RoomDO> selectPage(RoomPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<RoomDO>()
                .likeIfPresent(RoomDO::getRoomNo, reqVO.getRoomNo())
                .eqIfPresent(RoomDO::getRoomTypeId, reqVO.getRoomTypeId())
                .eqIfPresent(RoomDO::getFloor, reqVO.getFloor())
                .eqIfPresent(RoomDO::getStatus, reqVO.getStatus())
                .orderByAsc(RoomDO::getFloor)
                .orderByAsc(RoomDO::getRoomNo));
    }

    default RoomDO selectByRoomNo(String roomNo) {
        return selectOne(RoomDO::getRoomNo, roomNo);
    }

    default Long selectCountByRoomTypeId(Long roomTypeId) {
        return selectCount(new LambdaQueryWrapperX<RoomDO>().eq(RoomDO::getRoomTypeId, roomTypeId));
    }

    default List<RoomDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<RoomDO>()
                .eqIfPresent(RoomDO::getStatus, status)
                .orderByAsc(RoomDO::getRoomNo));
    }

    default List<RoomDO> selectListByIds(Collection<Long> ids) {
        return selectBatchIds(ids);
    }

    default List<RoomDO> selectListAll() {
        return selectList(new LambdaQueryWrapperX<RoomDO>().orderByAsc(RoomDO::getRoomNo));
    }
}
