package cn.iocoder.yudao.module.hotel.dal.dataobject.roomtype;

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
 * 房型 DO
 */
@TableName("hotel_room_type")
@KeySequence("hotel_room_type_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomTypeDO extends TenantBaseDO {

    @TableId
    private Long id;

    /** 门店编号（0 表示未分配） */
    private Long storeId;

    /** 房型名称，如「豪华双床房」 */
    private String name;

    /** 房型编码，如 DOUBLE */
    private String code;

    /** 基础价格（每晚） */
    private BigDecimal basePrice;

    /** 钟点房价格（元/时段） */
    private BigDecimal hourlyPrice;

    /** 钟点房时长（小时） */
    private Integer hourlyHours;

    /** 是否开放钟点房 */
    private Boolean hourlyEnabled;

    /** 建议押金 */
    private BigDecimal deposit;

    /** 最大入住人数 */
    private Integer maxOccupancy;

    /** 面积（平方米） */
    private Integer area;

    /** 设施，逗号分隔 */
    private String amenities;

    /** 描述 */
    private String description;

    /** 状态，见 CommonStatusEnum：0 开启 1 关闭 */
    private Integer status;

    /** 排序 */
    private Integer sort;
}
