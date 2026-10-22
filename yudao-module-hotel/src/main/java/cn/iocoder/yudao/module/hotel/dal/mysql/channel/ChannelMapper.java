package cn.iocoder.yudao.module.hotel.dal.mysql.channel;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.controller.admin.channel.vo.ChannelPageReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.channel.ChannelDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ChannelMapper extends BaseMapperX<ChannelDO> {

    default PageResult<ChannelDO> selectPage(ChannelPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ChannelDO>()
                .likeIfPresent(ChannelDO::getName, reqVO.getName())
                .eqIfPresent(ChannelDO::getCode, reqVO.getCode())
                .eqIfPresent(ChannelDO::getType, reqVO.getType())
                .eqIfPresent(ChannelDO::getStatus, reqVO.getStatus())
                .orderByAsc(ChannelDO::getSort)
                .orderByAsc(ChannelDO::getId));
    }

    default ChannelDO selectByCode(String code) {
        return selectOne(new LambdaQueryWrapperX<ChannelDO>()
                .eq(ChannelDO::getCode, code));
    }

    default List<ChannelDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<ChannelDO>()
                .eq(ChannelDO::getStatus, status)
                .orderByAsc(ChannelDO::getSort));
    }
}
