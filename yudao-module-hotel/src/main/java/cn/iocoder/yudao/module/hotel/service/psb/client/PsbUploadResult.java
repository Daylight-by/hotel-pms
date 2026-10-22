package cn.iocoder.yudao.module.hotel.service.psb.client;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 公安上传结果
 */
@Data
@AllArgsConstructor
public class PsbUploadResult {

    private boolean success;
    private String receiptNo;
    private String message;

    public static PsbUploadResult ok(String receiptNo) {
        return new PsbUploadResult(true, receiptNo, "上传成功");
    }

    public static PsbUploadResult fail(String message) {
        return new PsbUploadResult(false, null, message);
    }

}
