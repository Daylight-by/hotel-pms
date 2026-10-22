package cn.iocoder.yudao.module.hotel.dal.mysql.pay;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.controller.admin.pay.vo.PayOrderPageReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.pay.PayOrderDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PayOrderMapper extends BaseMapperX<PayOrderDO> {

    default PayOrderDO selectByPayOrderNo(String payOrderNo) {
        return selectOne(PayOrderDO::getPayOrderNo, payOrderNo);
    }

    default PayOrderDO selectByChannelTradeNo(String channelTradeNo) {
        return selectOne(PayOrderDO::getChannelTradeNo, channelTradeNo);
    }

    default PageResult<PayOrderDO> selectPage(PayOrderPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PayOrderDO>()
                .likeIfPresent(PayOrderDO::getPayOrderNo, reqVO.getPayOrderNo())
                .eqIfPresent(PayOrderDO::getBizOrderId, reqVO.getBizOrderId())
                .eqIfPresent(PayOrderDO::getChannel, reqVO.getChannel())
                .eqIfPresent(PayOrderDO::getStatus, reqVO.getStatus())
                .orderByDesc(PayOrderDO::getId));
    }
}
