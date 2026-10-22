package cn.iocoder.yudao.module.hotel.service.report;

import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.hotel.controller.admin.report.vo.DailyReportExcelVO;
import cn.iocoder.yudao.module.hotel.controller.admin.report.vo.MonthlyReportExcelVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.audit.BusinessDateDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.folio.FolioDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.folio.FolioItemDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.folio.PaymentRecordDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.order.OrderDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.rate.RoomDailyRateDO;
import cn.iocoder.yudao.module.hotel.dal.mysql.audit.BusinessDateMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.folio.FolioItemMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.folio.FolioMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.folio.PaymentRecordMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.order.OrderMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.rate.RoomDailyRateMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.room.RoomMapper;
import cn.iocoder.yudao.module.hotel.enums.FolioDirectionEnum;
import cn.iocoder.yudao.module.hotel.enums.FolioItemStatusEnum;
import cn.iocoder.yudao.module.hotel.enums.FolioItemTypeEnum;
import cn.iocoder.yudao.module.hotel.enums.FolioStatusEnum;
import cn.iocoder.yudao.module.hotel.enums.OrderStatusEnum;
import cn.iocoder.yudao.module.hotel.enums.PaymentRecordStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static cn.iocoder.yudao.module.hotel.enums.HotelT4ErrorCodeConstants.ERROR_REPORT_DATE_INVALID;
import static cn.iocoder.yudao.module.hotel.service.report.ReportCalcUtils.percent;
import static cn.iocoder.yudao.module.hotel.service.report.ReportCalcUtils.sum;

/**
 * 报表中心 Service：日报 / 月报 / 导出
 *
 * <p>口径（与 P0 统计保持一致）：<br>
 * - OCC = 在售间夜 sold_nights / 可售间夜 available_nights（来自房价日历 room_daily_rate）<br>
 * - ADR = 房费收入 room_revenue / 在售间夜<br>
 * - RevPAR = 房费收入 / 可售间夜，恒等：RevPAR == ADR × OCC / 100<br>
 * - 收入 = 账务明细（借方、有效）按营业日汇总，房费=ROOM，其余为其他收入<br>
 * - 欠账余额 = 未结账页 balance 之和</p>
 */
@Service
@Validated
@Slf4j
public class ReportService {

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    @Resource
    private FolioItemMapper folioItemMapper;
    @Resource
    private RoomDailyRateMapper rateMapper;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private FolioMapper folioMapper;
    @Resource
    private PaymentRecordMapper paymentRecordMapper;
    @Resource
    private RoomMapper roomMapper;
    @Resource
    private BusinessDateMapper businessDateMapper;

    // ==================== 接口 ====================

    /**
     * 日报：指定营业日的完整指标
     */
    public Map<String, Object> dailyReport(LocalDate bizDate) {
        return buildDailyRow(bizDate, true);
    }

    /**
     * 月报：指定月份的逐日指标 + 汇总
     */
    public Map<String, Object> monthlyReport(String month) {
        YearMonth ym = YearMonth.parse(month, MONTH_FMT);
        LocalDate monthStart = ym.atDay(1);
        LocalDate monthEnd = ym.atEndOfMonth();
        LocalDate today = LocalDate.now();
        if (monthEnd.isAfter(today)) {
            monthEnd = today;
        }
        if (monthStart.isAfter(monthEnd)) {
            throw ServiceExceptionUtil.exception(ERROR_REPORT_DATE_INVALID);
        }
        List<Map<String, Object>> days = new ArrayList<>();
        BigDecimal totalRoomRevenue = BigDecimal.ZERO;
        BigDecimal totalOtherRevenue = BigDecimal.ZERO;
        long totalSold = 0;
        long totalAvailable = 0;
        for (LocalDate date = monthStart; !date.isAfter(monthEnd); date = date.plusDays(1)) {
            Map<String, Object> row = buildDailyRow(date, false);
            days.add(row);
            totalRoomRevenue = totalRoomRevenue.add((BigDecimal) row.get("roomRevenue"));
            totalOtherRevenue = totalOtherRevenue.add((BigDecimal) row.get("otherRevenue"));
            totalSold += (Long) row.get("soldNights");
            totalAvailable += (Long) row.get("availableNights");
        }
        // 汇总
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("days", days.size());
        summary.put("roomRevenue", totalRoomRevenue.setScale(2, RoundingMode.HALF_UP));
        summary.put("otherRevenue", totalOtherRevenue.setScale(2, RoundingMode.HALF_UP));
        summary.put("totalRevenue", totalRoomRevenue.add(totalOtherRevenue).setScale(2, RoundingMode.HALF_UP));
        summary.put("occ", totalAvailable > 0 ? percent(totalSold * 100D / totalAvailable) : BigDecimal.ZERO);
        summary.put("adr", totalSold > 0 ? totalRoomRevenue.divide(BigDecimal.valueOf(totalSold), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        summary.put("revpar", totalAvailable > 0 ? totalRoomRevenue.divide(BigDecimal.valueOf(totalAvailable), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("month", month);
        result.put("days", days);
        result.put("summary", summary);
        return result;
    }

    // ==================== 导出 ====================

    public List<DailyReportExcelVO> exportDailyRows(LocalDate begin, LocalDate end) {
        List<DailyReportExcelVO> list = new ArrayList<>();
        for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
            Map<String, Object> row = buildDailyRow(date, false);
            DailyReportExcelVO vo = BeanUtils.toBean(row, DailyReportExcelVO.class);
            vo.setBizDate(date.toString());
            vo.setOcc(((BigDecimal) row.get("occ")).doubleValue());
            vo.setAdr(((BigDecimal) row.get("adr")).doubleValue());
            vo.setRevpar(((BigDecimal) row.get("revpar")).doubleValue());
            list.add(vo);
        }
        return list;
    }

    public List<MonthlyReportExcelVO> exportMonthlyRows(LocalDate begin, LocalDate end) {
        List<MonthlyReportExcelVO> list = new ArrayList<>();
        YearMonth cursor = YearMonth.from(begin);
        YearMonth endMonth = YearMonth.from(end);
        while (!cursor.isAfter(endMonth)) {
            Map<String, Object> row = monthlyReport(cursor.toString());
            Map<String, Object> summary = (Map<String, Object>) row.get("summary");
            MonthlyReportExcelVO vo = new MonthlyReportExcelVO();
            vo.setMonth(cursor.toString());
            vo.setDays((Integer) summary.get("days"));
            vo.setOcc(((BigDecimal) summary.get("occ")).doubleValue());
            vo.setAdr(((BigDecimal) summary.get("adr")).doubleValue());
            vo.setRevpar(((BigDecimal) summary.get("revpar")).doubleValue());
            vo.setRoomRevenue((BigDecimal) summary.get("roomRevenue"));
            vo.setOtherRevenue((BigDecimal) summary.get("otherRevenue"));
            vo.setTotalRevenue((BigDecimal) summary.get("totalRevenue"));
            list.add(vo);
            cursor = cursor.plusMonths(1);
        }
        return list;
    }

    // ==================== 内部口径 ====================

    /**
     * 构造单日指标行
     *
     * @param withPayment 是否附带收款结构（日报接口带，月报逐日行不带）
     */
    public Map<String, Object> buildDailyRow(LocalDate bizDate, boolean withPayment) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("bizDate", bizDate.toString());

        // 1. 可售/在售间夜（房价日历，兜底：未维护则按房间总数计可售）
        List<RoomDailyRateDO> rates = rateMapper.selectList(
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<RoomDailyRateDO>()
                        .eq(RoomDailyRateDO::getBizDate, bizDate));
        long availableNights = 0;
        long soldNights = 0;
        if (rates.isEmpty()) {
            availableNights = roomMapper.selectCount(null);
        } else {
            for (RoomDailyRateDO rate : rates) {
                availableNights += rate.getTotalInventory() == null ? 0 : rate.getTotalInventory();
                soldNights += rate.getSoldInventory() == null ? 0 : rate.getSoldInventory();
            }
        }
        row.put("availableNights", availableNights);
        row.put("soldNights", soldNights);

        // 2. 房费 / 其他收入（账务明细：借方 + 有效）
        List<FolioItemDO> items = folioItemMapper.selectList(
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<FolioItemDO>()
                        .eq(FolioItemDO::getBusinessDate, bizDate)
                        .eq(FolioItemDO::getDirection, FolioDirectionEnum.DEBIT.getDirection())
                        .eq(FolioItemDO::getStatus, FolioItemStatusEnum.VALID.getStatus()));
        BigDecimal roomRevenue = sum(items, item -> FolioItemTypeEnum.ROOM.getType().equals(item.getItemType()), item -> item.getAmount());
        BigDecimal otherRevenue = sum(items, item -> !FolioItemTypeEnum.ROOM.getType().equals(item.getItemType()), item -> item.getAmount());
        row.put("roomRevenue", roomRevenue.setScale(2, RoundingMode.HALF_UP));
        row.put("otherRevenue", otherRevenue.setScale(2, RoundingMode.HALF_UP));
        row.put("totalRevenue", roomRevenue.add(otherRevenue).setScale(2, RoundingMode.HALF_UP));

        // 3. 核心指标（恒等：RevPAR == ADR × OCC / 100）
        BigDecimal occ = availableNights > 0 ? percent(soldNights * 100D / availableNights) : BigDecimal.ZERO;
        BigDecimal adr = soldNights > 0 ? roomRevenue.divide(BigDecimal.valueOf(soldNights), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal revpar = availableNights > 0 ? roomRevenue.divide(BigDecimal.valueOf(availableNights), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        row.put("occ", occ);
        row.put("adr", adr);
        row.put("revpar", revpar);

        // 4. 到店 / 离店（订单口径）
        List<OrderDO> arrivals = orderMapper.selectList(
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<OrderDO>()
                        .eq(OrderDO::getCheckInDate, bizDate)
                        .in(OrderDO::getStatus, OrderStatusEnum.PENDING.getStatus(), OrderStatusEnum.WAIT_CONFIRM.getStatus(),
                                OrderStatusEnum.CONFIRMED.getStatus(), OrderStatusEnum.CHECKED_IN.getStatus()));
        List<OrderDO> departures = orderMapper.selectList(
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<OrderDO>()
                        .eq(OrderDO::getCheckOutDate, bizDate)
                        .in(OrderDO::getStatus, OrderStatusEnum.CHECKED_IN.getStatus(), OrderStatusEnum.CHECKED_OUT.getStatus()));
        row.put("arrivals", arrivals.size());
        row.put("departures", departures.size());
        long departureNights = departures.stream().filter(o -> o.getNights() != null).mapToLong(OrderDO::getNights).sum();
        row.put("departureNights", departureNights);

        // 5. 欠账余额（未结账页 balance > 0 之和）
        List<FolioDO> openFolios = folioMapper.selectList(
                new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<FolioDO>()
                        .gt(FolioDO::getBalance, BigDecimal.ZERO)
                        .in(FolioDO::getStatus, FolioStatusEnum.UNSETTLED.getStatus(), FolioStatusEnum.PARTIAL.getStatus()));
        BigDecimal arBalance = openFolios.stream()
                .map(FolioDO::getBalance).filter(b -> b != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        row.put("arBalance", arBalance.setScale(2, RoundingMode.HALF_UP));

        // 6. 收款结构（按支付方式汇总当日成功收款）
        if (withPayment) {
            List<PaymentRecordDO> pays = paymentRecordMapper.selectList(
                    new cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX<PaymentRecordDO>()
                            .ge(PaymentRecordDO::getPayTime, bizDate.atStartOfDay())
                            .lt(PaymentRecordDO::getPayTime, bizDate.plusDays(1).atStartOfDay())
                            .eq(PaymentRecordDO::getStatus, PaymentRecordStatusEnum.SUCCESS.getStatus()));
            Map<Integer, BigDecimal> byMethod = new TreeMap<>();
            for (PaymentRecordDO pay : pays) {
                BigDecimal amount = pay.getAmount() == null ? BigDecimal.ZERO : pay.getAmount();
                byMethod.merge(pay.getMethod() == null ? 0 : pay.getMethod(), amount, BigDecimal::add);
            }
            List<Map<String, Object>> paymentStructure = new ArrayList<>();
            for (Map.Entry<Integer, BigDecimal> entry : byMethod.entrySet()) {
                if (entry.getValue().compareTo(BigDecimal.ZERO) == 0) {
                    continue;
                }
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("method", entry.getKey());
                item.put("name", ReportCalcUtils.paymentMethodName(entry.getKey()));
                item.put("amount", entry.getValue().setScale(2, RoundingMode.HALF_UP));
                paymentStructure.add(item);
            }
            row.put("paymentStructure", paymentStructure);
        }
        return row;
    }

    /**
     * 当前营业日（不存在则按今天）
     */
    public LocalDate getCurrentBizDate() {
        BusinessDateDO current = businessDateMapper.selectCurrent();
        if (current != null && current.getBizDate() != null) {
            return current.getBizDate();
        }
        return LocalDate.now();
    }
}
