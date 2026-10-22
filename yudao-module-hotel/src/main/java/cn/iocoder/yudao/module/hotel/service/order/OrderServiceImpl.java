package cn.iocoder.yudao.module.hotel.service.order;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.datapermission.core.util.DataPermissionUtils;
import cn.iocoder.yudao.module.hotel.controller.admin.folio.vo.FolioPaymentReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.folio.vo.FolioPostReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.folio.vo.FolioRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.order.vo.*;
import cn.iocoder.yudao.module.hotel.dal.dataobject.cleaning.CleaningTaskDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.folio.FolioDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.guest.GuestDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.order.OrderDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.order.RoomChangeLogDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.room.RoomDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.roomtype.RoomTypeDO;
import cn.iocoder.yudao.module.hotel.dal.mysql.cleaning.CleaningTaskMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.folio.FolioMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.guest.GuestMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.order.OrderMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.order.RoomChangeLogMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.room.RoomMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.roomtype.RoomTypeMapper;
import cn.iocoder.yudao.module.hotel.enums.*;
import cn.iocoder.yudao.module.hotel.framework.store.StoreContext;
import cn.iocoder.yudao.module.hotel.service.audit.BusinessDateService;
import cn.iocoder.yudao.module.hotel.service.folio.FolioService;
import cn.iocoder.yudao.module.hotel.service.operationlog.annotation.HotelOperationLog;
import cn.iocoder.yudao.module.hotel.service.lock.LockServiceImpl;
import cn.iocoder.yudao.module.hotel.service.psb.PsbServiceImpl;
import cn.iocoder.yudao.module.hotel.service.rate.RateCalendarService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hotel.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.hotel.enums.T5ErrorCodeConstants.ERROR_STORE_NO_PERMISSION;

/**
 * 订单 Service 实现：包含订单状态机、房态联动、房价日历库存与账页联动
 *
 * <pre>
 * PENDING(待确认) → CONFIRMED(已确认) → CHECKED_IN(已入住) → CHECKED_OUT(已退房)
 *        ↓                ↓
 *    CANCELLED(已取消)  CANCELLED(已取消)
 * </pre>
 */
@Service
@Validated
@Slf4j
public class OrderServiceImpl {

    private static final List<Integer> ACTIVE_STATUSES = List.of(
            OrderStatusEnum.PENDING.getStatus(),
            OrderStatusEnum.CONFIRMED.getStatus(),
            OrderStatusEnum.CHECKED_IN.getStatus());

    private static final DateTimeFormatter ORDER_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Resource
    private OrderMapper orderMapper;
    @Resource
    private RoomMapper roomMapper;
    @Resource
    private RoomTypeMapper roomTypeMapper;
    @Resource
    private GuestMapper guestMapper;
    @Resource
    private CleaningTaskMapper cleaningTaskMapper;
    @Resource
    private RoomChangeLogMapper roomChangeLogMapper;
    @Resource
    private FolioMapper folioMapper;
    @Resource
    private RateCalendarService rateCalendarService;
    @Resource
    private BusinessDateService businessDateService;
    @Resource
    private FolioService folioService;
    @Resource
    private PsbServiceImpl psbService;
    @Resource
    private LockServiceImpl lockService;

    // ============================================================ 预订 / 修改

    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(OrderSaveReqVO reqVO) {
        RoomDO room = validateRoom(reqVO.getRoomId());
        GuestDO guest = guestMapper.selectById(reqVO.getGuestId());
        if (guest == null) {
            throw exception(GUEST_NOT_EXISTS);
        }
        validateDates(reqVO.getCheckInDate(), reqVO.getCheckOutDate());
        validateNoConflict(room.getId(), reqVO.getCheckInDate(), reqVO.getCheckOutDate(), null);
        validateRoomBookable(room);

        Long roomTypeId = room.getRoomTypeId();
        int nights = (int) ChronoUnit.DAYS.between(reqVO.getCheckInDate(), reqVO.getCheckOutDate());
        // 房价日历优先，未维护的日期按房型基础价 / 房间售价兜底
        rateCalendarService.validateAvailable(roomTypeId, reqVO.getCheckInDate(), reqVO.getCheckOutDate(), 1, null);

        OrderDO order = new OrderDO();
        order.setOrderNo(generateOrderNo());
        order.setGuestId(reqVO.getGuestId());
        order.setRoomId(reqVO.getRoomId());
        order.setRoomTypeId(roomTypeId);
        order.setOrderType(OrderTypeEnum.FULL_DAY.getType());
        order.setBusinessDate(businessDateService.getCurrentBusinessDate());
        order.setCheckInDate(reqVO.getCheckInDate());
        order.setCheckOutDate(reqVO.getCheckOutDate());
        order.setCheckInTime(reqVO.getCheckInDate().atTime(14, 0));
        order.setCheckOutTime(reqVO.getCheckOutDate().atTime(12, 0));
        order.setNights(nights);
        order.setAdultCount(reqVO.getAdultCount() == null ? 1 : reqVO.getAdultCount());
        order.setChildCount(reqVO.getChildCount() == null ? 0 : reqVO.getChildCount());
        order.setParentOrderId(reqVO.getParentOrderId());
        order.setAmount(calculateAmount(roomTypeId, reqVO.getCheckInDate(), reqVO.getCheckOutDate(), room.getPrice()));
        order.setDeposit(reqVO.getDeposit() == null ? BigDecimal.ZERO : reqVO.getDeposit());
        order.setPaidAmount(BigDecimal.ZERO);
        // P1 规格：后台手工开单（前台 walk-in / 协议单位）直接落「20 已确认」，跳过待支付环节
        order.setStatus(OrderStatusEnum.CONFIRMED.getStatus());
        order.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());
        order.setInvoiceRequested(false);
        order.setRemark(reqVO.getRemark());
        orderMapper.insert(order);

        occupyInventory(roomTypeId, reqVO.getCheckInDate(), reqVO.getCheckOutDate());
        // 开单即开主账页（幂等），保证后续挂账 / 收款 / 结账链路可用
        folioService.ensureFolioForOrder(order.getId(), order.getGuestId(), order.getRoomId());
        return order.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateOrder(OrderSaveReqVO reqVO) {
        OrderDO order = validateExists(reqVO.getId());
        if (!Objects.equals(order.getStatus(), OrderStatusEnum.PENDING.getStatus())
                && !Objects.equals(order.getStatus(), OrderStatusEnum.CONFIRMED.getStatus())) {
            throw exception(ORDER_STATUS_NOT_ALLOW);
        }
        RoomDO room = validateRoom(reqVO.getRoomId());
        validateDates(reqVO.getCheckInDate(), reqVO.getCheckOutDate());
        validateNoConflict(room.getId(), reqVO.getCheckInDate(), reqVO.getCheckOutDate(), order.getId());

        Long oldRoomTypeId = order.getRoomTypeId() != null ? order.getRoomTypeId() : room.getRoomTypeId();
        // 先释放原库存，再校验并占用新库存
        releaseInventory(oldRoomTypeId, order.getCheckInDate(), order.getCheckOutDate());
        rateCalendarService.validateAvailable(room.getRoomTypeId(),
                reqVO.getCheckInDate(), reqVO.getCheckOutDate(), 1, order.getId());

        int nights = (int) ChronoUnit.DAYS.between(reqVO.getCheckInDate(), reqVO.getCheckOutDate());
        OrderDO update = new OrderDO();
        update.setId(order.getId());
        update.setGuestId(reqVO.getGuestId());
        update.setRoomId(reqVO.getRoomId());
        update.setRoomTypeId(room.getRoomTypeId());
        update.setCheckInDate(reqVO.getCheckInDate());
        update.setCheckOutDate(reqVO.getCheckOutDate());
        update.setCheckInTime(reqVO.getCheckInDate().atTime(14, 0));
        update.setCheckOutTime(reqVO.getCheckOutDate().atTime(12, 0));
        update.setNights(nights);
        update.setAdultCount(reqVO.getAdultCount());
        update.setChildCount(reqVO.getChildCount());
        update.setDeposit(reqVO.getDeposit());
        update.setAmount(calculateAmount(room.getRoomTypeId(), reqVO.getCheckInDate(),
                reqVO.getCheckOutDate(), room.getPrice()));
        update.setRemark(reqVO.getRemark());
        orderMapper.updateById(update);

        occupyInventory(room.getRoomTypeId(), reqVO.getCheckInDate(), reqVO.getCheckOutDate());
    }

    /**
     * 办理入住：PENDING / CONFIRMED → CHECKED_IN，房间置为已入住
     */
    @Transactional(rollbackFor = Exception.class)
    @HotelOperationLog(module = "order", operation = "入住")
    public void checkIn(Long id) {
        OrderDO order = validateExists(id);
        if (!Objects.equals(order.getStatus(), OrderStatusEnum.PENDING.getStatus())
                && !Objects.equals(order.getStatus(), OrderStatusEnum.CONFIRMED.getStatus())) {
            throw exception(ORDER_STATUS_NOT_ALLOW);
        }
        RoomDO room = validateRoom(order.getRoomId());
        if (Objects.equals(room.getStatus(), RoomStatusEnum.MAINTENANCE.getStatus())) {
            throw exception(ROOM_UNDER_MAINTENANCE);
        }
        OrderDO update = new OrderDO();
        update.setId(order.getId());
        update.setStatus(OrderStatusEnum.CHECKED_IN.getStatus());
        update.setActualCheckInTime(LocalDateTime.now());
        if (order.getBusinessDate() == null) {
            update.setBusinessDate(businessDateService.getCurrentBusinessDate());
        }
        if (order.getRoomTypeId() == null) {
            update.setRoomTypeId(room.getRoomTypeId());
        }
        orderMapper.updateById(update);

        RoomDO roomUpdate = new RoomDO();
        roomUpdate.setId(room.getId());
        roomUpdate.setStatus(RoomStatusEnum.OCCUPIED.getStatus());
        roomMapper.updateById(roomUpdate);

        // 入住即开账，保证前台能马上记杂费
        folioService.ensureFolioForOrder(order.getId(), order.getGuestId(), order.getRoomId());

        // T5 合规联动：公安上传 + 门锁发卡（Mock，失败不阻断入住）
        try {
            psbService.logCheckIn(order, room);
        } catch (Exception ex) {
            log.warn("[checkIn][公安上传登记失败 orderId={}]", order.getId(), ex);
        }
        try {
            lockService.issueForOrder(order);
        } catch (Exception ex) {
            log.warn("[checkIn][门锁发卡失败 orderId={}]", order.getId(), ex);
        }
    }

    /**
     * 办理退房：CHECKED_IN → CHECKED_OUT，结算、房间转待清洁、自动生成清洁任务
     */
    @Transactional(rollbackFor = Exception.class)
    @HotelOperationLog(module = "order", operation = "退房")
    public void checkOut(Long id) {
        OrderDO order = validateExists(id);
        if (!Objects.equals(order.getStatus(), OrderStatusEnum.CHECKED_IN.getStatus())) {
            throw exception(ORDER_STATUS_NOT_ALLOW);
        }
        OrderDO update = new OrderDO();
        update.setId(order.getId());
        update.setStatus(OrderStatusEnum.CHECKED_OUT.getStatus());
        update.setActualCheckOutTime(LocalDateTime.now());
        if (!Objects.equals(order.getPaymentStatus(), PaymentStatusEnum.PAID.getStatus())
                && !Objects.equals(order.getPaymentStatus(), PaymentStatusEnum.REFUNDED.getStatus())
                && (!Objects.equals(order.getOrderType(), OrderTypeEnum.HOURLY.getType()))) {
            update.setPaidAmount(order.getAmount());
            update.setPaymentStatus(PaymentStatusEnum.PAID.getStatus());
            if (order.getPaymentMethod() == null) {
                update.setPaymentMethod(PaymentMethodEnum.CASH.getMethod());
            }
        }
        orderMapper.updateById(update);

        RoomDO roomUpdate = new RoomDO();
        roomUpdate.setId(order.getRoomId());
        roomUpdate.setStatus(RoomStatusEnum.CLEANING.getStatus());
        roomMapper.updateById(roomUpdate);

        CleaningTaskDO task = new CleaningTaskDO();
        task.setRoomId(order.getRoomId());
        task.setType(CleaningTypeEnum.CHECKOUT.getType());
        task.setStatus(CleaningTaskStatusEnum.PENDING.getStatus());
        task.setPlanDate(LocalDate.now());
        task.setRemark("订单 " + order.getOrderNo() + " 退房后自动生成");
        cleaningTaskMapper.insert(task);

        // 释放当日及之后的库存（已过去的房晚不再释放）
        if (order.getRoomTypeId() != null && order.getCheckInDate() != null && order.getCheckOutDate() != null) {
            LocalDate from = LocalDate.now().isAfter(order.getCheckInDate()) ? LocalDate.now() : order.getCheckInDate();
            releaseInventory(order.getRoomTypeId(), from, order.getCheckOutDate());
        }

        // T5 合规联动：公安退房注销 + 门锁销卡（Mock，失败不阻断退房）
        try {
            psbService.logCheckOut(order, roomMapper.selectById(order.getRoomId()));
        } catch (Exception ex) {
            log.warn("[checkOut][公安退房注销失败 orderId={}]", order.getId(), ex);
        }
        try {
            lockService.cancelForOrder(order);
        } catch (Exception ex) {
            log.warn("[checkOut][门锁销卡失败 orderId={}]", order.getId(), ex);
        }
    }

    public void cancelOrder(Long id) {
        cancelOrder(id, null, null);
    }

    /**
     * 取消订单：释放日历库存、违约金入账
     */
    @Transactional(rollbackFor = Exception.class)
    @HotelOperationLog(module = "order", operation = "取消订单")
    public void cancelOrder(Long id, String reason, BigDecimal penalty) {
        OrderDO order = validateExists(id);
        if (!Objects.equals(order.getStatus(), OrderStatusEnum.PENDING.getStatus())
                && !Objects.equals(order.getStatus(), OrderStatusEnum.CONFIRMED.getStatus())) {
            throw exception(ORDER_STATUS_NOT_ALLOW);
        }
        if (Objects.equals(order.getPaymentStatus(), PaymentStatusEnum.PAID.getStatus())
                && (penalty == null || penalty.compareTo(BigDecimal.ZERO) <= 0)) {
            // 已收款订单需先退款，除非明确按违约金处理
            throw exception(ORDER_HAS_PAID);
        }
        OrderDO update = new OrderDO();
        update.setId(id);
        update.setStatus(OrderStatusEnum.CANCELLED.getStatus());
        if (reason != null && !reason.isEmpty()) {
            update.setRemark((order.getRemark() == null ? "" : order.getRemark() + "；") + "取消原因：" + reason);
        }
        orderMapper.updateById(update);

        // 释放库存（钟点房不占用房型库存）
        if (!Objects.equals(order.getOrderType(), OrderTypeEnum.HOURLY.getType())
                && order.getRoomTypeId() != null) {
            releaseInventory(order.getRoomTypeId(), order.getCheckInDate(), order.getCheckOutDate());
        }

        if (penalty != null && penalty.compareTo(BigDecimal.ZERO) > 0) {
            Long folioId = folioService.ensureFolioForOrder(order.getId(), order.getGuestId(), order.getRoomId());
            FolioPostReqVO post = new FolioPostReqVO();
            post.setFolioId(folioId);
            post.setItemType(FolioItemTypeEnum.OTHER.getType());
            post.setItemName("取消违约金");
            post.setQuantity(1);
            post.setUnitPrice(penalty);
            post.setAmount(penalty);
            post.setRemark(reason);
            folioService.postCharge(post);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmOrder(Long id) {
        OrderDO order = validateExists(id);
        // 幂等：后台开单已直接落「已确认」，再次调用仅保底补开账页，不报错
        if (Objects.equals(order.getStatus(), OrderStatusEnum.CONFIRMED.getStatus())) {
            folioService.ensureFolioForOrder(order.getId(), order.getGuestId(), order.getRoomId());
            return;
        }
        // P1 新增 11 待确认（线下转账 / 协议单位挂账）也允许确认
        if (!Objects.equals(order.getStatus(), OrderStatusEnum.PENDING.getStatus())
                && !Objects.equals(order.getStatus(), OrderStatusEnum.WAIT_CONFIRM.getStatus())) {
            throw exception(ORDER_STATUS_NOT_ALLOW);
        }
        OrderDO update = new OrderDO();
        update.setId(id);
        update.setStatus(OrderStatusEnum.CONFIRMED.getStatus());
        if (order.getBusinessDate() == null) {
            update.setBusinessDate(businessDateService.getCurrentBusinessDate());
        }
        if (order.getRoomTypeId() == null) {
            RoomDO room = roomMapper.selectById(order.getRoomId());
            update.setRoomTypeId(room == null ? null : room.getRoomTypeId());
        }
        orderMapper.updateById(update);

        // 订单确认即开主账页
        folioService.ensureFolioForOrder(order.getId(), order.getGuestId(), order.getRoomId());
    }

    // ============================================================ P0 订单操作

    /**
     * 续住：仅已入住订单，后延离店日期并占用新增房晚的库存
     */
    @Transactional(rollbackFor = Exception.class)
    public Long extendOrder(OrderExtendReqVO reqVO) {
        OrderDO order = validateExists(reqVO.getId());
        if (!Objects.equals(order.getStatus(), OrderStatusEnum.CHECKED_IN.getStatus())) {
            throw exception(ERROR_ORDER_CANNOT_EXTEND);
        }
        if (!Objects.equals(order.getOrderType(), OrderTypeEnum.FULL_DAY.getType())) {
            throw exception(ERROR_ORDER_CANNOT_EXTEND);
        }
        RoomDO room = validateRoom(order.getRoomId());
        Long roomTypeId = order.getRoomTypeId() != null ? order.getRoomTypeId() : room.getRoomTypeId();
        LocalDate oldCheckOut = order.getCheckOutDate();
        LocalDate newCheckOut = oldCheckOut.plusDays(reqVO.getNights());

        // 新增房晚必须可售，且房间本身不能被其他订单占用
        rateCalendarService.validateAvailable(roomTypeId, oldCheckOut, newCheckOut, 1, order.getId());
        validateNoConflict(room.getId(), order.getCheckInDate(), newCheckOut, order.getId());

        BigDecimal extra = calculateAmount(roomTypeId, oldCheckOut, newCheckOut, room.getPrice());
        int nights = (int) ChronoUnit.DAYS.between(order.getCheckInDate(), newCheckOut);

        OrderDO update = new OrderDO();
        update.setId(order.getId());
        update.setCheckOutDate(newCheckOut);
        update.setCheckOutTime(newCheckOut.atTime(12, 0));
        update.setNights(nights);
        update.setAmount((order.getAmount() == null ? BigDecimal.ZERO : order.getAmount()).add(extra));
        if (reqVO.getRemark() != null) {
            update.setRemark((order.getRemark() == null ? "" : order.getRemark() + "；") + reqVO.getRemark());
        }
        orderMapper.updateById(update);

        occupyInventory(roomTypeId, oldCheckOut, newCheckOut);

        // T5：续住同步续卡（Mock，失败不阻断）
        try {
            lockService.extendForOrder(order, newCheckOut);
        } catch (Exception ex) {
            log.warn("[extendOrder][门锁续卡失败 orderId={}]", order.getId(), ex);
        }
        return order.getId();
    }

    /**
     * 换房：原房置待清洁并生成清洁任务，新房按订单状态置位
     */
    @Transactional(rollbackFor = Exception.class)
    @HotelOperationLog(module = "order", operation = "换房")
    public Long changeRoom(OrderChangeRoomReqVO reqVO) {
        OrderDO order = validateExists(reqVO.getId());
        if (!Objects.equals(order.getStatus(), OrderStatusEnum.CONFIRMED.getStatus())
                && !Objects.equals(order.getStatus(), OrderStatusEnum.CHECKED_IN.getStatus())) {
            throw exception(ERROR_ORDER_CANNOT_CHANGE_ROOM);
        }
        if (Objects.equals(order.getRoomId(), reqVO.getToRoomId())) {
            throw exception(ERROR_ORDER_CANNOT_CHANGE_ROOM);
        }
        RoomDO target = validateRoom(reqVO.getToRoomId());
        if (!Objects.equals(target.getStatus(), RoomStatusEnum.AVAILABLE.getStatus())) {
            throw exception(ERROR_ORDER_CANNOT_CHANGE_ROOM);
        }
        if (target.getLockStatus() != null && target.getLockStatus() != 0) {
            throw exception(ERROR_ORDER_CANNOT_CHANGE_ROOM);
        }
        validateNoConflict(target.getId(), order.getCheckInDate(), order.getCheckOutDate(), order.getId());

        RoomDO oldRoom = roomMapper.selectById(order.getRoomId());

        // 记录换房
        RoomChangeLogDO changeLog = new RoomChangeLogDO();
        changeLog.setOrderId(order.getId());
        changeLog.setFromRoomId(order.getRoomId());
        changeLog.setToRoomId(target.getId());
        changeLog.setChangeTime(LocalDateTime.now());
        changeLog.setReason(reqVO.getReason());
        roomChangeLogMapper.insert(changeLog);

        // 原房置待清洁 + 生成清洁任务
        if (oldRoom != null) {
            RoomDO oldUpdate = new RoomDO();
            oldUpdate.setId(oldRoom.getId());
            oldUpdate.setStatus(RoomStatusEnum.CLEANING.getStatus());
            roomMapper.updateById(oldUpdate);

            CleaningTaskDO task = new CleaningTaskDO();
            task.setRoomId(oldRoom.getId());
            task.setType(CleaningTypeEnum.CHECKOUT.getType());
            task.setStatus(CleaningTaskStatusEnum.PENDING.getStatus());
            task.setPlanDate(LocalDate.now());
            task.setRemark("订单 " + order.getOrderNo() + " 换房后自动生成");
            cleaningTaskMapper.insert(task);
        }

        // 新房按订单状态置位
        RoomDO targetUpdate = new RoomDO();
        targetUpdate.setId(target.getId());
        targetUpdate.setStatus(Objects.equals(order.getStatus(), OrderStatusEnum.CHECKED_IN.getStatus())
                ? RoomStatusEnum.OCCUPIED.getStatus() : RoomStatusEnum.AVAILABLE.getStatus());
        roomMapper.updateById(targetUpdate);

        OrderDO update = new OrderDO();
        update.setId(order.getId());
        update.setRoomId(target.getId());
        update.setRoomTypeId(target.getRoomTypeId());
        orderMapper.updateById(update);

        // 跨房型换房时，把库存从原房型挪到新房型（同房型不需要调整）
        Long oldTypeId = order.getRoomTypeId() != null ? order.getRoomTypeId()
                : (oldRoom == null ? null : oldRoom.getRoomTypeId());
        if (oldTypeId != null && !Objects.equals(oldTypeId, target.getRoomTypeId())) {
            releaseInventory(oldTypeId, order.getCheckInDate(), order.getCheckOutDate());
            try {
                occupyInventory(target.getRoomTypeId(), order.getCheckInDate(), order.getCheckOutDate());
            } catch (Exception ex) {
                // 新房型库存不足时不阻断换房：房间维度的冲突校验已在上面完成
                log.warn("[changeRoom][订单 {} 跨房型占用库存失败：{}]", order.getOrderNo(), ex.getMessage());
            }
        }

        // T5：换房同步公安备案（Mock，失败不阻断）
        try {
            psbService.logChangeRoom(order, target);
        } catch (Exception ex) {
            log.warn("[changeRoom][公安换房备案失败 orderId={}]", order.getId(), ex);
        }
        return order.getId();
    }

    /**
     * 钟点房开单：按时段计价，不占用房型日历库存
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createHourlyOrder(OrderHourlyCreateReqVO reqVO) {
        RoomDO room = validateRoom(reqVO.getRoomId());
        GuestDO guest = guestMapper.selectById(reqVO.getGuestId());
        if (guest == null) {
            throw exception(GUEST_NOT_EXISTS);
        }
        RoomTypeDO roomType = roomTypeMapper.selectById(room.getRoomTypeId());
        if (roomType == null || !Boolean.TRUE.equals(roomType.getHourlyEnabled())
                || roomType.getHourlyPrice() == null) {
            throw exception(ERROR_HOURLY_NOT_ENABLED);
        }
        LocalDateTime checkIn = reqVO.getCheckInTime();
        LocalDateTime checkOut = reqVO.getCheckOutTime();
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)
                || !checkIn.toLocalDate().equals(checkOut.toLocalDate())) {
            throw exception(ORDER_DATE_INVALID);
        }

        OrderDO order = new OrderDO();
        order.setOrderNo(generateOrderNo());
        order.setGuestId(reqVO.getGuestId());
        order.setRoomId(room.getId());
        order.setRoomTypeId(room.getRoomTypeId());
        order.setOrderType(OrderTypeEnum.HOURLY.getType());
        order.setBusinessDate(businessDateService.getCurrentBusinessDate());
        order.setCheckInDate(checkIn.toLocalDate());
        order.setCheckOutDate(checkOut.toLocalDate());
        order.setCheckInTime(checkIn);
        order.setCheckOutTime(checkOut);
        order.setNights(0);
        order.setAdultCount(reqVO.getAdultCount() == null ? 1 : reqVO.getAdultCount());
        order.setChildCount(reqVO.getChildCount() == null ? 0 : reqVO.getChildCount());
        order.setAmount(roomType.getHourlyPrice());
        order.setDeposit(reqVO.getDeposit() == null ? BigDecimal.ZERO : reqVO.getDeposit());
        order.setPaidAmount(BigDecimal.ZERO);
        order.setStatus(OrderStatusEnum.PENDING.getStatus());
        order.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());
        order.setInvoiceRequested(false);
        order.setRemark(reqVO.getRemark());
        orderMapper.insert(order);
        return order.getId();
    }

    /**
     * 提前退房：按实际夜数重算房费，差额退到账页
     */
    @Transactional(rollbackFor = Exception.class)
    public Long earlyCheckout(Long id) {
        OrderDO order = validateExists(id);
        if (!Objects.equals(order.getStatus(), OrderStatusEnum.CHECKED_IN.getStatus())) {
            throw exception(ORDER_STATUS_NOT_ALLOW);
        }
        RoomDO room = validateRoom(order.getRoomId());
        Long roomTypeId = order.getRoomTypeId() != null ? order.getRoomTypeId() : room.getRoomTypeId();
        LocalDate today = LocalDate.now();
        long actualNights = Math.max(1L, ChronoUnit.DAYS.between(order.getCheckInDate(), today));
        if (order.getCheckOutDate() != null
                && !today.isBefore(order.getCheckOutDate())) {
            actualNights = Math.max(1L, ChronoUnit.DAYS.between(order.getCheckInDate(), order.getCheckOutDate()));
        }
        LocalDate actualCheckOut = order.getCheckInDate().plusDays(actualNights);
        BigDecimal newAmount = calculateAmount(roomTypeId, order.getCheckInDate(), actualCheckOut, room.getPrice());
        BigDecimal oldAmount = order.getAmount() == null ? BigDecimal.ZERO : order.getAmount();
        BigDecimal diff = oldAmount.subtract(newAmount);

        OrderDO update = new OrderDO();
        update.setId(order.getId());
        update.setCheckOutDate(actualCheckOut);
        update.setCheckOutTime(LocalDateTime.now());
        update.setNights((int) actualNights);
        update.setAmount(newAmount);
        update.setStatus(OrderStatusEnum.CHECKED_OUT.getStatus());
        update.setActualCheckOutTime(LocalDateTime.now());
        orderMapper.updateById(update);

        if (diff.compareTo(BigDecimal.ZERO) > 0) {
            Long folioId = folioService.ensureFolioForOrder(order.getId(), order.getGuestId(), order.getRoomId());
            FolioPaymentReqVO refund = new FolioPaymentReqVO();
            refund.setFolioId(folioId);
            refund.setAmount(diff.negate());
            refund.setPaymentMethod(order.getPaymentMethod() == null
                    ? PaymentMethodEnum.CASH.getMethod() : order.getPaymentMethod());
            refund.setRemark("提前退房差额退款");
            folioService.postPayment(refund);
        }

        RoomDO roomUpdate = new RoomDO();
        roomUpdate.setId(order.getRoomId());
        roomUpdate.setStatus(RoomStatusEnum.CLEANING.getStatus());
        roomMapper.updateById(roomUpdate);

        CleaningTaskDO task = new CleaningTaskDO();
        task.setRoomId(order.getRoomId());
        task.setType(CleaningTypeEnum.CHECKOUT.getType());
        task.setStatus(CleaningTaskStatusEnum.PENDING.getStatus());
        task.setPlanDate(today);
        task.setRemark("订单 " + order.getOrderNo() + " 提前退房后自动生成");
        cleaningTaskMapper.insert(task);

        releaseInventory(roomTypeId, actualCheckOut, order.getCheckOutDate());
        return order.getId();
    }

    /**
     * 订单详情：订单 + 账页 + 明细 + 换房记录
     */
    public OrderDetailRespVO getOrderDetail(Long id) {
        OrderDO order = validateExists(id);
        OrderDetailRespVO detail = new OrderDetailRespVO();
        detail.setOrder(buildVOList(List.of(order)).get(0));

        List<FolioDO> folios = folioMapper.selectListByOrderId(id);
        List<FolioRespVO> folioVOs = new ArrayList<>();
        for (FolioDO folio : folios) {
            folioVOs.add(folioService.getFolio(folio.getId()));
        }
        detail.setFolios(folioVOs);
        detail.setFolio(folioVOs.isEmpty() ? null : folioVOs.get(0));

        List<RoomChangeLogDO> logs = roomChangeLogMapper.selectListByOrderId(id);
        List<RoomChangeLogRespVO> changes = new ArrayList<>();
        if (!logs.isEmpty()) {
            Set<Long> roomIds = new HashSet<>();
            logs.forEach(l -> {
                roomIds.add(l.getFromRoomId());
                roomIds.add(l.getToRoomId());
            });
            Map<Long, RoomDO> roomMap = roomMapper.selectBatchIds(roomIds).stream()
                    .collect(Collectors.toMap(RoomDO::getId, Function.identity(), (a, b) -> a));
            logs.forEach(l -> {
                RoomChangeLogRespVO vo = new RoomChangeLogRespVO();
                vo.setId(l.getId());
                vo.setOrderId(l.getOrderId());
                vo.setFromRoomId(l.getFromRoomId());
                vo.setToRoomId(l.getToRoomId());
                vo.setChangeTime(l.getChangeTime());
                vo.setReason(l.getReason());
                RoomDO from = roomMap.get(l.getFromRoomId());
                RoomDO to = roomMap.get(l.getToRoomId());
                vo.setFromRoomNo(from == null ? null : from.getRoomNo());
                vo.setToRoomNo(to == null ? null : to.getRoomNo());
                changes.add(vo);
            });
        }
        detail.setRoomChanges(changes);
        return detail;
    }

    // ============================================================ 支付 / 退款

    /**
     * 支付 / 退款处理
     */
    public void updatePayment(Long id, OrderPaymentReqVO reqVO) {
        OrderDO order = validateExists(id);
        if (Objects.equals(order.getStatus(), OrderStatusEnum.CANCELLED.getStatus())) {
            throw exception(ORDER_STATUS_NOT_ALLOW);
        }
        Integer paymentStatus = reqVO.getPaymentStatus();
        BigDecimal paid = reqVO.getAmount() != null ? reqVO.getAmount() : order.getAmount();

        OrderDO update = new OrderDO();
        update.setId(id);
        update.setPaymentStatus(paymentStatus);
        if (reqVO.getPaymentMethod() != null) {
            update.setPaymentMethod(reqVO.getPaymentMethod());
        }
        if (reqVO.getInvoiceNo() != null) {
            update.setInvoiceNo(reqVO.getInvoiceNo());
        }
        if (reqVO.getInvoiceRequested() != null) {
            update.setInvoiceRequested(reqVO.getInvoiceRequested());
        }
        if (Objects.equals(paymentStatus, PaymentStatusEnum.PAID.getStatus())) {
            update.setPaidAmount(paid.max(order.getPaidAmount() == null ? BigDecimal.ZERO : order.getPaidAmount()));
        } else if (Objects.equals(paymentStatus, PaymentStatusEnum.PARTIAL.getStatus())) {
            update.setPaidAmount(paid.min(order.getAmount()));
        } else if (Objects.equals(paymentStatus, PaymentStatusEnum.UNPAID.getStatus())
                || Objects.equals(paymentStatus, PaymentStatusEnum.REFUNDED.getStatus())) {
            update.setPaidAmount(BigDecimal.ZERO);
        }
        orderMapper.updateById(update);
    }

    @Transactional(rollbackFor = Exception.class)
    public void refund(Long id, OrderPaymentReqVO reqVO) {
        OrderDO order = validateExists(id);
        if (!Objects.equals(order.getPaymentStatus(), PaymentStatusEnum.PAID.getStatus())
                && !Objects.equals(order.getPaymentStatus(), PaymentStatusEnum.PARTIAL.getStatus())) {
            throw exception(ORDER_NOT_PAID);
        }
        BigDecimal paidAmount = order.getPaidAmount() == null ? BigDecimal.ZERO : order.getPaidAmount();
        BigDecimal refund = reqVO.getAmount() == null ? paidAmount : reqVO.getAmount();
        if (refund.compareTo(paidAmount) > 0) {
            throw exception(ORDER_REFUND_EXCEED);
        }
        BigDecimal remain = paidAmount.subtract(refund);

        OrderDO update = new OrderDO();
        update.setId(id);
        update.setPaidAmount(remain);
        update.setPaymentStatus(remain.compareTo(BigDecimal.ZERO) == 0
                ? PaymentStatusEnum.REFUNDED.getStatus() : PaymentStatusEnum.PARTIAL.getStatus());
        String reason = reqVO.getReason() == null ? "" : "（" + reqVO.getReason() + "）";
        update.setRemark((order.getRemark() == null ? "" : order.getRemark() + "；") + "退款 " + refund + reason);
        orderMapper.updateById(update);

        // 已入住/已确认的订单退款后取消，并释放房间
        if (Objects.equals(order.getStatus(), OrderStatusEnum.CHECKED_IN.getStatus())
                || Objects.equals(order.getStatus(), OrderStatusEnum.CONFIRMED.getStatus())) {
            OrderDO cancel = new OrderDO();
            cancel.setId(id);
            cancel.setStatus(OrderStatusEnum.CANCELLED.getStatus());
            orderMapper.updateById(cancel);

            RoomDO roomUpdate = new RoomDO();
            roomUpdate.setId(order.getRoomId());
            roomUpdate.setStatus(RoomStatusEnum.AVAILABLE.getStatus());
            roomMapper.updateById(roomUpdate);

            if (order.getRoomTypeId() != null) {
                releaseInventory(order.getRoomTypeId(), order.getCheckInDate(), order.getCheckOutDate());
            }
        }
    }

    public void deleteOrder(Long id) {
        OrderDO order = validateExists(id);
        if (!Objects.equals(order.getStatus(), OrderStatusEnum.CANCELLED.getStatus())) {
            throw exception(ORDER_DELETE_NOT_ALLOW);
        }
        orderMapper.deleteById(id);
    }

    // ============================================================ 查询

    public OrderDO getOrder(Long id) {
        return orderMapper.selectById(id);
    }

    public PageResult<OrderDO> getOrderPage(OrderPageReqVO reqVO) {
        return orderMapper.selectPage(reqVO);
    }

    public PageResult<OrderRespVO> getOrderVOPage(OrderPageReqVO reqVO) {
        PageResult<OrderDO> page = orderMapper.selectPage(reqVO);
        return new PageResult<>(buildVOList(page.getList()), page.getTotal());
    }

    public OrderRespVO getOrderVO(Long id) {
        OrderDO order = validateExists(id);
        return buildVOList(List.of(order)).get(0);
    }

    public List<OrderRespVO> getTodayCheckInVOs() {
        return buildVOList(orderMapper.selectListByCheckInDate(LocalDate.now(),
                List.of(OrderStatusEnum.CHECKED_IN.getStatus(), OrderStatusEnum.CONFIRMED.getStatus())));
    }

    public List<OrderRespVO> getDueCheckOutVOs() {
        return buildVOList(orderMapper.selectListByCheckOutDate(LocalDate.now(), OrderStatusEnum.CHECKED_IN.getStatus()));
    }

    public List<OrderRespVO> getRecentOrders(int limit) {
        List<OrderDO> orders = orderMapper.selectListByStatus(ACTIVE_STATUSES);
        return buildVOList(orders.stream().limit(limit).toList());
    }

    public List<OrderDO> getActiveOrders() {
        return orderMapper.selectListByStatus(ACTIVE_STATUSES);
    }

    public List<OrderDO> getTodayCheckIns() {
        return orderMapper.selectListByCheckInDate(LocalDate.now(),
                List.of(OrderStatusEnum.CHECKED_IN.getStatus(), OrderStatusEnum.CONFIRMED.getStatus()));
    }

    public List<OrderDO> getDueCheckOuts() {
        return orderMapper.selectListByCheckOutDate(LocalDate.now(), OrderStatusEnum.CHECKED_IN.getStatus());
    }

    public List<OrderDO> getCheckedOutBetween(LocalDateTime start, LocalDateTime end) {
        return orderMapper.selectListByCheckOutTimeBetween(start, end);
    }

    /**
     * 房间在未来一段时间的占用情况：roomId → 占用的订单列表
     */
    public Map<Long, List<OrderDO>> getCalendarData(LocalDate from, LocalDate to) {
        List<OrderDO> orders = orderMapper.selectList(new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<OrderDO>()
                .lt(OrderDO::getCheckInDate, to)
                .gt(OrderDO::getCheckOutDate, from)
                .in(OrderDO::getStatus, ACTIVE_STATUSES));
        return orders.stream().collect(Collectors.groupingBy(OrderDO::getRoomId));
    }

    // ============================================================ 私有方法

    /**
     * 按房价日历计算区间房费；未维护的日期取房型基础价 / 房间售价兜底
     */
    private BigDecimal calculateAmount(Long roomTypeId, LocalDate checkIn, LocalDate checkOut, BigDecimal fallback) {
        BigDecimal total = BigDecimal.ZERO;
        for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
            total = total.add(rateCalendarService.getEffectivePrice(roomTypeId, date, fallback));
        }
        return total;
    }

    private void occupyInventory(Long roomTypeId, LocalDate checkIn, LocalDate checkOut) {
        if (roomTypeId == null || checkIn == null || checkOut == null) {
            return;
        }
        for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
            rateCalendarService.occupy(roomTypeId, date, 1);
        }
    }

    private void releaseInventory(Long roomTypeId, LocalDate checkIn, LocalDate checkOut) {
        if (roomTypeId == null || checkIn == null || checkOut == null) {
            return;
        }
        for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
            rateCalendarService.release(roomTypeId, date, 1);
        }
    }

    private List<OrderRespVO> buildVOList(List<OrderDO> orders) {
        if (orders == null || orders.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> roomIds = orders.stream().map(OrderDO::getRoomId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, RoomDO> roomMap = roomIds.isEmpty() ? Collections.emptyMap()
                : roomMapper.selectBatchIds(roomIds).stream()
                .collect(Collectors.toMap(RoomDO::getId, Function.identity(), (a, b) -> a));
        Set<Long> typeIds = new HashSet<>();
        roomMap.values().forEach(room -> {
            if (room.getRoomTypeId() != null) {
                typeIds.add(room.getRoomTypeId());
            }
        });
        orders.stream().map(OrderDO::getRoomTypeId).filter(Objects::nonNull).forEach(typeIds::add);
        Map<Long, RoomTypeDO> typeMap = typeIds.isEmpty() ? Collections.emptyMap()
                : roomTypeMapper.selectBatchIds(typeIds).stream()
                .collect(Collectors.toMap(RoomTypeDO::getId, Function.identity(), (a, b) -> a));
        Set<Long> guestIds = orders.stream().map(OrderDO::getGuestId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, GuestDO> guestMap = guestIds.isEmpty() ? Collections.emptyMap()
                : guestMapper.selectBatchIds(guestIds).stream()
                .collect(Collectors.toMap(GuestDO::getId, Function.identity(), (a, b) -> a));

        return orders.stream().map(order -> {
            OrderRespVO vo = new OrderRespVO();
            org.springframework.beans.BeanUtils.copyProperties(order, vo);
            RoomDO room = roomMap.get(order.getRoomId());
            if (room != null) {
                vo.setRoomNo(room.getRoomNo());
                if (vo.getRoomTypeId() == null) {
                    vo.setRoomTypeId(room.getRoomTypeId());
                }
            }
            RoomTypeDO type = typeMap.get(vo.getRoomTypeId());
            vo.setRoomTypeName(type == null ? null : type.getName());
            GuestDO guest = guestMap.get(order.getGuestId());
            if (guest != null) {
                vo.setGuestName(guest.getName());
                vo.setGuestMobile(guest.getMobile());
            }
            vo.setStatusName(statusName(order.getStatus()));
            vo.setPaymentStatusName(paymentStatusName(order.getPaymentStatus()));
            vo.setPaymentMethodName(paymentMethodName(order.getPaymentMethod()));
            vo.setOrderTypeName(orderTypeName(order.getOrderType()));
            return vo;
        }).collect(Collectors.toList());
    }

    private String statusName(Integer status) {
        return Arrays.stream(OrderStatusEnum.values()).filter(e -> Objects.equals(e.getStatus(), status))
                .map(OrderStatusEnum::getName).findFirst().orElse("");
    }

    private String paymentStatusName(Integer status) {
        return Arrays.stream(PaymentStatusEnum.values()).filter(e -> Objects.equals(e.getStatus(), status))
                .map(PaymentStatusEnum::getName).findFirst().orElse("");
    }

    private String paymentMethodName(Integer method) {
        if (method == null) {
            return "";
        }
        return Arrays.stream(PaymentMethodEnum.values()).filter(e -> Objects.equals(e.getMethod(), method))
                .map(PaymentMethodEnum::getName).findFirst().orElse("");
    }

    private String orderTypeName(Integer type) {
        return Arrays.stream(OrderTypeEnum.values()).filter(e -> Objects.equals(e.getType(), type))
                .map(OrderTypeEnum::getName).findFirst().orElse("");
    }

    private OrderDO validateExists(Long id) {
        // 先绕过门店过滤取原始单据，再显式判权，跨店操作返回 1003004303
        OrderDO order = id == null ? null
                : DataPermissionUtils.executeIgnore(() -> orderMapper.selectById(id));
        if (order == null) {
            throw exception(ORDER_NOT_EXISTS);
        }
        Long storeId = StoreContext.getStoreId();
        if (storeId != null && storeId > 0 && order.getStoreId() != null && order.getStoreId() > 0
                && !storeId.equals(order.getStoreId())) {
            throw exception(ERROR_STORE_NO_PERMISSION);
        }
        return order;
    }

    private RoomDO validateRoom(Long roomId) {
        RoomDO room = roomId == null ? null : roomMapper.selectById(roomId);
        if (room == null) {
            throw exception(ROOM_NOT_EXISTS);
        }
        return room;
    }

    private void validateRoomBookable(RoomDO room) {
        if (Objects.equals(room.getStatus(), RoomStatusEnum.MAINTENANCE.getStatus())) {
            throw exception(ROOM_UNDER_MAINTENANCE);
        }
        if (room.getLockStatus() != null && room.getLockStatus() != 0) {
            throw exception(ROOM_UNDER_MAINTENANCE);
        }
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw exception(ORDER_DATE_INVALID);
        }
    }

    private void validateNoConflict(Long roomId, LocalDate checkIn, LocalDate checkOut, Long excludeId) {
        Long count = orderMapper.selectConflictCount(roomId, checkIn, checkOut, ACTIVE_STATUSES, excludeId);
        if (count != null && count > 0) {
            throw exception(ORDER_ROOM_CONFLICT);
        }
    }

    private String generateOrderNo() {
        return "HT" + LocalDateTime.now().format(ORDER_NO_FORMAT)
                + String.format("%03d", ThreadLocalRandom.current().nextInt(1000));
    }
}
