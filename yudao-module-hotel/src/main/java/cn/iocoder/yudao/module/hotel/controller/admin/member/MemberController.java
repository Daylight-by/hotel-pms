package cn.iocoder.yudao.module.hotel.controller.admin.member;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.member.vo.*;
import cn.iocoder.yudao.module.hotel.service.member.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 酒店会员")
@RestController
@RequestMapping("/hotel/member")
@Validated
public class MemberController {

    @Resource
    private MemberService memberService;

    @GetMapping("/page")
    @Operation(summary = "会员分页")
    @PreAuthorize("@ss.hasPermission('hotel:member:query')")
    public CommonResult<PageResult<MemberRespVO>> getMemberPage(@Valid MemberPageReqVO reqVO) {
        return success(memberService.getMemberPage(reqVO));
    }

    @GetMapping("/get")
    @Operation(summary = "会员详情")
    @PreAuthorize("@ss.hasPermission('hotel:member:query')")
    public CommonResult<MemberRespVO> getMember(@RequestParam("id") Long id) {
        return success(memberService.getMember(id));
    }

    @PostMapping("/create")
    @Operation(summary = "创建会员")
    @PreAuthorize("@ss.hasPermission('hotel:member:create')")
    public CommonResult<Long> createMember(@Valid @RequestBody MemberSaveReqVO reqVO) {
        return success(memberService.createMember(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "修改会员")
    @PreAuthorize("@ss.hasPermission('hotel:member:update')")
    public CommonResult<Boolean> updateMember(@Valid @RequestBody MemberSaveReqVO reqVO) {
        memberService.updateMember(reqVO);
        return success(true);
    }

    @PutMapping("/adjust-points")
    @Operation(summary = "调整积分")
    @PreAuthorize("@ss.hasPermission('hotel:member:update')")
    public CommonResult<Boolean> adjustPoints(@Valid @RequestBody MemberPointsAdjustReqVO reqVO) {
        memberService.adjustPoints(reqVO.getMemberId(), reqVO.getPoints(),
                cn.iocoder.yudao.module.hotel.enums.PointsTypeEnum.ADJUST.getType(), null, reqVO.getRemark());
        return success(true);
    }

    @PutMapping("/adjust-balance")
    @Operation(summary = "调整储值余额")
    @PreAuthorize("@ss.hasPermission('hotel:member:update')")
    public CommonResult<Boolean> adjustBalance(@Valid @RequestBody MemberBalanceAdjustReqVO reqVO) {
        memberService.adjustBalance(reqVO.getMemberId(), reqVO.getAmount(), reqVO.getRemark());
        return success(true);
    }

    @GetMapping("/points-log")
    @Operation(summary = "会员积分流水")
    @PreAuthorize("@ss.hasPermission('hotel:member:query')")
    public CommonResult<List<MemberPointsLogRespVO>> getPointsLog(@RequestParam("memberId") Long memberId) {
        return success(memberService.getPointsLog(memberId));
    }
}
