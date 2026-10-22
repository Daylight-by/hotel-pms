package cn.iocoder.yudao.module.hotel.service.booking;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * T1 定时任务开关
 *
 * <p>项目里没有引入 Quartz（已裁剪），这里在本模块内开启 {@code @Scheduled} 支持，
 * 供 {@link BookingHoldExpireJob} 使用。放在自己的包内，不改动主启动类。</p>
 */
@Configuration
@EnableScheduling
public class BookingSchedulingConfiguration {
}
