package cn.iocoder.yudao.module.hotel.dal.mysql.guest;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.controller.admin.guest.vo.GuestPageReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.guest.GuestDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface GuestMapper extends BaseMapperX<GuestDO> {

    default PageResult<GuestDO> selectPage(GuestPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<GuestDO>()
                .likeIfPresent(GuestDO::getName, reqVO.getName())
                .likeIfPresent(GuestDO::getMobile, reqVO.getMobile())
                .orderByDesc(GuestDO::getId));
    }

    default GuestDO selectByMobile(String mobile) {
        return selectOne(GuestDO::getMobile, mobile);
    }

    default List<GuestDO> selectListAll() {
        return selectList(new LambdaQueryWrapperX<GuestDO>().orderByDesc(GuestDO::getId));
    }
}
