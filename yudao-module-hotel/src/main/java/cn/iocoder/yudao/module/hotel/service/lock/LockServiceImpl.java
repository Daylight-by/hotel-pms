package cn.iocoder.yudao.module.hotel.service.lock;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.lock.vo.LockPageReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.lock.LockCardLogDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.order.OrderDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.room.RoomDO;
import cn.iocoder.yudao.module.hotel.dal.mysql.lock.LockCardLogMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.order.OrderMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.room.RoomMapper;
import cn.iocoder.yudao.module.hotel.framework.store.StoreContext;
import cn.iocoder.yudao.module.hotel.service.lock.client.HotelLockClient;
import cn.iocoder.yudao.module.hotel.service.lock.client.LockResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hotel.enums.ErrorCodeConstants.ORDER_NOT_EXISTS;
import static cn.iocoder.yudao.module.hotel.enums.T5ErrorCodeConstants.ERROR_LOCK_LOG_NOT_EXIST;

/**
 * 门锁 Service（Mock 全链路，失败不阻断主流程）。
 */
@Service
@Slf4j
public class LockServiceImpl {

    @Resource
    private LockCardLogMapper lockCardLogMapper;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private RoomMapper roomMapper;
    @Resource
    private HotelLockClient hotelLockClient;

    /** 入住发卡 */
    public Long issueForOrder(OrderDO order) {
        return doIssue(order, 10);
    }

    /** 手工发卡 */
    public Long issueByOrderId(Long orderId) {
        return doIssue(validateOrder(orderId), 10);
    }

    /** 补卡 */
    public Long reissue(Long cardLogId) {
        LockCardLogDO source = lockCardLogMapper.selectById(cardLogId);
        if (source == null) {
            throw exception(ERROR_LOCK_LOG_NOT_EXIST);
        }
        LockCardLogDO logDO = new LockCardLogDO();
        logDO.setStoreId(source.getStoreId());
        logDO.setOrderId(source.getOrderId());
        logDO.setRoomId(source.getRoomId());
        logDO.setRoomNo(source.getRoomNo());
        logDO.setActionType(20);
        logDO.setValidFrom(LocalDateTime.now());
        logDO.setValidTo(source.getValidTo());
        safeCall(() -> hotelLockClient.reissueCard(cardLogId), logDO);
        return save(logDO);
    }

    /** 退房销卡 */
    public Long cancelForOrder(OrderDO order) {
        LockCardLogDO logDO = new LockCardLogDO();
        logDO.setStoreId(resolveStoreId(order));
        logDO.setOrderId(order.getId());
        logDO.setRoomId(order.getRoomId());
        RoomDO room = roomMapper.selectById(order.getRoomId());
        logDO.setRoomNo(room == null ? null : room.getRoomNo());
        logDO.setActionType(30);
        logDO.setValidFrom(LocalDateTime.now());
        safeCall(() -> hotelLockClient.cancelCard(order.getId()), logDO);
        return save(logDO);
    }

    /** 手工销卡 */
    public Long cancelByOrderId(Long orderId) {
        return cancelForOrder(validateOrder(orderId));
    }

    /** 续住续卡 */
    public Long extendForOrder(OrderDO order, LocalDate newCheckOutDate) {
        LockCardLogDO logDO = new LockCardLogDO();
        logDO.setStoreId(resolveStoreId(order));
        logDO.setOrderId(order.getId());
        logDO.setRoomId(order.getRoomId());
        RoomDO room = roomMapper.selectById(order.getRoomId());
        logDO.setRoomNo(room == null ? null : room.getRoomNo());
        logDO.setActionType(40);
        logDO.setValidFrom(LocalDateTime.now());
        LocalDateTime validTo = LocalDateTime.of(
                newCheckOutDate == null ? LocalDate.now().plusDays(1) : newCheckOutDate, LocalTime.NOON);
        logDO.setValidTo(validTo);
        safeCall(() -> hotelLockClient.extendCard(order.getId(), validTo), logDO);
        return save(logDO);
    }

    /** 手工续卡 */
    public Long extendByOrderId(Long orderId, LocalDate newCheckOutDate) {
        return extendForOrder(validateOrder(orderId), newCheckOutDate);
    }

    private Long doIssue(OrderDO order, int actionType) {
        RoomDO room = roomMapper.selectById(order.getRoomId());
        LockCardLogDO logDO = new LockCardLogDO();
        logDO.setStoreId(resolveStoreId(order));
        logDO.setOrderId(order.getId());
        logDO.setRoomId(order.getRoomId());
        logDO.setRoomNo(room == null ? null : room.getRoomNo());
        logDO.setActionType(actionType);
        logDO.setValidFrom(LocalDateTime.now());
        LocalDate checkOut = order.getCheckOutDate() == null ? LocalDate.now().plusDays(1) : order.getCheckOutDate();
        LocalDateTime validTo = LocalDateTime.of(checkOut, LocalTime.NOON);
        logDO.setValidTo(validTo);
        safeCall(() -> hotelLockClient.issueCard(order.getId(), logDO.getRoomNo(), validTo), logDO);
        return save(logDO);
    }

    private void safeCall(LockCall call, LockCardLogDO logDO) {
        try {
            LockResult result = call.call();
            if (result.isSuccess()) {
                logDO.setStatus(20);
                logDO.setCardNo(result.getCardNo());
                logDO.setFailReason(null);
            } else {
                logDO.setStatus(30);
                logDO.setFailReason(result.getMessage());
            }
        } catch (Exception ex) {
            // 门锁失败不阻断入住/退房主流程
            log.warn("[safeCall][门锁操作失败 orderId={}]", logDO.getOrderId(), ex);
            logDO.setStatus(30);
            logDO.setFailReason(ex.getMessage());
        }
        logDO.setHandleTime(LocalDateTime.now());
    }

    private Long save(LockCardLogDO logDO) {
        lockCardLogMapper.insert(logDO);
        return logDO.getId();
    }

    public PageResult<LockCardLogDO> getPage(LockPageReqVO reqVO) {
        return lockCardLogMapper.selectPage(reqVO);
    }

    private Long resolveStoreId(OrderDO order) {
        Long contextStoreId = StoreContext.getStoreId();
        if (contextStoreId != null && contextStoreId > 0) {
            return contextStoreId;
        }
        return order.getStoreId() == null ? 0L : order.getStoreId();
    }

    private OrderDO validateOrder(Long orderId) {
        OrderDO order = orderId == null ? null : orderMapper.selectById(orderId);
        if (order == null) {
            throw exception(ORDER_NOT_EXISTS);
        }
        return order;
    }

    @FunctionalInterface
    private interface LockCall {
        LockResult call();
    }

}
