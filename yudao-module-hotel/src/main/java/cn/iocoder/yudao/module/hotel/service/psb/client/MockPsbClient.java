package cn.iocoder.yudao.module.hotel.service.psb.client;

import cn.iocoder.yudao.module.hotel.dal.dataobject.psb.PsbUploadLogDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 公安上传模拟器：不接真实公安系统，返回模拟回执号。
 */
@Component
@Slf4j
public class MockPsbClient implements PsbClient {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public String name() {
        return "mock-psb";
    }

    @Override
    public PsbUploadResult uploadCheckIn(PsbUploadLogDO data) {
        return success(data);
    }

    @Override
    public PsbUploadResult uploadCheckOut(PsbUploadLogDO data) {
        return success(data);
    }

    @Override
    public PsbUploadResult uploadChange(PsbUploadLogDO data) {
        return success(data);
    }

    private PsbUploadResult success(PsbUploadLogDO data) {
        String receiptNo = "PSB-MOCK-" + LocalDateTime.now().format(FORMAT)
                + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
        log.info("[success][Mock 公安上传成功 guest={}, action={}, receipt={}]",
                data.getGuestName(), data.getActionType(), receiptNo);
        return PsbUploadResult.ok(receiptNo);
    }

}
