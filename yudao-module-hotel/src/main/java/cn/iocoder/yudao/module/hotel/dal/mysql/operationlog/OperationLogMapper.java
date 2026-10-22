package cn.iocoder.yudao.module.hotel.dal.mysql.operationlog;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.controller.admin.operationlog.vo.OperationLogPageReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.operationlog.OperationLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作审计日志 Mapper
 */
@Mapper
public interface OperationLogMapper extends BaseMapperX<OperationLogDO> {

    default PageResult<OperationLogDO> selectPage(OperationLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OperationLogDO>()
                .eqIfPresent(OperationLogDO::getModule, reqVO.getModule())
                .likeIfPresent(OperationLogDO::getBizId, reqVO.getBizId())
                .likeIfPresent(OperationLogDO::getOperation, reqVO.getOperation())
                .likeIfPresent(OperationLogDO::getOperatorName, reqVO.getOperatorName())
                .geIfPresent(OperationLogDO::getCreateTime, reqVO.getBeginTime())
                .leIfPresent(OperationLogDO::getCreateTime, reqVO.getEndTime())
                .orderByDesc(OperationLogDO::getId));
    }
}
