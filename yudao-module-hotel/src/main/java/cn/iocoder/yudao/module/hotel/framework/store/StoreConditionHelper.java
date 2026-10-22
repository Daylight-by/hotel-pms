package cn.iocoder.yudao.module.hotel.framework.store;

import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;

/**
 * 门店条件助手：T5 新增的 Service/Mapper 显式拼条件时统一走这里。
 *
 * <p>已有查询由 {@link StoreDataPermissionRule} 自动追加，不需要逐个方法手写；
 * 这里用于新表的聚合查询、自定义 SQL 等数据权限插件覆盖不到的场景。</p>
 */
public final class StoreConditionHelper {

    private StoreConditionHelper() {
    }

    public static <T> LambdaQueryWrapperX<T> apply(LambdaQueryWrapperX<T> wrapper, Long storeId) {
        if (storeId != null && storeId > 0) {
            wrapper.apply("store_id = {0}", storeId);
        }
        return wrapper;
    }

    public static <T> LambdaQueryWrapperX<T> applyCurrent(LambdaQueryWrapperX<T> wrapper) {
        return apply(wrapper, StoreContext.getStoreId());
    }

}
