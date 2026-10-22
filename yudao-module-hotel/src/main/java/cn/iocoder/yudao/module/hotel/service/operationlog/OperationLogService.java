package cn.iocoder.yudao.module.hotel.service.operationlog;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.operationlog.vo.OperationLogPageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.operationlog.vo.OperationLogRespVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.operationlog.OperationLogDO;

/**
 * 操作审计日志 Service
 */
public interface OperationLogService {

    /**
     * 分页查询
     */
    PageResult<OperationLogDO> getOperationLogPage(OperationLogPageReqVO pageReqVO);

    /**
     * 转换为 VO（before/after 保持原样 JSON）
     */
    OperationLogRespVO toRespVO(OperationLogDO logDO);
}
