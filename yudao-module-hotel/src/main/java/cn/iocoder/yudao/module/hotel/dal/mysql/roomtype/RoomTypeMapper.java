package cn.iocoder.yudao.module.hotel.dal.mysql.roomtype;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.controller.admin.roomtype.vo.RoomTypePageReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.roomtype.RoomTypeDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RoomTypeMapper extends BaseMapperX<RoomTypeDO> {

    default PageResult<RoomTypeDO> selectPage(RoomTypePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<RoomTypeDO>()
                .likeIfPresent(RoomTypeDO::getName, reqVO.getName())
                .likeIfPresent(RoomTypeDO::getCode, reqVO.getCode())
                .eqIfPresent(RoomTypeDO::getStatus, reqVO.getStatus())
                .orderByAsc(RoomTypeDO::getSort)
                .orderByDesc(RoomTypeDO::getId));
    }

    default RoomTypeDO selectByCode(String code) {
        return selectOne(RoomTypeDO::getCode, code);
    }

    default List<RoomTypeDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<RoomTypeDO>()
                .eqIfPresent(RoomTypeDO::getStatus, status)
                .orderByAsc(RoomTypeDO::getSort));
    }
}
