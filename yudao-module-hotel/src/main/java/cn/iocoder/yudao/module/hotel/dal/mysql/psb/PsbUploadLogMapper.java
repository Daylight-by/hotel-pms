package cn.iocoder.yudao.module.hotel.dal.mysql.psb;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.controller.admin.psb.vo.PsbPageReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.psb.PsbUploadLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PsbUploadLogMapper extends BaseMapperX<PsbUploadLogDO> {

    default PageResult<PsbUploadLogDO> selectPage(PsbPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PsbUploadLogDO>()
                .likeIfPresent(PsbUploadLogDO::getGuestName, reqVO.getGuestName())
                .likeIfPresent(PsbUploadLogDO::getIdCard, reqVO.getIdCard())
                .eqIfPresent(PsbUploadLogDO::getActionType, reqVO.getActionType())
                .eqIfPresent(PsbUploadLogDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(PsbUploadLogDO::getBizTime, reqVO.getBeginTime(), reqVO.getEndTime())
                .orderByDesc(PsbUploadLogDO::getId));
    }

    default List<PsbUploadLogDO> selectRetryable() {
        return selectList(new LambdaQueryWrapperX<PsbUploadLogDO>()
                .eq(PsbUploadLogDO::getStatus, 30)
                .lt(PsbUploadLogDO::getRetryCount, 5)
                .orderByAsc(PsbUploadLogDO::getId)
                .last("LIMIT 20"));
    }

    default Long selectCountByStatusAndTime(Integer status, LocalDateTime begin, LocalDateTime end) {
        return selectCount(new LambdaQueryWrapperX<PsbUploadLogDO>()
                .eq(PsbUploadLogDO::getStatus, status)
                .ge(PsbUploadLogDO::getBizTime, begin)
                .lt(PsbUploadLogDO::getBizTime, end));
    }

    default Long selectCountByTime(LocalDateTime begin, LocalDateTime end) {
        return selectCount(new LambdaQueryWrapperX<PsbUploadLogDO>()
                .ge(PsbUploadLogDO::getBizTime, begin)
                .lt(PsbUploadLogDO::getBizTime, end));
    }

}
