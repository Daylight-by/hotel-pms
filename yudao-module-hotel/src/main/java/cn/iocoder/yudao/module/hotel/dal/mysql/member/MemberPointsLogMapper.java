package cn.iocoder.yudao.module.hotel.dal.mysql.member;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.dal.dataobject.member.MemberPointsLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MemberPointsLogMapper extends BaseMapperX<MemberPointsLogDO> {

    default List<MemberPointsLogDO> selectListByMemberId(Long memberId) {
        return selectList(new LambdaQueryWrapperX<MemberPointsLogDO>()
                .eq(MemberPointsLogDO::getMemberId, memberId)
                .orderByDesc(MemberPointsLogDO::getId));
    }
}
