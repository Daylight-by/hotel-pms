package cn.iocoder.yudao.module.hotel.dal.mysql.audit;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.dal.dataobject.audit.BusinessDateDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BusinessDateMapper extends BaseMapperX<BusinessDateDO> {

    default BusinessDateDO selectCurrent() {
        return selectOne(new LambdaQueryWrapperX<BusinessDateDO>()
                .orderByDesc(BusinessDateDO::getId)
                .last("LIMIT 1"));
    }
}
