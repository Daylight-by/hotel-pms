package cn.iocoder.yudao.module.hotel.controller.app.pay;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.hotel.controller.app.pay.vo.AppPayCreateReqVO;
import cn.iocoder.yudao.module.hotel.controller.app.pay.vo.AppPayRespVO;
import cn.iocoder.yudao.module.hotel.controller.app.pay.vo.AppPayResultRespVO;
import cn.iocoder.yudao.module.hotel.service.pay.PayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 用户 App - 预订支付（与 T1 预订引擎共用 /hotel/booking 前缀，本类只负责 pay / pay-result）。
 *
 * <p>说明：T1 的 AppBookingController 只实现 availability / hold / submit，
 * 支付契约（P1 §2.2 第 4、5 项）由 T2 在本类补齐，避免同一路径重复映射。</p>
 */
@Tag(name = "用户 App - 预订支付")
@RestController
@RequestMapping("/hotel/booking")
@Validated
public class AppBookingPayController {

    @Resource
    private PayService payService;

    @PostMapping("/pay")
    @PermitAll
    @Operation(summary = "创建预订支付单")
    public CommonResult<AppPayRespVO> pay(@Valid @RequestBody AppPayCreateReqVO reqVO) {
        return success(payService.createPayOrder(reqVO.getOrderId(), reqVO.getChannel()));
    }

    @GetMapping("/pay-result")
    @PermitAll
    @Operation(summary = "查询预订支付结果")
    public CommonResult<AppPayResultRespVO> payResult(@RequestParam("payOrderNo") String payOrderNo) {
        return success(payService.getPayResult(payOrderNo));
    }
}
