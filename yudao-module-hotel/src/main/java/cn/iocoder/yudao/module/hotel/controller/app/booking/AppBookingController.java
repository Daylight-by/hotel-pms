package cn.iocoder.yudao.module.hotel.controller.app.booking;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.hotel.controller.app.booking.vo.AppAvailabilityReqVO;
import cn.iocoder.yudao.module.hotel.controller.app.booking.vo.AppBookingReqVO;
import cn.iocoder.yudao.module.hotel.controller.app.booking.vo.AppBookingRespVO;
import cn.iocoder.yudao.module.hotel.controller.app.booking.vo.AppHoldReqVO;
import cn.iocoder.yudao.module.hotel.controller.app.booking.vo.AppHoldRespVO;
import cn.iocoder.yudao.module.hotel.controller.app.booking.vo.AppRoomTypeAvailabilityRespVO;
import cn.iocoder.yudao.module.hotel.service.booking.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 用户 App - 酒店预订引擎（对外，免登录）
 */
@Tag(name = "用户 App - 酒店预订")
@RestController
@RequestMapping("/hotel/booking")
@Validated
public class AppBookingController {

    @Resource
    private BookingService bookingService;

    @GetMapping("/availability")
    @Operation(summary = "查询可售房型与价格")
    @PermitAll
    public CommonResult<List<AppRoomTypeAvailabilityRespVO>> availability(@Valid AppAvailabilityReqVO reqVO) {
        return success(bookingService.searchAvailability(reqVO));
    }

    @PostMapping("/hold")
    @Operation(summary = "创建预订占位（15 分钟有效）")
    @PermitAll
    public CommonResult<AppHoldRespVO> hold(@Valid @RequestBody AppHoldReqVO reqVO) {
        return success(bookingService.createHold(reqVO));
    }

    @PostMapping("/submit")
    @Operation(summary = "提交预订（生成待支付订单）")
    @PermitAll
    public CommonResult<AppBookingRespVO> submit(@Valid @RequestBody AppBookingReqVO reqVO) {
        return success(bookingService.submitBooking(reqVO));
    }
}
