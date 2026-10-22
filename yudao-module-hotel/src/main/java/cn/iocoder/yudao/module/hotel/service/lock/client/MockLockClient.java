package cn.iocoder.yudao.module.hotel.service.lock.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 门锁模拟器：不接真实硬件，返回模拟房卡号。
 */
@Component
@Slf4j
public class MockLockClient implements HotelLockClient {

    @Override
    public String name() {
        return "mock-lock";
    }

    @Override
    public LockResult issueCard(Long orderId, String roomNo, LocalDateTime validTo) {
        return ok("CARD-MOCK-" + orderId + "-" + random4());
    }

    @Override
    public LockResult reissueCard(Long cardLogId) {
        return ok("CARD-MOCK-R" + cardLogId + "-" + random4());
    }

    @Override
    public LockResult cancelCard(Long orderId) {
        return LockResult.ok("CANCEL-MOCK-" + orderId);
    }

    @Override
    public LockResult extendCard(Long orderId, LocalDateTime newValidTo) {
        return LockResult.ok("CARD-MOCK-E" + orderId + "-" + random4());
    }

    private LockResult ok(String cardNo) {
        log.info("[ok][Mock 门锁操作成功 cardNo={}]", cardNo);
        return LockResult.ok(cardNo);
    }

    private String random4() {
        return String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }

}
