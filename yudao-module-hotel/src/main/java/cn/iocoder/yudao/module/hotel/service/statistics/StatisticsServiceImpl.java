package cn.iocoder.yudao.module.hotel.service.statistics;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.dal.dataobject.folio.FolioItemDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.folio.PaymentRecordDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.guest.GuestDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.order.OrderDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.rate.RoomDailyRateDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.room.RoomDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.roomtype.RoomTypeDO;
import cn.iocoder.yudao.module.hotel.dal.mysql.folio.FolioItemMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.folio.PaymentRecordMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.guest.GuestMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.order.OrderMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.rate.RoomDailyRateMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.room.RoomMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.roomtype.RoomTypeMapper;
import cn.iocoder.yudao.module.hotel.enums.*;
import cn.iocoder.yudao.module.hotel.enums.FolioDirectionEnum;
import cn.iocoder.yudao.module.hotel.enums.FolioItemTypeEnum;
import cn.iocoder.yudao.module.hotel.enums.GuestLevelEnum;
import cn.iocoder.yudao.module.hotel.enums.PaymentRecordStatusEnum;
import cn.iocoder.yudao.module.hotel.enums.OrderStatusEnum;
import cn.iocoder.yudao.module.hotel.enums.PaymentMethodEnum;
import cn.iocoder.yudao.module.hotel.enums.RoomStatusEnum;
import cn.iocoder.yudao.module.hotel.service.cleaning.CleaningTaskServiceImpl;
import cn.iocoder.yudao.module.hotel.service.order.OrderServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 酒店经营统计
 */
@Service
@Slf4j
public class StatisticsServiceImpl {

    @Resource
    private OrderMapper orderMapper;
    @Resource
    private RoomMapper roomMapper;
    @Resource
    private RoomTypeMapper roomTypeMapper;
    @Resource
    private GuestMapper guestMapper;
    @Resource
    private RoomDailyRateMapper rateMapper;
    @Resource
    private FolioItemMapper folioItemMapper;
    @Resource
    private PaymentRecordMapper paymentRecordMapper;
    @Resource
    private OrderServiceImpl orderService;
    @Resource
    private CleaningTaskServiceImpl cleaningTaskService;

    /** 参与合计的明细状态：有效 + 已冲销（冲销通过反向明细体现） */
    private static final List<Integer> TOTAL_STATUSES = List.of(
            FolioItemStatusEnum.VALID.getStatus(), FolioItemStatusEnum.VOIDED.getStatus());

    public Map<String, Object> overview() {
        LocalDate today = LocalDate.now();
        LocalDateTime dayStart = today.atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();

        List<OrderDO> todayCheckedOut = orderMapper.selectListByCheckOutTimeBetween(dayStart, dayEnd);
        List<OrderDO> monthCheckedOut = orderMapper.selectListByCheckOutTimeBetween(monthStart, dayEnd);
        double todayRevenue = sumPaid(todayCheckedOut);
        double monthRevenue = sumPaid(monthCheckedOut);

        List<RoomDO> rooms = roomMapper.selectListAll();
        long total = rooms.size();
        long occupied = rooms.stream().filter(r -> Objects.equals(r.getStatus(), RoomStatusEnum.OCCUPIED.getStatus())).count();
        long available = rooms.stream().filter(r -> Objects.equals(r.getStatus(), RoomStatusEnum.AVAILABLE.getStatus())).count();
        long cleaning = rooms.stream().filter(r -> Objects.equals(r.getStatus(), RoomStatusEnum.CLEANING.getStatus())).count();
        long maintenance = rooms.stream().filter(r -> Objects.equals(r.getStatus(), RoomStatusEnum.MAINTENANCE.getStatus())).count();
        double occupancyRate = total == 0 ? 0 : Math.round(occupied * 1000.0 / total) / 10.0;

        List<OrderDO> active = orderService.getActiveOrders();
        double expectedRevenue = active.stream()
                .map(o -> o.getAmount() == null ? BigDecimal.ZERO : o.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue();

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("todayRevenue", round2(todayRevenue));
        map.put("monthRevenue", round2(monthRevenue));
        map.put("todayOrders", orderMapper.selectCountByStatus(OrderStatusEnum.PENDING.getStatus())
                + orderMapper.selectCountByStatus(OrderStatusEnum.CONFIRMED.getStatus()));
        map.put("activeOrders", active.size());
        map.put("totalRooms", total);
        map.put("availableRooms", available);
        map.put("occupiedRooms", occupied);
        map.put("cleaningRooms", cleaning);
        map.put("maintenanceRooms", maintenance);
        map.put("occupancyRate", occupancyRate);
        map.put("expectedRevenue", round2(expectedRevenue));
        map.put("todayCheckIns", orderService.getTodayCheckIns().size());
        map.put("dueCheckOuts", orderService.getDueCheckOuts().size());
        map.put("openCleaningTasks", cleaningTaskService.countOpenTasks());
        return map;
    }

    /**
     * 近 N 天趋势：收入（按退房时间）、订单数（按创建时间）、入住率
     */
    public List<Map<String, Object>> trend(int days) {
        int window = Math.min(Math.max(days, 7), 60);
        LocalDate from = LocalDate.now().minusDays(window - 1L);
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = LocalDate.now().plusDays(1).atStartOfDay();

        List<OrderDO> orders = orderMapper.selectListByCheckOutTimeBetween(start, end);
        List<OrderDO> allOrders = orderMapper.selectListByStatus(Arrays.stream(OrderStatusEnum.values())
                .map(OrderStatusEnum::getStatus).toList());
        long totalRooms = roomMapper.selectListAll().size();

        Map<String, Double> revenueMap = new LinkedHashMap<>();
        Map<String, Long> orderMap = new LinkedHashMap<>();
        Map<String, Long> occupiedMap = new LinkedHashMap<>();
        for (int i = 0; i < window; i++) {
            String key = from.plusDays(i).toString().substring(5);
            revenueMap.put(key, 0.0);
            orderMap.put(key, 0L);
            occupiedMap.put(key, 0L);
        }
        for (OrderDO o : orders) {
            if (o.getActualCheckOutTime() != null) {
                String key = o.getActualCheckOutTime().toLocalDate().toString().substring(5);
                if (revenueMap.containsKey(key)) {
                    revenueMap.merge(key, (o.getPaidAmount() == null ? BigDecimal.ZERO : o.getPaidAmount()).doubleValue(), Double::sum);
                }
            }
        }
        for (OrderDO o : allOrders) {
            if (o.getCreateTime() != null) {
                String key = o.getCreateTime().toLocalDate().toString().substring(5);
                if (orderMap.containsKey(key)) {
                    orderMap.merge(key, 1L, Long::sum);
                }
            }
            for (int i = 0; i < window; i++) {
                LocalDate d = from.plusDays(i);
                boolean covers = o.getCheckInDate() != null && o.getCheckOutDate() != null
                        && !d.isBefore(o.getCheckInDate()) && d.isBefore(o.getCheckOutDate());
                boolean valid = Objects.equals(o.getStatus(), OrderStatusEnum.CHECKED_IN.getStatus())
                        || Objects.equals(o.getStatus(), OrderStatusEnum.CHECKED_OUT.getStatus());
                if (covers && valid) {
                    occupiedMap.merge(d.toString().substring(5), 1L, Long::sum);
                }
            }
        }

        List<Map<String, Object>> list = new ArrayList<>();
        revenueMap.forEach((date, revenue) -> {
            Map<String, Object> point = new LinkedHashMap<>();
            point.put("date", date);
            point.put("revenue", round2(revenue));
            point.put("orders", orderMap.getOrDefault(date, 0L));
            point.put("occupancyRate", totalRooms == 0 ? 0
                    : Math.round(occupiedMap.getOrDefault(date, 0L) * 1000.0 / totalRooms) / 10.0);
            list.add(point);
        });
        return list;
    }

    /**
     * 支付方式占比
     */
    public List<Map<String, Object>> paymentShare() {
        List<OrderDO> orders = orderMapper.selectListByStatus(List.of(OrderStatusEnum.CHECKED_OUT.getStatus()));
        Map<Integer, Double> map = new LinkedHashMap<>();
        for (PaymentMethodEnum method : PaymentMethodEnum.values()) {
            map.put(method.getMethod(), 0.0);
        }
        orders.forEach(o -> {
            if (o.getPaymentMethod() != null) {
                map.merge(o.getPaymentMethod(), (o.getPaidAmount() == null ? BigDecimal.ZERO : o.getPaidAmount()).doubleValue(), Double::sum);
            }
        });
        List<Map<String, Object>> list = new ArrayList<>();
        map.forEach((method, amount) -> {
            if (amount > 0) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("name", Arrays.stream(PaymentMethodEnum.values())
                        .filter(e -> Objects.equals(e.getMethod(), method))
                        .map(PaymentMethodEnum::getName).findFirst().orElse("其他"));
                item.put("value", round2(amount));
                list.add(item);
            }
        });
        return list;
    }

    private double sumPaid(List<OrderDO> orders) {
        return orders.stream()
                .map(o -> o.getPaidAmount() == null ? BigDecimal.ZERO : o.getPaidAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue();
    }

    // ============================================================ P0 经营指标

    /**
     * 核心经营指标：OCC / ADR / RevPAR / 房费收入 / 总营收 / 间夜 / 平均停留
     *
     * <ul>
     *   <li>可售房晚 = Σ(房型日历 total_inventory)，未维护的日期按房间数兜底</li>
     *   <li>已售房晚 = Σ(房型日历 sold_inventory)</li>
     *   <li>OCC = 已售房晚 / 可售房晚 × 100%</li>
     *   <li>ADR = 房费收入 / 已售房晚</li>
     *   <li>RevPAR = 房费收入 / 可售房晚 = OCC × ADR</li>
     * </ul>
     */
    public Map<String, Object> summary(LocalDate begin, LocalDate end) {
        LocalDate[] range = normalizeRange(begin, end, 7);
        begin = range[0];
        end = range[1];

        List<FolioItemDO> items = folioItemMapper.selectList(new LambdaQueryWrapperX<FolioItemDO>()
                .between(FolioItemDO::getBusinessDate, begin, end)
                .in(FolioItemDO::getStatus, TOTAL_STATUSES));
        BigDecimal roomRevenue = BigDecimal.ZERO;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        for (FolioItemDO item : items) {
            if (!Objects.equals(item.getDirection(), FolioDirectionEnum.DEBIT.getDirection())) {
                continue;
            }
            BigDecimal amount = item.getAmount() == null ? BigDecimal.ZERO : item.getAmount();
            totalRevenue = totalRevenue.add(amount);
            if (Objects.equals(item.getItemType(), FolioItemTypeEnum.ROOM.getType())) {
                roomRevenue = roomRevenue.add(amount);
            }
        }

        long availableNights = 0;
        long soldNights = 0;
        List<RoomTypeDO> roomTypes = roomTypeMapper.selectList(null);
        Map<Long, Integer> roomCountMap = new HashMap<>();
        for (RoomTypeDO type : roomTypes) {
            Long count = roomMapper.selectCountByRoomTypeId(type.getId());
            roomCountMap.put(type.getId(), count == null ? 0 : count.intValue());
        }
        Map<String, RoomDailyRateDO> rateMap = new HashMap<>();
        for (RoomDailyRateDO row : rateMapper.selectListByDateRange(begin, end)) {
            rateMap.put(row.getRoomTypeId() + "_" + row.getBizDate(), row);
        }
        for (RoomTypeDO type : roomTypes) {
            int defaultTotal = roomCountMap.getOrDefault(type.getId(), 0);
            for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
                RoomDailyRateDO row = rateMap.get(type.getId() + "_" + date);
                if (row == null) {
                    availableNights += defaultTotal;
                } else {
                    int total = row.getTotalInventory() == null ? 0 : row.getTotalInventory();
                    int sold = row.getSoldInventory() == null ? 0 : row.getSoldInventory();
                    availableNights += total;
                    soldNights += sold;
                }
            }
        }

        double occ = availableNights == 0 ? 0 : round2(soldNights * 100.0 / availableNights);
        double adr = soldNights == 0 ? 0 : round2(roomRevenue.doubleValue() / soldNights);
        double revpar = availableNights == 0 ? 0 : round2(roomRevenue.doubleValue() / availableNights);

        List<OrderDO> orders = orderMapper.selectList(new LambdaQueryWrapperX<OrderDO>()
                .le(OrderDO::getCheckInDate, end)
                .gt(OrderDO::getCheckOutDate, begin)
                .in(OrderDO::getStatus, List.of(OrderStatusEnum.CHECKED_IN.getStatus(),
                        OrderStatusEnum.CHECKED_OUT.getStatus())));
        long orderNights = orders.stream()
                .map(o -> o.getNights() == null ? 0 : o.getNights()).mapToLong(Integer::longValue).sum();
        double avgStay = orders.isEmpty() ? 0 : round2(orderNights * 1.0 / orders.size());

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("beginDate", begin.toString());
        map.put("endDate", end.toString());
        map.put("occ", occ);
        map.put("adr", adr);
        map.put("revpar", revpar);
        map.put("roomRevenue", round2(roomRevenue.doubleValue()));
        map.put("totalRevenue", round2(totalRevenue.doubleValue()));
        map.put("availableNights", availableNights);
        map.put("soldNights", soldNights);
        map.put("orderCount", orders.size());
        map.put("roomNights", orderNights);
        map.put("avgStay", avgStay);
        return map;
    }

    /**
     * 趋势：按日 / 按月返回房费收入与入住率
     */
    public List<Map<String, Object>> trendRange(LocalDate begin, LocalDate end, String typeParam) {
        LocalDate[] range = normalizeRange(begin, end, 14);
        begin = range[0];
        end = range[1];
        boolean monthly = "month".equalsIgnoreCase(typeParam);

        Map<String, Map<String, Object>> buckets = new LinkedHashMap<>();
        if (monthly) {
            LocalDate cursor = begin.withDayOfMonth(1);
            while (!cursor.isAfter(end)) {
                buckets.put(cursor.toString().substring(0, 7), emptyPoint(cursor.toString().substring(0, 7)));
                cursor = cursor.plusMonths(1);
            }
        } else {
            for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
                buckets.put(date.toString(), emptyPoint(date.toString()));
            }
        }

        List<FolioItemDO> items = folioItemMapper.selectList(new LambdaQueryWrapperX<FolioItemDO>()
                .between(FolioItemDO::getBusinessDate, begin, end)
                .eq(FolioItemDO::getItemType, FolioItemTypeEnum.ROOM.getType())
                .in(FolioItemDO::getStatus, TOTAL_STATUSES));
        for (FolioItemDO item : items) {
            if (item.getBusinessDate() == null
                    || !Objects.equals(item.getDirection(), FolioDirectionEnum.DEBIT.getDirection())) {
                continue;
            }
            String key = monthly ? item.getBusinessDate().toString().substring(0, 7)
                    : item.getBusinessDate().toString();
            Map<String, Object> point = buckets.get(key);
            if (point == null) {
                continue;
            }
            double value = point.get("revenue") == null ? 0.0 : ((Number) point.get("revenue")).doubleValue();
            point.put("revenue", round2(value + (item.getAmount() == null ? 0.0 : item.getAmount().doubleValue())));
        }

        // 入住率按营业日聚合
        Map<String, RoomDailyRateDO> rateMap = new HashMap<>();
        for (RoomDailyRateDO row : rateMapper.selectListByDateRange(begin, end)) {
            rateMap.put(row.getRoomTypeId() + "_" + row.getBizDate(), row);
        }
        List<RoomTypeDO> roomTypes = roomTypeMapper.selectList(null);
        for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
            String key = monthly ? date.toString().substring(0, 7) : date.toString();
            Map<String, Object> point = buckets.get(key);
            if (point == null) {
                continue;
            }
            int total = 0;
            int sold = 0;
            for (RoomTypeDO type : roomTypes) {
                RoomDailyRateDO row = rateMap.get(type.getId() + "_" + date);
                if (row == null) {
                    Long count = roomMapper.selectCountByRoomTypeId(type.getId());
                    total += count == null ? 0 : count.intValue();
                } else {
                    total += row.getTotalInventory() == null ? 0 : row.getTotalInventory();
                    sold += row.getSoldInventory() == null ? 0 : row.getSoldInventory();
                }
            }
            point.put("soldNights", ((Number) point.getOrDefault("soldNights", 0)).longValue() + sold);
            point.put("totalNights", ((Number) point.getOrDefault("totalNights", 0)).longValue() + total);
            long totalAll = ((Number) point.get("totalNights")).longValue();
            long soldAll = ((Number) point.get("soldNights")).longValue();
            point.put("occupancyRate", totalAll == 0 ? 0 : round2(soldAll * 100.0 / totalAll));
        }
        return new ArrayList<>(buckets.values());
    }

    /**
     * 客源结构与收款结构
     */
    public Map<String, Object> source(LocalDate begin, LocalDate end) {
        LocalDate[] range = normalizeRange(begin, end, 30);
        begin = range[0];
        end = range[1];

        List<OrderDO> orders = orderMapper.selectList(new LambdaQueryWrapperX<OrderDO>()
                .le(OrderDO::getCheckInDate, end)
                .gt(OrderDO::getCheckOutDate, begin)
                .ne(OrderDO::getStatus, OrderStatusEnum.CANCELLED.getStatus()));
        Set<Long> guestIds = new HashSet<>();
        orders.forEach(o -> {
            if (o.getGuestId() != null) {
                guestIds.add(o.getGuestId());
            }
        });
        Map<Long, GuestDO> guestMap = new HashMap<>();
        if (!guestIds.isEmpty()) {
            guestMapper.selectBatchIds(guestIds).forEach(g -> guestMap.put(g.getId(), g));
        }
        Map<Integer, Map<String, Object>> levelMap = new LinkedHashMap<>();
        for (GuestLevelEnum level : GuestLevelEnum.values()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", level.getName());
            item.put("value", 0);
            item.put("amount", 0.0);
            levelMap.put(level.getLevel(), item);
        }
        for (OrderDO order : orders) {
            GuestDO guest = guestMap.get(order.getGuestId());
            int level = guest == null || guest.getGuestLevel() == null ? 0 : guest.getGuestLevel();
            Map<String, Object> item = levelMap.computeIfAbsent(level, k -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("name", "其他");
                m.put("value", 0);
                m.put("amount", 0.0);
                return m;
            });
            item.put("value", ((Number) item.get("value")).intValue() + 1);
            item.put("amount", round2(((Number) item.get("amount")).doubleValue()
                    + (order.getAmount() == null ? 0.0 : order.getAmount().doubleValue())));
        }

        List<PaymentRecordDO> records = paymentRecordMapper.selectListByTimeRange(
                begin.atStartOfDay(), end.plusDays(1).atStartOfDay());
        Map<Integer, Double> methodMap = new LinkedHashMap<>();
        for (PaymentMethodEnum method : PaymentMethodEnum.values()) {
            methodMap.put(method.getMethod(), 0.0);
        }
        for (PaymentRecordDO record : records) {
            if (!Objects.equals(record.getStatus(), PaymentRecordStatusEnum.SUCCESS.getStatus())) {
                continue;
            }
            methodMap.merge(record.getMethod() == null ? 0 : record.getMethod(),
                    record.getAmount() == null ? 0.0 : record.getAmount().doubleValue(), Double::sum);
        }
        List<Map<String, Object>> paymentList = new ArrayList<>();
        methodMap.forEach((method, amount) -> {
            if (Math.abs(amount) < 0.001) {
                return;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("name", Arrays.stream(PaymentMethodEnum.values())
                    .filter(e -> Objects.equals(e.getMethod(), method))
                    .map(PaymentMethodEnum::getName).findFirst().orElse("其他"));
            item.put("value", round2(amount));
            paymentList.add(item);
        });

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("guestSource", new ArrayList<>(levelMap.values()));
        result.put("paymentSource", paymentList);
        return result;
    }

    private Map<String, Object> emptyPoint(String date) {
        Map<String, Object> point = new LinkedHashMap<>();
        point.put("date", date);
        point.put("revenue", 0.0);
        point.put("orders", 0);
        point.put("occupancyRate", 0);
        point.put("soldNights", 0L);
        point.put("totalNights", 0L);
        return point;
    }

    private LocalDate[] normalizeRange(LocalDate begin, LocalDate end, int defaultDays) {
        LocalDate today = LocalDate.now();
        if (end == null) {
            end = today;
        }
        if (begin == null) {
            begin = end.minusDays(defaultDays - 1L);
        }
        if (end.isBefore(begin)) {
            LocalDate tmp = begin;
            begin = end;
            end = tmp;
        }
        if (ChronoUnit.DAYS.between(begin, end) > 366) {
            begin = end.minusDays(366);
        }
        return new LocalDate[]{begin, end};
    }

    private double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
