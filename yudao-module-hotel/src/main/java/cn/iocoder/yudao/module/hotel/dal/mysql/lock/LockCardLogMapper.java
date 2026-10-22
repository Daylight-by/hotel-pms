package cn.iocoder.yudao.module.hotel.dal.mysql.lock;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.controller.admin.lock.vo.LockPageReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.lock.LockCardLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LockCardLogMapper extends BaseMapperX<LockCardLogDO> {

    default PageResult<LockCardLogDO> selectPage(LockPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<LockCardLogDO>()
                .eqIfPresent(LockCardLogDO::getOrderId, reqVO.getOrderId())
                .eqIfPresent(LockCardLogDO::getRoomNo, reqVO.getRoomNo())
                .eqIfPresent(LockCardLogDO::getActionType, reqVO.getActionType())
                .eqIfPresent(LockCardLogDO::getStatus, reqVO.getStatus())
                .orderByDesc(LockCardLogDO::getId));
    }

    default List<LockCardLogDO> selectByOrderId(Long orderId) {
        return selectList(new LambdaQueryWrapperX<LockCardLogDO>()
                .eq(LockCardLogDO::getOrderId, orderId)
                .orderByDesc(LockCardLogDO::getId));
    }

    default LockCardLogDO selectLatestIssued(Long orderId) {
        List<LockCardLogDO> list = selectList(new LambdaQueryWrapperX<LockCardLogDO>()
                .eq(LockCardLogDO::getOrderId, orderId)
                .in(LockCardLogDO::getActionType, List.of(10, 20, 40))
                .orderByDesc(LockCardLogDO::getId)
                .last("LIMIT 1"));
        return list.isEmpty() ? null : list.get(0);
    }

}
