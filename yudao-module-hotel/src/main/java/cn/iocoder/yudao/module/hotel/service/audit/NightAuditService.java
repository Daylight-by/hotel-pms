package cn.iocoder.yudao.module.hotel.service.audit;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hotel.controller.admin.audit.vo.NightAuditPageReqVO;
import cn.iocoder.yudao.module.hotel.controller.admin.audit.vo.NightAuditRespVO;

/**
 * 夜审 Service
 */
public interface NightAuditService {

    /**
     * 执行夜审：过账当日房费、写夜审记录、营业日 +1
     */
    Long runNightAudit();

    /**
     * 夜审历史分页
     */
    PageResult<NightAuditRespVO> getNightAuditPage(NightAuditPageReqVO reqVO);

    /**
     * 最近一次夜审
     */
    NightAuditRespVO getLastNightAudit();

    /**
     * 按编号获得夜审记录
     */
    NightAuditRespVO getNightAudit(Long id);
}
