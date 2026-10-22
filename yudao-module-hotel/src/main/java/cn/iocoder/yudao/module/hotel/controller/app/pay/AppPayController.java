package cn.iocoder.yudao.module.hotel.controller.app.pay;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.hotel.controller.app.pay.vo.AppMockPayReqVO;
import cn.iocoder.yudao.module.hotel.controller.app.pay.vo.AppPayCreateReqVO;
import cn.iocoder.yudao.module.hotel.controller.app.pay.vo.AppPayRespVO;
import cn.iocoder.yudao.module.hotel.controller.app.pay.vo.AppPayResultRespVO;
import cn.iocoder.yudao.module.hotel.service.pay.PayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 用户 App - 在线支付。
 *
 * <p>Mock 渠道：创建支付单后调用 /mock-success 一键支付成功，供答辩演示。</p>
 */
@Tag(name = "用户 App - 酒店在线支付")
@RestController
@RequestMapping("/hotel/pay")
@Validated
public class AppPayController {

    @Resource
    private PayService payService;

    @PostMapping("/create")
    @PermitAll
    @Operation(summary = "创建支付单")
    public CommonResult<AppPayRespVO> createPayOrder(@Valid @RequestBody AppPayCreateReqVO reqVO) {
        return success(payService.createPayOrder(reqVO.getOrderId(), reqVO.getChannel()));
    }

    @GetMapping("/result")
    @PermitAll
    @Operation(summary = "查询支付结果")
    public CommonResult<AppPayResultRespVO> getPayResult(@RequestParam("payOrderNo") String payOrderNo) {
        return success(payService.getPayResult(payOrderNo));
    }

    @PostMapping("/mock-success")
    @PermitAll
    @Operation(summary = "模拟支付成功（演示用）")
    public CommonResult<Boolean> mockSuccess(@RequestParam(value = "payOrderNo", required = false) String payOrderNo,
                                             @RequestBody(required = false) AppMockPayReqVO reqVO) {
        String no = payOrderNo != null ? payOrderNo : (reqVO == null ? null : reqVO.getPayOrderNo());
        payService.mockSuccess(no);
        return success(true);
    }

    @PostMapping("/notify/wxpay")
    @PermitAll
    @Operation(summary = "微信支付回调")
    public CommonResult<Boolean> wxpayNotify(@RequestBody(required = false) String body,
                                             @RequestParam Map<String, String> params,
                                             HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Wechatpay-Signature", request.getHeader("Wechatpay-Signature"));
        boolean ok = payService.handleNotify("wxpay", body, params == null ? Collections.emptyMap() : params, headers);
        return success(ok);
    }

    @PostMapping("/notify/alipay")
    @PermitAll
    @Operation(summary = "支付宝回调")
    public CommonResult<Boolean> alipayNotify(@RequestBody(required = false) String body,
                                              @RequestParam Map<String, String> params) {
        boolean ok = payService.handleNotify("alipay", body, params == null ? Collections.emptyMap() : params,
                Collections.emptyMap());
        return success(ok);
    }
}
