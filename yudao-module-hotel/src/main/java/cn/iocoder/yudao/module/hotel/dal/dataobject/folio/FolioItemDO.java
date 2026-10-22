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
import java.time.LocalDate;

/**
 * 账务明细 DO
 */
@TableName("hotel_folio_item")
@KeySequence("hotel_folio_item_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolioItemDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 门店编号（0 表示未分配） */
    private Long storeId;

    /** 账页编号 */
    private Long folioId;

    /** 订单编号 */
    private Long orderId;

    /** 营业日（入账归属） */
    private LocalDate businessDate;

    /** 项目类型，见 FolioItemTypeEnum */
    private Integer itemType;

    /** 项目名称 */
    private String itemName;

    /** 方向，见 FolioDirectionEnum：1 借方 2 贷方 */
    private Integer direction;

    /** 数量 */
    private Integer quantity;

    /** 单价 */
    private BigDecimal unitPrice;

    /** 金额 */
    private BigDecimal amount;

    /** 支付方式，见 PaymentMethodEnum */
    private Integer paymentMethod;

    /** 状态，见 FolioItemStatusEnum */
    private Integer status;

    /** 被冲销时指向的冲销明细号 */
    private Long voidId;

    /** 转出的目标账页号 */
    private Long transferToFolioId;

    /** 操作人 */
    private Long operatorId;

    /** 备注 */
    private String remark;
}
