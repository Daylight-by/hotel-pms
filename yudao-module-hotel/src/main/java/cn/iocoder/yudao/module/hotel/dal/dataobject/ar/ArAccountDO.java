package cn.iocoder.yudao.module.hotel.dal.dataobject.ar;

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
 * 协议单位账户 DO
 */
@TableName("hotel_ar_account")
@KeySequence("hotel_ar_account_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArAccountDO extends TenantBaseDO {

    @TableId
    private Long id;
    private Long storeId;
    private String name;
    private String code;
    private String taxNo;
    private String contactName;
    private String contactMobile;
    private BigDecimal creditLimit;
    /** 已用额度（未结挂账） */
    private BigDecimal usedAmount;
    /** 10 现结 20 周结 30 月结 40 季结 */
    private Integer settleCycle;
    /** 0 正常 1 停用 */
    private Integer status;
    private String remark;

}
