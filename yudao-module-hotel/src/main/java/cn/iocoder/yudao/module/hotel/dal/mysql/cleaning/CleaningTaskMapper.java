package cn.iocoder.yudao.module.hotel.dal.mysql.cleaning;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.controller.admin.cleaning.vo.CleaningTaskPageReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.cleaning.CleaningTaskDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Mapper
public interface CleaningTaskMapper extends BaseMapperX<CleaningTaskDO> {

    default PageResult<CleaningTaskDO> selectPage(CleaningTaskPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CleaningTaskDO>()
                .eqIfPresent(CleaningTaskDO::getRoomId, reqVO.getRoomId())
                .eqIfPresent(CleaningTaskDO::getStatus, reqVO.getStatus())
                .eqIfPresent(CleaningTaskDO::getType, reqVO.getType())
                .eqIfPresent(CleaningTaskDO::getPlanDate, reqVO.getPlanDate())
                .orderByDesc(CleaningTaskDO::getId));
    }

    default Long selectCountByRoomIdAndStatusIn(Long roomId, Collection<Integer> statuses) {
        return selectCount(new LambdaQueryWrapperX<CleaningTaskDO>()
                .eq(CleaningTaskDO::getRoomId, roomId)
                .in(CleaningTaskDO::getStatus, statuses));
    }

    default Long selectCountByStatusIn(Collection<Integer> statuses) {
        return selectCount(new LambdaQueryWrapperX<CleaningTaskDO>()
                .in(CleaningTaskDO::getStatus, statuses));
    }

    default List<CleaningTaskDO> selectListByStatusIn(Collection<Integer> statuses) {
        return selectList(new LambdaQueryWrapperX<CleaningTaskDO>()
                .in(CleaningTaskDO::getStatus, statuses)
                .orderByAsc(CleaningTaskDO::getPlanDate));
    }

    default List<CleaningTaskDO> selectListByPlanDate(LocalDate planDate) {
        return selectList(new LambdaQueryWrapperX<CleaningTaskDO>()
                .eq(CleaningTaskDO::getPlanDate, planDate));
    }
}
