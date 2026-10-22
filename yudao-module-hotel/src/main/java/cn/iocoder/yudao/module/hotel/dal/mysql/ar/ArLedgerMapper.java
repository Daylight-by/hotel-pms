package cn.iocoder.yudao.module.hotel.dal.mysql.ar;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.controller.admin.ar.vo.ArLedgerPageReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.ar.ArLedgerDO;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Mapper
public interface ArLedgerMapper extends BaseMapperX<ArLedgerDO> {

    default PageResult<ArLedgerDO> selectPage(ArLedgerPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ArLedgerDO>()
                .eqIfPresent(ArLedgerDO::getArAccountId, reqVO.getArAccountId())
                .eqIfPresent(ArLedgerDO::getOrderId, reqVO.getOrderId())
                .eqIfPresent(ArLedgerDO::getType, reqVO.getType())
                .eqIfPresent(ArLedgerDO::getStatus, reqVO.getStatus())
                .geIfPresent(ArLedgerDO::getBizDate, reqVO.getBeginDate())
                .leIfPresent(ArLedgerDO::getBizDate, reqVO.getEndDate())
                .orderByDesc(ArLedgerDO::getId));
    }

    default List<ArLedgerDO> selectUnsettled(Long arAccountId, LocalDate beginDate, LocalDate endDate) {
        return selectList(new LambdaQueryWrapperX<ArLedgerDO>()
                .eq(ArLedgerDO::getArAccountId, arAccountId)
                .eq(ArLedgerDO::getStatus, 10)
                .in(ArLedgerDO::getType, List.of(10, 20))
                .ge(ArLedgerDO::getBizDate, beginDate)
                .le(ArLedgerDO::getBizDate, endDate)
                .orderByAsc(ArLedgerDO::getId));
    }

    default int markSettled(Collection<Long> ids, Long settlementId) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        return update(null, new UpdateWrapper<ArLedgerDO>()
                .in("id", ids)
                .set("status", 20)
                .set("settlement_id", settlementId));
    }

}
