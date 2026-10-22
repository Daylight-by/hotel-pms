package cn.iocoder.yudao.module.hotel.dal.mysql.store;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.controller.admin.store.vo.StorePageReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.store.StoreDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StoreMapper extends BaseMapperX<StoreDO> {

    default PageResult<StoreDO> selectPage(StorePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<StoreDO>()
                .likeIfPresent(StoreDO::getName, reqVO.getName())
                .likeIfPresent(StoreDO::getCode, reqVO.getCode())
                .eqIfPresent(StoreDO::getStatus, reqVO.getStatus())
                .orderByAsc(StoreDO::getSort)
                .orderByAsc(StoreDO::getId));
    }

    default List<StoreDO> selectListAll() {
        return selectList(new LambdaQueryWrapperX<StoreDO>()
                .eq(StoreDO::getStatus, 0)
                .orderByAsc(StoreDO::getSort)
                .orderByAsc(StoreDO::getId));
    }

    default StoreDO selectByCode(String code) {
        return selectOne(StoreDO::getCode, code);
    }

}
