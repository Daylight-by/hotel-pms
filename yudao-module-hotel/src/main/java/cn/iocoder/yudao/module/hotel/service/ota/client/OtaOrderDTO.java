package cn.iocoder.yudao.module.hotel.service.ota.client;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * OTA 拉取的订单 DTO
 */
@Data
@AllArgsConstructor
public class OtaOrderDTO {

    private String otaCode;
    private String otaOrderNo;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BigDecimal amount;
    private String guestName;
    private String guestMobile;
    private String rawData;

}
