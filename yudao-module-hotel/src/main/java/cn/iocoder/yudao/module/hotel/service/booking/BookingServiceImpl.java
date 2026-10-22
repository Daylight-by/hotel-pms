package cn.iocoder.yudao.module.hotel.service.booking;

import cn.iocoder.yudao.module.hotel.controller.app.booking.vo.AppAvailabilityReqVO;
import cn.iocoder.yudao.module.hotel.controller.app.booking.vo.AppBookingReqVO;
import cn.iocoder.yudao.module.hotel.controller.app.booking.vo.AppBookingRespVO;
import cn.iocoder.yudao.module.hotel.controller.app.booking.vo.AppHoldReqVO;
import cn.iocoder.yudao.module.hotel.controller.app.booking.vo.AppHoldRespVO;
import cn.iocoder.yudao.module.hotel.controller.app.booking.vo.AppRoomTypeAvailabilityRespVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.booking.BookingHoldDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.guest.GuestDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.order.OrderDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.rate.RoomDailyRateDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.rateplan.RatePlanDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.room.RoomDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.roomtype.RoomTypeDO;
import cn.iocoder.yudao.module.hotel.dal.mysql.booking.BookingHoldMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.guest.GuestMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.order.OrderMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.rate.RoomDailyRateMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.rateplan.RatePlanMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.room.RoomMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.roomtype.RoomTypeMapper;
import cn.iocoder.yudao.module.hotel.enums.DailyRateStatusEnum;
import cn.iocoder.yudao.module.hotel.enums.HoldStatusEnum;
import cn.iocoder.yudao.module.hotel.enums.OrderStatusEnum;
import cn.iocoder.yudao.module.hotel.enums.OrderTypeEnum;
import cn.iocoder.yudao.module.hotel.enums.PaymentStatusEnum;
import cn.iocoder.yudao.module.hotel.service.audit.BusinessDateService;
import cn.iocoder.yudao.module.hotel.service.channel.ChannelService;
import cn.iocoder.yudao.module.hotel.service.rate.RateCalendarService;
import cn.iocoder.yudao.module.hotel.service.rateplan.RatePlanService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hotel.enums.HotelT1ErrorCodeConstants.ERROR_HOLD_EXPIRED;
import static cn.iocoder.yudao.module.hotel.enums.HotelT1ErrorCodeConstants.ERROR_HOLD_FAILED;
import static cn.iocoder.yudao.module.hotel.enums.HotelT1ErrorCodeConstants.ERROR_RATE_PLAN_ADVANCE;
import static cn.iocoder.yudao.module.hotel.enums.HotelT1ErrorCodeConstants.ERROR_RATE_PLAN_CLOSED;
import static cn.iocoder.yudao.module.hotel.enums.HotelT1ErrorCodeConstants.ERROR_RATE_PLAN_MIN_NIGHTS;
import static cn.iocoder.yudao.module.hotel.enums.HotelT1ErrorCodeConstants.ERROR_RATE_PLAN_NOT_EXIST;
import static cn.iocoder.yudao.module.hotel.enums.ErrorCodeConstants.ORDER_DATE_INVALID;

/**
 * 预订引擎 Service 实现（T1）
 *
 * <p><b>防超售的关键</b>：真正扣库存只走一条 SQL 原子更新
 * （{@code RoomDailyRateMapper#occupyInventory}）：
 * <pre>
 * UPDATE hotel_room_daily_rate SET sold_inventory = sold_inventory + #{count}
 * WHERE room_type_id = ? AND biz_date = ? AND deleted = b'0'
 *   AND sold_inventory + #{count} &lt;= total_inventory
 * </pre>
 * 影响行数为 0 即代表库存已被别人抢走，抛 {@code ERROR_HOLD_FAILED (1003004221)}。
 * <b>不做「先查再写」</b>，并发下不会超卖。</p>
 */
@Service
@Validated
@Slf4j
public class BookingServiceImpl implements BookingService {

    /** 占位有效期（分钟） */
    private static final int HOLD_MINUTES = 15;

    private static final DateTimeFormatter ORDER_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Resource
    private BookingHoldMapper holdMapper;
    @Resource
    private RoomDailyRateMapper rateMapper;
    @Resource
    private RoomTypeMapper roomTypeMapper;
    @Resource
    private RoomMapper roomMapper;
    @Resource
    private GuestMapper guestMapper;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private RatePlanMapper ratePlanMapper;
    @Resource
    private RatePlanService ratePlanService;
    @Resource
    private RateCalendarService rateCalendarService;
    @Resource
    private ChannelService channelService;
    @Resource
    private BusinessDateService businessDateService;

    @Override
    public List<AppRoomTypeAvailabilityRespVO> searchAvailability(AppAvailabilityReqVO reqVO) {
        validateDates(reqVO.getCheckInDate(), reqVO.getCheckOutDate());
        List<RoomTypeDO> roomTypes = roomTypeMapper.selectListByStatus(0);
        List<AppRoomTypeAvailabilityRespVO> result = new ArrayList<>();
        for (RoomTypeDO roomType : roomTypes) {
            if (reqVO.getAdultCount() != null && roomType.getMaxOccupancy() != null
                    && reqVO.getAdultCount() > roomType.getMaxOccupancy()) {
                continue;
            }
            List<RatePlanDO> plans = ratePlanMapper.selectEnabledListByRoomType(roomType.getId());
            int roomCount = countRooms(roomType.getId());
            List<AppRoomTypeAvailabilityRespVO.DayPrice> days = new ArrayList<>();
            BigDecimal lowest = null;
            Integer minRemaining = null;
            boolean closed = false;
            RatePlanDO lowestPlan = null;
            for (LocalDate date = reqVO.getCheckInDate();
                 date.isBefore(reqVO.getCheckOutDate()); date = date.plusDays(1)) {
                RoomDailyRateDO row = rateMapper.selectByTypeAndDate(roomType.getId(), date);
                if (row != null && Objects.equals(row.getStatus(), DailyRateStatusEnum.CLOSED.getStatus())) {
                    closed = true;
                    break;
                }
                int total = row == null || row.getTotalInventory() == null ? roomCount : row.getTotalInventory();
                int sold = row == null || row.getSoldInventory() == null ? 0 : row.getSoldInventory();
                int held = holdMapper.sumActiveCount(roomType.getId(), date);
                int remaining = Math.max(total - sold - held, 0);

                BigDecimal price = lowestPrice(roomType, date, plans);
                RatePlanDO cheapestPlan = ratePlanService.getCheapestPlan(roomType.getId(), date);

                AppRoomTypeAvailabilityRespVO.DayPrice day = new AppRoomTypeAvailabilityRespVO.DayPrice();
                day.setBizDate(date);
                day.setPrice(price);
                day.setRemaining(remaining);
                days.add(day);

                if (lowest == null || price.compareTo(lowest) < 0) {
                    lowest = price;
                }
                if (minRemaining == null || remaining < minRemaining) {
                    minRemaining = remaining;
                }
                if (lowestPlan == null && cheapestPlan != null) {
                    lowestPlan = cheapestPlan;
                }
            }
            if (closed || days.isEmpty()) {
                continue;
            }
            AppRoomTypeAvailabilityRespVO vo = new AppRoomTypeAvailabilityRespVO();
            vo.setRoomTypeId(roomType.getId());
            vo.setRoomTypeName(roomType.getName());
            vo.setPrice(lowest);
            vo.setAvailableCount(minRemaining == null ? 0 : minRemaining);
            vo.setRatePlanId(lowestPlan == null ? null : lowestPlan.getId());
            vo.setRatePlanName(lowestPlan == null ? null : lowestPlan.getName());
            vo.setBreakfast(lowestPlan == null ? null : lowestPlan.getBreakfast());
            vo.setMaxOccupancy(roomType.getMaxOccupancy());
            vo.setBasePrice(roomType.getBasePrice());
            vo.setDailyPrices(days);
            result.add(vo);
        }
        result.sort((a, b) -> {
            if (a.getPrice() == null) {
                return 1;
            }
            if (b.getPrice() == null) {
                return -1;
            }
            return a.getPrice().compareTo(b.getPrice());
        });
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppHoldRespVO createHold(AppHoldReqVO reqVO) {
        validateDates(reqVO.getCheckInDate(), reqVO.getCheckOutDate());
        RoomTypeDO roomType = reqVO.getRoomTypeId() == null ? null
                : roomTypeMapper.selectById(reqVO.getRoomTypeId());
        if (roomType == null) {
            throw exception(cn.iocoder.yudao.module.hotel.enums.ErrorCodeConstants.ROOM_TYPE_NOT_EXISTS);
        }
        int roomCount = reqVO.getRoomCount() == null ? 1 : Math.max(reqVO.getRoomCount(), 1);
        int nights = (int) ChronoUnit.DAYS.between(reqVO.getCheckInDate(), reqVO.getCheckOutDate());

        RatePlanDO plan = null;
        if (reqVO.getRatePlanId() != null) {
            plan = ratePlanMapper.selectById(reqVO.getRatePlanId());
            if (plan == null) {
                throw exception(ERROR_RATE_PLAN_NOT_EXIST);
            }
            if (Objects.equals(plan.getStatus(), 1)) {
                throw exception(ERROR_RATE_PLAN_CLOSED);
            }
            if (plan.getMinNights() != null && nights < plan.getMinNights()) {
                throw exception(ERROR_RATE_PLAN_MIN_NIGHTS);
            }
            if (plan.getMinAdvanceHours() != null) {
                long hours = ChronoUnit.HOURS.between(LocalDateTime.now(),
                        reqVO.getCheckInDate().atTime(14, 0));
                if (hours < plan.getMinAdvanceHours()) {
                    throw exception(ERROR_RATE_PLAN_ADVANCE);
                }
            }
        }

        // 预检：区间内每天都得有足够可售（真正的强一致保证在 submit 的原子扣减）
        List<RatePlanDO> plans = ratePlanMapper.selectEnabledListByRoomType(roomType.getId());
        int roomCountOfType = countRooms(roomType.getId());
        BigDecimal unitPrice = BigDecimal.ZERO;
        for (LocalDate date = reqVO.getCheckInDate();
             date.isBefore(reqVO.getCheckOutDate()); date = date.plusDays(1)) {
            RoomDailyRateDO row = rateMapper.selectByTypeAndDate(roomType.getId(), date);
            int total = row == null || row.getTotalInventory() == null
                    ? roomCountOfType : row.getTotalInventory();
            int sold = row == null || row.getSoldInventory() == null ? 0 : row.getSoldInventory();
            int held = holdMapper.sumActiveCount(roomType.getId(), date);
            if (total - sold - held < roomCount) {
                throw exception(ERROR_HOLD_FAILED);
            }
            unitPrice = unitPrice.add(plan == null ? lowestPrice(roomType, date, plans)
                    : ratePlanService.calcDailyPrice(plan, roomType.getId(), date));
        }

        String holdNo = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expireTime = LocalDateTime.now().plusMinutes(HOLD_MINUTES);
        for (LocalDate date = reqVO.getCheckInDate();
             date.isBefore(reqVO.getCheckOutDate()); date = date.plusDays(1)) {
            BookingHoldDO hold = new BookingHoldDO();
            hold.setHoldNo(holdNo);
            hold.setRoomTypeId(roomType.getId());
            hold.setBizDate(date);
            hold.setRoomCount(roomCount);
            hold.setStatus(HoldStatusEnum.HOLDING.getStatus());
            hold.setExpireTime(expireTime);
            holdMapper.insert(hold);
        }

        AppHoldRespVO resp = new AppHoldRespVO();
        resp.setHoldNo(holdNo);
        resp.setExpireTime(expireTime);
        resp.setRoomTypeId(roomType.getId());
        resp.setRoomTypeName(roomType.getName());
        resp.setRatePlanId(plan == null ? null : plan.getId());
        resp.setNights(nights);
        resp.setRoomCount(roomCount);
        resp.setTotalAmount(unitPrice.multiply(BigDecimal.valueOf(roomCount))
                .setScale(2, RoundingMode.HALF_UP));
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AppBookingRespVO submitBooking(AppBookingReqVO reqVO) {
        List<BookingHoldDO> holds = holdMapper.selectListByHoldNo(reqVO.getHoldNo());
        if (holds.isEmpty()) {
            throw exception(ERROR_HOLD_EXPIRED);
        }
        LocalDateTime now = LocalDateTime.now();
        for (BookingHoldDO hold : holds) {
            if (!Objects.equals(hold.getStatus(), HoldStatusEnum.HOLDING.getStatus())) {
                throw exception(ERROR_HOLD_EXPIRED);
            }
            if (hold.getExpireTime() == null || !hold.getExpireTime().isAfter(now)) {
                holdMapper.expireTimeout(now);
                throw exception(ERROR_HOLD_EXPIRED);
            }
        }

        BookingHoldDO first = holds.get(0);
        Long roomTypeId = first.getRoomTypeId();
        int roomCount = first.getRoomCount() == null ? 1 : first.getRoomCount();
        RoomTypeDO roomType = roomTypeMapper.selectById(roomTypeId);
        if (roomType == null) {
            throw exception(cn.iocoder.yudao.module.hotel.enums.ErrorCodeConstants.ROOM_TYPE_NOT_EXISTS);
        }

        // ① 原子扣库存：影响行数 0 说明被并发抢走，直接抛 ERROR_HOLD_FAILED
        for (BookingHoldDO hold : holds) {
            RoomDailyRateDO row = rateMapper.selectByTypeAndDate(roomTypeId, hold.getBizDate());
            if (row == null) {
                // 未排房的日期首次售卖：交给房价日历落一条（内部走房型房间数兜底）
                rateCalendarService.occupy(roomTypeId, hold.getBizDate(), roomCount);
                continue;
            }
            int updated = rateMapper.occupyInventory(roomTypeId, hold.getBizDate(), roomCount);
            if (updated == 0) {
                throw exception(ERROR_HOLD_FAILED);
            }
        }

        // ② 算价 + 找房
        List<RatePlanDO> plans = ratePlanMapper.selectEnabledListByRoomType(roomTypeId);
        BigDecimal unitPrice = BigDecimal.ZERO;
        for (BookingHoldDO hold : holds) {
            unitPrice = unitPrice.add(lowestPrice(roomType, hold.getBizDate(), plans));
        }
        BigDecimal amount = unitPrice.multiply(BigDecimal.valueOf(roomCount))
                .setScale(2, RoundingMode.HALF_UP);
        RatePlanDO cheapestPlan = ratePlanService.getCheapestPlan(roomTypeId, first.getBizDate());

        // ③ 住客档案：手机号已存在则复用
        String mobile = reqVO.getContactMobile();
        String guestName = reqVO.getGuestName() == null || reqVO.getGuestName().isBlank()
                ? reqVO.getContactName() : reqVO.getGuestName();
        if (guestName == null || guestName.isBlank()) {
            guestName = "线上客人";
        }
        GuestDO guest = guestMapper.selectByMobile(mobile);
        if (guest == null) {
            guest = new GuestDO();
            guest.setName(guestName);
            guest.setMobile(mobile);
            guest.setGuestLevel(0);
            guest.setRemark("App 预订自动建档");
            guestMapper.insert(guest);
        }

        Long roomId = pickRoomId(roomTypeId);
        LocalDate checkIn = holds.get(0).getBizDate();
        LocalDate checkOut = holds.get(holds.size() - 1).getBizDate().plusDays(1);
        int nights = (int) ChronoUnit.DAYS.between(checkIn, checkOut);

        OrderDO order = new OrderDO();
        order.setOrderNo(generateOrderNo());
        order.setGuestId(guest.getId());
        order.setRoomId(roomId);
        order.setRoomTypeId(roomTypeId);
        order.setOrderType(OrderTypeEnum.FULL_DAY.getType());
        order.setRatePlanId(cheapestPlan == null ? null : cheapestPlan.getId());
        order.setChannelId(channelService.getDefaultDirectChannelId());
        order.setBusinessDate(businessDateService.getCurrentBusinessDate());
        order.setCheckInDate(checkIn);
        order.setCheckOutDate(checkOut);
        order.setCheckInTime(checkIn.atTime(14, 0));
        order.setCheckOutTime(checkOut.atTime(12, 0));
        order.setNights(nights);
        order.setAdultCount(1);
        order.setChildCount(0);
        order.setAmount(amount);
        order.setDeposit(BigDecimal.ZERO);
        order.setPaidAmount(BigDecimal.ZERO);
        order.setStatus(OrderStatusEnum.PENDING.getStatus());
        order.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());
        order.setInvoiceRequested(false);
        StringBuilder remark = new StringBuilder("线上预订");
        if (reqVO.getContactName() != null && !reqVO.getContactName().isBlank()) {
            remark.append("·联系人 ").append(reqVO.getContactName());
        }
        remark.append("·手机 ").append(mobile);
        if (reqVO.getArrivalTime() != null && !reqVO.getArrivalTime().isBlank()) {
            remark.append("·预计到店 ").append(reqVO.getArrivalTime());
        }
        if (reqVO.getRemark() != null && !reqVO.getRemark().isBlank()) {
            remark.append("·").append(reqVO.getRemark());
        }
        order.setRemark(remark.length() > 480 ? remark.substring(0, 480) : remark.toString());
        orderMapper.insert(order);

        // ④ 占位转已转化
        holdMapper.updateStatusByHoldNo(reqVO.getHoldNo(), HoldStatusEnum.CONVERTED.getStatus(), order.getId());

        AppBookingRespVO resp = new AppBookingRespVO();
        resp.setOrderId(order.getId());
        resp.setOrderNo(order.getOrderNo());
        resp.setAmount(amount);
        resp.setStatus(order.getStatus());
        resp.setStatusName(OrderStatusEnum.PENDING.getName());
        resp.setCheckInDate(checkIn);
        resp.setCheckOutDate(checkOut);
        resp.setNights(nights);
        resp.setHoldNo(reqVO.getHoldNo());
        return resp;
    }

    @Override
    public int expireHolds() {
        int count = holdMapper.expireTimeout(LocalDateTime.now());
        if (count > 0) {
            log.info("[expireHolds][释放过期占位 {} 条]", count);
        }
        return count;
    }

    // ============================================================ 私有方法

    /**
     * 某房型某日最低可售价：取所有开启中房价计划的最低价；没有计划时回落到日历价/基础价
     */
    private BigDecimal lowestPrice(RoomTypeDO roomType, LocalDate date, List<RatePlanDO> plans) {
        BigDecimal lowest = null;
        if (plans != null) {
            for (RatePlanDO plan : plans) {
                BigDecimal price = ratePlanService.calcDailyPrice(plan, roomType.getId(), date);
                if (lowest == null || price.compareTo(lowest) < 0) {
                    lowest = price;
                }
            }
        }
        if (lowest == null) {
            lowest = rateCalendarService.getEffectivePrice(roomType.getId(), date, roomType.getBasePrice());
        }
        return lowest == null ? BigDecimal.ZERO : lowest;
    }

    private int countRooms(Long roomTypeId) {
        Long count = roomMapper.selectCountByRoomTypeId(roomTypeId);
        return count == null ? 0 : count.intValue();
    }

    /**
     * 线上订单没有指定具体房间：优先给一间空房，其次给该房型任意房间
     */
    private Long pickRoomId(Long roomTypeId) {
        Long fallback = null;
        for (RoomDO room : roomMapper.selectListAll()) {
            if (!Objects.equals(room.getRoomTypeId(), roomTypeId)) {
                continue;
            }
            if (fallback == null) {
                fallback = room.getId();
            }
            if (Objects.equals(room.getStatus(), 10)) {
                return room.getId();
            }
        }
        if (fallback == null) {
            log.warn("[pickRoomId][房型 {} 下没有房间，订单 room_id 置 0]", roomTypeId);
            return 0L;
        }
        return fallback;
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw exception(ORDER_DATE_INVALID);
        }
    }

    private String generateOrderNo() {
        return "HT" + LocalDateTime.now().format(ORDER_NO_FORMAT)
                + String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    }
}
