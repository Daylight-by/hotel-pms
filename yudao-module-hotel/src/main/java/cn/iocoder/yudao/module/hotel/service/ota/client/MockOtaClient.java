package cn.iocoder.yudao.module.hotel.service.ota.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * OTA 模拟器：生成固定假订单，保证无携程/美团账号也能跑通全链路。
 */
@Component
@Slf4j
public class MockOtaClient implements OtaClient {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Override
    public String otaCode() {
        return "MOCK";
    }

    @Override
    public OtaPushResult pushRate(Long roomTypeId, LocalDate begin, LocalDate end) {
        log.info("[pushRate][Mock 推送房价成功 roomTypeId={}, {} ~ {}]", roomTypeId, begin, end);
        return OtaPushResult.ok("Mock 房价库存推送成功");
    }

    @Override
    public OtaPushResult pushInventory(Long roomTypeId, LocalDate begin, LocalDate end) {
        return OtaPushResult.ok("Mock 库存推送成功");
    }

    @Override
    public List<OtaOrderDTO> pullOrders(LocalDateTime since) {
        LocalDate today = LocalDate.now();
        String orderNo = "MOCK-" + today.format(DAY) + "-001";
        OtaOrderDTO dto = new OtaOrderDTO("MOCK", orderNo,
                today.plusDays(1), today.plusDays(3), new BigDecimal("599.00"),
                "OTA模拟客", "13900008888",
                "{\"mock\":true,\"source\":\"MockOtaClient\",\"orderNo\":\"" + orderNo + "\"}");
        return List.of(dto);
    }

    @Override
    public boolean confirmOrder(String otaOrderNo) {
        return true;
    }

    @Override
    public boolean rejectOrder(String otaOrderNo, String reason) {
        return true;
    }

}
