package cn.iocoder.yudao.module.hotel.dal.dataobject.member;

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
 * 会员积分流水 DO
 */
@TableName("hotel_member_points_log")
@KeySequence("hotel_member_points_log_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberPointsLogDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 门店编号（0 表示未分配） */
    private Long storeId;

    /** 会员编号 */
    private Long memberId;

    /** 类型，见 PointsTypeEnum */
    private Integer type;

    /** 积分（正为增加，负为扣减） */
    private Integer points;

    /** 变动后积分 */
    private Integer balance;

    /** 关联订单 */
    private Long orderId;

    /** 备注 */
    private String remark;
}
