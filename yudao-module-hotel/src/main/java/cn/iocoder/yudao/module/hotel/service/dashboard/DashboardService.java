package cn.iocoder.yudao.module.hotel.service.dashboard;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.dal.dataobject.cleaning.CleaningTaskDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.folio.FolioDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.folio.FolioItemDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.guest.GuestDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.order.OrderDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.rate.RoomDailyRateDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.room.RoomDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.roomtype.RoomTypeDO;
import cn.iocoder.yudao.module.hotel.dal.mysql.cleaning.CleaningTaskMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.folio.FolioItemMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.folio.FolioMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.guest.GuestMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.order.OrderMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.rate.RoomDailyRateMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.room.RoomMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.roomtype.RoomTypeMapper;
import cn.iocoder.yudao.module.hotel.enums.CleaningTaskStatusEnum;
import cn.iocoder.yudao.module.hotel.enums.FolioDirectionEnum;
import cn.iocoder.yudao.module.hotel.enums.FolioItemStatusEnum;
import cn.iocoder.yudao.module.hotel.enums.FolioItemTypeEnum;
import cn.iocoder.yudao.module.hotel.enums.FolioStatusEnum;
import cn.iocoder.yudao.module.hotel.enums.OrderStatusEnum;
import cn.iocoder.yudao.module.hotel.enums.RoomStatusEnum;
import cn.iocoder.yudao.module.hotel.service.report.ReportService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.module.hotel.service.report.ReportCalcUtils.percent;
import static cn.iocoder.yudao.module.hotel.service.report.ReportCalcUtils.sum;

/**
 * 经营数据大屏 Service
 *
 * <p>一屏四区：①今日核心指标+环比 ②实时房态网格 ③近 30 天趋势 ④今日预抵/预离+待办。</p>
 */
@Service
@Validated
@Slf4j
public class DashboardService {

    private static final int TREND_DAYS = 30;

    @Resource
    private ReportService reportService;
    @Resource
    private RoomMapper roomMapper;
    @Resource
    private RoomTypeMapper roomTypeMapper;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private FolioItemMapper folioItemMapper;
    @Resource
    private RoomDailyRateMapper rateMapper;
    @Resource
    private CleaningTaskMapper cleaningTaskMapper;
    @Resource
    private FolioMapper folioMapper;
    @Resource
    private GuestMapper guestMapper;

    /**
     * 大屏总览
     */
    public Map<String, Object> overview() {
        LocalDate today = reportService.getCurrentBizDate();
        LocalDate yesterday = today.minusDays(1);

        Map<String, Object> todayRow = reportService.buildDailyRow(today, true);
        Map<String, Object> yestRow = reportService.buildDailyRow(yesterday, false);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("bizDate", today.toString());
        result.put("today", buildTodayMetrics(todayRow, yestRow));
        result.put("roomStatus", buildRoomStatus(today));
        result.put("roomGrid", buildRoomGrid(today));
        result.put("trend", buildTrend(today));
        result.put("todayArrivals", buildTodayArrivals(today));
        result.put("todayDepartures", buildTodayDepartures(today));
        result.put("todo", buildTodo(today));
        return result;
    }

    // ==================== 指标 ====================

    private Map<String, Object> buildTodayMetrics(Map<String, Object> todayRow, Map<String, Object> yestRow) {
        BigDecimal occ = (BigDecimal) todayRow.get("occ");
        BigDecimal adr = (BigDecimal) todayRow.get("adr");
        BigDecimal revpar = (BigDecimal) todayRow.get("revpar");
        BigDecimal totalRevenue = (BigDecimal) todayRow.get("totalRevenue");

        BigDecimal yOcc = (BigDecimal) yestRow.get("occ");
        BigDecimal yAdr = (BigDecimal) yestRow.get("adr");
        BigDecimal yRevpar = (BigDecimal) yestRow.get("revpar");
        BigDecimal yRevenue = (BigDecimal) yestRow.get("totalRevenue");

        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("occ", occ);
        metrics.put("adr", adr);
        metrics.put("revpar", revpar);
        metrics.put("roomRevenue", todayRow.get("roomRevenue"));
        metrics.put("otherRevenue", todayRow.get("otherRevenue"));
        metrics.put("totalRevenue", totalRevenue);
        // 环比：出租率用百分点差，其余用相对变化 %
        metrics.put("occChange", occ.subtract(yOcc).setScale(2, RoundingMode.HALF_UP));
        metrics.put("adrChange", ratioChange(adr, yAdr));
        metrics.put("revparChange", ratioChange(revpar, yRevpar));
        metrics.put("revenueChange", ratioChange(totalRevenue, yRevenue));
        return metrics;
    }

    private BigDecimal ratioChange(BigDecimal current, BigDecimal base) {
        if (base == null || base.compareTo(BigDecimal.ZERO) == 0) {
            return current == null || current.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : null;
        }
        return current.subtract(base).multiply(BigDecimal.valueOf(100))
                .divide(base, 2, RoundingMode.HALF_UP);
    }

    // ==================== 房态 ====================

    private Map<String, Object> buildRoomStatus(LocalDate today) {
        List<RoomDO> rooms = roomMapper.selectList();
        Set<Long> occupied = occupiedRoomIds(today);
        Set<Long> cleaning = cleaningRoomIds(today);
        int total = rooms.size();
        int occ = 0, cleaningCount = 0, maintenance = 0, available = 0;
        for (RoomDO room : rooms) {
            if (RoomStatusEnum.MAINTENANCE.getStatus().equals(room.getStatus())) {
                maintenance++;
            } else if (occupied.contains(room.getId())) {
                occ++;
            } else if (cleaning.contains(room.getId()) || RoomStatusEnum.CLEANING.getStatus().equals(room.getStatus())) {
                cleaningCount++;
            } else {
                available++;
            }
        }
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("total", total);
        status.put("occupied", occ);
        status.put("cleaning", cleaningCount);
        status.put("maintenance", maintenance);
        status.put("available", available);
        return status;
    }

    private List<Map<String, Object>> buildRoomGrid(LocalDate today) {
        List<RoomDO> rooms = roomMapper.selectList();
        Map<Long, RoomTypeDO> typeMap = roomTypeMapper.selectList().stream()
                .collect(Collectors.toMap(RoomTypeDO::getId, Function.identity()));
        Set<Long> occupied = occupiedRoomIds(today);
        Set<Long> cleaning = cleaningRoomIds(today);

        Map<String, List<Map<String, Object>>> byFloor = new TreeMap<>();
        for (RoomDO room : rooms) {
            String statusName;
            int status;
            if (RoomStatusEnum.MAINTENANCE.getStatus().equals(room.getStatus())) {
                status = RoomStatusEnum.MAINTENANCE.getStatus();
                statusName = RoomStatusEnum.MAINTENANCE.getName();
            } else if (occupied.contains(room.getId())) {
                status = RoomStatusEnum.OCCUPIED.getStatus();
                statusName = RoomStatusEnum.OCCUPIED.getName();
            } else if (cleaning.contains(room.getId()) || RoomStatusEnum.CLEANING.getStatus().equals(room.getStatus())) {
                status = RoomStatusEnum.CLEANING.getStatus();
                statusName = RoomStatusEnum.CLEANING.getName();
            } else {
                status = RoomStatusEnum.AVAILABLE.getStatus();
                statusName = RoomStatusEnum.AVAILABLE.getName();
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("roomId", room.getId());
            item.put("roomNo", room.getRoomNo());
            item.put("status", status);
            item.put("statusName", statusName);
            item.put("roomTypeId", room.getRoomTypeId());
            RoomTypeDO type = typeMap.get(room.getRoomTypeId());
            item.put("roomTypeName", type == null ? "" : type.getName());
            String floor = String.valueOf(room.getRoomNo() == null ? "" : room.getRoomNo().charAt(0));
            byFloor.computeIfAbsent(floor, k -> new ArrayList<>()).add(item);
        }
        List<Map<String, Object>> grid = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : byFloor.entrySet()) {
            Map<String, Object> floor = new LinkedHashMap<>();
            floor.put("floor", entry.getKey());
            floor.put("rooms", entry.getValue());
            grid.add(floor);
        }
        return grid;
    }

    private Set<Long> occupiedRoomIds(LocalDate today) {
        List<OrderDO> active = orderMapper.selectList(
                new LambdaQueryWrapperX<OrderDO>()
                        .le(OrderDO::getCheckInDate, today)
                        .ge(OrderDO::getCheckOutDate, today)
                        .in(OrderDO::getStatus, OrderStatusEnum.CONFIRMED.getStatus(), OrderStatusEnum.CHECKED_IN.getStatus()));
        return active.stream().map(OrderDO::getRoomId).filter(java.util.Objects::nonNull).collect(Collectors.toSet());
    }

    private Set<Long> cleaningRoomIds(LocalDate today) {
        List<CleaningTaskDO> tasks = cleaningTaskMapper.selectList(
                new LambdaQueryWrapperX<CleaningTaskDO>()
                        .eq(CleaningTaskDO::getPlanDate, today)
                        .in(CleaningTaskDO::getStatus, CleaningTaskStatusEnum.PENDING.getStatus(), CleaningTaskStatusEnum.PROCESSING.getStatus()));
        return tasks.stream().map(CleaningTaskDO::getRoomId).filter(java.util.Objects::nonNull).collect(Collectors.toSet());
    }

    // ==================== 趋势 ====================

    private List<Map<String, Object>> buildTrend(LocalDate today) {
        LocalDate start = today.minusDays(TREND_DAYS - 1L);
        // 一次取房价日历与账务明细，按日期聚合
        List<RoomDailyRateDO> rates = rateMapper.selectList(
                new LambdaQueryWrapperX<RoomDailyRateDO>()
                        .ge(RoomDailyRateDO::getBizDate, start)
                        .le(RoomDailyRateDO::getBizDate, today));
        Map<LocalDate, Double> occMap = new HashMap<>();
        Map<LocalDate, BigDecimal> revenueMap = new HashMap<>();
        for (RoomDailyRateDO rate : rates) {
            if (rate.getTotalInventory() == null || rate.getTotalInventory() == 0) {
                continue;
            }
            double occ = (rate.getSoldInventory() == null ? 0D : rate.getSoldInventory() * 100D / rate.getTotalInventory());
            occMap.merge(rate.getBizDate(), occ, Double::max);
        }
        List<FolioItemDO> items = folioItemMapper.selectList(
                new LambdaQueryWrapperX<FolioItemDO>()
                        .ge(FolioItemDO::getBusinessDate, start)
                        .le(FolioItemDO::getBusinessDate, today)
                        .eq(FolioItemDO::getDirection, FolioDirectionEnum.DEBIT.getDirection())
                        .eq(FolioItemDO::getStatus, FolioItemStatusEnum.VALID.getStatus()));
        for (FolioItemDO item : items) {
            if (item.getAmount() == null) {
                continue;
            }
            revenueMap.merge(item.getBusinessDate(), item.getAmount(), BigDecimal::add);
        }
        List<Map<String, Object>> trend = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(today); date = date.plusDays(1)) {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", date.toString());
            point.put("occ", percent(occMap.getOrDefault(date, 0D)));
            point.put("revenue", revenueMap.getOrDefault(date, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP));
            trend.add(point);
        }
        return trend;
    }

    // ==================== 预抵/预离/待办 ====================

    private List<Map<String, Object>> buildTodayArrivals(LocalDate today) {
        List<OrderDO> orders = orderMapper.selectList(
                new LambdaQueryWrapperX<OrderDO>()
                        .eq(OrderDO::getCheckInDate, today)
                        .in(OrderDO::getStatus, OrderStatusEnum.PENDING.getStatus(), OrderStatusEnum.WAIT_CONFIRM.getStatus(),
                                OrderStatusEnum.CONFIRMED.getStatus(), OrderStatusEnum.CHECKED_IN.getStatus()));
        return toGuestRows(orders);
    }

    private List<Map<String, Object>> buildTodayDepartures(LocalDate today) {
        List<OrderDO> orders = orderMapper.selectList(
                new LambdaQueryWrapperX<OrderDO>()
                        .eq(OrderDO::getCheckOutDate, today)
                        .in(OrderDO::getStatus, OrderStatusEnum.CHECKED_IN.getStatus(), OrderStatusEnum.CHECKED_OUT.getStatus()));
        return toGuestRows(orders);
    }

    private List<Map<String, Object>> toGuestRows(List<OrderDO> orders) {
        if (orders.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> guestIds = orders.stream().map(OrderDO::getGuestId).filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        Map<Long, GuestDO> guestMap = guestMapper.selectBatchIds(guestIds).stream()
                .collect(Collectors.toMap(GuestDO::getId, Function.identity()));
        Set<Long> roomIds = orders.stream().map(OrderDO::getRoomId).filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        Map<Long, RoomDO> roomMap = roomMapper.selectBatchIds(roomIds).stream()
                .collect(Collectors.toMap(RoomDO::getId, Function.identity()));
        Set<Long> typeIds = orders.stream().map(OrderDO::getRoomTypeId).filter(java.util.Objects::nonNull).collect(Collectors.toSet());
        Map<Long, RoomTypeDO> typeMap = roomTypeMapper.selectBatchIds(typeIds).stream()
                .collect(Collectors.toMap(RoomTypeDO::getId, Function.identity()));

        List<Map<String, Object>> rows = new ArrayList<>();
        for (OrderDO order : orders) {
            GuestDO guest = guestMap.get(order.getGuestId());
            RoomDO room = roomMap.get(order.getRoomId());
            RoomTypeDO type = typeMap.get(order.getRoomTypeId());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("orderId", order.getId());
            row.put("orderNo", order.getOrderNo());
            row.put("guestId", order.getGuestId());
            row.put("guestName", guest == null ? "" : guest.getName());
            row.put("roomId", order.getRoomId());
            row.put("roomNo", room == null ? "" : room.getRoomNo());
            row.put("roomTypeName", type == null ? "" : type.getName());
            row.put("nights", order.getNights());
            row.put("amount", order.getAmount());
            row.put("status", order.getStatus());
            rows.add(row);
        }
        return rows;
    }

    private Map<String, Object> buildTodo(LocalDate today) {
        Long pendingConfirm = orderMapper.selectCount(new LambdaQueryWrapperX<OrderDO>()
                .eq(OrderDO::getStatus, OrderStatusEnum.WAIT_CONFIRM.getStatus()));
        Long dueCheckOuts = orderMapper.selectCount(new LambdaQueryWrapperX<OrderDO>()
                .eq(OrderDO::getCheckOutDate, today)
                .eq(OrderDO::getStatus, OrderStatusEnum.CHECKED_IN.getStatus()));
        Long openCleaning = cleaningTaskMapper.selectCount(new LambdaQueryWrapperX<CleaningTaskDO>()
                .eq(CleaningTaskDO::getPlanDate, today)
                .in(CleaningTaskDO::getStatus, CleaningTaskStatusEnum.PENDING.getStatus(), CleaningTaskStatusEnum.PROCESSING.getStatus()));
        Long unpaidOrders = folioMapper.selectCount(new LambdaQueryWrapperX<FolioDO>()
                .gt(FolioDO::getBalance, BigDecimal.ZERO)
                .in(FolioDO::getStatus, FolioStatusEnum.UNSETTLED.getStatus(), FolioStatusEnum.PARTIAL.getStatus()));
        Map<String, Object> todo = new LinkedHashMap<>();
        todo.put("pendingConfirm", pendingConfirm);
        todo.put("dueCheckOuts", dueCheckOuts);
        todo.put("openCleaning", openCleaning);
        todo.put("unpaidOrders", unpaidOrders);
        return todo;
    }
}
