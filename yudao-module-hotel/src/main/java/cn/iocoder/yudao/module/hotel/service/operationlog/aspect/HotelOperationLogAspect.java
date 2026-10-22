package cn.iocoder.yudao.module.hotel.service.operationlog.aspect;

import cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils;
import cn.iocoder.yudao.module.hotel.dal.dataobject.operationlog.OperationLogDO;
import cn.iocoder.yudao.module.hotel.dal.mysql.operationlog.OperationLogMapper;
import cn.iocoder.yudao.module.hotel.framework.store.StoreContext;
import cn.iocoder.yudao.module.hotel.service.operationlog.annotation.HotelOperationLog;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserNickname;

/**
 * 操作审计切面
 *
 * <p>拦截 {@code @HotelOperationLog} 标注的方法：调用前序列化入参为 beforeData，
 * 成功后序列化返回值/关键出参为 afterData，写入 {@code hotel_operation_log}。
 * 日志写入失败不阻断主业务。</p>
 */
@Aspect
@Component
@Slf4j
public class HotelOperationLogAspect {

    private static final int MAX_LEN = 4000;

    @Resource
    private OperationLogMapper operationLogMapper;
    @Resource
    private ObjectMapper objectMapper;

    @Around("@annotation(hotelOperationLog)")
    public Object around(ProceedingJoinPoint joinPoint, HotelOperationLog hotelOperationLog) throws Throwable {
        String before = serializeArgs(joinPoint.getArgs());
        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable ex) {
            // 业务失败不写审计（与事务回滚一致）
            throw ex;
        }
        try {
            String after = serializeResult(result);
            String bizId = extractBizId(joinPoint.getArgs());
            OperationLogDO logDO = new OperationLogDO();
            logDO.setStoreId(StoreContext.getStoreId());
            logDO.setModule(hotelOperationLog.module());
            logDO.setBizId(bizId);
            logDO.setOperation(hotelOperationLog.operation());
            logDO.setBeforeData(before);
            logDO.setAfterData(after);
            logDO.setOperatorId(getLoginUserId());
            logDO.setOperatorName(getLoginUserNickname());
            logDO.setIp(obtainClientIp());
            operationLogMapper.insert(logDO);
        } catch (Exception ex) {
            // 审计写入失败不影响主业务
            log.warn("[around][操作审计写入失败：module={}, operation={}]", hotelOperationLog.module(),
                    hotelOperationLog.operation(), ex);
        }
        return result;
    }

    private String serializeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        try {
            return truncate(objectMapper.writeValueAsString(args));
        } catch (Exception ex) {
            return "[序列化失败]";
        }
    }

    private String serializeResult(Object result) {
        if (result == null) {
            return "{}";
        }
        try {
            return truncate(objectMapper.writeValueAsString(result));
        } catch (Exception ex) {
            return String.valueOf(result);
        }
    }

    /**
     * 提取业务单据号：优先取参数中的数字 id，其次在首个非空参数 JSON 中找 id/folioId/orderId/itemId/roomTypeId。
     */
    private String extractBizId(Object[] args) {
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof Number) {
                    return String.valueOf(arg);
                }
            }
            for (Object arg : args) {
                if (arg == null) {
                    continue;
                }
                try {
                    JsonNode node = objectMapper.valueToTree(arg);
                    for (String field : new String[]{"folioId", "orderId", "itemId", "roomTypeId", "id"}) {
                        JsonNode v = node.get(field);
                        if (v != null && !v.isNull()) {
                            return v.asText();
                        }
                    }
                } catch (Exception ignored) {
                    // 尝试下一个参数
                }
            }
        }
        return null;
    }

    private String obtainClientIp() {
        try {
            HttpServletRequest request = WebFrameworkUtils.getRequest();
            if (request != null) {
                return request.getRemoteAddr();
            }
        } catch (Exception ignored) {
            // 无请求上下文（如内部调用）时忽略
        }
        return null;
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() > MAX_LEN ? value.substring(0, MAX_LEN) : value;
    }
}
