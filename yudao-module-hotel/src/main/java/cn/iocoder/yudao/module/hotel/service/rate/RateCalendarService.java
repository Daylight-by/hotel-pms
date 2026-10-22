package cn.iocoder.yudao.module.hotel.service.rate;

import cn.iocoder.yudao.module.hotel.controller.admin.rate.vo.RateCalendarBoardRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.rate.vo.RoomDailyRateBatchSaveReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.rate.vo.RoomDailyRateRespVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 房价日历 Service
 */
public interface RateCalendarService {

    /**
     * 批量维护：按房型 + 日期区间设置价格 / 库存 / 开关，已存在则更新，不存在则插入
     *
     * @return 维护的天数
     */
    Long batchSave(RoomDailyRateBatchSaveReqVO reqVO);

    /**
     * 查询某房型在某日期区间的日历。缺失的日期也要返回（价格取房型基础价、默认开房、库存 0）。
     */
    List<RoomDailyRateRespVO> listByRange(Long roomTypeId, LocalDate begin, LocalDate end);

    /**
     * 可用性校验：区间内每天都要开房，且剩余房量充足。未维护的日期按兜底价可售，不报错。
     */
    void validateAvailable(Long roomTypeId, LocalDate checkIn, LocalDate checkOut, Integer roomCount, Long excludeOrderId);

    /**
     * 占用库存（下单成功）
     */
    void occupy(Long roomTypeId, LocalDate date, int count);

    /**
     * 释放库存（取消 / 退房）
     */
    void release(Long roomTypeId, LocalDate date, int count);

    /**
     * 房态盘：返回每房型每天的价格 / 总房量 / 已售 / 剩余
     */
    List<RateCalendarBoardRespVO> getBoard(LocalDate begin, LocalDate end);

    /**
     * 取某房型某天的有效房价（日历价 → 房型基础价 → 房间售价）
     */
    java.math.BigDecimal getEffectivePrice(Long roomTypeId, LocalDate date, java.math.BigDecimal fallback);
}
