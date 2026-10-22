package cn.iocoder.yudao.module.hotel.service.pricing;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.pricing.vo.PriceBacktestPageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.pricing.vo.PriceBacktestReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.pricing.vo.PriceBacktestRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.pricing.vo.PriceGenerateReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.pricing.vo.PriceSuggestionRespVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 智能定价 Service（T1 · 核心创新点）
 */
public interface PricingService {

    /**
     * 生成未来区间内某房型的建议价（幂等：同 房型+日期 覆盖更新）
     */
    int generateSuggestions(PriceGenerateReqVO reqVO);

    /**
     * 查询建议（前端日历/表格用）
     */
    List<PriceSuggestionRespVO> listSuggestions(Long roomTypeId, LocalDate begin, LocalDate end);

    /**
     * 采纳建议：写回房价日历，并记录 adopted_price
     */
    void adopt(Long suggestionId, BigDecimal finalPrice);

    /**
     * 忽略建议
     */
    void ignore(Long suggestionId);

    /**
     * 回测：历史区间「原价 vs 建议价」的 RevPAR 对比
     */
    PriceBacktestRespVO runBacktest(PriceBacktestReqVO reqVO);

    PageResult<PriceBacktestRespVO> getBacktestPage(PriceBacktestPageReqVO reqVO);
}
