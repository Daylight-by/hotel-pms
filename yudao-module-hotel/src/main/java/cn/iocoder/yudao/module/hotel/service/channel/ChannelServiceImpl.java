package cn.iocoder.yudao.module.hotel.service.channel;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.channel.vo.ChannelPageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.channel.vo.ChannelRespVO;
import cn.iocoder.yudao.module.hotel.controller.admin.channel.vo.ChannelSaveReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.channel.ChannelDO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.rateplan.RatePlanDO;
import cn.iocoder.yudao.module.hotel.dal.mysql.channel.ChannelMapper;
import cn.iocoder.yudao.module.hotel.dal.mysql.rateplan.RatePlanMapper;
import cn.iocoder.yudao.module.hotel.enums.ChannelTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.hotel.enums.HotelT1ErrorCodeConstants.*;

/**
 * 渠道 Service 实现（T1）
 */
@Service
@Validated
@Slf4j
public class ChannelServiceImpl implements ChannelService {

    @Resource
    private ChannelMapper channelMapper;
    @Resource
    private RatePlanMapper ratePlanMapper;

    @Override
    public Long createChannel(ChannelSaveReqVO reqVO) {
        validateCodeUnique(null, reqVO.getCode());
        ChannelDO channel = new ChannelDO();
        channel.setName(reqVO.getName());
        channel.setCode(reqVO.getCode().trim().toUpperCase());
        channel.setType(reqVO.getType());
        channel.setCommissionRate(reqVO.getCommissionRate() == null ? BigDecimal.ZERO : reqVO.getCommissionRate());
        channel.setStatus(reqVO.getStatus() == null ? 0 : reqVO.getStatus());
        channel.setSort(reqVO.getSort() == null ? 0 : reqVO.getSort());
        channel.setRemark(reqVO.getRemark());
        channelMapper.insert(channel);
        return channel.getId();
    }

    @Override
    public void updateChannel(ChannelSaveReqVO reqVO) {
        validateChannelExists(reqVO.getId());
        validateCodeUnique(reqVO.getId(), reqVO.getCode());
        ChannelDO update = new ChannelDO();
        update.setId(reqVO.getId());
        update.setName(reqVO.getName());
        update.setCode(reqVO.getCode().trim().toUpperCase());
        update.setType(reqVO.getType());
        update.setCommissionRate(reqVO.getCommissionRate());
        update.setStatus(reqVO.getStatus());
        update.setSort(reqVO.getSort());
        update.setRemark(reqVO.getRemark());
        channelMapper.updateById(update);
    }

    @Override
    public void deleteChannel(Long id) {
        validateChannelExists(id);
        List<RatePlanDO> plans = ratePlanMapper.selectListByChannelId(id);
        if (!plans.isEmpty()) {
            throw exception(ERROR_CHANNEL_HAS_RATE_PLAN);
        }
        channelMapper.deleteById(id);
    }

    @Override
    public ChannelRespVO getChannel(Long id) {
        return buildVOList(List.of(validateChannelExists(id))).get(0);
    }

    @Override
    public PageResult<ChannelRespVO> getChannelPage(ChannelPageReqVO reqVO) {
        PageResult<ChannelDO> page = channelMapper.selectPage(reqVO);
        return new PageResult<>(buildVOList(page.getList()), page.getTotal());
    }

    @Override
    public List<ChannelRespVO> getEnabledChannelList() {
        return buildVOList(channelMapper.selectListByStatus(0));
    }

    @Override
    public Long getDefaultDirectChannelId() {
        List<ChannelDO> directList = channelMapper.selectListByStatus(0).stream()
                .filter(c -> Objects.equals(c.getType(), ChannelTypeEnum.DIRECT.getType()))
                .toList();
        if (!directList.isEmpty()) {
            return directList.get(0).getId();
        }
        List<ChannelDO> all = channelMapper.selectListByStatus(0);
        return all.isEmpty() ? null : all.get(0).getId();
    }

    // ============================================================ 私有方法

    private ChannelDO validateChannelExists(Long id) {
        ChannelDO channel = id == null ? null : channelMapper.selectById(id);
        if (channel == null) {
            throw exception(ERROR_CHANNEL_NOT_EXIST);
        }
        return channel;
    }

    private void validateCodeUnique(Long id, String code) {
        if (code == null || code.isBlank()) {
            return;
        }
        ChannelDO exists = channelMapper.selectByCode(code.trim().toUpperCase());
        if (exists != null && !Objects.equals(exists.getId(), id)) {
            throw exception(ERROR_CHANNEL_CODE_DUPLICATE);
        }
    }

    private List<ChannelRespVO> buildVOList(List<ChannelDO> list) {
        List<ChannelRespVO> result = new ArrayList<>();
        if (list == null) {
            return result;
        }
        for (ChannelDO channel : list) {
            ChannelRespVO vo = new ChannelRespVO();
            vo.setId(channel.getId());
            vo.setName(channel.getName());
            vo.setCode(channel.getCode());
            vo.setType(channel.getType());
            vo.setTypeName(typeName(channel.getType()));
            vo.setCommissionRate(channel.getCommissionRate());
            vo.setStatus(channel.getStatus());
            vo.setStatusName(Objects.equals(channel.getStatus(), 0) ? "开启" : "关闭");
            vo.setSort(channel.getSort());
            vo.setRemark(channel.getRemark());
            vo.setCreateTime(channel.getCreateTime());
            result.add(vo);
        }
        return result;
    }

    private String typeName(Integer type) {
        for (ChannelTypeEnum e : ChannelTypeEnum.values()) {
            if (Objects.equals(e.getType(), type)) {
                return e.getName();
            }
        }
        return "";
    }
}
