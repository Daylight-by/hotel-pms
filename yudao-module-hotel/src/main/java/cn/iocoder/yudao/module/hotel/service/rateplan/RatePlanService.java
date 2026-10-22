package cn.iocoder.yudao.module.hotel.service.rateplan;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.rateplan.vo.RatePlanCalcPriceReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.rateplan.vo.RatePlanCalcPriceRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.rateplan.vo.RatePlanPageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.rateplan.vo.RatePlanRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.rateplan.vo.RatePlanSaveReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.rateplan.RatePlanDO;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 房价计划 Service（T1）
 */
public interface RatePlanService {

    Long createRatePlan(RatePlanSaveReqVO reqVO);

    void updateRatePlan(RatePlanSaveReqVO reqVO);

    void deleteRatePlan(Long id);

    RatePlanRespVO getRatePlan(Long id);

    PageResult<RatePlanRespVO> getRatePlanPage(RatePlanPageReqVO reqVO);

    /**
     * 试算：某房价计划在某日期区间内每天的实际售价 + 总价
     */
    RatePlanCalcPriceRespVO calcPrice(RatePlanCalcPriceReqVO reqVO);

    /**
     * 单日算价（预订引擎复用）：日历价按规则折算，缺失日历价时用房型基础价兜底
     */
    BigDecimal calcDailyPrice(RatePlanDO plan, Long roomTypeId, LocalDate date);

    /**
     * 某房型某日最便宜的开启中房价计划（可售查询用），没有可用计划时返回 null
     */
    RatePlanDO getCheapestPlan(Long roomTypeId, LocalDate date);
}
