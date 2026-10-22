package cn.iocoder.yudao.module.hotel.controller.admin.rate;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.rate.vo.RateCalendarBoardRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.rate.vo.RoomDailyRateBatchSaveReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.rate.vo.RoomDailyRatePageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.rate.vo.RoomDailyRateRespVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.rate.RoomDailyRateDO;
import cn.iocoder.yudao.module.hotel.dal.mysql.rate.RoomDailyRateMapper;
import cn.iocoder.yudao.module.hotel.service.rate.RateCalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Tag(name = "管理后台 - 酒店房价日历")
@RestController
@RequestMapping("/hotel/rate")
@Validated
public class RateController {

    @Resource
    private RateCalendarService rateCalendarService;
    @Resource
    private RoomDailyRateMapper rateMapper;

    @PostMapping("/batch-save")
    @Operation(summary = "批量维护房价日历")
    @PreAuthorize("@ss.hasPermission('hotel:rate:update')")
    public CommonResult<Long> batchSave(@Valid @RequestBody RoomDailyRateBatchSaveReqVO reqVO) {
        return success(rateCalendarService.batchSave(reqVO));
    }

    @GetMapping("/list")
    @Operation(summary = "按日期区间获得房价日历（缺失日期也会返回）")
    @Parameter(name = "roomTypeId", description = "房型编号", required = true)
    @Parameter(name = "beginDate", description = "开始日期", required = true)
    @Parameter(name = "endDate", description = "结束日期", required = true)
    @PreAuthorize("@ss.hasPermission('hotel:rate:query')")
    public CommonResult<List<RoomDailyRateRespVO>> list(
            @RequestParam("roomTypeId") Long roomTypeId,
            @RequestParam("beginDate") @DateTimeFormat(iso = DATE) LocalDate beginDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DATE) LocalDate endDate) {
        return success(rateCalendarService.listByRange(roomTypeId, beginDate, endDate));
    }

    @GetMapping("/board")
    @Operation(summary = "房态盘（房型 × 日期）")
    @PreAuthorize("@ss.hasPermission('hotel:rate:query')")
    public CommonResult<List<RateCalendarBoardRespVO>> board(
            @RequestParam(value = "beginDate", required = false) @DateTimeFormat(iso = DATE) LocalDate beginDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DATE) LocalDate endDate) {
        return success(rateCalendarService.getBoard(beginDate, endDate));
    }

    @GetMapping("/page")
    @Operation(summary = "获得房价日历分页")
    @PreAuthorize("@ss.hasPermission('hotel:rate:query')")
    public CommonResult<PageResult<RoomDailyRateRespVO>> page(@Valid RoomDailyRatePageReqVO reqVO) {
        PageResult<RoomDailyRateDO> page = rateMapper.selectPage(reqVO);
        List<RoomDailyRateRespVO> list = new ArrayList<>();
        page.getList().forEach(row -> {
            RoomDailyRateRespVO vo = new RoomDailyRateRespVO();
            vo.setId(row.getId());
            vo.setRoomTypeId(row.getRoomTypeId());
            vo.setBizDate(row.getBizDate());
            vo.setPrice(row.getPrice());
            vo.setTotalInventory(row.getTotalInventory());
            vo.setSoldInventory(row.getSoldInventory());
            int total = row.getTotalInventory() == null ? 0 : row.getTotalInventory();
            int sold = row.getSoldInventory() == null ? 0 : row.getSoldInventory();
            vo.setRemaining(Math.max(total - sold, 0));
            vo.setStatus(row.getStatus());
            vo.setRemark(row.getRemark());
            vo.setUpdateTime(row.getUpdateTime());
            list.add(vo);
        });
        return success(new PageResult<>(list, page.getTotal()));
    }
}
