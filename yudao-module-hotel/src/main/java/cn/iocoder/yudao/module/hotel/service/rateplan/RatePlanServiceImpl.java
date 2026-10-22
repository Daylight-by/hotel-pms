package cn.iocoder.yudao.module.hotel.service.rateplan;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.rateplan.vo.RatePlanCalcPriceReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.rateplan.vo.RatePlanCalcPriceRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.rateplan.vo.RatePlanPageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.rateplan.vo.RatePlanRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.rateplan.vo.RatePlanSaveReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.channel.ChannelDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.rate.RoomDailyRateDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.rateplan.RatePlanDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.roomtype.RoomTypeDO;
import cn.iocoder.yudao.module.hotel.dal.mysql.channel.ChannelMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.rate.RoomDailyRateMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.rateplan.RatePlanMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.roomtype.RoomTypeMapper;
import cn.iocoder.yudao.module.hotel.enums.CancelPolicyEnum;
import cn.iocoder.yudao.module.hotel.enums.PriceRuleTypeEnum;
import cn.iocoder.yudao.module.hotel.service.rate.RateCalendarService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hotel.enums.HotelT1ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.hotel.enums.HotelT1ErrorCodeConstants.ERROR_RATE_PLAN_NOT_EXIST;

/**
 * 房价计划 Service 实现（T1）
 *
 * <p>算价规则（P1 §2.1）：
 * <ul>
 *   <li>10 按日历价：直接取当日日历价</li>
 *   <li>20 日历价折扣：日历价 × 规则值 / 100（规则值 90 表示 9 折）</li>
 *   <li>30 日历价减额：日历价 − 规则值（下限 0）</li>
 *   <li>40 固定价：规则值本身</li>
 * </ul>
 * 金额一律 {@link BigDecimal}，保留 2 位、四舍五入。</p>
 */
@Service
@Validated
@Slf4j
public class RatePlanServiceImpl implements RatePlanService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    @Resource
    private RatePlanMapper ratePlanMapper;
    @Resource
    private ChannelMapper channelMapper;
    @Resource
    private RoomTypeMapper roomTypeMapper;
    @Resource
    private RoomDailyRateMapper rateMapper;
    @Resource
    private RateCalendarService rateCalendarService;

    @Override
    public Long createRatePlan(RatePlanSaveReqVO reqVO) {
        validateSaveReq(reqVO);
        validateCodeUnique(null, reqVO.getCode());
        RatePlanDO plan = new RatePlanDO();
        copyProperties(reqVO, plan);
        plan.setCode(reqVO.getCode().trim().toUpperCase());
        ratePlanMapper.insert(plan);
        return plan.getId();
    }

    @Override
    public void updateRatePlan(RatePlanSaveReqVO reqVO) {
        validateRatePlanExists(reqVO.getId());
        validateSaveReq(reqVO);
        validateCodeUnique(reqVO.getId(), reqVO.getCode());
        RatePlanDO update = new RatePlanDO();
        copyProperties(reqVO, update);
        update.setId(reqVO.getId());
        update.setCode(reqVO.getCode().trim().toUpperCase());
        ratePlanMapper.updateById(update);
    }

    @Override
    public void deleteRatePlan(Long id) {
        validateRatePlanExists(id);
        ratePlanMapper.deleteById(id);
    }

    @Override
    public RatePlanRespVO getRatePlan(Long id) {
        return buildVOList(List.of(validateRatePlanExists(id))).get(0);
    }

    @Override
    public PageResult<RatePlanRespVO> getRatePlanPage(RatePlanPageReqVO reqVO) {
        PageResult<RatePlanDO> page = ratePlanMapper.selectPage(reqVO);
        return new PageResult<>(buildVOList(page.getList()), page.getTotal());
    }

    @Override
    public RatePlanCalcPriceRespVO calcPrice(RatePlanCalcPriceReqVO reqVO) {
        RatePlanDO plan = validateRatePlanExists(reqVO.getRatePlanId());
        Long roomTypeId = reqVO.getRoomTypeId() != null ? reqVO.getRoomTypeId() : plan.getRoomTypeId();
        if (roomTypeId == null) {
            // 房价计划未绑定房型：取第一个启用房型试算，保证「未绑定也能预览」
            List<RoomTypeDO> types = roomTypeMapper.selectListByStatus(0);
            if (types.isEmpty()) {
                throw exception(ERROR_RATE_PLAN_NO_ROOM_TYPE);
            }
            roomTypeId = types.get(0).getId();
        }
        RoomTypeDO roomType = roomTypeMapper.selectById(roomTypeId);
        if (roomType == null) {
            throw exception(ERROR_RATE_PLAN_NO_ROOM_TYPE);
        }
        LocalDate begin = reqVO.getBeginDate();
        LocalDate end = reqVO.getEndDate();
        if (end.isBefore(begin)) {
            LocalDate tmp = begin;
            begin = end;
            end = tmp;
        }

        RatePlanCalcPriceRespVO resp = new RatePlanCalcPriceRespVO();
        resp.setRatePlanId(plan.getId());
        resp.setRatePlanName(plan.getName());
        resp.setRoomTypeId(roomTypeId);
        resp.setRoomTypeName(roomType.getName());
        resp.setNights((int) (ChronoUnit.DAYS.between(begin, end) + 1));

        BigDecimal total = BigDecimal.ZERO;
        List<RatePlanCalcPriceRespVO.DailyPrice> daily = new ArrayList<>();
        for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
            BigDecimal base = rateCalendarService.getEffectivePrice(roomTypeId, date, roomType.getBasePrice());
            BigDecimal price = applyRule(plan, base);
            RatePlanCalcPriceRespVO.DailyPrice day = new RatePlanCalcPriceRespVO.DailyPrice();
            day.setBizDate(date);
            day.setBasePrice(base);
            day.setPrice(price);
            RoomDailyRateDO row = rateMapper.selectByTypeAndDate(roomTypeId, date);
            int totalInventory = row == null || row.getTotalInventory() == null ? 0 : row.getTotalInventory();
            int sold = row == null || row.getSoldInventory() == null ? 0 : row.getSoldInventory();
            day.setRemaining(Math.max(totalInventory - sold, 0));
            daily.add(day);
            total = total.add(price);
        }
        resp.setDailyPrices(daily);
        resp.setTotalAmount(total.setScale(2, RoundingMode.HALF_UP));
        return resp;
    }

    @Override
    public BigDecimal calcDailyPrice(RatePlanDO plan, Long roomTypeId, LocalDate date) {
        RoomTypeDO roomType = roomTypeId == null ? null : roomTypeMapper.selectById(roomTypeId);
        BigDecimal fallback = roomType == null ? BigDecimal.ZERO : roomType.getBasePrice();
        BigDecimal base = rateCalendarService.getEffectivePrice(roomTypeId, date, fallback);
        return applyRule(plan, base);
    }

    @Override
    public RatePlanDO getCheapestPlan(Long roomTypeId, LocalDate date) {
        RatePlanDO cheapest = null;
        BigDecimal cheapestPrice = null;
        for (RatePlanDO plan : ratePlanMapper.selectEnabledListByRoomType(roomTypeId)) {
            BigDecimal price = calcDailyPrice(plan, roomTypeId, date);
            if (cheapestPrice == null || price.compareTo(cheapestPrice) < 0) {
                cheapest = plan;
                cheapestPrice = price;
            }
        }
        return cheapest;
    }

    // ============================================================ 算价核心

    /**
     * 按价格规则折算单日售价（2 位小数，四舍五入）
     */
    private BigDecimal applyRule(RatePlanDO plan, BigDecimal basePrice) {
        BigDecimal base = basePrice == null ? BigDecimal.ZERO : basePrice;
        BigDecimal value = plan.getPriceRuleValue();
        Integer rule = plan.getPriceRuleType();
        BigDecimal price;
        if (Objects.equals(rule, PriceRuleTypeEnum.DISCOUNT.getType()) && value != null) {
            price = base.multiply(value).divide(HUNDRED, 2, RoundingMode.HALF_UP);
        } else if (Objects.equals(rule, PriceRuleTypeEnum.REDUCE.getType()) && value != null) {
            price = base.subtract(value);
        } else if (Objects.equals(rule, PriceRuleTypeEnum.FIXED.getType()) && value != null) {
            price = value;
        } else {
            price = base;
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            price = BigDecimal.ZERO;
        }
        return price.setScale(2, RoundingMode.HALF_UP);
    }

    // ============================================================ 私有方法

    private void validateSaveReq(RatePlanSaveReqVO reqVO) {
        ChannelDO channel = reqVO.getChannelId() == null ? null : channelMapper.selectById(reqVO.getChannelId());
        if (channel == null) {
            throw exception(ERROR_CHANNEL_NOT_EXIST);
        }
        if (reqVO.getRoomTypeId() != null && roomTypeMapper.selectById(reqVO.getRoomTypeId()) == null) {
            throw exception(ERROR_RATE_PLAN_NO_ROOM_TYPE);
        }
        Integer rule = reqVO.getPriceRuleType();
        if (rule != null && !Objects.equals(rule, PriceRuleTypeEnum.CALENDAR.getType())
                && reqVO.getPriceRuleValue() == null) {
            throw exception(new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                    1_003_004_207, "该价格规则必须填写规则值"));
        }
        if (Objects.equals(reqVO.getCancelPolicy(), CancelPolicyEnum.HOURS_BEFORE.getPolicy())
                && reqVO.getCancelFreeHours() == null) {
            throw exception(new cn.iocoder.yudao.framework.common.exception.ErrorCode(
                    1_003_004_208, "入住前 N 小时可免费取消必须填写截止小时数"));
        }
    }

    private RatePlanDO validateRatePlanExists(Long id) {
        RatePlanDO plan = id == null ? null : ratePlanMapper.selectById(id);
        if (plan == null) {
            throw exception(ERROR_RATE_PLAN_NOT_EXIST);
        }
        return plan;
    }

    private void validateCodeUnique(Long id, String code) {
        if (code == null || code.isBlank()) {
            return;
        }
        RatePlanDO exists = ratePlanMapper.selectByCode(code.trim().toUpperCase());
        if (exists != null && !Objects.equals(exists.getId(), id)) {
            throw exception(ERROR_RATE_PLAN_CODE_DUPLICATE);
        }
    }

    private void copyProperties(RatePlanSaveReqVO reqVO, RatePlanDO plan) {
        plan.setName(reqVO.getName());
        plan.setChannelId(reqVO.getChannelId());
        plan.setRoomTypeId(reqVO.getRoomTypeId());
        plan.setPriceRuleType(reqVO.getPriceRuleType() == null
                ? PriceRuleTypeEnum.CALENDAR.getType() : reqVO.getPriceRuleType());
        plan.setPriceRuleValue(reqVO.getPriceRuleValue());
        plan.setBreakfast(reqVO.getBreakfast() == null ? 0 : reqVO.getBreakfast());
        plan.setMinNights(reqVO.getMinNights() == null ? 1 : reqVO.getMinNights());
        plan.setMaxNights(reqVO.getMaxNights());
        plan.setMinAdvanceHours(reqVO.getMinAdvanceHours());
        plan.setCancelPolicy(reqVO.getCancelPolicy() == null
                ? CancelPolicyEnum.FREE.getPolicy() : reqVO.getCancelPolicy());
        plan.setCancelFreeHours(reqVO.getCancelFreeHours());
        plan.setNeedDeposit(Boolean.TRUE.equals(reqVO.getNeedDeposit()));
        plan.setDepositRate(reqVO.getDepositRate());
        plan.setStatus(reqVO.getStatus() == null ? 0 : reqVO.getStatus());
        plan.setSort(reqVO.getSort() == null ? 0 : reqVO.getSort());
        plan.setRemark(reqVO.getRemark());
    }

    private List<RatePlanRespVO> buildVOList(List<RatePlanDO> list) {
        List<RatePlanRespVO> result = new ArrayList<>();
        if (list == null) {
            return result;
        }
        for (RatePlanDO plan : list) {
            RatePlanRespVO vo = new RatePlanRespVO();
            vo.setId(plan.getId());
            vo.setName(plan.getName());
            vo.setCode(plan.getCode());
            vo.setChannelId(plan.getChannelId());
            ChannelDO channel = plan.getChannelId() == null ? null : channelMapper.selectById(plan.getChannelId());
            vo.setChannelName(channel == null ? null : channel.getName());
            vo.setRoomTypeId(plan.getRoomTypeId());
            if (plan.getRoomTypeId() != null) {
                RoomTypeDO roomType = roomTypeMapper.selectById(plan.getRoomTypeId());
                vo.setRoomTypeName(roomType == null ? null : roomType.getName());
            }
            vo.setPriceRuleType(plan.getPriceRuleType());
            vo.setPriceRuleTypeName(ruleName(plan.getPriceRuleType()));
            vo.setPriceRuleValue(plan.getPriceRuleValue());
            vo.setBreakfast(plan.getBreakfast());
            vo.setMinNights(plan.getMinNights());
            vo.setMaxNights(plan.getMaxNights());
            vo.setMinAdvanceHours(plan.getMinAdvanceHours());
            vo.setCancelPolicy(plan.getCancelPolicy());
            vo.setCancelPolicyName(policyName(plan.getCancelPolicy()));
            vo.setCancelFreeHours(plan.getCancelFreeHours());
            vo.setNeedDeposit(plan.getNeedDeposit());
            vo.setDepositRate(plan.getDepositRate());
            vo.setStatus(plan.getStatus());
            vo.setStatusName(Objects.equals(plan.getStatus(), 0) ? "开启" : "关闭");
            vo.setSort(plan.getSort());
            vo.setRemark(plan.getRemark());
            vo.setCreateTime(plan.getCreateTime());
            result.add(vo);
        }
        return result;
    }

    private String ruleName(Integer rule) {
        for (PriceRuleTypeEnum e : PriceRuleTypeEnum.values()) {
            if (Objects.equals(e.getType(), rule)) {
                return e.getName();
            }
        }
        return "";
    }

    private String policyName(Integer policy) {
        for (CancelPolicyEnum e : CancelPolicyEnum.values()) {
            if (Objects.equals(e.getPolicy(), policy)) {
                return e.getName();
            }
        }
        return "";
    }
}
