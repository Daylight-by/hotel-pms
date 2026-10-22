package cn.iocoder.yudao.module.hotel.service.operationlog;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.operationlog.vo.OperationLogPageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.operationlog.vo.OperationLogRespVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.operationlog.OperationLogDO;
import cn.iocoder.yudao.module.hotel.dal.mysql.operationlog.OperationLogMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

/**
 * 操作审计日志 Service 实现
 */
@Service
@Validated
public class OperationLogServiceImpl implements OperationLogService {

    @Resource
    private OperationLogMapper operationLogMapper;

    @Override
    public PageResult<OperationLogDO> getOperationLogPage(OperationLogPageReqVO pageReqVO) {
        return operationLogMapper.selectPage(pageReqVO);
    }

    @Override
    public OperationLogRespVO toRespVO(OperationLogDO logDO) {
        OperationLogRespVO respVO = new OperationLogRespVO();
        respVO.setId(logDO.getId());
        respVO.setStoreId(logDO.getStoreId());
        respVO.setModule(logDO.getModule());
        respVO.setBizId(logDO.getBizId());
        respVO.setOperation(logDO.getOperation());
        respVO.setBeforeData(logDO.getBeforeData());
        respVO.setAfterData(logDO.getAfterData());
        respVO.setOperatorId(logDO.getOperatorId());
        respVO.setOperatorName(logDO.getOperatorName());
        respVO.setIp(logDO.getIp());
        respVO.setCreateTime(logDO.getCreateTime());
        return respVO;
    }
}
