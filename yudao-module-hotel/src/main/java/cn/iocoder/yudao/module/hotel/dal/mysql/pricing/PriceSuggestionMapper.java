package cn.iocoder.yudao.module.hotel.dal.mysql.pricing;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.dal.dataobject.pricing.PriceSuggestionDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface PriceSuggestionMapper extends BaseMapperX<PriceSuggestionDO> {

    default PriceSuggestionDO selectByTypeAndDate(Long roomTypeId, LocalDate bizDate) {
        return selectOne(new LambdaQueryWrapperX<PriceSuggestionDO>()
                .eq(PriceSuggestionDO::getRoomTypeId, roomTypeId)
                .eq(PriceSuggestionDO::getBizDate, bizDate));
    }

    default List<PriceSuggestionDO> selectListByRange(Long roomTypeId, LocalDate begin, LocalDate end) {
        return selectList(new LambdaQueryWrapperX<PriceSuggestionDO>()
                .eqIfPresent(PriceSuggestionDO::getRoomTypeId, roomTypeId)
                .between(PriceSuggestionDO::getBizDate, begin, end)
                .orderByAsc(PriceSuggestionDO::getBizDate)
                .orderByAsc(PriceSuggestionDO::getRoomTypeId));
    }
}
