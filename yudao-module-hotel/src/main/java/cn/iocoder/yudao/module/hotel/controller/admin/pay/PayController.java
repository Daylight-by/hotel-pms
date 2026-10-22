package cn.iocoder.yudao.module.hotel.controller.admin.pay;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.pay.vo.PayOrderPageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.pay.vo.PayOrderRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.pay.vo.PayRefundReqVO;
import cn.iocoder.yudao.module.hotel.service.pay.PayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 酒店支付单")
@RestController
@RequestMapping("/hotel/pay")
@Validated
public class PayController {

    @Resource
    private PayService payService;

    @GetMapping("/page")
    @Operation(summary = "支付单分页")
    @PreAuthorize("@ss.hasPermission('hotel:pay:query')")
    public CommonResult<PageResult<PayOrderRespVO>> getPayOrderPage(@Valid PayOrderPageReqVO reqVO) {
        return success(payService.getPayOrderPage(reqVO));
    }

    @PutMapping("/refund")
    @Operation(summary = "支付单退款")
    @PreAuthorize("@ss.hasPermission('hotel:pay:refund')")
    public CommonResult<Boolean> refund(@Valid @RequestBody PayRefundReqVO reqVO) {
        payService.refund(reqVO.getPayOrderId(), reqVO.getAmount(), reqVO.getReason());
        return success(true);
    }
}
