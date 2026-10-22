package cn.iocoder.yudao.module.hotel.dal.dataobject.cleaning;

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
 * 清洁任务 DO
 */
@TableName("hotel_cleaning_task")
@KeySequence("hotel_cleaning_task_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CleaningTaskDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 门店编号（0 表示未分配） */
    private Long storeId;

    /** 房间编号 */
    private Long roomId;

    /** 任务类型，见 CleaningTypeEnum */
    private Integer type;

    /** 任务状态，见 CleaningTaskStatusEnum */
    private Integer status;

    /** 指派的保洁人员（system_users.id） */
    private Long assigneeId;

    /** 计划日期 */
    private LocalDate planDate;

    /** 完成时间 */
    private LocalDateTime finishTime;

    /** 备注 */
    private String remark;
}
