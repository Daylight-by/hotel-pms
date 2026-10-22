package cn.iocoder.yudao.module.hotel.dal.dataobject.operationlog;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 操作审计日志 DO
 *
 * <p>由 {@code @HotelOperationLog} 注解 + AOP 切面织入，记录关键写操作（入账/冲销/改价/夜审/换房/取消订单）的变更前后。</p>
 */
@TableName("hotel_operation_log")
@KeySequence("hotel_operation_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 门店编号（多门店预留） */
    private Long storeId;

    /** 模块：order/folio/rate/audit/... */
    private String module;

    /** 业务单据号 */
    private String bizId;

    /** 操作：如 入账/冲销/改价/夜审 */
    private String operation;

    /** 变更前（JSON） */
    private String beforeData;

    /** 变更后（JSON） */
    private String afterData;

    /** 操作人 */
    private Long operatorId;

    /** 操作人姓名 */
    private String operatorName;

    /** IP */
    private String ip;
}
