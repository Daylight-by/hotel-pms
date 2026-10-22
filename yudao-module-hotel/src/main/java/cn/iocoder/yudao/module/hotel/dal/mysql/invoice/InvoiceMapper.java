package cn.iocoder.yudao.module.hotel.dal.mysql.invoice;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.controller.admin.invoice.vo.InvoicePageReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.invoice.InvoiceDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface InvoiceMapper extends BaseMapperX<InvoiceDO> {

    default PageResult<InvoiceDO> selectPage(InvoicePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<InvoiceDO>()
                .likeIfPresent(InvoiceDO::getInvoiceNo, reqVO.getInvoiceNo())
                .likeIfPresent(InvoiceDO::getTitle, reqVO.getTitle())
                .eqIfPresent(InvoiceDO::getOrderId, reqVO.getOrderId())
                .eqIfPresent(InvoiceDO::getStatus, reqVO.getStatus())
                .eqIfPresent(InvoiceDO::getType, reqVO.getType())
                .orderByDesc(InvoiceDO::getId));
    }

    default List<InvoiceDO> selectListByOrderId(Long orderId, Integer status) {
        return selectList(new LambdaQueryWrapperX<InvoiceDO>()
                .eq(InvoiceDO::getOrderId, orderId)
                .eqIfPresent(InvoiceDO::getStatus, status));
    }
}
