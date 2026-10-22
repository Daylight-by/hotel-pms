package cn.iocoder.yudao.module.hotel.dal.mysql.rate;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.controller.admin.rate.vo.RoomDailyRatePageReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.rate.RoomDailyRateDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface RoomDailyRateMapper extends BaseMapperX<RoomDailyRateDO> {

    default RoomDailyRateDO selectByTypeAndDate(Long roomTypeId, LocalDate bizDate) {
        return selectOne(new LambdaQueryWrapperX<RoomDailyRateDO>()
                .eq(RoomDailyRateDO::getRoomTypeId, roomTypeId)
                .eq(RoomDailyRateDO::getBizDate, bizDate));
    }

    default List<RoomDailyRateDO> selectListByRange(Long roomTypeId, LocalDate begin, LocalDate end) {
        return selectList(new LambdaQueryWrapperX<RoomDailyRateDO>()
                .eq(RoomDailyRateDO::getRoomTypeId, roomTypeId)
                .between(RoomDailyRateDO::getBizDate, begin, end)
                .orderByAsc(RoomDailyRateDO::getBizDate));
    }

    default List<RoomDailyRateDO> selectListByDateRange(LocalDate begin, LocalDate end) {
        return selectList(new LambdaQueryWrapperX<RoomDailyRateDO>()
                .between(RoomDailyRateDO::getBizDate, begin, end)
                .orderByAsc(RoomDailyRateDO::getRoomTypeId)
                .orderByAsc(RoomDailyRateDO::getBizDate));
    }

    default PageResult<RoomDailyRateDO> selectPage(RoomDailyRatePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<RoomDailyRateDO>()
                .eqIfPresent(RoomDailyRateDO::getRoomTypeId, reqVO.getRoomTypeId())
                .geIfPresent(RoomDailyRateDO::getBizDate, reqVO.getBeginDate())
                .leIfPresent(RoomDailyRateDO::getBizDate, reqVO.getEndDate())
                .eqIfPresent(RoomDailyRateDO::getStatus, reqVO.getStatus())
                .orderByAsc(RoomDailyRateDO::getBizDate));
    }

    /**
     * 原子占用库存：已售 + count 不得超过总库存，返回受影响行数（0 表示库存不足）
     */
    @Update("UPDATE hotel_room_daily_rate SET sold_inventory = sold_inventory + #{count}, update_time = NOW() "
            + "WHERE room_type_id = #{roomTypeId} AND biz_date = #{bizDate} AND deleted = b'0' "
            + "AND sold_inventory + #{count} <= total_inventory")
    int occupyInventory(@Param("roomTypeId") Long roomTypeId, @Param("bizDate") LocalDate bizDate,
                        @Param("count") int count);

    /**
     * 释放库存：释放后不得为负
     */
    @Update("UPDATE hotel_room_daily_rate SET sold_inventory = GREATEST(sold_inventory - #{count}, 0), update_time = NOW() "
            + "WHERE room_type_id = #{roomTypeId} AND biz_date = #{bizDate} AND deleted = b'0'")
    int releaseInventory(@Param("roomTypeId") Long roomTypeId, @Param("bizDate") LocalDate bizDate,
                         @Param("count") int count);

    @Update("UPDATE hotel_room_daily_rate SET total_inventory = #{total}, update_time = NOW() WHERE id = #{id}")
    int updateInventory(@Param("id") Long id, @Param("total") int total);

    @Update("UPDATE hotel_room_daily_rate SET price = #{price}, update_time = NOW() WHERE id = #{id}")
    int updatePrice(@Param("id") Long id, @Param("price") BigDecimal price);
}
