package cn.iocoder.yudao.module.hotel.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * T4「数据与演示」错误码（号段 1-003-004-4261 ~ 4280）
 *
 * <p>说明：按并行协作铁律，不直接改公共 {@link ErrorCodeConstants}，本包错误码统一登记在此，
 * 交付时由派发者合并进公共常量。</p>
 */
public interface HotelT4ErrorCodeConstants {

    // ========== 演示数据 1-003-004-261 ==========
    ErrorCode ERROR_DEMO_DATA_EXISTS = new ErrorCode(1_003_004_261, "已有演示数据，请先清理后再生成");
    ErrorCode ERROR_DEMO_DATA_NOT_EXISTS = new ErrorCode(1_003_004_262, "当前没有演示数据");
    ErrorCode ERROR_DEMO_GENERATE_FAILED = new ErrorCode(1_003_004_263, "演示数据生成失败");
    ErrorCode ERROR_DEMO_MONTHS_INVALID = new ErrorCode(1_003_004_264, "演示月份需在 1-12 之间");

    // ========== 报表中心 1-003-004-265 ==========
    ErrorCode ERROR_REPORT_DATE_INVALID = new ErrorCode(1_003_004_265, "报表日期区间不合法");

    // ========== 操作审计 1-003-004-266 ==========
    ErrorCode ERROR_OPERATION_LOG_NOT_EXIST = new ErrorCode(1_003_004_266, "操作审计日志不存在");
}
