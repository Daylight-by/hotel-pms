package cn.iocoder.yudao.module.hotel.controller.admin.ar.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ArAccountPageReqVO extends PageParam {

    private String name;
    private String code;
    private Integer status;

}
