package cn.iocoder.yudao.module.hotel.dal.mysql.rateplan;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.controller.admin.rateplan.vo.RatePlanPageReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.rateplan.RatePlanDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RatePlanMapper extends BaseMapperX<RatePlanDO> {

    default PageResult<RatePlanDO> selectPage(RatePlanPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<RatePlanDO>()
                .likeIfPresent(RatePlanDO::getName, reqVO.getName())
                .eqIfPresent(RatePlanDO::getCode, reqVO.getCode())
                .eqIfPresent(RatePlanDO::getChannelId, reqVO.getChannelId())
                .eqIfPresent(RatePlanDO::getRoomTypeId, reqVO.getRoomTypeId())
                .eqIfPresent(RatePlanDO::getStatus, reqVO.getStatus())
                .orderByAsc(RatePlanDO::getSort)
                .orderByAsc(RatePlanDO::getId));
    }

    default RatePlanDO selectByCode(String code) {
        return selectOne(new LambdaQueryWrapperX<RatePlanDO>()
                .eq(RatePlanDO::getCode, code));
    }

    default List<RatePlanDO> selectListByChannelId(Long channelId) {
        return selectList(new LambdaQueryWrapperX<RatePlanDO>()
                .eq(RatePlanDO::getChannelId, channelId));
    }

    /**
     * 开启中且适用于指定房型的房价计划（room_type_id 为空表示全部房型）
     */
    default List<RatePlanDO> selectEnabledListByRoomType(Long roomTypeId) {
        return selectList(new LambdaQueryWrapperX<RatePlanDO>()
                .eq(RatePlanDO::getStatus, 0)
                .orderByAsc(RatePlanDO::getSort)
                .orderByAsc(RatePlanDO::getId)).stream()
                .filter(plan -> plan.getRoomTypeId() == null || plan.getRoomTypeId().equals(roomTypeId))
                .toList();
    }
}
