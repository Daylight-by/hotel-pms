package cn.iocoder.yudao.module.hotel.service.channel;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.channel.vo.ChannelPageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.channel.vo.ChannelRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.channel.vo.ChannelSaveReqVO;

import java.util.List;

/**
 * 渠道 Service（T1）
 */
public interface ChannelService {

    Long createChannel(ChannelSaveReqVO reqVO);

    void updateChannel(ChannelSaveReqVO reqVO);

    void deleteChannel(Long id);

    ChannelRespVO getChannel(Long id);

    PageResult<ChannelRespVO> getChannelPage(ChannelPageReqVO reqVO);

    List<ChannelRespVO> getEnabledChannelList();

    /**
     * 默认直销渠道：预订引擎未指定渠道时使用（找不到时返回 null）
     */
    Long getDefaultDirectChannelId();
}
