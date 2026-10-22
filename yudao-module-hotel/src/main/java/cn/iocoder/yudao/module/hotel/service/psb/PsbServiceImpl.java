package cn.iocoder.yudao.module.hotel.service.psb;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import cn.iocoder.yudao.module.hotel.controller.admin.psb.vo.PsbPageReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.guest.GuestDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.order.OrderDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.psb.PsbUploadLogDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.room.RoomDO;
import cn.iocoder.yudao.module.hotel.dal.mysql.guest.GuestMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.psb.PsbUploadLogMapper;
import cn.iocoder.yudao.module.hotel.framework.store.StoreContext;
import cn.iocoder.yudao.module.hotel.service.psb.client.PsbClient;
import cn.iocoder.yudao.module.hotel.service.psb.client.PsbUploadResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hotel.enums.T5ErrorCodeConstants.ERROR_PSB_UPLOAD_FAILED;

/**
 * 公安上传 Service（Mock 全链路）。
 */
@Service
@Slf4j
public class PsbServiceImpl {

    private static final int MAX_RETRY = 5;

    @Resource
    private PsbUploadLogMapper psbUploadLogMapper;
    @Resource
    private GuestMapper guestMapper;
    @Resource
    private PsbClient psbClient;

    /** 入住登记 */
    public Long logCheckIn(OrderDO order, RoomDO room) {
        return log(order, room, 10);
    }

    /** 退房注销 */
    public Long logCheckOut(OrderDO order, RoomDO room) {
        return log(order, room, 20);
    }

    /** 换房 */
    public Long logChangeRoom(OrderDO order, RoomDO room) {
        return log(order, room, 30);
    }

    private Long log(OrderDO order, RoomDO room, int actionType) {
        if (order == null) {
            return null;
        }
        GuestDO guest = guestMapper.selectById(order.getGuestId());
        if (guest == null) {
            return null;
        }
        PsbUploadLogDO logDO = new PsbUploadLogDO();
        logDO.setStoreId(resolveStoreId(order.getStoreId()));
        logDO.setOrderId(order.getId());
        logDO.setGuestId(guest.getId());
        logDO.setIdCard(guest.getIdCard());
        logDO.setGuestName(guest.getName());
        logDO.setRoomNo(room == null ? null : room.getRoomNo());
        logDO.setActionType(actionType);
        logDO.setBizTime(LocalDateTime.now());
        logDO.setStatus(10);
        logDO.setRetryCount(0);
        logDO.setRequestData("{\"orderId\":" + order.getId() + ",\"guestId\":" + guest.getId()
                + ",\"actionType\":" + actionType + ",\"mock\":true}");
        if (!IdCardUtils.isCreatable(guest.getIdCard())) {
            logDO.setStatus(30);
            logDO.setFailReason("身份证号格式不合法，已转人工核验");
            psbUploadLogMapper.insert(logDO);
            return logDO.getId();
        }
        PsbUploadResult result = upload(logDO, actionType);
        applyResult(logDO, result);
        psbUploadLogMapper.insert(logDO);
        return logDO.getId();
    }

    /** 手动重试 */
    public void retry(Long id) {
        PsbUploadLogDO logDO = psbUploadLogMapper.selectById(id);
        if (logDO == null) {
            throw exception(ERROR_PSB_UPLOAD_FAILED);
        }
        if (logDO.getRetryCount() != null && logDO.getRetryCount() >= MAX_RETRY) {
            throw exception(ERROR_PSB_UPLOAD_FAILED);
        }
        logDO.setRetryCount((logDO.getRetryCount() == null ? 0 : logDO.getRetryCount()) + 1);
        PsbUploadResult result = upload(logDO, logDO.getActionType());
        applyResult(logDO, result);
        psbUploadLogMapper.updateById(logDO);
    }

    /** 每 60 秒自动重试失败记录（最多 5 次） */
    @Scheduled(fixedDelay = 60_000, initialDelay = 30_000)
    public void retryFailed() {
        // 定时任务没有请求上下文，显式指定租户（演示环境租户 1）
        TenantUtils.execute(1L, () -> {
            List<PsbUploadLogDO> list = psbUploadLogMapper.selectRetryable();
            for (PsbUploadLogDO logDO : list) {
                try {
                    retry(logDO.getId());
                } catch (Exception ex) {
                    log.warn("[retryFailed][上传重试失败 id={}]", logDO.getId(), ex);
                }
            }
        });
    }

    public PageResult<PsbUploadLogDO> getPage(PsbPageReqVO reqVO) {
        return psbUploadLogMapper.selectPage(reqVO);
    }

    public Map<String, Object> getStat(Long storeId, LocalDate beginDate, LocalDate endDate) {
        LocalDate begin = beginDate == null ? LocalDate.now() : beginDate;
        LocalDate end = endDate == null ? LocalDate.now() : endDate;
        LocalDateTime beginTime = begin.atStartOfDay();
        LocalDateTime endTime = end.plusDays(1).atStartOfDay();
        Long total = psbUploadLogMapper.selectCountByTime(beginTime, endTime);
        Long success = psbUploadLogMapper.selectCountByStatusAndTime(20, beginTime, endTime);
        Long failed = psbUploadLogMapper.selectCountByStatusAndTime(30, beginTime, endTime);
        Map<String, Object> stat = new LinkedHashMap<>();
        stat.put("total", total == null ? 0 : total);
        stat.put("success", success == null ? 0 : success);
        stat.put("failed", failed == null ? 0 : failed);
        stat.put("successRate", total == null || total == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(success == null ? 0 : success)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP));
        stat.put("storeId", storeId);
        stat.put("beginDate", begin);
        stat.put("endDate", end);
        return stat;
    }

    private PsbUploadResult upload(PsbUploadLogDO logDO, Integer actionType) {
        if (Objects.equals(actionType, 20)) {
            return psbClient.uploadCheckOut(logDO);
        }
        if (Objects.equals(actionType, 30)) {
            return psbClient.uploadChange(logDO);
        }
        return psbClient.uploadCheckIn(logDO);
    }

    private void applyResult(PsbUploadLogDO logDO, PsbUploadResult result) {
        logDO.setUploadTime(LocalDateTime.now());
        if (result.isSuccess()) {
            logDO.setStatus(20);
            logDO.setFailReason("");
            logDO.setResponseData("{\"success\":true,\"receiptNo\":\"" + result.getReceiptNo() + "\"}");
        } else {
            logDO.setStatus(30);
            logDO.setFailReason(result.getMessage());
            logDO.setResponseData("{\"success\":false,\"message\":\"" + result.getMessage() + "\"}");
        }
    }

    private Long resolveStoreId(Long orderStoreId) {
        Long contextStoreId = StoreContext.getStoreId();
        if (contextStoreId != null && contextStoreId > 0) {
            return contextStoreId;
        }
        return orderStoreId == null ? 0L : orderStoreId;
    }

}
