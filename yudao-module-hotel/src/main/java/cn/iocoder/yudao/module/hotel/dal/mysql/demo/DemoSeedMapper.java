package cn.iocoder.yudao.module.hotel.dal.mysql.demo;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.dal.dataobject.demo.DemoSeedDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 演示数据批次 Mapper
 */
@Mapper
public interface DemoSeedMapper extends BaseMapperX<DemoSeedDO> {

    default DemoSeedDO selectLatest() {
        return selectOne(new LambdaQueryWrapperX<DemoSeedDO>()
                .orderByDesc(DemoSeedDO::getId)
                .last("LIMIT 1"));
    }
}
