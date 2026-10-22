package cn.iocoder.yudao.module.hotel.framework.store;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

/**
 * 门店编号自动填充：INSERT 时若实体 storeId 为空，写入当前门店。
 *
 * <p>这样所有 T1–T4 的建单/建档逻辑无需逐个改 Service 就能落到当前门店；
 * 集团视角（storeId=0）保持数据库默认值 0。</p>
 */
@Intercepts({@Signature(type = Executor.class, method = "update",
        args = {MappedStatement.class, Object.class})})
public class StoreInsertInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement statement = (MappedStatement) invocation.getArgs()[0];
        if (statement.getSqlCommandType() == SqlCommandType.INSERT) {
            fill(invocation.getArgs()[1]);
        }
        return invocation.proceed();
    }

    private void fill(Object parameter) {
        if (parameter == null) {
            return;
        }
        if (parameter instanceof Map<?, ?> map) {
            for (Object value : map.values()) {
                fill(value);
            }
            return;
        }
        if (parameter instanceof Collection<?> collection) {
            for (Object value : collection) {
                fill(value);
            }
            return;
        }
        if (parameter.getClass().isArray()) {
            for (Object value : (Object[]) parameter) {
                fill(value);
            }
            return;
        }
        fillObject(parameter);
    }

    private void fillObject(Object entity) {
        Long storeId = StoreContext.getStoreId();
        if (storeId == null || storeId <= 0) {
            return;
        }
        try {
            Method getter = entity.getClass().getMethod("getStoreId");
            Object current = getter.invoke(entity);
            if (current != null) {
                return;
            }
            Method setter = entity.getClass().getMethod("setStoreId", Long.class);
            setter.invoke(entity, storeId);
        } catch (NoSuchMethodException ignored) {
            // 非门店业务表（system_* 等），忽略
        } catch (Exception ignored) {
            // 填充失败不影响主流程，数据库默认值兜底
        }
    }

}
