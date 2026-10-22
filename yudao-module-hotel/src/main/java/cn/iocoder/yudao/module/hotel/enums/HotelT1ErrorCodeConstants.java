package cn.iocoder.yudao.module.hotel.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * T1 卖房引擎错误码（独立文件，避免与并行开发的其它包冲突）。
 *
 * <p>号段：1-003-004-4201 ~ 1-003-004-4213，占位类错误 4221 / 4222。
 * 交付后由派发者统一合并进 {@link ErrorCodeConstants}。</p>
 */
public interface HotelT1ErrorCodeConstants {

    // ========== 房价计划 1-003-004-4201 ==========
    ErrorCode ERROR_RATE_PLAN_NOT_EXIST = new ErrorCode(1_003_004_201, "房价计划不存在");
    ErrorCode ERROR_RATE_PLAN_CLOSED = new ErrorCode(1_003_004_202, "房价计划已关闭");
    ErrorCode ERROR_RATE_PLAN_MIN_NIGHTS = new ErrorCode(1_003_004_203, "不满足最少入住晚数要求");
    ErrorCode ERROR_RATE_PLAN_ADVANCE = new ErrorCode(1_003_004_204, "不满足最少提前预订时间");
    ErrorCode ERROR_RATE_PLAN_CODE_DUPLICATE = new ErrorCode(1_003_004_205, "房价计划编码已存在");
    ErrorCode ERROR_RATE_PLAN_NO_ROOM_TYPE = new ErrorCode(1_003_004_206, "请先创建房型再配置房价计划");

    // ========== 渠道 1-003-004-4211 ==========
    ErrorCode ERROR_CHANNEL_NOT_EXIST = new ErrorCode(1_003_004_211, "渠道不存在");
    ErrorCode ERROR_CHANNEL_CODE_DUPLICATE = new ErrorCode(1_003_004_212, "渠道编码已存在");
    ErrorCode ERROR_CHANNEL_HAS_RATE_PLAN = new ErrorCode(1_003_004_213, "该渠道下还有房价计划，无法删除");

    // ========== 预订占位 1-003-004-4221 ==========
    ErrorCode ERROR_HOLD_FAILED = new ErrorCode(1_003_004_221, "房源已被占用，请重新选择");
    ErrorCode ERROR_HOLD_EXPIRED = new ErrorCode(1_003_004_222, "预订占位已过期，请重新下单");

    // ========== 智能定价 1-003-004-4231 ==========
    // 注：P1 规格里 4271/4272 落在 T4 号段（4261-4280），为避免并行冲突改用 4231/4232
    ErrorCode ERROR_PRICE_SUGGESTION_NOT_EXIST = new ErrorCode(1_003_004_231, "定价建议不存在");
    ErrorCode ERROR_PRICE_SUGGESTION_NO_BASE_PRICE = new ErrorCode(1_003_004_232, "该日期无基线价，无法生成建议");
    ErrorCode ERROR_PRICE_BACKTEST_EMPTY = new ErrorCode(1_003_004_233, "回测区间内无可用的房价数据");
}
