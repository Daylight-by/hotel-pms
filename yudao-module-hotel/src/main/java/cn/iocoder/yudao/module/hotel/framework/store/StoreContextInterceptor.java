package cn.iocoder.yudao.module.hotel.framework.store;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 门店上下文拦截器：从请求头 {@code store-id} 读取门店编号。
 *
 * <p>无请求头时按集团视角（0）处理；门店编号非法时也回退到集团视角，
 * 避免前端脏数据把整个列表查空。</p>
 */
@Slf4j
public class StoreContextInterceptor implements HandlerInterceptor {

    public static final String HEADER_STORE_ID = "store-id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String value = request.getHeader(HEADER_STORE_ID);
        if (value == null || value.isBlank()) {
            StoreContext.setStoreId(0L);
            return true;
        }
        try {
            StoreContext.setStoreId(Long.parseLong(value.trim()));
        } catch (NumberFormatException ex) {
            log.warn("[preHandle][非法 store-id 请求头({})，按集团视角处理]", value);
            StoreContext.setStoreId(0L);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        StoreContext.clear();
    }

}
