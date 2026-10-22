package cn.iocoder.yudao.module.hotel.controller.app.member;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.hotel.controller.admin.member.vo.MemberPointsLogRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.invoiceApply.vo.InvoiceApplyRespVO;
import cn.iocoder.yudao.module.hotel.controller.app.member.vo.AppMemberInfoRespVO;
import cn.iocoder.yudao.module.hotel.controller.app.member.vo.AppMemberInvoiceApplyReqVO;
import cn.iocoder.yudao.module.hotel.controller.app.member.vo.AppMemberLoginReqVO;
import cn.iocoder.yudao.module.hotel.controller.app.member.vo.AppMemberLoginRespVO;
import cn.iocoder.yudao.module.hotel.service.invoiceApply.InvoiceApplyService;
import cn.iocoder.yudao.module.hotel.service.member.MemberService;
import cn.iocoder.yudao.module.hotel.service.member.SelfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.hotel.enums.HotelT2ErrorCodeConstants.ERROR_MEMBER_NOT_EXIST;

/**
 * 用户 App - 酒店会员：登录、信息、积分、发票申请、自助入住退房。
 */
@Tag(name = "用户 App - 酒店会员")
@RestController
@RequestMapping("/hotel/member")
@Validated
public class AppMemberController {

    @Resource
    private MemberService memberService;
    @Resource
    private SelfService selfService;
    @Resource
    private InvoiceApplyService invoiceApplyService;

    @PostMapping("/login")
    @PermitAll
    @Operation(summary = "会员登录（手机号 + 验证码，未注册自动注册）")
    public CommonResult<AppMemberLoginRespVO> login(@Valid @RequestBody AppMemberLoginReqVO reqVO) {
        return success(memberService.login(reqVO));
    }

    @GetMapping("/info")
    @Operation(summary = "会员信息")
    public CommonResult<AppMemberInfoRespVO> getInfo() {
        return success(memberService.getMemberInfo(requireMemberId()));
    }

    @GetMapping("/points-log")
    @Operation(summary = "我的积分流水")
    public CommonResult<List<MemberPointsLogRespVO>> getPointsLog() {
        return success(memberService.getPointsLog(requireMemberId()));
    }

    @PostMapping("/invoice-apply")
    @Operation(summary = "提交电子发票申请")
    public CommonResult<Long> applyInvoice(@Valid @RequestBody AppMemberInvoiceApplyReqVO reqVO) {
        return success(invoiceApplyService.apply(requireMemberId(), reqVO));
    }

    @GetMapping("/invoice-apply/list")
    @Operation(summary = "我的发票申请")
    public CommonResult<List<InvoiceApplyRespVO>> getMyInvoiceApplies() {
        return success(invoiceApplyService.getMyApplies(requireMemberId()));
    }

    @PutMapping("/self-check-in")
    @Operation(summary = "自助入住")
    public CommonResult<Boolean> selfCheckIn(@RequestParam("orderId") Long orderId) {
        selfService.selfCheckIn(requireMemberId(), orderId);
        return success(true);
    }

    @PutMapping("/self-check-out")
    @Operation(summary = "自助退房")
    public CommonResult<Boolean> selfCheckOut(@RequestParam("orderId") Long orderId) {
        selfService.selfCheckOut(requireMemberId(), orderId);
        return success(true);
    }

    private Long requireMemberId() {
        Long memberId = getLoginUserId();
        if (memberId == null) {
            throw exception(ERROR_MEMBER_NOT_EXIST);
        }
        return memberId;
    }
}
