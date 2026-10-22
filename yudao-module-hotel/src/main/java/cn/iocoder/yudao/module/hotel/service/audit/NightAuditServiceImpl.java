package cn.iocoder.yudao.module.hotel.service.audit;

import cn.iocoder.yudao.framework.common.exception.ServiceException;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.tenant.core.context.TenantContextHolder;
import cn.iocoder.yudao.module.hotel.controller.admin.audit.vo.NightAuditPageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.audit.vo.NightAuditRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.folio.vo.FolioPostReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.audit.NightAuditDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.order.OrderDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.room.RoomDO;
import cn.iocoder.yudao.module.hotel.dal.mysql.audit.NightAuditMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.folio.FolioItemMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.order.OrderMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.room.RoomMapper;
import cn.iocoder.yudao.module.hotel.enums.FolioItemStatusEnum;
import cn.iocoder.yudao.module.hotel.enums.FolioItemTypeEnum;
import cn.iocoder.yudao.module.hotel.enums.NightAuditStatusEnum;
import cn.iocoder.yudao.module.hotel.enums.OrderStatusEnum;
import cn.iocoder.yudao.module.hotel.service.folio.FolioService;
import cn.iocoder.yudao.module.hotel.service.operationlog.annotation.HotelOperationLog;
import cn.iocoder.yudao.module.hotel.service.rate.RateCalendarService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.hotel.enums.ErrorCodeConstants.*;

/**
 * 夜审 Service 实现
 *
 * <p>夜审流程（单事务）：校验 → 过账在住订单当日房费 → 写夜审记录 → 营业日 +1。
 * 任何一步异常都回滚，并单独落一条失败记录，营业日不推进。</p>
 */
@Service
@Validated
@Slf4j
public class NightAuditServiceImpl implements NightAuditService {

    private static final String LOCK_KEY_PREFIX = "hotel:night-audit:";

    @Resource
    private NightAuditMapper nightAuditMapper;
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private RoomMapper roomMapper;
    @Resource
    private FolioItemMapper folioItemMapper;
    @Resource
    private FolioService folioService;
    @Resource
    private RateCalendarService rateCalendarService;
    @Resource
    private BusinessDateService businessDateService;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @HotelOperationLog(module = "audit", operation = "夜审")
    public Long runNightAudit() {
        Long tenantId = TenantContextHolder.getTenantId();
        String lockKey = LOCK_KEY_PREFIX + (tenantId == null ? 0L : tenantId);
        Boolean locked = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, String.valueOf(System.currentTimeMillis()), Duration.ofMinutes(10));
        if (!Boolean.TRUE.equals(locked)) {
            throw exception(ERROR_NIGHT_AUDIT_ALREADY_DONE);
        }
        LocalDate bizDate = businessDateService.getCurrentBusinessDate();
        try {
            // 该营业日已成功夜审过，直接拒绝（不写失败记录，避免污染历史）
            checkNotAudited(bizDate);

            LocalDateTime startTime = LocalDateTime.now();
            try {
                Long id = transactionTemplate.execute(status -> doAudit(bizDate, startTime));
                return id;
            } catch (RuntimeException ex) {
                // 事务已回滚，单独落一条失败记录，营业日不推进
                recordFailure(bizDate, startTime, ex);
                throw ex;
            }
        } finally {
            stringRedisTemplate.delete(lockKey);
        }
    }

    /**
     * 同一营业日只能夜审一次。
     *
     * <p>夜审成功后营业日会推进到 D+1。若 D+1 已经超过自然日，说明当天的夜审已经做完
     * （营业日不能「跑到未来」），再次触发一律视为重复夜审。</p>
     */
    private void checkNotAudited(LocalDate bizDate) {
        NightAuditDO success = nightAuditMapper.selectSuccessByDate(bizDate,
                NightAuditStatusEnum.SUCCESS.getStatus());
        if (success != null) {
            throw exception(ERROR_NIGHT_AUDIT_ALREADY_DONE);
        }
        if (bizDate.isAfter(LocalDate.now())) {
            throw exception(ERROR_NIGHT_AUDIT_ALREADY_DONE);
        }
    }

    private Long doAudit(LocalDate bizDate, LocalDateTime startTime) {
        // 1. 应离未离校验：已入住且离店日期 <= 营业日，说明前台漏办退房
        List<OrderDO> checkedInOrders = orderMapper.selectList(new LambdaQueryWrapperX<OrderDO>()
                .eq(OrderDO::getStatus, OrderStatusEnum.CHECKED_IN.getStatus()));
        List<String> overdue = new ArrayList<>();
        for (OrderDO order : checkedInOrders) {
            if (order.getCheckOutDate() != null && !order.getCheckOutDate().isAfter(bizDate)) {
                overdue.add(order.getOrderNo());
            }
        }
        if (!overdue.isEmpty()) {
            throw exception(ERROR_NIGHT_AUDIT_NOT_ALLOWED);
        }

        // 2. 过账当日房费
        BigDecimal roomChargeTotal = BigDecimal.ZERO;
        int postedRoomCount = 0;
        int postedItemCount = 0;
        for (OrderDO order : checkedInOrders) {
            RoomDO room = order.getRoomId() == null ? null : roomMapper.selectById(order.getRoomId());
            Long roomTypeId = order.getRoomTypeId() != null ? order.getRoomTypeId()
                    : (room == null ? null : room.getRoomTypeId());
            BigDecimal fallback = room == null ? BigDecimal.ZERO : room.getPrice();
            BigDecimal price = rateCalendarService.getEffectivePrice(roomTypeId, bizDate, fallback);
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            Long folioId = folioService.ensureFolioForOrder(order.getId(), order.getGuestId(), order.getRoomId());
            // 幂等保护：同营业日、同账页的房费只过账一次
            Long posted = folioItemMapper.selectCountRoomCharge(folioId, bizDate,
                    List.of(FolioItemStatusEnum.VALID.getStatus()));
            if (posted != null && posted > 0) {
                continue;
            }
            FolioPostReqVO reqVO = new FolioPostReqVO();
            reqVO.setFolioId(folioId);
            reqVO.setItemType(FolioItemTypeEnum.ROOM.getType());
            reqVO.setItemName("房费 " + bizDate);
            reqVO.setQuantity(1);
            reqVO.setUnitPrice(price);
            reqVO.setAmount(price);
            reqVO.setBusinessDate(bizDate);
            reqVO.setRemark("夜审过账");
            folioService.postCharge(reqVO);

            roomChargeTotal = roomChargeTotal.add(price);
            postedRoomCount++;
            postedItemCount++;
        }

        // 3. 写夜审记录
        NightAuditDO audit = new NightAuditDO();
        audit.setBizDate(bizDate);
        audit.setStatus(NightAuditStatusEnum.SUCCESS.getStatus());
        audit.setStartTime(startTime);
        audit.setEndTime(LocalDateTime.now());
        audit.setRoomChargeTotal(roomChargeTotal);
        audit.setPostedRoomCount(postedRoomCount);
        audit.setPostedItemCount(postedItemCount);
        audit.setOperatorId(getLoginUserId());
        nightAuditMapper.insert(audit);

        // 4. 营业日 +1
        businessDateService.rollBusinessDate();
        log.info("[doAudit][营业日 {} 夜审完成，过账房费 {}，房数 {}]", bizDate, roomChargeTotal, postedRoomCount);
        return audit.getId();
    }

    private void recordFailure(LocalDate bizDate, LocalDateTime startTime, RuntimeException ex) {
        try {
            String reason = ex instanceof ServiceException ? ex.getMessage() : String.valueOf(ex.getMessage());
            if (reason != null && reason.length() > 900) {
                reason = reason.substring(0, 900);
            }
            NightAuditDO exists = nightAuditMapper.selectOne(new LambdaQueryWrapperX<NightAuditDO>()
                    .eq(NightAuditDO::getBizDate, bizDate)
                    .eq(NightAuditDO::getStatus, NightAuditStatusEnum.FAILED.getStatus())
                    .last("LIMIT 1"));
            if (exists != null) {
                NightAuditDO update = new NightAuditDO();
                update.setId(exists.getId());
                update.setStartTime(startTime);
                update.setEndTime(LocalDateTime.now());
                update.setFailReason(reason);
                nightAuditMapper.updateById(update);
                return;
            }
            NightAuditDO audit = new NightAuditDO();
            audit.setBizDate(bizDate);
            audit.setStatus(NightAuditStatusEnum.FAILED.getStatus());
            audit.setStartTime(startTime);
            audit.setEndTime(LocalDateTime.now());
            audit.setRoomChargeTotal(BigDecimal.ZERO);
            audit.setPostedRoomCount(0);
            audit.setPostedItemCount(0);
            audit.setFailReason(reason);
            audit.setOperatorId(getLoginUserId());
            nightAuditMapper.insert(audit);
        } catch (Exception e) {
            log.warn("[recordFailure][夜审失败记录写入异常]", e);
        }
    }

    @Override
    public PageResult<NightAuditRespVO> getNightAuditPage(NightAuditPageReqVO reqVO) {
        PageResult<NightAuditDO> page = nightAuditMapper.selectPage(reqVO);
        List<NightAuditRespVO> list = new ArrayList<>(page.getList().size());
        page.getList().forEach(doAudit -> list.add(buildRespVO(doAudit)));
        return new PageResult<>(list, page.getTotal());
    }

    @Override
    public NightAuditRespVO getLastNightAudit() {
        NightAuditDO last = nightAuditMapper.selectLastSuccess(NightAuditStatusEnum.SUCCESS.getStatus());
        return last == null ? null : buildRespVO(last);
    }

    @Override
    public NightAuditRespVO getNightAudit(Long id) {
        NightAuditDO audit = id == null ? null : nightAuditMapper.selectById(id);
        return audit == null ? null : buildRespVO(audit);
    }

    private NightAuditRespVO buildRespVO(NightAuditDO audit) {
        NightAuditRespVO vo = new NightAuditRespVO();
        vo.setId(audit.getId());
        vo.setBizDate(audit.getBizDate());
        vo.setStatus(audit.getStatus());
        for (NightAuditStatusEnum item : NightAuditStatusEnum.values()) {
            if (Objects.equals(item.getStatus(), audit.getStatus())) {
                vo.setStatusName(item.getName());
            }
        }
        vo.setStartTime(audit.getStartTime());
        vo.setEndTime(audit.getEndTime());
        if (audit.getStartTime() != null && audit.getEndTime() != null) {
            vo.setDurationMs(ChronoUnit.MILLIS.between(audit.getStartTime(), audit.getEndTime()));
        }
        vo.setRoomChargeTotal(audit.getRoomChargeTotal());
        vo.setPostedRoomCount(audit.getPostedRoomCount());
        vo.setPostedItemCount(audit.getPostedItemCount());
        vo.setFailReason(audit.getFailReason());
        vo.setCreateTime(audit.getCreateTime());
        return vo;
    }
}
