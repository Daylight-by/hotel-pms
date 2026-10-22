package cn.iocoder.yudao.module.hotel.controller.admin.store.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "管理后台 - 门店 Response VO")
@Data
public class StoreRespVO {

    private Long id;
    private String name;
    private String code;
    private String province;
    private String city;
    private String district;
    private String address;
    private String phone;
    private String legalPerson;
    private String psbCode;
    private String checkInTime;
    private String checkOutTime;
    private Integer status;
    private Integer sort;
    private String remark;
    private LocalDateTime createTime;

}
