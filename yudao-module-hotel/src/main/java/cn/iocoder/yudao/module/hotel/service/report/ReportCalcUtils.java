package cn.iocoder.yudao.module.hotel.service.report;

import cn.iocoder.yudao.module.hotel.enums.PaymentMethodEnum;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 报表计算小工具
 */
public final class ReportCalcUtils {

    private ReportCalcUtils() {}

    /**
     * 按条件汇总金额
     */
    public static <T> BigDecimal sum(List<T> list, Predicate<T> filter, Function<T, BigDecimal> mapper) {
        BigDecimal total = BigDecimal.ZERO;
        for (T obj : list) {
            if (filter.test(obj)) {
                BigDecimal v = mapper.apply(obj);
                if (v != null) {
                    total = total.add(v);
                }
            }
        }
        return total;
    }

    /**
     * 汇总金额
     */
    public static <T> BigDecimal sum(List<T> list, Function<T, BigDecimal> mapper) {
        BigDecimal total = BigDecimal.ZERO;
        for (T obj : list) {
            BigDecimal v = mapper.apply(obj);
            if (v != null) {
                total = total.add(v);
            }
        }
        return total;
    }

    /**
     * 百分比保留 2 位小数
     */
    public static BigDecimal percent(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 支付方式名称（避免改动公共枚举）
     */
    public static String paymentMethodName(Integer method) {
        if (method == null) {
            return "";
        }
        for (PaymentMethodEnum value : PaymentMethodEnum.values()) {
            if (value.getMethod().equals(method)) {
                return value.getName();
            }
        }
        return String.valueOf(method);
    }
}
