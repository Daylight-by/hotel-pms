package cn.iocoder.yudao.module.hotel.service.audit;

import cn.iocoder.yudao.module.hotel.dal.dataobject.audit.BusinessDateDO;

import java.time.LocalDate;

/**
 * 营业日 Service
 */
public interface BusinessDateService {

    /**
     * 获得当前营业日；不存在则初始化为今天
     */
    LocalDate getCurrentBusinessDate();

    /**
     * 当前营业日实体（不存在则初始化）
     */
    BusinessDateDO getCurrentBusinessDateDO();

    /**
     * 不存在则初始化
     */
    void initIfAbsent();

    /**
     * 营业日 +1，状态回到进行中
     */
    Long rollBusinessDate();
}
