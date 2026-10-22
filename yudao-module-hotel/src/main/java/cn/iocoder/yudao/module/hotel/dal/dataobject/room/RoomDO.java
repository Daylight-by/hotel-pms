package cn.iocoder.yudao.module.hotel.dal.dataobject.room;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 房间 DO
 */
@TableName("hotel_room")
@KeySequence("hotel_room_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 门店编号（0 表示未分配） */
    private Long storeId;

    /** 房间号，如 301 */
    private String roomNo;

    /** 房型编号 */
    private Long roomTypeId;

    /** 楼层 */
    private Integer floor;

    /** 面积 */
    private Integer area;

    /** 实际售价，可覆盖房型基础价 */
    private BigDecimal price;

    /** 房态，见 RoomStatusEnum */
    private Integer status;

    /** 锁定状态，见 RoomLockStatusEnum：0 正常 1 维修锁定 2 预留锁定 */
    private Integer lockStatus;

    /** 排序 */
    private Integer sort;

    /** 备注 */
    private String remark;
}
