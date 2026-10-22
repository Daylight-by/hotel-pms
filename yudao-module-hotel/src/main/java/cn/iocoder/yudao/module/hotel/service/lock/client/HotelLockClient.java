package cn.iocoder.yudao.module.hotel.service.lock.client;

import java.time.LocalDateTime;

/**
 * 门锁系统客户端。
 *
 * <p>本机没有真实门锁网关，默认使用 {@link MockLockClient}；
 * 真实厂商适配继承 {@link AbstractLockClient} 实现 HTTP 协议。</p>
 */
public interface HotelLockClient {

    String name();

    LockResult issueCard(Long orderId, String roomNo, LocalDateTime validTo);

    LockResult reissueCard(Long cardLogId);

    LockResult cancelCard(Long orderId);

    LockResult extendCard(Long orderId, LocalDateTime newValidTo);

}
