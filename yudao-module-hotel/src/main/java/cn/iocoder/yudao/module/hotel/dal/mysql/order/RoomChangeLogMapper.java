package cn.iocoder.yudao.module.hotel.dal.mysql.order;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.dal.dataobject.order.RoomChangeLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RoomChangeLogMapper extends BaseMapperX<RoomChangeLogDO> {

    default List<RoomChangeLogDO> selectListByOrderId(Long orderId) {
        return selectList(new LambdaQueryWrapperX<RoomChangeLogDO>()
                .eq(RoomChangeLogDO::getOrderId, orderId)
                .orderByAsc(RoomChangeLogDO::getId));
    }
}
