package cn.iocoder.yudao.module.hotel.dal.mysql.folio;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.controller.admin.folio.vo.FolioPageReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.folio.FolioDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface FolioMapper extends BaseMapperX<FolioDO> {

    default FolioDO selectByFolioNo(String folioNo) {
        return selectOne(FolioDO::getFolioNo, folioNo);
    }

    default List<FolioDO> selectListByOrderId(Long orderId) {
        return selectList(new LambdaQueryWrapperX<FolioDO>()
                .eq(FolioDO::getOrderId, orderId)
                .orderByAsc(FolioDO::getId));
    }

    default FolioDO selectMainByOrderId(Long orderId) {
        return selectOne(new LambdaQueryWrapperX<FolioDO>()
                .eq(FolioDO::getOrderId, orderId)
                .eq(FolioDO::getType, 10)
                .orderByAsc(FolioDO::getId)
                .last("LIMIT 1"));
    }

    default String selectMaxFolioNoByPrefix(String prefix) {
        FolioDO last = selectOne(new LambdaQueryWrapperX<FolioDO>()
                .likeRight(FolioDO::getFolioNo, prefix)
                .orderByDesc(FolioDO::getFolioNo)
                .last("LIMIT 1"));
        return last == null ? null : last.getFolioNo();
    }

    default PageResult<FolioDO> selectPage(FolioPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FolioDO>()
                .likeIfPresent(FolioDO::getFolioNo, reqVO.getFolioNo())
                .eqIfPresent(FolioDO::getOrderId, reqVO.getOrderId())
                .eqIfPresent(FolioDO::getGuestId, reqVO.getGuestId())
                .eqIfPresent(FolioDO::getStatus, reqVO.getStatus())
                .eqIfPresent(FolioDO::getType, reqVO.getType())
                .orderByDesc(FolioDO::getId));
    }

    /**
     * 余额恒等式由 SQL 计算，避免应用层重复 set 造成并发不一致
     */
    @Update("UPDATE hotel_folio SET total_charge = #{charge}, total_payment = #{payment}, "
            + "balance = #{charge} - #{payment}, update_time = NOW() WHERE id = #{id} AND deleted = b'0'")
    int updateTotals(@Param("id") Long id, @Param("charge") BigDecimal charge,
                     @Param("payment") BigDecimal payment);

    @Update("UPDATE hotel_folio SET status = #{status}, update_time = NOW() WHERE id = #{id} AND deleted = b'0'")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
