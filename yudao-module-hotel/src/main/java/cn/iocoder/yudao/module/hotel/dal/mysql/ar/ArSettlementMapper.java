package cn.iocoder.yudao.module.hotel.dal.mysql.ar;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.controller.admin.ar.vo.ArSettlementPageReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.ar.ArSettlementDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArSettlementMapper extends BaseMapperX<ArSettlementDO> {

    default PageResult<ArSettlementDO> selectPage(ArSettlementPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ArSettlementDO>()
                .eqIfPresent(ArSettlementDO::getArAccountId, reqVO.getArAccountId())
                .eqIfPresent(ArSettlementDO::getStatus, reqVO.getStatus())
                .geIfPresent(ArSettlementDO::getBeginDate, reqVO.getBeginDate())
                .leIfPresent(ArSettlementDO::getEndDate, reqVO.getEndDate())
                .orderByDesc(ArSettlementDO::getId));
    }

    default ArSettlementDO selectByNo(String settlementNo) {
        return selectOne(ArSettlementDO::getSettlementNo, settlementNo);
    }

}
