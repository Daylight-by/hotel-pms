package cn.iocoder.yudao.module.hotel.dal.dataobject.audit;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 营业日 DO（每个租户只有一条）
 */
@TableName("hotel_business_date")
@KeySequence("hotel_business_date_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessDateDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 门店编号（0 表示未分配） */
    private Long storeId;

    /** 当前营业日 */
    private LocalDate bizDate;

    /** 状态，见 BusinessDateStatusEnum */
    private Integer status;

    /** 夜审完成时间 */
    private LocalDateTime auditTime;

    /** 操作人 */
    private Long operatorId;

    /** 备注 */
    private String remark;
}
