package cn.iocoder.yudao.module.hotel.dal.mysql.folio;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.dal.dataobject.folio.PaymentRecordDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PaymentRecordMapper extends BaseMapperX<PaymentRecordDO> {

    default List<PaymentRecordDO> selectListByFolioId(Long folioId) {
        return selectList(new LambdaQueryWrapperX<PaymentRecordDO>()
                .eq(PaymentRecordDO::getFolioId, folioId)
                .orderByAsc(PaymentRecordDO::getId));
    }

    default List<PaymentRecordDO> selectListByOrderId(Long orderId) {
        return selectList(new LambdaQueryWrapperX<PaymentRecordDO>()
                .eq(PaymentRecordDO::getOrderId, orderId)
                .orderByAsc(PaymentRecordDO::getId));
    }

    /**
     * 班次结算口径：pay_time ∈ [begin, end)，type=收款、status=成功
     */
    default List<PaymentRecordDO> selectIncomeBetween(LocalDateTime begin, LocalDateTime end,
                                                      Integer type, Integer status) {
        return selectList(new LambdaQueryWrapperX<PaymentRecordDO>()
                .ge(PaymentRecordDO::getPayTime, begin)
                .lt(PaymentRecordDO::getPayTime, end)
                .eq(PaymentRecordDO::getType, type)
                .eq(PaymentRecordDO::getStatus, status));
    }

    default List<PaymentRecordDO> selectListByTimeRange(LocalDateTime begin, LocalDateTime end) {
        return selectList(new LambdaQueryWrapperX<PaymentRecordDO>()
                .ge(PaymentRecordDO::getPayTime, begin)
                .lt(PaymentRecordDO::getPayTime, end)
                .orderByAsc(PaymentRecordDO::getPayTime));
    }
}
