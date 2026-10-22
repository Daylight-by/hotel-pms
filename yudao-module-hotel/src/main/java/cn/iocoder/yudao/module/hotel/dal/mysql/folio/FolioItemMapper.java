package cn.iocoder.yudao.module.hotel.dal.mysql.folio;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.controller.admin.folio.vo.FolioItemPageReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.folio.FolioItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FolioItemMapper extends BaseMapperX<FolioItemDO> {

    default List<FolioItemDO> selectListByFolioId(Long folioId) {
        return selectList(new LambdaQueryWrapperX<FolioItemDO>()
                .eq(FolioItemDO::getFolioId, folioId)
                .orderByAsc(FolioItemDO::getId));
    }

    default List<FolioItemDO> selectListByOrderId(Long orderId) {
        return selectList(new LambdaQueryWrapperX<FolioItemDO>()
                .eq(FolioItemDO::getOrderId, orderId)
                .orderByAsc(FolioItemDO::getId));
    }

    default PageResult<FolioItemDO> selectPage(FolioItemPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FolioItemDO>()
                .eqIfPresent(FolioItemDO::getFolioId, reqVO.getFolioId())
                .eqIfPresent(FolioItemDO::getOrderId, reqVO.getOrderId())
                .eqIfPresent(FolioItemDO::getItemType, reqVO.getItemType())
                .eqIfPresent(FolioItemDO::getStatus, reqVO.getStatus())
                .geIfPresent(FolioItemDO::getBusinessDate, reqVO.getBeginDate())
                .leIfPresent(FolioItemDO::getBusinessDate, reqVO.getEndDate())
                .orderByDesc(FolioItemDO::getId));
    }

    /**
     * 是否已存在某营业日的房费入账（夜审幂等保护）
     */
    default Long selectCountRoomCharge(Long folioId, java.time.LocalDate businessDate, java.util.Collection<Integer> statuses) {
        return selectCount(new LambdaQueryWrapperX<FolioItemDO>()
                .eq(FolioItemDO::getFolioId, folioId)
                .eq(FolioItemDO::getBusinessDate, businessDate)
                .eq(FolioItemDO::getItemType, 10)
                .eq(FolioItemDO::getDirection, 1)
                .in(FolioItemDO::getStatus, statuses));
    }
}
