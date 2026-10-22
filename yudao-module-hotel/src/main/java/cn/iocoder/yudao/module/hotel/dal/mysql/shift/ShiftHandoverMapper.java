package cn.iocoder.yudao.module.hotel.dal.mysql.shift;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.controller.admin.shift.vo.ShiftPageReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.shift.ShiftHandoverDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ShiftHandoverMapper extends BaseMapperX<ShiftHandoverDO> {

    default ShiftHandoverDO selectByStatus(Integer status) {
        return selectOne(new LambdaQueryWrapperX<ShiftHandoverDO>()
                .eq(ShiftHandoverDO::getStatus, status)
                .orderByDesc(ShiftHandoverDO::getId)
                .last("LIMIT 1"));
    }

    default String selectMaxShiftNoByPrefix(String prefix) {
        ShiftHandoverDO last = selectOne(new LambdaQueryWrapperX<ShiftHandoverDO>()
                .likeRight(ShiftHandoverDO::getShiftNo, prefix)
                .orderByDesc(ShiftHandoverDO::getShiftNo)
                .last("LIMIT 1"));
        return last == null ? null : last.getShiftNo();
    }

    default PageResult<ShiftHandoverDO> selectPage(ShiftPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ShiftHandoverDO>()
                .eqIfPresent(ShiftHandoverDO::getStatus, reqVO.getStatus())
                .geIfPresent(ShiftHandoverDO::getBizDate, reqVO.getBeginDate())
                .leIfPresent(ShiftHandoverDO::getBizDate, reqVO.getEndDate())
                .orderByDesc(ShiftHandoverDO::getId));
    }
}
