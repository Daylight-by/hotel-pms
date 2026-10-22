package cn.iocoder.yudao.module.hotel.service.pricing;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.module.hotel.controller.admin.pricing.vo.PriceBacktestPageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.pricing.vo.PriceBacktestReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.pricing.vo.PriceBacktestRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.pricing.vo.PriceGenerateReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.pricing.vo.PriceSuggestionRespVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.pricing.PriceBacktestDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.pricing.PriceSuggestionDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.rate.RoomDailyRateDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.roomtype.RoomTypeDO;
import cn.iocoder.yudao.module.hotel.dal.mysql.pricing.PriceBacktestMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.pricing.PriceSuggestionMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.rate.RoomDailyRateMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.room.RoomMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.roomtype.RoomTypeMapper;
import cn.iocoder.yudao.module.hotel.enums.DailyRateStatusEnum;
import cn.iocoder.yudao.module.hotel.enums.PriceSuggestionStatusEnum;
import cn.iocoder.yudao.module.hotel.service.rate.RateCalendarService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hotel.enums.HotelT1ErrorCodeConstants.ERROR_PRICE_BACKTEST_EMPTY;
import static cn.iocoder.yudao.module.hotel.enums.HotelT1ErrorCodeConstants.ERROR_PRICE_SUGGESTION_NO_BASE_PRICE;
import static cn.iocoder.yudao.module.hotel.enums.HotelT1ErrorCodeConstants.ERROR_PRICE_SUGGESTION_NOT_EXIST;

/**
 * 智能定价 Service 实现（T1 · 核心创新点）
 *
 * <pre>
 * 建议价 = 基线价 × 出租率系数 × 提前期系数 × 周末系数，并夹在 [0.7x, 1.5x] 之间
 * </pre>
 *
 * <p><b>诚实性声明（答辩口径）</b>：回测里的需求弹性 {@link #DEMAND_ELASTICITY} 是
 * <b>行业经验假设值</b>，不是用本项目数据拟合出来的。回测结论只能说明「在弹性 -1.2 的假设下，
 * 该定价策略的 RevPAR 会如何变化」，不能说成「真实提升 XX%」。评委追问时按这个口径回答。</p>
 */
@Service
@Validated
@Slf4j
public class PricingServiceImpl implements PricingService {

    /** 需求弹性假设值：价格每上涨 1%，需求下降 1.2%（假设，非拟合结果） */
    private static final BigDecimal DEMAND_ELASTICITY = new BigDecimal("-1.2");

    /** 历史同期回看周数：向前找 8 个同 weekday 的样本 */
    private static final int HIST_WEEKS = 8;

    /** 回测区间完全没有日历数据时使用的兜底出租率（假设值，明细里会标记 estimated=true） */
    private static final BigDecimal DEFAULT_OCCUPANCY = new BigDecimal("0.60");

    /** 安全边界：建议价不得低于基线价的 70% / 不得高于 150% */
    private static final BigDecimal MIN_FACTOR = new BigDecimal("0.70");
    private static final BigDecimal MAX_FACTOR = new BigDecimal("1.50");

    /**
     * 回测的决策提前期（天）：假设模型在入住日前 14 天定价。
     * 注意不能用「今天 到 历史日期」的天数——那会是负数，把历史日期全判成临期甩卖，属于口径错误。
     */
    private static final int BACKTEST_DECISION_LEAD_DAYS = 14;

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    /**
     * 节假日表（演示用固定表，生产应接节假日服务）。
     * 只需覆盖答辩演示区间，不做全量日历。
     */
    private static final Set<LocalDate> HOLIDAYS = new HashSet<>(List.of(
            LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2), LocalDate.of(2026, 1, 3),
            LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 17), LocalDate.of(2026, 2, 18),
            LocalDate.of(2026, 2, 19), LocalDate.of(2026, 2, 20), LocalDate.of(2026, 2, 21),
            LocalDate.of(2026, 4, 4), LocalDate.of(2026, 4, 5), LocalDate.of(2026, 4, 6),
            LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 2), LocalDate.of(2026, 5, 3),
            LocalDate.of(2026, 5, 4), LocalDate.of(2026, 5, 5),
            LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 2), LocalDate.of(2026, 10, 3),
            LocalDate.of(2026, 10, 4), LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 6),
            LocalDate.of(2026, 10, 7)));

    @Resource
    private PriceSuggestionMapper suggestionMapper;
    @Resource
    private PriceBacktestMapper backtestMapper;
    @Resource
    private RoomTypeMapper roomTypeMapper;
    @Resource
    private RoomDailyRateMapper rateMapper;
    @Resource
    private RoomMapper roomMapper;
    @Resource
    private RateCalendarService rateCalendarService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int generateSuggestions(PriceGenerateReqVO reqVO) {
        RoomTypeDO roomType = validateRoomType(reqVO.getRoomTypeId());
        LocalDate begin = reqVO.getBeginDate();
        LocalDate end = reqVO.getEndDate();
        if (end.isBefore(begin)) {
            LocalDate tmp = begin;
            begin = end;
            end = tmp;
        }
        // 一次性取出「回看窗口 + 生成区间」的日历数据，避免逐日查库
        Map<LocalDate, RoomDailyRateDO> rateMap = loadRateMap(roomType.getId(),
                begin.minusWeeks(HIST_WEEKS), end);

        int count = 0;
        for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
            BigDecimal base = rateCalendarService.getEffectivePrice(roomType.getId(), date, roomType.getBasePrice());
            if (base == null || base.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal histOccupancy = histOccupancy(rateMap, date);
            BigDecimal pace = bookingPace(rateMap.get(date), roomType.getId());
            int leadDays = (int) ChronoUnit.DAYS.between(LocalDate.now(), date);

            BigDecimal occFactor = occupancyFactor(histOccupancy, pace);
            BigDecimal advanceFactor = advanceFactor(leadDays, pace);
            BigDecimal weekendFactor = weekendFactor(date);

            BigDecimal suggested = suggestPrice(base, occFactor, advanceFactor, weekendFactor);
            String reason = buildReason(base, suggested, histOccupancy, pace, leadDays, weekendFactor, date);

            PriceSuggestionDO exists = suggestionMapper.selectByTypeAndDate(roomType.getId(), date);
            PriceSuggestionDO row = new PriceSuggestionDO();
            row.setRoomTypeId(roomType.getId());
            row.setBizDate(date);
            row.setBasePrice(base.setScale(2, RoundingMode.HALF_UP));
            row.setSuggestedPrice(suggested);
            row.setOccFactor(occFactor);
            row.setAdvanceFactor(advanceFactor);
            row.setWeekendFactor(weekendFactor);
            row.setHistOccupancy(histOccupancy);
            row.setBookingPace(pace);
            row.setLeadDays(leadDays);
            row.setReason(reason);
            row.setStatus(PriceSuggestionStatusEnum.SUGGESTING.getStatus());
            if (exists == null) {
                suggestionMapper.insert(row);
            } else {
                row.setId(exists.getId());
                suggestionMapper.updateById(row);
            }
            count++;
        }
        if (count == 0) {
            throw exception(ERROR_PRICE_SUGGESTION_NO_BASE_PRICE);
        }
        return count;
    }

    @Override
    public List<PriceSuggestionRespVO> listSuggestions(Long roomTypeId, LocalDate begin, LocalDate end) {
        if (begin == null) {
            begin = LocalDate.now();
        }
        if (end == null || end.isBefore(begin)) {
            end = begin.plusDays(13);
        }
        List<PriceSuggestionDO> list = suggestionMapper.selectListByRange(roomTypeId, begin, end);
        List<PriceSuggestionRespVO> result = new ArrayList<>();
        Map<Long, String> typeNameCache = new HashMap<>();
        for (PriceSuggestionDO row : list) {
            PriceSuggestionRespVO vo = new PriceSuggestionRespVO();
            vo.setId(row.getId());
            vo.setRoomTypeId(row.getRoomTypeId());
            vo.setRoomTypeName(typeNameCache.computeIfAbsent(row.getRoomTypeId(), id -> {
                RoomTypeDO type = roomTypeMapper.selectById(id);
                return type == null ? null : type.getName();
            }));
            vo.setBizDate(row.getBizDate());
            vo.setBasePrice(row.getBasePrice());
            vo.setSuggestedPrice(row.getSuggestedPrice());
            vo.setOccFactor(row.getOccFactor());
            vo.setAdvanceFactor(row.getAdvanceFactor());
            vo.setWeekendFactor(row.getWeekendFactor());
            vo.setHistOccupancy(row.getHistOccupancy());
            vo.setBookingPace(row.getBookingPace());
            vo.setLeadDays(row.getLeadDays());
            vo.setReason(row.getReason());
            vo.setStatus(row.getStatus());
            vo.setStatusName(statusName(row.getStatus()));
            vo.setAdoptedPrice(row.getAdoptedPrice());
            vo.setCreateTime(row.getCreateTime());
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adopt(Long suggestionId, BigDecimal finalPrice) {
        PriceSuggestionDO suggestion = suggestionId == null ? null : suggestionMapper.selectById(suggestionId);
        if (suggestion == null) {
            throw exception(ERROR_PRICE_SUGGESTION_NOT_EXIST);
        }
        BigDecimal price = finalPrice == null ? suggestion.getSuggestedPrice() : finalPrice;
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            price = suggestion.getSuggestedPrice();
        }
        price = price.setScale(2, RoundingMode.HALF_UP);

        PriceSuggestionDO update = new PriceSuggestionDO();
        update.setId(suggestion.getId());
        update.setStatus(PriceSuggestionStatusEnum.ADOPTED.getStatus());
        update.setAdoptedPrice(price);
        suggestionMapper.updateById(update);

        // 写回房价日历：有当日行就改价，没有就补一条（库存取房型房间数）
        RoomDailyRateDO row = rateMapper.selectByTypeAndDate(suggestion.getRoomTypeId(), suggestion.getBizDate());
        if (row != null) {
            rateMapper.updatePrice(row.getId(), price);
        } else {
            Long roomCount = roomMapper.selectCountByRoomTypeId(suggestion.getRoomTypeId());
            RoomDailyRateDO create = new RoomDailyRateDO();
            create.setRoomTypeId(suggestion.getRoomTypeId());
            create.setBizDate(suggestion.getBizDate());
            create.setPrice(price);
            create.setTotalInventory(roomCount == null ? 0 : roomCount.intValue());
            create.setSoldInventory(0);
            create.setStatus(DailyRateStatusEnum.OPEN.getStatus());
            create.setRemark("智能定价采纳");
            rateMapper.insert(create);
        }
    }

    @Override
    public void ignore(Long suggestionId) {
        PriceSuggestionDO suggestion = suggestionId == null ? null : suggestionMapper.selectById(suggestionId);
        if (suggestion == null) {
            throw exception(ERROR_PRICE_SUGGESTION_NOT_EXIST);
        }
        PriceSuggestionDO update = new PriceSuggestionDO();
        update.setId(suggestionId);
        update.setStatus(PriceSuggestionStatusEnum.IGNORED.getStatus());
        suggestionMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PriceBacktestRespVO runBacktest(PriceBacktestReqVO reqVO) {
        RoomTypeDO roomType = validateRoomType(reqVO.getRoomTypeId());
        LocalDate begin = reqVO.getBeginDate();
        LocalDate end = reqVO.getEndDate();
        if (end.isBefore(begin)) {
            LocalDate tmp = begin;
            begin = end;
            end = tmp;
        }
        Map<LocalDate, RoomDailyRateDO> rateMap = loadRateMap(roomType.getId(),
                begin.minusWeeks(HIST_WEEKS), end);

        List<PriceBacktestRespVO.Day> days = new ArrayList<>();
        BigDecimal sumActual = BigDecimal.ZERO;
        BigDecimal sumModel = BigDecimal.ZERO;
        for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
            RoomDailyRateDO row = rateMap.get(date);
            BigDecimal base = row != null && row.getPrice() != null
                    ? row.getPrice() : roomType.getBasePrice();
            if (base == null || base.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            boolean estimated = false;
            BigDecimal occupancy;
            int total = row == null || row.getTotalInventory() == null ? 0 : row.getTotalInventory();
            int sold = row == null || row.getSoldInventory() == null ? 0 : row.getSoldInventory();
            if (total > 0) {
                occupancy = BigDecimal.valueOf(sold)
                        .divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP);
            } else {
                occupancy = DEFAULT_OCCUPANCY;
                estimated = true;
            }
            // 模型价：用同一套因子重算（历史出租率用回看窗口，不含当日）
            BigDecimal histOccupancy = histOccupancy(rateMap, date);
            BigDecimal modelPrice = suggestPrice(base, occupancyFactor(histOccupancy, occupancy),
                    advanceFactor(BACKTEST_DECISION_LEAD_DAYS, occupancy), weekendFactor(date));

            BigDecimal modelOccupancy = modelOccupancy(occupancy, base, modelPrice);
            BigDecimal actualRevpar = base.multiply(occupancy).setScale(2, RoundingMode.HALF_UP);
            BigDecimal modelRevpar = modelPrice.multiply(modelOccupancy).setScale(2, RoundingMode.HALF_UP);

            PriceBacktestRespVO.Day day = new PriceBacktestRespVO.Day();
            day.setBizDate(date);
            day.setBasePrice(base.setScale(2, RoundingMode.HALF_UP));
            day.setSuggestedPrice(modelPrice);
            day.setOccupancy(occupancy.setScale(3, RoundingMode.HALF_UP));
            day.setActualRevpar(actualRevpar);
            day.setModelRevpar(modelRevpar);
            day.setOccupancyEstimated(estimated);
            days.add(day);
            sumActual = sumActual.add(actualRevpar);
            sumModel = sumModel.add(modelRevpar);
        }
        if (days.isEmpty()) {
            throw exception(ERROR_PRICE_BACKTEST_EMPTY);
        }

        BigDecimal actualRevpar = sumActual.divide(BigDecimal.valueOf(days.size()), 2, RoundingMode.HALF_UP);
        BigDecimal modelRevpar = sumModel.divide(BigDecimal.valueOf(days.size()), 2, RoundingMode.HALF_UP);
        BigDecimal upliftRate = BigDecimal.ZERO;
        if (actualRevpar.compareTo(BigDecimal.ZERO) > 0) {
            upliftRate = modelRevpar.subtract(actualRevpar)
                    .multiply(HUNDRED)
                    .divide(actualRevpar, 4, RoundingMode.HALF_UP);
        }

        PriceBacktestDO record = new PriceBacktestDO();
        record.setName(reqVO.getName() == null || reqVO.getName().isBlank()
                ? roomType.getName() + " 定价回测 " + begin + " ~ " + end
                : reqVO.getName());
        record.setRoomTypeId(roomType.getId());
        record.setBeginDate(begin);
        record.setEndDate(end);
        record.setActualRevpar(actualRevpar);
        record.setModelRevpar(modelRevpar);
        record.setUpliftRate(upliftRate);
        record.setDetail(trimDetail(JsonUtils.toJsonString(days)));
        backtestMapper.insert(record);

        PriceBacktestRespVO resp = buildBacktestVO(record);
        resp.setDays(days);
        return resp;
    }

    @Override
    public PageResult<PriceBacktestRespVO> getBacktestPage(PriceBacktestPageReqVO reqVO) {
        PageResult<PriceBacktestDO> page = backtestMapper.selectPage(reqVO);
        List<PriceBacktestRespVO> list = new ArrayList<>();
        for (PriceBacktestDO record : page.getList()) {
            list.add(buildBacktestVO(record));
        }
        return new PageResult<>(list, page.getTotal());
    }

    // ============================================================ 算法

    /**
     * 出租率系数：淡季降价保量、旺季提价
     *
     * <p>信号优先级：历史同期出租率 → 当前已售进度（历史无数据时退化使用，仍在 reason 里说明）</p>
     */
    private BigDecimal occupancyFactor(BigDecimal histOccupancy, BigDecimal bookingPace) {
        BigDecimal signal = histOccupancy != null ? histOccupancy : bookingPace;
        if (signal == null) {
            return BigDecimal.ONE.setScale(3, RoundingMode.HALF_UP);
        }
        double v = signal.doubleValue();
        BigDecimal factor;
        if (v < 0.40) {
            factor = new BigDecimal("0.85");
        } else if (v <= 0.70) {
            factor = new BigDecimal("1.00");
        } else if (v <= 0.85) {
            factor = new BigDecimal("1.15");
        } else {
            factor = new BigDecimal("1.30");
        }
        return factor.setScale(3, RoundingMode.HALF_UP);
    }

    /**
     * 提前期系数：早鸟优惠 / 尾房甩卖 / 临期涨价
     */
    private BigDecimal advanceFactor(int leadDays, BigDecimal pace) {
        double paceValue = pace == null ? 0D : pace.doubleValue();
        BigDecimal factor;
        if (leadDays > 30) {
            factor = new BigDecimal("0.90");
        } else if (leadDays < 3) {
            factor = paceValue < 0.5 ? new BigDecimal("0.90") : new BigDecimal("1.20");
        } else {
            factor = BigDecimal.ONE;
        }
        return factor.setScale(3, RoundingMode.HALF_UP);
    }

    /**
     * 周末 / 节假日系数：周五周六 1.20，法定节假日 1.35
     */
    private BigDecimal weekendFactor(LocalDate date) {
        if (HOLIDAYS.contains(date)) {
            return new BigDecimal("1.350");
        }
        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.FRIDAY || dow == DayOfWeek.SATURDAY) {
            return new BigDecimal("1.200");
        }
        return new BigDecimal("1.000");
    }

    /**
     * 建议价 = 基线价 × 三系数，夹在 [0.7x, 1.5x]，四舍五入到整数元
     */
    private BigDecimal suggestPrice(BigDecimal base, BigDecimal occFactor,
                                    BigDecimal advanceFactor, BigDecimal weekendFactor) {
        BigDecimal raw = base.multiply(occFactor).multiply(advanceFactor).multiply(weekendFactor);
        BigDecimal min = base.multiply(MIN_FACTOR);
        BigDecimal max = base.multiply(MAX_FACTOR);
        if (raw.compareTo(min) < 0) {
            raw = min;
        }
        if (raw.compareTo(max) > 0) {
            raw = max;
        }
        BigDecimal price = raw.setScale(0, RoundingMode.HALF_UP);
        // 取整后再夹一次，避免四舍五入越界
        BigDecimal minYuan = min.setScale(0, RoundingMode.CEILING);
        BigDecimal maxYuan = max.setScale(0, RoundingMode.FLOOR);
        if (price.compareTo(minYuan) < 0) {
            price = minYuan;
        }
        if (price.compareTo(maxYuan) > 0) {
            price = maxYuan;
        }
        return price.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 弹性假设下的模型出租率：occ × (新价/原价)^(-1.2)，并夹在 [0,1]
     */
    private BigDecimal modelOccupancy(BigDecimal actualOccupancy, BigDecimal basePrice, BigDecimal modelPrice) {
        if (basePrice.compareTo(BigDecimal.ZERO) <= 0) {
            return actualOccupancy;
        }
        double ratio = modelPrice.doubleValue() / basePrice.doubleValue();
        double adjusted = actualOccupancy.doubleValue() * Math.pow(ratio, DEMAND_ELASTICITY.doubleValue());
        adjusted = Math.max(0D, Math.min(1D, adjusted));
        return BigDecimal.valueOf(adjusted).setScale(4, RoundingMode.HALF_UP);
    }

    // ============================================================ 人话理由

    private String buildReason(BigDecimal base, BigDecimal suggested, BigDecimal histOccupancy,
                              BigDecimal pace, int leadDays, BigDecimal weekendFactor, LocalDate date) {
        StringBuilder sb = new StringBuilder();
        if (histOccupancy == null) {
            int currentPct = pace == null ? 0
                    : pace.multiply(HUNDRED).setScale(0, RoundingMode.HALF_UP).intValue();
            sb.append("历史同期无出租数据，按当前已售进度 ").append(currentPct).append("%，属")
                    .append(seasonName(pace == null ? BigDecimal.ZERO : pace));
        } else {
            int histPct = histOccupancy.multiply(HUNDRED).setScale(0, RoundingMode.HALF_UP).intValue();
            sb.append("历史同期出租率 ").append(histPct).append("%，属").append(seasonName(histOccupancy));
        }
        int pacePct = pace == null ? 0 : pace.multiply(HUNDRED).setScale(0, RoundingMode.HALF_UP).intValue();
        sb.append("；距入住 ").append(leadDays).append(" 天已售 ").append(pacePct).append("%");
        if (weekendFactor.compareTo(new BigDecimal("1.35")) == 0) {
            sb.append("；法定节假日需求旺，加价 35%");
        } else if (weekendFactor.compareTo(new BigDecimal("1.20")) == 0) {
            sb.append("；").append(date.getDayOfWeek() == DayOfWeek.FRIDAY ? "周五" : "周六").append("为周末，加价 20%");
        }
        int changePct = BigDecimal.ZERO.compareTo(base) == 0 ? 0
                : suggested.subtract(base).multiply(HUNDRED).divide(base, 0, RoundingMode.HALF_UP).intValue();
        if (changePct > 0) {
            sb.append("，建议上调 ").append(changePct).append("%");
        } else if (changePct < 0) {
            sb.append("，建议下调 ").append(Math.abs(changePct)).append("%");
        } else {
            sb.append("，建议维持原价");
        }
        sb.append("（¥").append(base.setScale(0, RoundingMode.HALF_UP))
                .append(" → ¥").append(suggested.setScale(0, RoundingMode.HALF_UP)).append("）");
        String reason = sb.toString();
        return reason.length() > 490 ? reason.substring(0, 490) : reason;
    }

    private String seasonName(BigDecimal histOccupancy) {
        double v = histOccupancy.doubleValue();
        if (v < 0.40) {
            return "淡季";
        }
        if (v <= 0.70) {
            return "平季";
        }
        if (v <= 0.85) {
            return "旺季";
        }
        return "爆满季";
    }

    // ============================================================ 私有方法

    private RoomTypeDO validateRoomType(Long roomTypeId) {
        RoomTypeDO roomType = roomTypeId == null ? null : roomTypeMapper.selectById(roomTypeId);
        if (roomType == null) {
            throw exception(cn.iocoder.yudao.module.hotel.enums.ErrorCodeConstants.ROOM_TYPE_NOT_EXISTS);
        }
        return roomType;
    }

    /**
     * 预取 [开始, 结束] 区间的日历数据，key = 日期
     */
    private Map<LocalDate, RoomDailyRateDO> loadRateMap(Long roomTypeId, LocalDate begin, LocalDate end) {
        Map<LocalDate, RoomDailyRateDO> map = new HashMap<>();
        for (RoomDailyRateDO row : rateMapper.selectListByRange(roomTypeId, begin, end)) {
            map.put(row.getBizDate(), row);
        }
        return map;
    }

    /**
     * 历史同期出租率：向前找 8 个「同 weekday」的样本取平均，无样本返回 null
     */
    private BigDecimal histOccupancy(Map<LocalDate, RoomDailyRateDO> rateMap, LocalDate date) {
        BigDecimal sum = BigDecimal.ZERO;
        int samples = 0;
        for (int i = 1; i <= HIST_WEEKS; i++) {
            RoomDailyRateDO row = rateMap.get(date.minusWeeks(i));
            if (row == null || row.getTotalInventory() == null || row.getTotalInventory() <= 0) {
                continue;
            }
            int sold = row.getSoldInventory() == null ? 0 : row.getSoldInventory();
            sum = sum.add(BigDecimal.valueOf(sold)
                    .divide(BigDecimal.valueOf(row.getTotalInventory()), 4, RoundingMode.HALF_UP));
            samples++;
        }
        if (samples == 0) {
            return null;
        }
        return sum.divide(BigDecimal.valueOf(samples), 3, RoundingMode.HALF_UP);
    }

    /**
     * 当日库存进度 = 已售 / 总库存；没排房（无日历行或总库存 0）时按 0 处理
     */
    private BigDecimal bookingPace(RoomDailyRateDO row, Long roomTypeId) {
        int total = row != null && row.getTotalInventory() != null ? row.getTotalInventory() : 0;
        if (total <= 0) {
            Long roomCount = roomMapper.selectCountByRoomTypeId(roomTypeId);
            total = roomCount == null ? 0 : roomCount.intValue();
        }
        if (total <= 0) {
            return BigDecimal.ZERO.setScale(3, RoundingMode.HALF_UP);
        }
        int sold = row == null || row.getSoldInventory() == null ? 0 : row.getSoldInventory();
        return BigDecimal.valueOf(Math.min(sold, total))
                .divide(BigDecimal.valueOf(total), 3, RoundingMode.HALF_UP);
    }

    private String statusName(Integer status) {
        for (PriceSuggestionStatusEnum e : PriceSuggestionStatusEnum.values()) {
            if (Objects.equals(e.getStatus(), status)) {
                return e.getName();
            }
        }
        return "";
    }

    private PriceBacktestRespVO buildBacktestVO(PriceBacktestDO record) {
        PriceBacktestRespVO vo = new PriceBacktestRespVO();
        vo.setId(record.getId());
        vo.setName(record.getName());
        vo.setRoomTypeId(record.getRoomTypeId());
        if (record.getRoomTypeId() != null) {
            RoomTypeDO type = roomTypeMapper.selectById(record.getRoomTypeId());
            vo.setRoomTypeName(type == null ? null : type.getName());
        }
        vo.setBeginDate(record.getBeginDate());
        vo.setEndDate(record.getEndDate());
        vo.setActualRevpar(record.getActualRevpar());
        vo.setModelRevpar(record.getModelRevpar());
        vo.setUpliftRate(record.getUpliftRate());
        vo.setAssumption("需求弹性 " + DEMAND_ELASTICITY.toPlainString()
                + " 为行业经验假设值（非本项目数据拟合），结论仅代表该假设下的策略收益");
        vo.setCreateTime(record.getCreateTime());
        if (record.getDetail() != null && !record.getDetail().isBlank()) {
            try {
                vo.setDays(JsonUtils.parseArray(record.getDetail(), PriceBacktestRespVO.Day.class));
            } catch (Exception ex) {
                log.warn("[buildBacktestVO][明细解析失败: {}]", ex.getMessage());
            }
        }
        return vo;
    }

    /** detail 字段 varchar(4000)，超长时截断（逐日明细过多时只保留前段） */
    private String trimDetail(String json) {
        if (json == null || json.length() <= 4000) {
            return json;
        }
        return json.substring(0, 3990) + "...}";
    }
}
