package cn.iocoder.yudao.module.hotel.dal.mysql.order;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.controller.admin.order.vo.OrderPageReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.order.OrderDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapperX<OrderDO> {

    default PageResult<OrderDO> selectPage(OrderPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OrderDO>()
                .likeIfPresent(OrderDO::getOrderNo, reqVO.getOrderNo())
                .eqIfPresent(OrderDO::getGuestId, reqVO.getGuestId())
                .eqIfPresent(OrderDO::getRoomId, reqVO.getRoomId())
                .eqIfPresent(OrderDO::getStatus, reqVO.getStatus())
                .eqIfPresent(OrderDO::getPaymentStatus, reqVO.getPaymentStatus())
                .eqIfPresent(OrderDO::getOrderType, reqVO.getOrderType())
                .geIfPresent(OrderDO::getCheckInDate, reqVO.getCheckInDateStart())
                .leIfPresent(OrderDO::getCheckOutDate, reqVO.getCheckOutDateEnd())
                .orderByDesc(OrderDO::getId));
    }

    default OrderDO selectByOrderNo(String orderNo) {
        return selectOne(OrderDO::getOrderNo, orderNo);
    }

    default Long selectCountByRoomIdAndStatusIn(Long roomId, Collection<Integer> statuses) {
        return selectCount(new LambdaQueryWrapperX<OrderDO>()
                .eq(OrderDO::getRoomId, roomId)
                .in(OrderDO::getStatus, statuses));
    }

    default List<OrderDO> selectListByRoomIdsAndStatus(Collection<Long> roomIds, Integer status) {
        if (roomIds == null || roomIds.isEmpty()) {
            return List.of();
        }
        return selectList(new LambdaQueryWrapperX<OrderDO>()
                .in(OrderDO::getRoomId, roomIds)
                .eq(OrderDO::getStatus, status));
    }

    /**
     * 冲突订单数：[checkIn, checkOut) 与已有占用订单相交
     */
    default Long selectConflictCount(Long roomId, LocalDate checkIn, LocalDate checkOut,
                                     Collection<Integer> activeStatuses, Long excludeId) {
        // 注意：MyBatis-Plus 继承来的 eq/lt/gt/in 返回的是父类型 LambdaQueryWrapper，
        // 因此这里先构造条件，再单独追加 ne，避免链式调用丢失 LambdaQueryWrapperX 的扩展方法
        LambdaQueryWrapperX<OrderDO> wrapper = new LambdaQueryWrapperX<>();
        wrapper.eq(OrderDO::getRoomId, roomId)
                .lt(OrderDO::getCheckInDate, checkOut)
                .gt(OrderDO::getCheckOutDate, checkIn)
                .in(OrderDO::getStatus, activeStatuses);
        if (excludeId != null) {
            wrapper.ne(OrderDO::getId, excludeId);
        }
        return selectCount(wrapper);
    }

    default List<OrderDO> selectListByStatus(Collection<Integer> statuses) {
        return selectList(new LambdaQueryWrapperX<OrderDO>()
                .in(OrderDO::getStatus, statuses)
                .orderByDesc(OrderDO::getId));
    }

    default Long selectCountByStatus(Integer status) {
        return selectCount(new LambdaQueryWrapperX<OrderDO>().eq(OrderDO::getStatus, status));
    }

    default List<OrderDO> selectListByCheckInDate(LocalDate date, Collection<Integer> statuses) {
        return selectList(new LambdaQueryWrapperX<OrderDO>()
                .eq(OrderDO::getCheckInDate, date)
                .in(OrderDO::getStatus, statuses));
    }

    default List<OrderDO> selectListByCheckOutDate(LocalDate date, Integer status) {
        return selectList(new LambdaQueryWrapperX<OrderDO>()
                .eq(OrderDO::getCheckOutDate, date)
                .eq(OrderDO::getStatus, status));
    }

    default List<OrderDO> selectListByCheckOutTimeBetween(java.time.LocalDateTime start,
                                                          java.time.LocalDateTime end) {
        return selectList(new LambdaQueryWrapperX<OrderDO>()
                .geIfPresent(OrderDO::getActualCheckOutTime, start)
                .ltIfPresent(OrderDO::getActualCheckOutTime, end));
    }
}
