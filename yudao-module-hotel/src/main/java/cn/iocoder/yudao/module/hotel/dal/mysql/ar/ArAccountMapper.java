package cn.iocoder.yudao.module.hotel.dal.mysql.ar;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.hotel.controller.admin.ar.vo.ArAccountPageReqVO;
import cn.iocoder.yudao.module.hotel.dal.dataobject.ar.ArAccountDO;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface ArAccountMapper extends BaseMapperX<ArAccountDO> {

    default PageResult<ArAccountDO> selectPage(ArAccountPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ArAccountDO>()
                .likeIfPresent(ArAccountDO::getName, reqVO.getName())
                .likeIfPresent(ArAccountDO::getCode, reqVO.getCode())
                .eqIfPresent(ArAccountDO::getStatus, reqVO.getStatus())
                .orderByDesc(ArAccountDO::getId));
    }

    default ArAccountDO selectByCode(String code) {
        return selectOne(ArAccountDO::getCode, code);
    }

    default List<ArAccountDO> selectListAll() {
        return selectList(new LambdaQueryWrapperX<ArAccountDO>().orderByAsc(ArAccountDO::getId));
    }

    default int addUsedAmount(Long id, BigDecimal amount) {
        return update(null, new UpdateWrapper<ArAccountDO>()
                .eq("id", id)
                .setSql("used_amount = GREATEST(used_amount + (" + amount.toPlainString() + "), 0)"));
    }

}
