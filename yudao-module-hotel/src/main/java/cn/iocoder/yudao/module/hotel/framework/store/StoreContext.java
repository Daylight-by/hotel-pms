package cn.iocoder.yudao.module.hotel.framework.store;

/**
 * 门店上下文：保存当前请求的门店视角。
 *
 * <p>storeId = 0 表示集团视角（不过滤）；大于 0 表示只看该门店数据。
 * 由 {@link StoreContextInterceptor} 从请求头 {@code store-id} 写入，
 * 在 {@link StoreDataPermissionRule} 中读取并拼接 SQL 条件。</p>
 */
public class StoreContext {

    private static final ThreadLocal<Long> STORE_ID = new ThreadLocal<>();

    public static void setStoreId(Long storeId) {
        STORE_ID.set(storeId == null || storeId < 0 ? 0L : storeId);
    }

    public static Long getStoreId() {
        Long storeId = STORE_ID.get();
        return storeId == null ? 0L : storeId;
    }

    public static boolean isGroupView() {
        return getStoreId() == 0L;
    }

    public static void clear() {
        STORE_ID.remove();
    }

}
