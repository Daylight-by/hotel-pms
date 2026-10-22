package cn.iocoder.yudao.module.hotel.dal.dataobject.channel;

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
 * 渠道 DO（T1）
 */
@TableName("hotel_channel")
@KeySequence("hotel_channel_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 门店编号（0 表示未分配） */
    private Long storeId;

    /** 渠道名称 */
    private String name;

    /** 渠道编码：DIRECT/CTRIP/MEITUAN/CORP */
    private String code;

    /** 类型，见 ChannelTypeEnum：10 直销 20 OTA 30 协议 40 会员 50 门店 */
    private Integer type;

    /** 佣金比例（%） */
    private BigDecimal commissionRate;

    /** 状态：0 开启 1 关闭 */
    private Integer status;

    /** 排序 */
    private Integer sort;

    /** 备注 */
    private String remark;
}
