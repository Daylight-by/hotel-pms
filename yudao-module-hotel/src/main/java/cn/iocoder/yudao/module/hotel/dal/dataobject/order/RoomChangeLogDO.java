package cn.iocoder.yudao.module.hotel.dal.dataobject.order;

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
 * 换房记录 DO
 */
@TableName("hotel_room_change_log")
@KeySequence("hotel_room_change_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomChangeLogDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 门店编号（0 表示未分配） */
    private Long storeId;

    /** 订单编号 */
    private Long orderId;

    /** 原房间编号 */
    private Long fromRoomId;

    /** 新房间编号 */
    private Long toRoomId;

    /** 换房时间 */
    private LocalDateTime changeTime;

    /** 换房原因 */
    private String reason;

    /** 操作人 */
    private Long operatorId;
}
