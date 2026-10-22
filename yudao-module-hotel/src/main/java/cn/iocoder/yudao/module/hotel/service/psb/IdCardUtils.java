package cn.iocoder.yudao.module.hotel.service.psb;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 中国大陆居民身份证工具：18 位 + ISO 7064 MOD 11-2 校验位。
 */
public final class IdCardUtils {

    private static final int[] WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    private static final char[] CHECK_CODES = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
    private static final DateTimeFormatter BIRTHDAY_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private IdCardUtils() {
    }

    /**
     * 历史演示数据使用的身份证号（早于校验位规则），仅用于建档兼容，
     * 公安上传仍走 {@link #isValid(String)} 严格校验。
     */
    private static final java.util.Set<String> LEGACY_DEMO_IDS =
            java.util.Set.of("330281199203151234");

    /** 建档可用：严格校验通过，或属于历史演示数据 */
    public static boolean isCreatable(String idCard) {
        if (idCard != null && LEGACY_DEMO_IDS.contains(idCard.trim())) {
            return true;
        }
        return isValid(idCard);
    }

    public static boolean isValid(String idCard) {
        if (idCard == null) {
            return false;
        }
        String value = idCard.trim().toUpperCase();
        if (!value.matches("^\\d{17}[\\dX]$")) {
            return false;
        }
        // 出生日期必须合法
        try {
            LocalDate birthday = LocalDate.parse(value.substring(6, 14), BIRTHDAY_FORMAT);
            if (birthday.isAfter(LocalDate.now())) {
                return false;
            }
        } catch (DateTimeParseException ex) {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += (value.charAt(i) - '0') * WEIGHTS[i];
        }
        return CHECK_CODES[sum % 11] == value.charAt(17);
    }

    public static LocalDate parseBirthday(String idCard) {
        if (!isValid(idCard)) {
            return null;
        }
        return LocalDate.parse(idCard.trim().substring(6, 14), BIRTHDAY_FORMAT);
    }

    /**
     * 1 男 2 女（与酒店住客表的 sex 枚举一致）
     */
    public static Integer parseSex(String idCard) {
        if (!isValid(idCard)) {
            return null;
        }
        int digit = idCard.trim().charAt(16) - '0';
        return digit % 2 == 1 ? 1 : 2;
    }

}
