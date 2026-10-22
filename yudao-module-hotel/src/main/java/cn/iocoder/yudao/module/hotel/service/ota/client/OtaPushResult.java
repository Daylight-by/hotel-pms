package cn.iocoder.yudao.module.hotel.service.ota.client;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * OTA 推送结果
 */
@Data
@AllArgsConstructor
public class OtaPushResult {

    private boolean success;
    private String message;

    public static OtaPushResult ok(String message) {
        return new OtaPushResult(true, message);
    }

    public static OtaPushResult fail(String message) {
        return new OtaPushResult(false, message);
    }

}
