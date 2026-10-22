package cn.iocoder.yudao.module.hotel.dal.mysql.pricing;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.controller.admin.pricing.vo.PriceBacktestPageReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.pricing.PriceBacktestDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PriceBacktestMapper extends BaseMapperX<PriceBacktestDO> {

    default PageResult<PriceBacktestDO> selectPage(PriceBacktestPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PriceBacktestDO>()
                .likeIfPresent(PriceBacktestDO::getName, reqVO.getName())
                .eqIfPresent(PriceBacktestDO::getRoomTypeId, reqVO.getRoomTypeId())
                .orderByDesc(PriceBacktestDO::getId));
    }
}
