package cn.iocoder.yudao.module.hotel.service.demo;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.hotel.dal.dataobject.audit.NightAuditDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.cleaning.CleaningTaskDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.demo.DemoSeedDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.folio.FolioDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.folio.FolioItemDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.folio.PaymentRecordDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.guest.GuestDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.order.OrderDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.rate.RoomDailyRateDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.room.RoomDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.roomtype.RoomTypeDO;
import cn.iocoder.yudao.module.hotel.dal.mysql.audit.NightAuditMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.cleaning.CleaningTaskMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.demo.DemoSeedMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.folio.FolioItemMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.folio.FolioMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.folio.PaymentRecordMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.guest.GuestMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.order.OrderMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.rate.RoomDailyRateMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.room.RoomMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.roomtype.RoomTypeMapper;
import cn.iocoder.yudao.module.hotel.enums.CleaningTaskStatusEnum;
import cn.iocoder.yudao.module.hotel.enums.CleaningTypeEnum;
import cn.iocoder.yudao.module.hotel.enums.DailyRateStatusEnum;
import cn.iocoder.yudao.module.hotel.enums.FolioDirectionEnum;
import cn.iocoder.yudao.module.hotel.enums.FolioItemStatusEnum;
import cn.iocoder.yudao.module.hotel.enums.FolioItemTypeEnum;
import cn.iocoder.yudao.module.hotel.enums.FolioStatusEnum;
import cn.iocoder.yudao.module.hotel.enums.FolioTypeEnum;
import cn.iocoder.yudao.module.hotel.enums.GuestLevelEnum;
import cn.iocoder.yudao.module.hotel.enums.NightAuditStatusEnum;
import cn.iocoder.yudao.module.hotel.enums.OrderStatusEnum;
import cn.iocoder.yudao.module.hotel.enums.OrderTypeEnum;
import cn.iocoder.yudao.module.hotel.enums.PaymentMethodEnum;
import cn.iocoder.yudao.module.hotel.enums.PaymentRecordStatusEnum;
import cn.iocoder.yudao.module.hotel.enums.PaymentStatusEnum;
import cn.iocoder.yudao.module.hotel.enums.PaymentTypeEnum;
import cn.iocoder.yudao.module.hotel.enums.RoomStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hotel.enums.HotelT4ErrorCodeConstants.ERROR_DEMO_DATA_EXISTS;
import static cn.iocoder.yudao.module.hotel.enums.HotelT4ErrorCodeConstants.ERROR_DEMO_MONTHS_INVALID;

/**
 * 演示数据生成器 Service 实现
 *
 * <p>固定随机种子（{@link #SEED}），6 个月约 400-600 笔订单；淡旺季（7-8 月峰）+ 周末峰 + 价格浮动；
 * 所有数据 remark=T4-demo 打标，{@code clean} 幂等精确清理本包数据。</p>
 */
@Service
@Validated
@Slf4j
public class DemoDataServiceImpl implements DemoDataService {

    private static final long SEED = 20261021L;
    /** 数据打标 */
    private static final String DEMO_TAG = "T4-demo";
    private static final int MAX_ORDERS = 900;
    private static final int GUEST_COUNT = 50;
    private static final int FUTURE_BOOKING_DAYS = 3;
    /** 近端优先窗口：这段日期的订单优先生成，保证大屏 / 日报 / 月报看得到真实客流 */
    private static final int RECENT_DAYS = 45;

    private static final DateTimeFormatter ORDER_TIME_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** 入住率：月份基准（淡季低、7-8 月峰） */
    private static final Map<Integer, Double> MONTH_OCC = Map.ofEntries(
            Map.entry(1, 0.18D), Map.entry(2, 0.20D), Map.entry(3, 0.24D), Map.entry(4, 0.27D),
            Map.entry(5, 0.32D), Map.entry(6, 0.34D), Map.entry(7, 0.50D), Map.entry(8, 0.53D),
            Map.entry(9, 0.45D), Map.entry(10, 0.62D), Map.entry(11, 0.25D), Map.entry(12, 0.19D));
    /** 入住率：星期系数（周末峰） */
    private static final Map<Integer, Double> WEEKEND_OCC = Map.of(
            1, 0.72D, 2, 0.72D, 3, 0.72D, 4, 0.80D, 5, 1.30D, 6, 1.40D, 7, 1.05D);
    /** 房价：月份浮动 */
    private static final Map<Integer, Double> MONTH_PRICE = Map.ofEntries(
            Map.entry(1, 0.80D), Map.entry(2, 0.85D), Map.entry(3, 0.90D), Map.entry(4, 0.95D),
            Map.entry(5, 1.00D), Map.entry(6, 1.05D), Map.entry(7, 1.35D), Map.entry(8, 1.45D),
            Map.entry(9, 1.00D), Map.entry(10, 1.15D), Map.entry(11, 0.90D), Map.entry(12, 0.82D));
    /** 房价：星期系数 */
    private static final Map<Integer, Double> WEEKEND_PRICE = Map.of(
            1, 0.88D, 2, 0.82D, 3, 0.85D, 4, 0.95D, 5, 1.25D, 6, 1.35D, 7, 1.05D);
    /** 晚数分布：1-5 晚 */
    private static final int[] NIGHT_WEIGHT = {45, 35, 12, 5, 3};
    /** 客人类别分布：散客 60% 会员 25% 协议 10% 中介 5% */
    private static final int[] LEVEL_WEIGHT = {30, 13, 5, 2};
    /** 支付方式分布：现金 25 银行卡 10 微信 35 支付宝 25 在线 5 */
    private static final int[] METHOD_WEIGHT = {25, 10, 35, 25, 5};

    private static final String[] SURNAMES = {"陈", "王", "李", "张", "刘", "杨", "黄", "赵", "周", "吴",
            "徐", "孙", "马", "朱", "胡", "郭", "何", "林", "罗", "郑"};
    private static final String[] GIVEN_NAMES = {"建国", "丽华", "志强", "晓明", "文静", "伟东", "雅婷", "俊杰", "雪梅", "国栋",
            "婷婷", "超群", "丽娟", "永强", "静怡", "浩然", "美玲", "子涵", "欣怡", "思远"};

    @Resource
    private OrderMapper orderMapper;
    @Resource
    private FolioMapper folioMapper;
    @Resource
    private FolioItemMapper folioItemMapper;
    @Resource
    private PaymentRecordMapper paymentRecordMapper;
    @Resource
    private GuestMapper guestMapper;
    @Resource
    private RoomMapper roomMapper;
    @Resource
    private RoomTypeMapper roomTypeMapper;
    @Resource
    private RoomDailyRateMapper rateMapper;
    @Resource
    private CleaningTaskMapper cleaningTaskMapper;
    @Resource
    private NightAuditMapper nightAuditMapper;
    @Resource
    private DemoSeedMapper demoSeedMapper;
    @Resource
    private JdbcTemplate jdbcTemplate;

    // ==================== 主流程 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> generate(int months) {
        if (months < 1 || months > 12) {
            throw exception(ERROR_DEMO_MONTHS_INVALID);
        }
        // 防重：存在进行中批次则拒绝
        DemoSeedDO latest = demoSeedMapper.selectLatest();
        if (latest != null && Integer.valueOf(10).equals(latest.getStatus())) {
            throw exception(ERROR_DEMO_DATA_EXISTS);
        }
        // 幂等清理历史残留
        cleanInternal();

        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(months * 30L - 1);
        LocalDate end = today.plusDays(FUTURE_BOOKING_DAYS);
        java.util.Random rnd = new java.util.Random(SEED);

        List<RoomDO> rooms = roomMapper.selectList();
        List<RoomTypeDO> roomTypes = roomTypeMapper.selectList();
        if (rooms.isEmpty() || roomTypes.isEmpty()) {
            throw exception(cn.iocoder.yudao.module.hotel.enums.HotelT4ErrorCodeConstants.ERROR_DEMO_GENERATE_FAILED);
        }
        Map<Long, RoomTypeDO> typeById = roomTypes.stream()
                .collect(Collectors.toMap(RoomTypeDO::getId, Function.identity()));

        // 1. 住客池（先落库拿到 id）
        List<GuestDO> guests = buildGuests(rnd);
        for (GuestDO guest : guests) {
            guestMapper.insert(guest);
        }
        List<Long> guestIds = guests.stream().map(GuestDO::getId).toList();

        // 2. 生成订单计划（覆盖式补缺：保证每日入住数接近目标，周末峰/淡旺季自然呈现）
        List<OrderPlan> plans = new ArrayList<>();
        Map<LocalDate, Integer> coverage = new HashMap<>();
        Map<LocalDate, List<OrderPlan>> plansByDate = new TreeMap<>();
        // 顺序「先近后远」：先铺满今天与最近 RECENT_DAYS 天，再回头补历史。
        // 原因：MAX_ORDERS 是总配额，若从最早日期开始生成，配额会在走到近端之前用尽，
        // 导致大屏 / 日报看到的「今天」几乎没有客流（出租率趋近 0），演示直接翻车。
        List<LocalDate> dateSeq = new ArrayList<>();
        LocalDate nearStart = today.minusDays(RECENT_DAYS);
        if (nearStart.isBefore(start)) {
            nearStart = start;
        }
        for (LocalDate d = nearStart; !d.isAfter(end); d = d.plusDays(1)) {
            dateSeq.add(d);
        }
        for (LocalDate d = start; d.isBefore(nearStart); d = d.plusDays(1)) {
            dateSeq.add(d);
        }
        for (LocalDate d : dateSeq) {
            int target = targetRooms(d, rooms.size(), rnd);
            if (d.isAfter(today)) {
                target = Math.max(1, (int) Math.round(target * 0.35D)); // 未来预抵少一些
            }
            while (coverage.getOrDefault(d, 0) < target && plans.size() < MAX_ORDERS) {
                OrderPlan plan = new OrderPlan();
                plan.checkIn = d;
                plan.nights = pickNights(rnd);
                plan.guestIdx = rnd.nextInt(guestIds.size());
                plan.room = rooms.get(rnd.nextInt(rooms.size()));
                plan.roomType = typeById.get(plan.room.getRoomTypeId());
                for (int i = 0; i < plan.nights; i++) {
                    coverage.merge(d.plusDays(i), 1, Integer::sum);
                }
                plans.add(plan);
                plansByDate.computeIfAbsent(d, k -> new ArrayList<>()).add(plan);
            }
        }

        // 3. 打状态（过去订单：已退房为主 + 少量取消；今日：在住/预抵；未来：已确认）
        Map<LocalDate, Double> roomChargeByDate = new HashMap<>();
        for (OrderPlan plan : plans) {
            if (plan.checkIn.isAfter(today)) {
                plan.status = rnd.nextDouble() < 0.05 ? OrderStatusEnum.WAIT_CONFIRM.getStatus() : OrderStatusEnum.CONFIRMED.getStatus();
            } else if (plan.checkIn.equals(today)) {
                // 今日预抵：部分已入住，部分待入住
                plan.status = rnd.nextDouble() < 0.6 ? OrderStatusEnum.CHECKED_IN.getStatus() : OrderStatusEnum.CONFIRMED.getStatus();
            } else {
                boolean cancelled = plan.checkOutDate().isBefore(today) && rnd.nextDouble() < 0.06;
                plan.status = cancelled ? OrderStatusEnum.CANCELLED.getStatus() : OrderStatusEnum.CHECKED_OUT.getStatus();
            }
            if (!OrderStatusEnum.CANCELLED.getStatus().equals(plan.status)) {
                for (int i = 0; i < plan.nights; i++) {
                    double price = effectivePrice(plan.roomType, plan.checkIn.plusDays(i));
                    plan.nightPrices.add(price);
                    roomChargeByDate.merge(plan.checkIn.plusDays(i), price, Double::sum);
                }
            }
        }

        // 4. 房价日历（可售/在售，仅统计有效订单）
        Map<String, Integer> soldByTypeDate = new HashMap<>();
        for (OrderPlan plan : plans) {
            if (OrderStatusEnum.CANCELLED.getStatus().equals(plan.status)) {
                continue;
            }
            for (int i = 0; i < plan.nights; i++) {
                LocalDate dd = plan.checkIn.plusDays(i);
                if (!dd.isAfter(today)) {
                    String key = plan.room.getRoomTypeId() + "_" + dd;
                    soldByTypeDate.merge(key, 1, Integer::sum);
                }
            }
        }
        int rateCount = 0;
        for (RoomTypeDO type : roomTypes) {
            long typeRooms = rooms.stream().filter(r -> r.getRoomTypeId().equals(type.getId())).count();
            for (LocalDate d = start; !d.isAfter(today); d = d.plusDays(1)) {
                RoomDailyRateDO exists = rateMapper.selectOne(
                        new LambdaQueryWrapperX<RoomDailyRateDO>()
                                .eq(RoomDailyRateDO::getRoomTypeId, type.getId())
                                .eq(RoomDailyRateDO::getBizDate, d)
                                .last("LIMIT 1"));
                String key = type.getId() + "_" + d;
                int sold = soldByTypeDate.getOrDefault(key, 0);
                int price = (int) Math.round(effectivePrice(type, d));
                if (exists != null) {
                    if (DEMO_TAG.equals(exists.getRemark())) {
                        // 历史演示残留行：刷新价格与在售
                        exists.setPrice(BigDecimal.valueOf(price));
                        exists.setSoldInventory(sold);
                        exists.setTotalInventory((int) typeRooms);
                        exists.setStatus(DailyRateStatusEnum.OPEN.getStatus());
                        rateMapper.updateById(exists);
                    }
                    // 非演示行（其它 agent 维护）不触碰
                } else {
                    RoomDailyRateDO rate = new RoomDailyRateDO();
                    rate.setRoomTypeId(type.getId());
                    rate.setBizDate(d);
                    rate.setPrice(BigDecimal.valueOf(price));
                    rate.setTotalInventory((int) typeRooms);
                    rate.setSoldInventory(sold);
                    rate.setStatus(DailyRateStatusEnum.OPEN.getStatus());
                    rate.setRemark(DEMO_TAG);
                    rate.setCreateTime(d.atTime(8, 0));
                    rate.setUpdateTime(d.atTime(8, 0));
                    rateMapper.insert(rate);
                    rateCount++;
                }
            }
        }

        // 5. 落库：订单 + 账页 + 明细 + 支付
        List<Long> inHouseRoomIds = new ArrayList<>();
        long folioSeq = nextFolioSeq(today);
        long orderSeq = nextOrderSeq(today);
        long tradeSeq = nextTradeSeq(today);
        for (OrderPlan plan : plans) {
            int nights = plan.nights;
            LocalDate checkOut = plan.checkOutDate();
            BigDecimal amount = BigDecimal.valueOf(plan.nightPrices.stream().mapToDouble(d -> d).sum())
                    .setScale(2, RoundingMode.HALF_UP);
            boolean cancelled = OrderStatusEnum.CANCELLED.getStatus().equals(plan.status);
            boolean checkedOut = OrderStatusEnum.CHECKED_OUT.getStatus().equals(plan.status);
            boolean inHouse = OrderStatusEnum.CHECKED_IN.getStatus().equals(plan.status);

            // 订单
            OrderDO order = new OrderDO();
            order.setOrderNo("HT4" + today.format(DATE_FMT) + String.format("%04d", ++orderSeq));
            order.setGuestId(guestIds.get(plan.guestIdx));
            order.setRoomId(plan.room.getId());
            order.setRoomTypeId(plan.room.getRoomTypeId());
            order.setOrderType(OrderTypeEnum.FULL_DAY.getType());
            order.setBusinessDate(plan.checkIn.isAfter(today) ? today : plan.checkIn);
            order.setCheckInDate(plan.checkIn);
            order.setCheckOutDate(checkOut);
            order.setCheckInTime(plan.checkIn.atTime(14, 0));
            order.setCheckOutTime(checkOut.atTime(12, 0));
            order.setNights(nights);
            order.setAdultCount(1 + rnd.nextInt(3));
            order.setChildCount(rnd.nextInt(2));
            order.setAmount(amount);
            order.setRemark(DEMO_TAG);
            order.setCreateTime(plan.checkIn.atTime(10 + rnd.nextInt(8), rnd.nextInt(60)));
            order.setUpdateTime(order.getCreateTime());
            if (cancelled) {
                order.setStatus(OrderStatusEnum.CANCELLED.getStatus());
                order.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());
                order.setDeposit(BigDecimal.ZERO);
                order.setPaidAmount(BigDecimal.ZERO);
            } else if (checkedOut) {
                order.setStatus(OrderStatusEnum.CHECKED_OUT.getStatus());
                order.setPaymentStatus(PaymentStatusEnum.PAID.getStatus());
                order.setDeposit(BigDecimal.ZERO);
                order.setPaidAmount(amount);
                order.setActualCheckInTime(plan.checkIn.atTime(13 + rnd.nextInt(4), rnd.nextInt(60)));
                order.setActualCheckOutTime(checkOut.atTime(11 + rnd.nextInt(2), rnd.nextInt(60)));
            } else if (inHouse) {
                order.setStatus(OrderStatusEnum.CHECKED_IN.getStatus());
                boolean paidDeposit = rnd.nextDouble() < 0.7;
                BigDecimal deposit = paidDeposit ? amount.multiply(BigDecimal.valueOf(0.3 + rnd.nextDouble() * 0.2))
                        .setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
                order.setDeposit(deposit);
                order.setPaidAmount(deposit);
                order.setPaymentStatus(deposit.compareTo(BigDecimal.ZERO) > 0 ? PaymentStatusEnum.PARTIAL.getStatus() : PaymentStatusEnum.UNPAID.getStatus());
                order.setActualCheckInTime(order.getCreateTime());
                inHouseRoomIds.add(plan.room.getId());
            } else {
                // 未来预抵
                order.setStatus(plan.status);
                order.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());
                order.setDeposit(BigDecimal.ZERO);
                order.setPaidAmount(BigDecimal.ZERO);
            }
            orderMapper.insert(order);

            // 主账页
            FolioDO folio = new FolioDO();
            folio.setFolioNo("F9" + today.format(DATE_FMT) + String.format("%06d", ++folioSeq));
            folio.setOrderId(order.getId());
            folio.setGuestId(order.getGuestId());
            folio.setType(FolioTypeEnum.MAIN.getType());
            folio.setStatus(cancelled ? FolioStatusEnum.CANCELLED.getStatus()
                    : (checkedOut ? FolioStatusEnum.SETTLED.getStatus()
                    : (order.getPaymentStatus().equals(PaymentStatusEnum.PARTIAL.getStatus())
                    ? FolioStatusEnum.PARTIAL.getStatus() : FolioStatusEnum.UNSETTLED.getStatus())));
            folio.setRemark(DEMO_TAG);
            folio.setCreateTime(order.getCreateTime());
            folio.setUpdateTime(order.getCreateTime());
            folioMapper.insert(folio);

            // 账务明细（房费逐夜 + 杂项 + 收款贷方）
            BigDecimal totalCharge = BigDecimal.ZERO;
            BigDecimal totalPayment = BigDecimal.ZERO;
            List<FolioItemDO> items = new ArrayList<>();
            for (int i = 0; i < plan.nightPrices.size(); i++) {
                LocalDate bizDate = plan.checkIn.plusDays(i);
                BigDecimal nightPrice = BigDecimal.valueOf(plan.nightPrices.get(i));
                FolioItemDO item = new FolioItemDO();
                item.setFolioId(folio.getId());
                item.setOrderId(order.getId());
                item.setBusinessDate(bizDate);
                item.setItemType(FolioItemTypeEnum.ROOM.getType());
                item.setItemName(FolioItemTypeEnum.ROOM.getName());
                item.setDirection(FolioDirectionEnum.DEBIT.getDirection());
                item.setQuantity(1);
                item.setUnitPrice(nightPrice);
                item.setAmount(nightPrice);
                item.setStatus(FolioItemStatusEnum.VALID.getStatus());
                item.setOperatorId(1L);
                item.setRemark(DEMO_TAG);
                item.setCreateTime(bizDate.atTime(23, 50));
                item.setUpdateTime(item.getCreateTime());
                items.add(item);
                totalCharge = totalCharge.add(nightPrice);
            }
            // 杂项消费：约 35% 的订单产生
            if (!cancelled && rnd.nextDouble() < 0.35) {
                int extraCount = 1 + rnd.nextInt(2);
                for (int e = 0; e < extraCount; e++) {
                    FolioItemDO item = new FolioItemDO();
                    item.setFolioId(folio.getId());
                    item.setOrderId(order.getId());
                    LocalDate bizDate = plan.checkIn.plusDays(rnd.nextInt(nights));
                    item.setBusinessDate(bizDate);
                    int typePick = rnd.nextInt(100);
                    if (typePick < 50) {
                        item.setItemType(FolioItemTypeEnum.FOOD.getType());
                        item.setItemName(FolioItemTypeEnum.FOOD.getName());
                        item.setUnitPrice(BigDecimal.valueOf(30 + rnd.nextInt(91)));
                    } else if (typePick < 75) {
                        item.setItemType(FolioItemTypeEnum.MINIBAR.getType());
                        item.setItemName(FolioItemTypeEnum.MINIBAR.getName());
                        item.setUnitPrice(BigDecimal.valueOf(15 + rnd.nextInt(66)));
                    } else {
                        item.setItemType(FolioItemTypeEnum.LAUNDRY.getType());
                        item.setItemName(FolioItemTypeEnum.LAUNDRY.getName());
                        item.setUnitPrice(BigDecimal.valueOf(20 + rnd.nextInt(41)));
                    }
                    item.setDirection(FolioDirectionEnum.DEBIT.getDirection());
                    item.setQuantity(1);
                    item.setAmount(item.getUnitPrice());
                    item.setStatus(FolioItemStatusEnum.VALID.getStatus());
                    item.setOperatorId(1L);
                    item.setRemark(DEMO_TAG);
                    item.setCreateTime(bizDate.atTime(19 + rnd.nextInt(3), rnd.nextInt(60)));
                    item.setUpdateTime(item.getCreateTime());
                    items.add(item);
                    totalCharge = totalCharge.add(item.getAmount());
                }
            }
            // 支付
            List<PaymentRecordDO> payments = new ArrayList<>();
            if (checkedOut || inHouse) {
                if (inHouse && order.getDeposit().compareTo(BigDecimal.ZERO) <= 0) {
                    // 无押金在住：不产生支付
                } else {
                    BigDecimal payAmount = checkedOut ? amount
                            : order.getDeposit().min(totalCharge);
                    PaymentRecordDO pay = new PaymentRecordDO();
                    pay.setFolioId(folio.getId());
                    pay.setOrderId(order.getId());
                    pay.setTradeNo("T4" + today.format(DATE_FMT) + String.format("%06d", ++tradeSeq));
                    pay.setAmount(payAmount);
                    pay.setMethod(pickMethod(rnd));
                    pay.setType(PaymentTypeEnum.INCOME.getType());
                    pay.setStatus(PaymentRecordStatusEnum.SUCCESS.getStatus());
                    pay.setPayTime(checkedOut ? checkOut.atTime(11, rnd.nextInt(60)) : order.getCreateTime());
                    pay.setOperatorId(1L);
                    pay.setRemark(DEMO_TAG);
                    pay.setCreateTime(pay.getPayTime());
                    pay.setUpdateTime(pay.getPayTime());
                    payments.add(pay);
                    totalPayment = totalPayment.add(payAmount);

                    FolioItemDO credit = new FolioItemDO();
                    credit.setFolioId(folio.getId());
                    credit.setOrderId(order.getId());
                    credit.setBusinessDate(pay.getPayTime().toLocalDate());
                    credit.setItemType(FolioItemTypeEnum.OTHER.getType());
                    credit.setItemName("收款");
                    credit.setDirection(FolioDirectionEnum.CREDIT.getDirection());
                    credit.setQuantity(1);
                    credit.setUnitPrice(payAmount);
                    credit.setAmount(payAmount);
                    credit.setPaymentMethod(pay.getMethod());
                    credit.setStatus(FolioItemStatusEnum.VALID.getStatus());
                    credit.setOperatorId(1L);
                    credit.setRemark(DEMO_TAG);
                    credit.setCreateTime(pay.getPayTime());
                    credit.setUpdateTime(pay.getPayTime());
                    items.add(credit);
                }
            }
            for (FolioItemDO item : items) {
                folioItemMapper.insert(item);
            }
            for (PaymentRecordDO pay : payments) {
                paymentRecordMapper.insert(pay);
            }

            // 回填账页汇总
            folio.setTotalCharge(totalCharge.setScale(2, RoundingMode.HALF_UP));
            folio.setTotalPayment(totalPayment.setScale(2, RoundingMode.HALF_UP));
            folio.setBalance(totalCharge.subtract(totalPayment).setScale(2, RoundingMode.HALF_UP));
            folioMapper.updateById(folio);
        }

        // 6. 清洁任务（退房清洁 + 今日待清洁日常任务）
        int cleaningCount = 0;
        for (OrderPlan plan : plans) {
            if (!OrderStatusEnum.CHECKED_OUT.getStatus().equals(plan.status)) {
                continue;
            }
            LocalDate planDate = plan.checkOutDate();
            CleaningTaskDO task = new CleaningTaskDO();
            task.setRoomId(plan.room.getId());
            task.setType(CleaningTypeEnum.CHECKOUT.getType());
            boolean finished = planDate.isBefore(today.minusDays(1)) || rnd.nextDouble() < 0.8;
            task.setStatus(finished ? (rnd.nextDouble() < 0.7 ? CleaningTaskStatusEnum.FINISHED.getStatus()
                    : CleaningTaskStatusEnum.VERIFIED.getStatus()) : CleaningTaskStatusEnum.FINISHED.getStatus());
            task.setPlanDate(planDate);
            task.setFinishTime(finished ? planDate.atTime(13 + rnd.nextInt(4), rnd.nextInt(60)) : null);
            task.setRemark(DEMO_TAG);
            task.setCreateTime(planDate.atTime(12, 30));
            task.setUpdateTime(task.getCreateTime());
            cleaningTaskMapper.insert(task);
            cleaningCount++;
        }
        // 今日待清洁：随机 2-3 间空房，日常清洁待处理
        List<RoomDO> idleRooms = rooms.stream()
                .filter(r -> !inHouseRoomIds.contains(r.getId()))
                .collect(Collectors.toList());
        int todayCleaning = Math.min(3, Math.max(2, idleRooms.size() / 8));
        for (int i = 0; i < todayCleaning && i < idleRooms.size(); i++) {
            RoomDO room = idleRooms.get(i);
            CleaningTaskDO task = new CleaningTaskDO();
            task.setRoomId(room.getId());
            task.setType(CleaningTypeEnum.DAILY.getType());
            task.setStatus(CleaningTaskStatusEnum.PENDING.getStatus());
            task.setPlanDate(today);
            task.setRemark(DEMO_TAG);
            task.setCreateTime(today.atTime(8, 0));
            task.setUpdateTime(task.getCreateTime());
            cleaningTaskMapper.insert(task);
            cleaningCount++;
        }

        // 7. 夜审记录（历史营业日：operator_id=-1 打标便于清理）
        int auditCount = 0;
        for (LocalDate d = start; !d.isAfter(today.minusDays(1)); d = d.plusDays(1)) {
            NightAuditDO audit = new NightAuditDO();
            audit.setBizDate(d);
            audit.setStatus(NightAuditStatusEnum.SUCCESS.getStatus());
            audit.setStartTime(d.atTime(23, 50));
            audit.setEndTime(d.plusDays(1).atTime(0, 2));
            int soldRooms = soldRoomsOn(soldByTypeDate, d);
            audit.setRoomChargeTotal(BigDecimal.valueOf(roomChargeByDate.getOrDefault(d, 0D)).setScale(2, RoundingMode.HALF_UP));
            audit.setPostedRoomCount(soldRooms);
            audit.setPostedItemCount(soldRooms);
            audit.setOperatorId(-1L);
            audit.setCreateTime(d.atTime(23, 55));
            audit.setUpdateTime(audit.getCreateTime());
            nightAuditMapper.insert(audit);
            auditCount++;
        }

        // 8. 在住房间房态置为已入住（演示一致性，清理时回滚）
        for (Long roomId : inHouseRoomIds) {
            jdbcTemplate.update("UPDATE hotel_room SET status = " + RoomStatusEnum.OCCUPIED.getStatus()
                    + ", update_time = NOW() WHERE id = ? AND deleted = b'0'", roomId);
        }

        // 9. 会员数据（T2 表存在则写入，否则跳过不报错）
        try {
            insertMembersIfPresent(guests);
        } catch (Exception ex) {
            log.warn("[generate][写入会员数据失败，已跳过：{}]", ex.getMessage());
        }

        // 10. 演示批次
        DemoSeedDO seed = new DemoSeedDO();
        seed.setBatchNo("T4" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        seed.setMonths(months);
        seed.setStartDate(start);
        seed.setEndDate(today);
        seed.setOrderCount(plans.size());
        seed.setGuestCount(guests.size());
        seed.setRateCount(rateCount);
        seed.setStatus(10);
        seed.setRemark("演示数据：" + months + " 个月，" + plans.size() + " 笔订单");
        demoSeedMapper.insert(seed);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("batchNo", seed.getBatchNo());
        result.put("months", months);
        result.put("startDate", start.toString());
        result.put("endDate", today.toString());
        result.put("orderCount", plans.size());
        result.put("guestCount", guests.size());
        result.put("rateCount", rateCount);
        result.put("status", 10);
        result.put("message", "演示数据生成完成");
        log.info("[generate][完成] 批次={}，订单 {} 笔，住客 {} 人，房价日历 {} 行", seed.getBatchNo(),
                plans.size(), guests.size(), rateCount);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> clean() {
        cleanInternal();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", 20);
        result.put("message", "演示数据已清理");
        return result;
    }

    @Override
    public Map<String, Object> status() {
        DemoSeedDO latest = demoSeedMapper.selectLatest();
        Map<String, Object> result = new LinkedHashMap<>();
        if (latest == null) {
            result.put("exists", false);
            result.put("status", 20);
            result.put("message", "暂无演示数据");
            return result;
        }
        result.put("exists", Integer.valueOf(10).equals(latest.getStatus()));
        result.put("batchNo", latest.getBatchNo());
        result.put("months", latest.getMonths());
        result.put("startDate", latest.getStartDate() == null ? null : latest.getStartDate().toString());
        result.put("endDate", latest.getEndDate() == null ? null : latest.getEndDate().toString());
        result.put("orderCount", latest.getOrderCount());
        result.put("guestCount", latest.getGuestCount());
        result.put("rateCount", latest.getRateCount());
        result.put("status", latest.getStatus());
        result.put("message", Integer.valueOf(10).equals(latest.getStatus()) ? "演示数据存在" : "已清理");
        return result;
    }

    // ==================== 清理 ====================

    /**
     * 幂等清理：只删本包打标数据
     */
    private void cleanInternal() {
        // 1. 订单（含在住房间回滚）
        List<OrderDO> demoOrders = orderMapper.selectList(new LambdaQueryWrapperX<OrderDO>()
                .like(OrderDO::getRemark, DEMO_TAG));
        List<Long> orderIds = demoOrders.stream().map(OrderDO::getId).toList();
        if (!orderIds.isEmpty()) {
            // 在住房间回滚为空闲（无其它在住订单时）
            List<Long> roomIds = demoOrders.stream()
                    .filter(o -> OrderStatusEnum.CHECKED_IN.getStatus().equals(o.getStatus()))
                    .map(OrderDO::getRoomId).filter(java.util.Objects::nonNull).distinct().toList();
            for (Long roomId : roomIds) {
                Long activeOthers = orderMapper.selectCount(new LambdaQueryWrapperX<OrderDO>()
                        .eq(OrderDO::getRoomId, roomId)
                        .in(OrderDO::getStatus, OrderStatusEnum.CHECKED_IN.getStatus(), OrderStatusEnum.CONFIRMED.getStatus())
                        .ne(OrderDO::getRemark, DEMO_TAG));
                if (activeOthers == null || activeOthers == 0) {
                    jdbcTemplate.update("UPDATE hotel_room SET status = " + RoomStatusEnum.AVAILABLE.getStatus()
                            + ", update_time = NOW() WHERE id = ? AND deleted = b'0'", roomId);
                }
            }
            List<Long> ids = orderIds;
            paymentRecordMapper.delete(new LambdaQueryWrapperX<PaymentRecordDO>().in(PaymentRecordDO::getOrderId, ids));
            folioItemMapper.delete(new LambdaQueryWrapperX<FolioItemDO>().in(FolioItemDO::getOrderId, ids));
            folioMapper.delete(new LambdaQueryWrapperX<FolioDO>().in(FolioDO::getOrderId, ids));
            orderMapper.deleteByIds(ids);
        }
        // 2. 住客（先物理清理关联会员行以释放 uk_member_no，再逻辑删住客）
        List<GuestDO> demoGuests = guestMapper.selectList(new LambdaQueryWrapperX<GuestDO>()
                .like(GuestDO::getRemark, DEMO_TAG));
        if (!demoGuests.isEmpty()) {
            List<Long> demoGuestIds = demoGuests.stream().map(GuestDO::getId).toList();
            try {
                String ids = demoGuestIds.stream().map(String::valueOf).collect(Collectors.joining(","));
                jdbcTemplate.update("DELETE FROM hotel_member WHERE guest_id IN (" + ids + ")");
            } catch (Exception ex) {
                log.warn("[clean][会员行清理失败，可忽略：{}]", ex.getMessage());
            }
            guestMapper.deleteByIds(demoGuestIds);
        }
        // 3. 清洁任务
        cleaningTaskMapper.delete(new LambdaQueryWrapperX<CleaningTaskDO>().like(CleaningTaskDO::getRemark, DEMO_TAG));
        // 4. 夜审（operator_id=-1 打标）
        nightAuditMapper.delete(new LambdaQueryWrapperX<NightAuditDO>()
                .eq(NightAuditDO::getOperatorId, -1L));
        // 5. 房价日历
        rateMapper.delete(new LambdaQueryWrapperX<RoomDailyRateDO>().eq(RoomDailyRateDO::getRemark, DEMO_TAG));
        // 6. 演示批次置为已清理
        DemoSeedDO latest = demoSeedMapper.selectLatest();
        if (latest != null) {
            latest.setStatus(20);
            demoSeedMapper.updateById(latest);
        }
    }

    // ==================== 内部工具 ====================

    private List<GuestDO> buildGuests(java.util.Random rnd) {
        List<GuestDO> guests = new ArrayList<>();
        for (int i = 0; i < GUEST_COUNT; i++) {
            GuestDO guest = new GuestDO();
            String name = SURNAMES[i % SURNAMES.length] + GIVEN_NAMES[(i * 7 + 3) % GIVEN_NAMES.length];
            guest.setName(name);
            guest.setMobile("139" + String.format("%08d", 10000000 + i * 7));
            guest.setGuestLevel(pickLevel(rnd));
            guest.setRemark(DEMO_TAG);
            guest.setCreateTime(LocalDate.now().minusDays(180).atTime(9, 0));
            guest.setUpdateTime(guest.getCreateTime());
            guests.add(guest);
        }
        return guests;
    }

    private int targetRooms(LocalDate d, int totalRooms, java.util.Random rnd) {
        double monthFactor = MONTH_OCC.getOrDefault(d.getMonthValue(), 0.3D);
        double weekendFactor = WEEKEND_OCC.getOrDefault(d.getDayOfWeek().getValue(), 0.9D);
        double noise = 0.85 + rnd.nextDouble() * 0.30;
        int target = (int) Math.round(totalRooms * monthFactor * weekendFactor * noise);
        return Math.max(1, Math.min(totalRooms, target));
    }

    private double effectivePrice(RoomTypeDO roomType, LocalDate d) {
        double base = roomType.getBasePrice() == null ? 300D : roomType.getBasePrice().doubleValue();
        double monthFactor = MONTH_PRICE.getOrDefault(d.getMonthValue(), 1.0D);
        double weekendFactor = WEEKEND_PRICE.getOrDefault(d.getDayOfWeek().getValue(), 0.9D);
        return Math.round(base * monthFactor * weekendFactor);
    }

    private int pickNights(java.util.Random rnd) {
        int total = 0;
        for (int w : NIGHT_WEIGHT) {
            total += w;
        }
        int pick = rnd.nextInt(total);
        int acc = 0;
        for (int i = 0; i < NIGHT_WEIGHT.length; i++) {
            acc += NIGHT_WEIGHT[i];
            if (pick < acc) {
                return i + 1;
            }
        }
        return 1;
    }

    private int pickLevel(java.util.Random rnd) {
        int total = 0;
        for (int w : LEVEL_WEIGHT) {
            total += w;
        }
        int pick = rnd.nextInt(total);
        int acc = 0;
        Integer[] levels = {GuestLevelEnum.FIT.getLevel(), GuestLevelEnum.MEMBER.getLevel(),
                GuestLevelEnum.COMPANY.getLevel(), GuestLevelEnum.AGENT.getLevel()};
        for (int i = 0; i < levels.length; i++) {
            acc += LEVEL_WEIGHT[i];
            if (pick < acc) {
                return levels[i];
            }
        }
        return GuestLevelEnum.FIT.getLevel();
    }

    private int pickMethod(java.util.Random rnd) {
        int total = 0;
        for (int w : METHOD_WEIGHT) {
            total += w;
        }
        int pick = rnd.nextInt(total);
        int acc = 0;
        Integer[] methods = {PaymentMethodEnum.CASH.getMethod(), PaymentMethodEnum.CARD.getMethod(),
                PaymentMethodEnum.WECHAT.getMethod(), PaymentMethodEnum.ALIPAY.getMethod(),
                PaymentMethodEnum.ONLINE.getMethod()};
        for (int i = 0; i < methods.length; i++) {
            acc += METHOD_WEIGHT[i];
            if (pick < acc) {
                return methods[i];
            }
        }
        return PaymentMethodEnum.WECHAT.getMethod();
    }

    private long nextFolioSeq(LocalDate today) {
        // 注意：uk_folio_no 唯一键不含 deleted 列，逻辑删除后号码仍被占用，
        // 因此必须用原生 SQL（含已删行）取真实最大号，避免幂等重跑撞号。
        // 前缀 F9 与 P0 的 F+日期 号段隔离，避免演示数据阻塞业务开账。
        List<String> nos = jdbcTemplate.queryForList(
                "SELECT folio_no FROM hotel_folio WHERE folio_no LIKE ? ORDER BY folio_no DESC LIMIT 1",
                String.class, "F9" + today.format(DATE_FMT) + "%");
        if (nos.isEmpty() || nos.get(0) == null || nos.get(0).length() <= 10) {
            return 0;
        }
        try {
            return Long.parseLong(nos.get(0).substring(10));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private long nextOrderSeq(LocalDate today) {
        List<String> nos = jdbcTemplate.queryForList(
                "SELECT order_no FROM hotel_order WHERE order_no LIKE ? ORDER BY order_no DESC LIMIT 1",
                String.class, "HT4" + today.format(DATE_FMT) + "%");
        if (nos.isEmpty() || nos.get(0) == null || nos.get(0).length() <= 17) {
            return 0;
        }
        try {
            return Long.parseLong(nos.get(0).substring(17));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private long nextTradeSeq(LocalDate today) {
        List<String> nos = jdbcTemplate.queryForList(
                "SELECT trade_no FROM hotel_payment_record WHERE trade_no LIKE ? ORDER BY trade_no DESC LIMIT 1",
                String.class, "T4" + today.format(DATE_FMT) + "%");
        if (nos.isEmpty() || nos.get(0) == null || nos.get(0).length() <= 17) {
            return 0;
        }
        try {
            return Long.parseLong(nos.get(0).substring(17));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private int soldRoomsOn(Map<String, Integer> soldByTypeDate, LocalDate d) {
        int total = 0;
        for (Map.Entry<String, Integer> entry : soldByTypeDate.entrySet()) {
            if (entry.getKey().endsWith("_" + d)) {
                total += entry.getValue();
            }
        }
        return total;
    }

    /**
     * T2 会员表存在则写入会员数据（列交集方式，表不存在/结构未就绪时静默跳过）
     */
    private void insertMembersIfPresent(List<GuestDO> guests) {
        Integer tableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'hotel_member'",
                Integer.class);
        if (tableCount == null || tableCount == 0) {
            return;
        }
        List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'hotel_member'");
        java.util.Set<String> cols = columns.stream()
                .map(m -> String.valueOf(m.get("column_name")).toLowerCase()).collect(Collectors.toSet());
        if (cols.contains("guest_id")) {
            Long tenantId = TenantContextHolder.getTenantId() == null ? 1L : TenantContextHolder.getTenantId();
            int count = 0;
            for (GuestDO guest : guests) {
                if (!GuestLevelEnum.MEMBER.getLevel().equals(guest.getGuestLevel())) {
                    continue;
                }
                StringBuilder sql = new StringBuilder("INSERT INTO hotel_member (");
                StringBuilder values = new StringBuilder(" VALUES (");
                List<Object> args = new ArrayList<>();
                int idx = 0;
                if (cols.contains("guest_id")) {
                    sql.append("guest_id"); values.append("?"); args.add(guest.getId()); idx++;
                }
                if (cols.contains("mobile")) {
                    sql.append(idx > 0 ? ",mobile" : "mobile"); values.append(idx > 0 ? ",?" : "?");
                    args.add(guest.getMobile()); idx++;
                }
                if (cols.contains("name")) {
                    sql.append(idx > 0 ? ",name" : "name"); values.append(idx > 0 ? ",?" : "?");
                    args.add(guest.getName()); idx++;
                }
                if (cols.contains("level")) {
                    sql.append(idx > 0 ? ",level" : "level"); values.append(idx > 0 ? ",?" : "?");
                    args.add(10); idx++;
                }
                if (cols.contains("status")) {
                    sql.append(idx > 0 ? ",status" : "status"); values.append(idx > 0 ? ",?" : "?");
                    args.add(0); idx++;
                }
                if (cols.contains("points")) {
                    sql.append(idx > 0 ? ",points" : "points"); values.append(idx > 0 ? ",?" : "?");
                    args.add(100 + guest.getId().intValue() % 900); idx++;
                }
                if (cols.contains("member_no")) {
                    sql.append(idx > 0 ? ",member_no" : "member_no"); values.append(idx > 0 ? ",?" : "?");
                    args.add("M" + guest.getMobile().substring(6)); idx++;
                }
                if (cols.contains("tenant_id")) {
                    sql.append(idx > 0 ? ",tenant_id" : "tenant_id"); values.append(idx > 0 ? ",?" : "?");
                    args.add(tenantId); idx++;
                }
                sql.append(idx > 0 ? ",create_time" : "create_time");
                values.append(idx > 0 ? ",NOW()" : "NOW()");
                sql.append(")").append(values).append(")");
                jdbcTemplate.update(sql.toString(), args.toArray());
                count++;
            }
            log.info("[generate][会员数据] 写入 {} 条", count);
        }
    }

    /** 订单计划（内存态） */
    private static class OrderPlan {
        private LocalDate checkIn;
        private int nights;
        private int guestIdx;
        private RoomDO room;
        private RoomTypeDO roomType;
        private Integer status;
        private final List<Double> nightPrices = new ArrayList<>();

        private LocalDate checkOutDate() {
            return checkIn.plusDays(nights);
        }
    }
}
