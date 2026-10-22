package cn.iocoder.yudao.module.hotel.service.booking;

import cn.iocoder.yudao.module.hotel.controller.app.booking.vo.AppAvailabilityReqVO;
import cn.iocoder.yudao.module.hotel.controller.app.booking.vo.AppBookingReqVO;
import cn.iocoder.yudao.module.hotel.controller.app.booking.vo.AppBookingRespVO;
import cn.iocoder.yudao.module.hotel.controller.app.booking.vo.AppHoldReqVO;
import cn.iocoder.yudao.module.hotel.controller.app.booking.vo.AppHoldRespVO;
import cn.iocoder.yudao.module.hotel.controller.app.booking.vo.AppRoomTypeAvailabilityRespVO;

import java.util.List;

/**
 * 预订引擎 Service（T1 · App 端）
 */
public interface BookingService {

    /**
     * 可售房型查询：返回每个房型的最低可售价、剩余房量和逐日价格
     */
    List<AppRoomTypeAvailabilityRespVO> searchAvailability(AppAvailabilityReqVO reqVO);

    /**
     * 创建占位（15 分钟有效）：对区间内每天校验「总库存 − 已售 − 有效占位」是否够
     */
    AppHoldRespVO createHold(AppHoldReqVO reqVO);

    /**
     * 提交订单：校验占位有效 → 原子扣减库存 → 建订单（10 待支付）
     */
    AppBookingRespVO submitBooking(AppBookingReqVO reqVO);

    /**
     * 释放过期占位（定时任务 5 分钟一次），返回处理条数
     */
    int expireHolds();
}
