package cn.iocoder.yudao.module.hotel.service.ota.client;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * OTA 直连客户端。
 *
 * <p>本机不接携程/美团，默认 {@link MockOtaClient}；
 * 真实厂商适配继承 {@link AbstractOtaClient} 实现签名与接口。</p>
 */
public interface OtaClient {

    String otaCode();

    OtaPushResult pushRate(Long roomTypeId, LocalDate begin, LocalDate end);

    OtaPushResult pushInventory(Long roomTypeId, LocalDate begin, LocalDate end);

    List<OtaOrderDTO> pullOrders(LocalDateTime since);

    boolean confirmOrder(String otaOrderNo);

    boolean rejectOrder(String otaOrderNo, String reason);

}
