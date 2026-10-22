package cn.iocoder.yudao.module.hotel.controller.admin.channel;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.channel.vo.ChannelPageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.channel.vo.ChannelRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.channel.vo.ChannelSaveReqVO;
import cn.iocoder.yudao.module.hotel.service.channel.ChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 酒店渠道")
@RestController
@RequestMapping("/hotel/channel")
@Validated
public class ChannelController {

    @Resource
    private ChannelService channelService;

    @PostMapping("/create")
    @Operation(summary = "创建渠道")
    @PreAuthorize("@ss.hasPermission('hotel:channel:create')")
    public CommonResult<Long> createChannel(@Valid @RequestBody ChannelSaveReqVO reqVO) {
        return success(channelService.createChannel(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新渠道")
    @PreAuthorize("@ss.hasPermission('hotel:channel:update')")
    public CommonResult<Boolean> updateChannel(@Valid @RequestBody ChannelSaveReqVO reqVO) {
        channelService.updateChannel(reqVO);
        return success(true);
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除渠道")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('hotel:channel:delete')")
    public CommonResult<Boolean> deleteChannel(@RequestParam("id") Long id) {
        channelService.deleteChannel(id);
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获取渠道")
    @Parameter(name = "id", description = "编号", required = true)
    @PreAuthorize("@ss.hasPermission('hotel:channel:query')")
    public CommonResult<ChannelRespVO> getChannel(@RequestParam("id") Long id) {
        return success(channelService.getChannel(id));
    }

    @GetMapping("/page")
    @Operation(summary = "获取渠道分页")
    @PreAuthorize("@ss.hasPermission('hotel:channel:query')")
    public CommonResult<PageResult<ChannelRespVO>> getChannelPage(@Valid ChannelPageReqVO reqVO) {
        return success(channelService.getChannelPage(reqVO));
    }

    @GetMapping("/list-enabled")
    @Operation(summary = "获取开启中的渠道列表（下拉用）")
    @PreAuthorize("@ss.hasPermission('hotel:channel:query')")
    public CommonResult<List<ChannelRespVO>> getEnabledChannelList() {
        return success(channelService.getEnabledChannelList());
    }
}
