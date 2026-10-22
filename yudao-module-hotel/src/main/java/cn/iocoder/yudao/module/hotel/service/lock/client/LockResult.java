package cn.iocoder.yudao.module.hotel.service.lock.client;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 门锁操作结果
 */
@Data
@AllArgsConstructor
public class LockResult {

    private boolean success;
    private String cardNo;
    private String message;

    public static LockResult ok(String cardNo) {
        return new LockResult(true, cardNo, "操作成功");
    }

    public static LockResult fail(String message) {
        return new LockResult(false, null, message);
    }

}
