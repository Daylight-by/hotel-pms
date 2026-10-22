package cn.iocoder.yudao.module.hotel.dal.mysql.audit;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.controller.admin.audit.vo.NightAuditPageReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.audit.NightAuditDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface NightAuditMapper extends BaseMapperX<NightAuditDO> {

    default NightAuditDO selectSuccessByDate(LocalDate bizDate, Integer successStatus) {
        return selectOne(new LambdaQueryWrapperX<NightAuditDO>()
                .eq(NightAuditDO::getBizDate, bizDate)
                .eq(NightAuditDO::getStatus, successStatus)
                .orderByDesc(NightAuditDO::getId)
                .last("LIMIT 1"));
    }

    default List<NightAuditDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<NightAuditDO>()
                .eq(NightAuditDO::getStatus, status)
                .orderByDesc(NightAuditDO::getId));
    }

    default NightAuditDO selectLastSuccess(Integer successStatus) {
        return selectOne(new LambdaQueryWrapperX<NightAuditDO>()
                .eq(NightAuditDO::getStatus, successStatus)
                .orderByDesc(NightAuditDO::getId)
                .last("LIMIT 1"));
    }

    default NightAuditDO selectLastByStatusAndTime(Integer status, LocalDateTime begin, LocalDateTime end) {
        return selectOne(new LambdaQueryWrapperX<NightAuditDO>()
                .eq(NightAuditDO::getStatus, status)
                .ge(NightAuditDO::getStartTime, begin)
                .lt(NightAuditDO::getStartTime, end)
                .orderByDesc(NightAuditDO::getId)
                .last("LIMIT 1"));
    }

    default PageResult<NightAuditDO> selectPage(NightAuditPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<NightAuditDO>()
                .eqIfPresent(NightAuditDO::getBizDate, reqVO.getBizDate())
                .eqIfPresent(NightAuditDO::getStatus, reqVO.getStatus())
                .orderByDesc(NightAuditDO::getId));
    }
}
