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

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 酒店会员 DO
 */
@TableName("hotel_member")
@KeySequence("hotel_member_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 门店编号（0 表示未分配） */
    private Long storeId;

    /** 会员卡号 */
    private String memberNo;

    /** 关联住客档案编号 */
    private Long guestId;

    /** 姓名 */
    private String name;

    /** 手机号（登录账号） */
    private String mobile;

    /** 密码（BCrypt，可为空=免密/验证码登录） */
    private String password;

    /** 等级，见 MemberLevelEnum */
    private Integer level;

    /** 积分 */
    private Integer points;

    /** 储值余额 */
    private BigDecimal balance;

    /** 累计间夜 */
    private Integer totalNights;

    /** 累计消费 */
    private BigDecimal totalAmount;

    /** 状态：0 正常 1 冻结 */
    private Integer status;

    /** 注册时间 */
    private LocalDateTime registerTime;
}
