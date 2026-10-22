package cn.iocoder.yudao.module.hotel.dal.dataobject.psb;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 旅馆业治安上传日志 DO
 */
@TableName("hotel_psb_upload_log")
@KeySequence("hotel_psb_upload_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PsbUploadLogDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long storeId;
    private Long orderId;
    private Long guestId;
    private String idCard;
    private String guestName;
    private String roomNo;
    /** 10 入住登记 20 退房注销 30 换房 40 信息更正 */
    private Integer actionType;
    private LocalDateTime bizTime;
    /** 10 待上传 20 成功 30 失败 40 无需上传 */
    private Integer status;
    private Integer retryCount;
    private String requestData;
    private String responseData;
    private String failReason;
    private LocalDateTime uploadTime;

}
