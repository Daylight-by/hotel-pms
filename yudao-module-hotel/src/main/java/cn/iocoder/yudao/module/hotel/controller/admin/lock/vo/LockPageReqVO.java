package cn.iocoder.yudao.module.hotel.controller.admin.lock.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LockPageReqVO extends PageParam {

    private Long orderId;
    private String roomNo;
    private Integer actionType;
    private Integer status;

}
