package cn.iocoder.yudao.module.hotel.dal.mysql.ota;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.controller.admin.ota.vo.OtaConfigPageReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.ota.OtaConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OtaConfigMapper extends BaseMapperX<OtaConfigDO> {

    default PageResult<OtaConfigDO> selectPage(OtaConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OtaConfigDO>()
                .eqIfPresent(OtaConfigDO::getOtaCode, reqVO.getOtaCode())
                .eqIfPresent(OtaConfigDO::getStatus, reqVO.getStatus())
                .orderByAsc(OtaConfigDO::getId));
    }

    default List<OtaConfigDO> selectEnabledList() {
        return selectList(new LambdaQueryWrapperX<OtaConfigDO>()
                .eq(OtaConfigDO::getStatus, 0)
                .orderByAsc(OtaConfigDO::getId));
    }

}
