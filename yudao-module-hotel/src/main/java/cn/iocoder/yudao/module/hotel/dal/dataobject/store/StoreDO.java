package cn.iocoder.yudao.module.hotel.dal.dataobject.store;

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
 * 酒店门店 DO
 */
@TableName("hotel_store")
@KeySequence("hotel_store_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 门店名称 */
    private String name;
    /** 门店编码 */
    private String code;
    private String province;
    private String city;
    private String district;
    private String address;
    private String phone;
    private String legalPerson;
    /** 公安机关旅馆业编码 */
    private String psbCode;
    private String checkInTime;
    private String checkOutTime;
    /** 状态：0 营业 1 停业 */
    private Integer status;
    private Integer sort;
    private String remark;

}
