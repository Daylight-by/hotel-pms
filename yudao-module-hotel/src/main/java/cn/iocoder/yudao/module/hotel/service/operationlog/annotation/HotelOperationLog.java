package cn.iocoder.yudao.module.hotel.service.operationlog.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 酒店操作审计注解
 *
 * <p>标注在关键写操作方法上（入账、冲销、改价、夜审、换房、挂账、取消订单），
 * 由 {@code HotelOperationLogAspect} 织入，写入 {@code hotel_operation_log} 变更前后数据。</p>
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HotelOperationLog {

    /** 模块：order / folio / rate / audit / ar / ... */
    String module() default "";

    /** 操作描述：如 入账 / 冲销 / 改价 / 夜审 / 换房 / 取消订单 */
    String operation() default "";
}
