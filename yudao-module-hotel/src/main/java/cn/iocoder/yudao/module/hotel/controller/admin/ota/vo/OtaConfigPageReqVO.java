package cn.iocoder.yudao.module.hotel.controller.admin.ota.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OtaConfigPageReqVO extends PageParam {

    private String otaCode;
    private Integer status;

}
