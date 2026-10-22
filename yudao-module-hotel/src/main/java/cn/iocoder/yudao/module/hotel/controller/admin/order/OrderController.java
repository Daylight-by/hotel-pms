package cn.iocoder.yudao.module.hotel.controller.admin.order;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.order.vo.*;
import cn.iocoder.yudao.module.hotel.service.order.OrderServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 酒店订单")
@RestController
@RequestMapping("/hotel/order")
@Validated
public class OrderController {

    @Resource
    private OrderServiceImpl orderService;

    @PostMapping("/create")
    @Operation(summary = "创建订单（预订）")
    @PreAuthorize("@ss.hasPermission('hotel:order:create')")
    public CommonResult<Long> createOrder(@Valid @RequestBody OrderSaveReqVO createReqVO) {
        return success(orderService.createOrder(createReqVO));
    }

    @PostMapping("/create-hourly")
    @Operation(summary = "创建钟点房订单")
    @PreAuthorize("@ss.hasPermission('hotel:order:create')")
    public CommonResult<Long> createHourlyOrder(@Valid @RequestBody OrderHourlyCreateReqVO reqVO) {
        return success(orderService.createHourlyOrder(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改订单")
    @PreAuthorize("@ss.hasPermission('hotel:order:update')")
    public CommonResult<Boolean> updateOrder(@Valid @RequestBody OrderSaveReqVO updateReqVO) {
        orderService.updateOrder(updateReqVO);
        return success(true);
    }

    @PostMapping("/check-in")
    @Operation(summary = "办理入住")
    @Parameter(name = "id", description = "订单编号", required = true)
    @PreAuthorize("@ss.hasPermission('hotel:order:update')")
    public CommonResult<Boolean> checkIn(@RequestParam("id") Long id) {
        orderService.checkIn(id);
        return success(true);
    }

    @PostMapping("/check-out")
    @Operation(summary = "办理退房")
    @Parameter(name = "id", description = "订单编号", required = true)
    @PreAuthorize("@ss.hasPermission('hotel:order:update')")
    public CommonResult<Boolean> checkOut(@RequestParam("id") Long id) {
        orderService.checkOut(id);
        return success(true);
    }

    @PutMapping("/early-checkout")
    @Operation(summary = "提前退房（按实际夜数重算）")
    @Parameter(name = "id", description = "订单编号", required = true)
    @PreAuthorize("@ss.hasPermission('hotel:order:update')")
    public CommonResult<Long> earlyCheckout(@RequestParam("id") Long id) {
        return success(orderService.earlyCheckout(id));
    }

    @PutMapping("/extend")
    @Operation(summary = "续住")
    @PreAuthorize("@ss.hasPermission('hotel:order:update')")
    public CommonResult<Long> extend(@Valid @RequestBody OrderExtendReqVO reqVO) {
        return success(orderService.extendOrder(reqVO));
    }

    @PutMapping("/change-room")
    @Operation(summary = "换房")
    @PreAuthorize("@ss.hasPermission('hotel:order:update')")
    public CommonResult<Long> changeRoom(@Valid @RequestBody OrderChangeRoomReqVO reqVO) {
        return success(orderService.changeRoom(reqVO));
    }

    @PostMapping("/confirm")
    @Operation(summary = "确认订单")
    @Parameter(name = "id", description = "订单编号", required = true)
    @PreAuthorize("@ss.hasPermission('hotel:order:update')")
    public CommonResult<Boolean> confirm(@RequestParam("id") Long id) {
        orderService.confirmOrder(id);
        return success(true);
    }

    /**
     * 兼容旧接口：不带违约金 / 原因的取消
     */
    @PostMapping("/cancel")
    @Operation(summary = "取消订单")
    @Parameter(name = "id", description = "订单编号", required = true)
    @PreAuthorize("@ss.hasPermission('hotel:order:update')")
    public CommonResult<Boolean> cancel(@RequestParam("id") Long id) {
        orderService.cancelOrder(id);
        return success(true);
    }

    @PutMapping("/cancel")
    @Operation(summary = "取消订单（可带原因与违约金）")
    @Parameter(name = "id", description = "订单编号", required = true)
    @PreAuthorize("@ss.hasPermission('hotel:order:update')")
    public CommonResult<Boolean> cancelWithReason(@RequestParam(value = "id", required = false) Long id,
                                                  @RequestBody(required = false) OrderCancelReqVO reqVO) {
        Long orderId = id != null ? id : (reqVO == null ? null : reqVO.getId());
        String reason = reqVO == null ? null : reqVO.getReason();
        java.math.BigDecimal penalty = reqVO == null ? null : reqVO.getPenalty();
        orderService.cancelOrder(orderId, reason, penalty);
        return success(true);
    }

    @PostMapping("/payment")
    @Operation(summary = "登记支付信息")
    @Parameter(name = "id", description = "订单编号", required = true)
    @PreAuthorize("@ss.hasPermission('hotel:order:update')")
    public CommonResult<Boolean> payment(@RequestParam("id") Long id,
                                         @Valid @RequestBody OrderPaymentReqVO reqVO) {
        orderService.updatePayment(id, reqVO);
        return success(true);
    }

    @PostMapping("/refund")
    @Operation(summary = "订单退款")
    @Parameter(name = "id", description = "订单编号", required = true)
    @PreAuthorize("@ss.hasPermission('hotel:order:update')")
    public CommonResult<Boolean> refund(@RequestParam("id") Long id,
                                        @Valid @RequestBody OrderPaymentReqVO reqVO) {
        orderService.refund(id, reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除订单")
    @Parameter(name = "id", description = "订单编号", required = true)
    @PreAuthorize("@ss.hasPermission('hotel:order:delete')")
    public CommonResult<Boolean> deleteOrder(@RequestParam("id") Long id) {
        orderService.deleteOrder(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得订单")
    @Parameter(name = "id", description = "订单编号", required = true)
    @PreAuthorize("@ss.hasPermission('hotel:order:query')")
    public CommonResult<OrderRespVO> getOrder(@RequestParam("id") Long id) {
        return success(orderService.getOrderVO(id));
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得订单详情（订单 + 账页 + 明细 + 换房记录）")
    @Parameter(name = "id", description = "订单编号", required = true)
    @PreAuthorize("@ss.hasPermission('hotel:order:query')")
    public CommonResult<OrderDetailRespVO> getOrderDetail(@RequestParam("id") Long id) {
        return success(orderService.getOrderDetail(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获得订单分页")
    @PreAuthorize("@ss.hasPermission('hotel:order:query')")
    public CommonResult<PageResult<OrderRespVO>> getOrderPage(@Valid OrderPageReqVO pageReqVO) {
        return success(orderService.getOrderVOPage(pageReqVO));
    }

    @GetMapping("/recent")
    @Operation(summary = "最近订单")
    @PreAuthorize("@ss.hasPermission('hotel:order:query')")
    public CommonResult<List<OrderRespVO>> getRecentOrders(@RequestParam(value = "limit", defaultValue = "8") int limit) {
        return success(orderService.getRecentOrders(limit));
    }

    @GetMapping("/today-check-ins")
    @Operation(summary = "今日预抵")
    @PreAuthorize("@ss.hasPermission('hotel:order:query')")
    public CommonResult<List<OrderRespVO>> getTodayCheckIns() {
        return success(orderService.getTodayCheckInVOs());
    }

    @GetMapping("/due-check-outs")
    @Operation(summary = "今日待退房")
    @PreAuthorize("@ss.hasPermission('hotel:order:query')")
    public CommonResult<List<OrderRespVO>> getDueCheckOuts() {
        return success(orderService.getDueCheckOutVOs());
    }
}
