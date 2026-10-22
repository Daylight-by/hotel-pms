package cn.iocoder.yudao.module.hotel.service.booking;

import cn.iocoder.yudao.framework.tenant.core.util.TenantUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 预订占位过期释放任务（T1）
 *
 * <p>每 5 分钟把 {@code expire_time <= now AND status = 10} 的占位置为「40 已过期」，
 * 让被预占但未支付的房量重新回到可售池。</p>
 *
 * <p>定时任务没有 Web 请求上下文，拿不到租户编号，必须用
 * {@link TenantUtils#executeIgnore} 做系统级扫描，否则 MyBatis 租户拦截器会抛
 * 「TenantContextHolder 不存在租户编号」。</p>
 */
@Component
@Slf4j
public class BookingHoldExpireJob {

    @Resource
    private BookingService bookingService;

    @Scheduled(cron = "0 */5 * * * ?")
    public void expireHolds() {
        try {
            Integer count = TenantUtils.executeIgnore(bookingService::expireHolds);
            if (count != null && count > 0) {
                log.info("[expireHolds][释放过期占位 {} 条]", count);
            }
        } catch (Exception ex) {
            log.error("[expireHolds][释放占位失败]", ex);
        }
    }
}
