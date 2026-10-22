package cn.iocoder.yudao.module.hotel.dal.mysql.member;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.controller.admin.member.vo.MemberPageReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.member.MemberDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MemberMapper extends BaseMapperX<MemberDO> {

    default MemberDO selectByMobile(String mobile) {
        return selectOne(MemberDO::getMobile, mobile);
    }

    default MemberDO selectByMemberNo(String memberNo) {
        return selectOne(MemberDO::getMemberNo, memberNo);
    }

    default PageResult<MemberDO> selectPage(MemberPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<MemberDO>()
                .likeIfPresent(MemberDO::getMemberNo, reqVO.getMemberNo())
                .likeIfPresent(MemberDO::getName, reqVO.getName())
                .likeIfPresent(MemberDO::getMobile, reqVO.getMobile())
                .eqIfPresent(MemberDO::getLevel, reqVO.getLevel())
                .eqIfPresent(MemberDO::getStatus, reqVO.getStatus())
                .orderByDesc(MemberDO::getId));
    }
}
