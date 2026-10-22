package cn.iocoder.yudao.module.hotel.dal.dataobject.folio;

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
 * 账务账页 DO（Folio）
 */
@TableName("hotel_folio")
@KeySequence("hotel_folio_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolioDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 门店编号（0 表示未分配） */
    private Long storeId;

    /** 账页号 F + yyyyMMdd + 6 位序号 */
    private String folioNo;

    /** 订单编号 */
    private Long orderId;

    /** 住客编号 */
    private Long guestId;

    /** 房间编号 */
    private Long roomId;

    /** 账页类型，见 FolioTypeEnum */
    private Integer type;

    /** 状态，见 FolioStatusEnum */
    private Integer status;

    /** 消费合计 */
    private BigDecimal totalCharge;

    /** 收款合计 */
    private BigDecimal totalPayment;

    /** 余额 = 消费 - 收款 */
    private BigDecimal balance;

    /** 备注 */
    private String remark;
}
