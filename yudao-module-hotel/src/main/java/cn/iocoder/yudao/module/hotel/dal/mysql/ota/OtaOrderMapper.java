package cn.iocoder.yudao.module.hotel.dal.mysql.ota;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.controller.admin.ota.vo.OtaOrderPageReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.ota.OtaOrderDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OtaOrderMapper extends BaseMapperX<OtaOrderDO> {

    default PageResult<OtaOrderDO> selectPage(OtaOrderPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OtaOrderDO>()
                .eqIfPresent(OtaOrderDO::getOtaCode, reqVO.getOtaCode())
                .likeIfPresent(OtaOrderDO::getOtaOrderNo, reqVO.getOtaOrderNo())
                .eqIfPresent(OtaOrderDO::getSyncStatus, reqVO.getSyncStatus())
                .orderByDesc(OtaOrderDO::getId));
    }

    default OtaOrderDO selectByOtaOrderNo(String otaCode, String otaOrderNo) {
        return selectOne(new LambdaQueryWrapperX<OtaOrderDO>()
                .eq(OtaOrderDO::getOtaCode, otaCode)
                .eq(OtaOrderDO::getOtaOrderNo, otaOrderNo));
    }

}
