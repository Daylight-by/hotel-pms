package cn.iocoder.yudao.module.hotel.service.shift;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.shift.vo.ShiftCloseReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.shift.vo.ShiftHandoverRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.shift.vo.ShiftOpenReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.shift.vo.ShiftPageReqVO;

/**
 * 交班 Service
 */
public interface ShiftService {

    /**
     * 开班（同一租户同时只能有一个进行中的班次）
     */
    Long openShift(ShiftOpenReqVO reqVO);

    /**
     * 交班：统计本班各支付方式收款合计并落库
     */
    Long closeShift(ShiftCloseReqVO reqVO);

    /**
     * 当前进行中的班次
     */
    ShiftHandoverRespVO getCurrentShift();

    /**
     * 班次分页
     */
    PageResult<ShiftHandoverRespVO> getShiftPage(ShiftPageReqVO reqVO);
}
