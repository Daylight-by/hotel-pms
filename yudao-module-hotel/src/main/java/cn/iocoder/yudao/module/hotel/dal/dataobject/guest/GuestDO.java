package cn.iocoder.yudao.module.hotel.dal.dataobject.guest;

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

/**
 * 住客档案 DO。注意：住客不等于后台用户（system_users），
 * 前台可以为线下到店客人建档，无需开通登录账号。
 */
@TableName("hotel_guest")
@KeySequence("hotel_guest_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 门店编号（0 表示未分配） */
    private Long storeId;

    /** 姓名 */
    private String name;

    /** 手机号 */
    private String mobile;

    /** 身份证号 */
    private String idCard;

    /** 邮箱 */
    private String email;

    /** 性别，见 SexEnum：1 男 2 女 */
    private Integer sex;

    /** 客人类别，见 GuestLevelEnum：0 散客 1 会员 2 协议单位 3 中介 */
    private Integer guestLevel;

    /** 所属单位 */
    private String company;

    /** 联系地址 */
    private String address;

    /** 出生日期 */
    private LocalDate birthday;

    /** 备注 */
    private String remark;
}
