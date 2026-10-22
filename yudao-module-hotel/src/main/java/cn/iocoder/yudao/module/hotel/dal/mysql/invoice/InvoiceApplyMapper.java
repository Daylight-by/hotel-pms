package cn.iocoder.yudao.module.hotel.dal.mysql.invoice;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.controller.admin.invoiceApply.vo.InvoiceApplyPageReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.invoice.InvoiceApplyDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface InvoiceApplyMapper extends BaseMapperX<InvoiceApplyDO> {

    default PageResult<InvoiceApplyDO> selectPage(InvoiceApplyPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<InvoiceApplyDO>()
                .likeIfPresent(InvoiceApplyDO::getApplyNo, reqVO.getApplyNo())
                .eqIfPresent(InvoiceApplyDO::getOrderId, reqVO.getOrderId())
                .eqIfPresent(InvoiceApplyDO::getMemberId, reqVO.getMemberId())
                .eqIfPresent(InvoiceApplyDO::getStatus, reqVO.getStatus())
                .orderByDesc(InvoiceApplyDO::getId));
    }

    default List<InvoiceApplyDO> selectListByOrderId(Long orderId) {
        return selectList(new LambdaQueryWrapperX<InvoiceApplyDO>()
                .eq(InvoiceApplyDO::getOrderId, orderId)
                .orderByDesc(InvoiceApplyDO::getId));
    }

    default List<InvoiceApplyDO> selectListByMemberId(Long memberId) {
        return selectList(new LambdaQueryWrapperX<InvoiceApplyDO>()
                .eq(InvoiceApplyDO::getMemberId, memberId)
                .orderByDesc(InvoiceApplyDO::getId));
    }
}
