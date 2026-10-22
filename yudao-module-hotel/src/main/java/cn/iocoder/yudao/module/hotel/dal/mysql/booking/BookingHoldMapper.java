package cn.iocoder.yudao.module.hotel.dal.mysql.booking;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.dal.dataobject.booking.BookingHoldDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface BookingHoldMapper extends BaseMapperX<BookingHoldDO> {

    default List<BookingHoldDO> selectListByHoldNo(String holdNo) {
        return selectList(new LambdaQueryWrapperX<BookingHoldDO>()
                .eq(BookingHoldDO::getHoldNo, holdNo)
                .orderByAsc(BookingHoldDO::getBizDate));
    }

    /**
     * 统计某房型某日期「占用中」的 hold 房数（含未过期），用于可售量计算
     */
    @Select("SELECT IFNULL(SUM(room_count), 0) FROM hotel_booking_hold "
            + "WHERE room_type_id = #{roomTypeId} AND biz_date = #{bizDate} AND status = 10 "
            + "AND expire_time > NOW() AND deleted = b'0'")
    int sumActiveCount(@Param("roomTypeId") Long roomTypeId, @Param("bizDate") LocalDate bizDate);

    /**
     * 批量过期：每 5 分钟由定时任务调用，把过期未转化的 hold 置为「40 已过期」
     */
    @Update("UPDATE hotel_booking_hold SET status = 40, update_time = NOW() "
            + "WHERE status = 10 AND expire_time <= #{now} AND deleted = b'0'")
    int expireTimeout(@Param("now") LocalDateTime now);

    @Update("UPDATE hotel_booking_hold SET status = #{status}, order_id = #{orderId}, update_time = NOW() "
            + "WHERE hold_no = #{holdNo} AND status = 10 AND deleted = b'0'")
    int updateStatusByHoldNo(@Param("holdNo") String holdNo, @Param("status") Integer status,
                            @Param("orderId") Long orderId);
}
