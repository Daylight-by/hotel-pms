package cn.iocoder.yudao.module.hotel.service.rate;

import cn.iocoder.yudao.module.hotel.controller.admin.rate.vo.RateCalendarBoardRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.rate.vo.RoomDailyRateBatchSaveReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.rate.vo.RoomDailyRateRespVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.rate.RoomDailyRateDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.roomtype.RoomTypeDO;
import cn.iocoder.yudao.module.hotel.dal.mysql.rate.RoomDailyRateMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.room.RoomMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.roomtype.RoomTypeMapper;
import cn.iocoder.yudao.module.hotel.enums.DailyRateStatusEnum;
import cn.iocoder.yudao.module.hotel.service.operationlog.annotation.HotelOperationLog;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hotel.enums.ErrorCodeConstants.ERROR_DAILY_RATE_CLOSED;
import static cn.iocoder.yudao.module.hotel.enums.ErrorCodeConstants.ERROR_DAILY_RATE_INVENTORY_NOT_ENOUGH;

/**
 * 房价日历 Service 实现
 *
 * <p>库存口径：剩余房量 = total_inventory - sold_inventory，任何时刻不得为负。
 * 未维护的日期不阻断前台开单（按房型基础价兜底），保证「漏维护也能开门」。</p>
 */
@Service
@Validated
@Slf4j
public class RateCalendarServiceImpl implements RateCalendarService {

    /** 单次批量维护的最大天数，避免误操作写入过大数据 */
    private static final int MAX_BATCH_DAYS = 366;

    @Resource
    private RoomDailyRateMapper rateMapper;
    @Resource
    private RoomTypeMapper roomTypeMapper;
    @Resource
    private RoomMapper roomMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @HotelOperationLog(module = "rate", operation = "改价")
    public Long batchSave(RoomDailyRateBatchSaveReqVO reqVO) {
        RoomTypeDO roomType = roomTypeMapper.selectById(reqVO.getRoomTypeId());
        if (roomType == null) {
            throw exception(cn.iocoder.yudao.module.hotel.enums.ErrorCodeConstants.ROOM_TYPE_NOT_EXISTS);
        }
        LocalDate begin = reqVO.getBeginDate();
        LocalDate end = reqVO.getEndDate();
        if (end.isBefore(begin)) {
            LocalDate tmp = begin;
            begin = end;
            end = tmp;
        }
        long days = ChronoUnit.DAYS.between(begin, end) + 1;
        if (days > MAX_BATCH_DAYS) {
            end = begin.plusDays(MAX_BATCH_DAYS - 1L);
        }
        int roomCount = countRooms(reqVO.getRoomTypeId());
        Integer inventory = reqVO.getTotalInventory() == null ? roomCount : reqVO.getTotalInventory();
        Integer status = reqVO.getStatus() == null ? DailyRateStatusEnum.OPEN.getStatus() : reqVO.getStatus();
        BigDecimal price = reqVO.getPrice() == null ? roomType.getBasePrice() : reqVO.getPrice();

        int affected = 0;
        for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
            RoomDailyRateDO exists = rateMapper.selectByTypeAndDate(reqVO.getRoomTypeId(), date);
            if (exists == null) {
                RoomDailyRateDO row = new RoomDailyRateDO();
                row.setRoomTypeId(reqVO.getRoomTypeId());
                row.setBizDate(date);
                row.setPrice(price);
                row.setTotalInventory(Math.max(inventory, 0));
                row.setSoldInventory(0);
                row.setStatus(status);
                row.setRemark(reqVO.getRemark());
                rateMapper.insert(row);
            } else {
                RoomDailyRateDO update = new RoomDailyRateDO();
                update.setId(exists.getId());
                update.setPrice(price);
                // 总库存不得低于已售，否则剩余房量为负
                int sold = exists.getSoldInventory() == null ? 0 : exists.getSoldInventory();
                update.setTotalInventory(Math.max(inventory, sold));
                update.setStatus(status);
                if (reqVO.getRemark() != null) {
                    update.setRemark(reqVO.getRemark());
                }
                rateMapper.updateById(update);
            }
            affected++;
        }
        return (long) affected;
    }

    @Override
    public List<RoomDailyRateRespVO> listByRange(Long roomTypeId, LocalDate begin, LocalDate end) {
        if (roomTypeId == null || begin == null || end == null) {
            return new ArrayList<>();
        }
        if (end.isBefore(begin)) {
            LocalDate tmp = begin;
            begin = end;
            end = tmp;
        }
        RoomTypeDO roomType = roomTypeMapper.selectById(roomTypeId);
        BigDecimal fallbackPrice = roomType == null || roomType.getBasePrice() == null
                ? BigDecimal.ZERO : roomType.getBasePrice();
        Map<LocalDate, RoomDailyRateDO> rowMap = rateMapper.selectListByRange(roomTypeId, begin, end).stream()
                .collect(Collectors.toMap(RoomDailyRateDO::getBizDate, Function.identity(), (a, b) -> a));

        List<RoomDailyRateRespVO> list = new ArrayList<>();
        for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
            RoomDailyRateDO row = rowMap.get(date);
            RoomDailyRateRespVO vo = new RoomDailyRateRespVO();
            vo.setRoomTypeId(roomTypeId);
            vo.setRoomTypeName(roomType == null ? null : roomType.getName());
            vo.setBizDate(date);
            if (row == null) {
                // 未维护：价格取房型基础价、默认开房、库存 0
                vo.setPrice(fallbackPrice);
                vo.setTotalInventory(0);
                vo.setSoldInventory(0);
                vo.setRemaining(0);
                vo.setStatus(DailyRateStatusEnum.OPEN.getStatus());
                vo.setStatusName(DailyRateStatusEnum.OPEN.getName());
            } else {
                vo.setId(row.getId());
                vo.setPrice(row.getPrice());
                int total = row.getTotalInventory() == null ? 0 : row.getTotalInventory();
                int sold = row.getSoldInventory() == null ? 0 : row.getSoldInventory();
                vo.setTotalInventory(total);
                vo.setSoldInventory(sold);
                vo.setRemaining(Math.max(total - sold, 0));
                vo.setStatus(row.getStatus());
                vo.setStatusName(statusName(row.getStatus()));
                vo.setRemark(row.getRemark());
                vo.setUpdateTime(row.getUpdateTime());
            }
            list.add(vo);
        }
        return list;
    }

    @Override
    public void validateAvailable(Long roomTypeId, LocalDate checkIn, LocalDate checkOut,
                                  Integer roomCount, Long excludeOrderId) {
        if (roomTypeId == null || checkIn == null || checkOut == null) {
            return;
        }
        int need = roomCount == null ? 1 : Math.max(roomCount, 1);
        for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
            RoomDailyRateDO row = rateMapper.selectByTypeAndDate(roomTypeId, date);
            if (row == null) {
                // 未维护的日期：按兜底价可售，不阻断前台开单
                continue;
            }
            if (DailyRateStatusEnum.CLOSED.getStatus().equals(row.getStatus())) {
                throw exception(ERROR_DAILY_RATE_CLOSED);
            }
            int total = row.getTotalInventory() == null ? 0 : row.getTotalInventory();
            int sold = row.getSoldInventory() == null ? 0 : row.getSoldInventory();
            if (total - sold < need) {
                throw exception(ERROR_DAILY_RATE_INVENTORY_NOT_ENOUGH);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void occupy(Long roomTypeId, LocalDate date, int count) {
        if (roomTypeId == null || date == null || count <= 0) {
            return;
        }
        RoomDailyRateDO row = rateMapper.selectByTypeAndDate(roomTypeId, date);
        if (row == null) {
            // 未维护的日期首次售卖时落一条日历，便于后续房态盘统计
            RoomTypeDO roomType = roomTypeMapper.selectById(roomTypeId);
            RoomDailyRateDO create = new RoomDailyRateDO();
            create.setRoomTypeId(roomTypeId);
            create.setBizDate(date);
            create.setPrice(roomType == null || roomType.getBasePrice() == null
                    ? BigDecimal.ZERO : roomType.getBasePrice());
            create.setTotalInventory(Math.max(countRooms(roomTypeId), count));
            create.setSoldInventory(count);
            create.setStatus(DailyRateStatusEnum.OPEN.getStatus());
            rateMapper.insert(create);
            return;
        }
        if (DailyRateStatusEnum.CLOSED.getStatus().equals(row.getStatus())) {
            throw exception(ERROR_DAILY_RATE_CLOSED);
        }
        int updated = rateMapper.occupyInventory(roomTypeId, date, count);
        if (updated == 0) {
            throw exception(ERROR_DAILY_RATE_INVENTORY_NOT_ENOUGH);
        }
    }

    @Override
    public void release(Long roomTypeId, LocalDate date, int count) {
        if (roomTypeId == null || date == null || count <= 0) {
            return;
        }
        rateMapper.releaseInventory(roomTypeId, date, count);
    }

    @Override
    public List<RateCalendarBoardRespVO> getBoard(LocalDate begin, LocalDate end) {
        if (begin == null) {
            begin = LocalDate.now();
        }
        if (end == null || end.isBefore(begin)) {
            end = begin.plusDays(6);
        }
        List<RoomTypeDO> roomTypes = roomTypeMapper.selectListByStatus(0);
        if (roomTypes.isEmpty()) {
            roomTypes = roomTypeMapper.selectList(null);
        }
        List<RoomDailyRateDO> rows = rateMapper.selectListByDateRange(begin, end);
        Map<Long, Map<LocalDate, RoomDailyRateDO>> rowMap = new HashMap<>();
        for (RoomDailyRateDO row : rows) {
            rowMap.computeIfAbsent(row.getRoomTypeId(), k -> new HashMap<>()).put(row.getBizDate(), row);
        }

        List<RateCalendarBoardRespVO> board = new ArrayList<>();
        for (RoomTypeDO type : roomTypes) {
            RateCalendarBoardRespVO vo = new RateCalendarBoardRespVO();
            vo.setRoomTypeId(type.getId());
            vo.setRoomTypeName(type.getName());
            vo.setBasePrice(type.getBasePrice());
            int roomCount = countRooms(type.getId());
            vo.setRoomCount((long) roomCount);
            Map<LocalDate, RoomDailyRateDO> typeRows = rowMap.getOrDefault(type.getId(), Collections.emptyMap());
            List<RateCalendarBoardRespVO.Day> days = new ArrayList<>();
            for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
                RoomDailyRateDO row = typeRows.get(date);
                RateCalendarBoardRespVO.Day day = new RateCalendarBoardRespVO.Day();
                day.setBizDate(date);
                if (row == null) {
                    day.setPrice(type.getBasePrice());
                    day.setTotalInventory(roomCount);
                    day.setSoldInventory(0);
                    day.setRemaining(roomCount);
                    day.setStatus(DailyRateStatusEnum.OPEN.getStatus());
                } else {
                    int total = row.getTotalInventory() == null ? 0 : row.getTotalInventory();
                    int sold = row.getSoldInventory() == null ? 0 : row.getSoldInventory();
                    day.setPrice(row.getPrice());
                    day.setTotalInventory(total);
                    day.setSoldInventory(sold);
                    day.setRemaining(Math.max(total - sold, 0));
                    day.setStatus(row.getStatus());
                }
                days.add(day);
            }
            vo.setDays(days);
            board.add(vo);
        }
        return board;
    }

    @Override
    public BigDecimal getEffectivePrice(Long roomTypeId, LocalDate date, BigDecimal fallback) {
        if (roomTypeId == null || date == null) {
            return fallback == null ? BigDecimal.ZERO : fallback;
        }
        RoomDailyRateDO row = rateMapper.selectByTypeAndDate(roomTypeId, date);
        if (row != null && row.getPrice() != null) {
            return row.getPrice();
        }
        RoomTypeDO roomType = roomTypeMapper.selectById(roomTypeId);
        if (roomType != null && roomType.getBasePrice() != null) {
            return roomType.getBasePrice();
        }
        return fallback == null ? BigDecimal.ZERO : fallback;
    }

    private int countRooms(Long roomTypeId) {
        Long count = roomMapper.selectCountByRoomTypeId(roomTypeId);
        return count == null ? 0 : count.intValue();
    }

    private String statusName(Integer status) {
        return Arrays.stream(DailyRateStatusEnum.values())
                .filter(e -> Objects.equals(e.getStatus(), status))
                .map(DailyRateStatusEnum::getName).findFirst().orElse("");
    }
}
