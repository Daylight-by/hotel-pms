package cn.iocoder.yudao.module.hotel.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * T5 商业化横切错误码（号段 1-003-004-4301 ~ 4360）。
 *
 * <p>按并行协作约定，不直接修改公共的 {@link ErrorCodeConstants}，
 * 由派发者统一合并；本类中的常量与交付说明中的清单一一对应。</p>
 */
public interface T5ErrorCodeConstants {

    // ========== 门店 1-003-004-4301 ==========
    ErrorCode ERROR_STORE_NOT_EXIST = new ErrorCode(1_003_004_301, "门店不存在");
    ErrorCode ERROR_STORE_CLOSED = new ErrorCode(1_003_004_302, "门店已停业");
    ErrorCode ERROR_STORE_NO_PERMISSION = new ErrorCode(1_003_004_303, "无该门店数据权限");
    ErrorCode ERROR_STORE_CODE_DUPLICATE = new ErrorCode(1_003_004_304, "门店编码已存在");
    ErrorCode ERROR_STORE_HAS_DATA = new ErrorCode(1_003_004_305, "门店下仍有业务数据，无法删除");

    // ========== 公安上传 1-003-004-4311 ==========
    ErrorCode ERROR_PSB_UPLOAD_FAILED = new ErrorCode(1_003_004_311, "公安系统上传失败");
    ErrorCode ERROR_PSB_ID_CARD_INVALID = new ErrorCode(1_003_004_312, "身份证号格式不合法");
    ErrorCode ERROR_PSB_NOT_SUPPORT = new ErrorCode(1_003_004_313, "该门店未开通公安上传");

    // ========== 门锁 1-003-004-4321 ==========
    ErrorCode ERROR_LOCK_CARD_FAILED = new ErrorCode(1_003_004_321, "门锁发卡失败");
    ErrorCode ERROR_LOCK_NOT_CONFIG = new ErrorCode(1_003_004_322, "未配置门锁系统");
    ErrorCode ERROR_LOCK_LOG_NOT_EXIST = new ErrorCode(1_003_004_323, "发卡记录不存在");

    // ========== 读卡器 1-003-004-4331 ==========
    ErrorCode ERROR_ID_READER_FAILED = new ErrorCode(1_003_004_331, "身份证读卡失败");

    // ========== 协议单位 AR 1-003-004-4341 ==========
    ErrorCode ERROR_AR_ACCOUNT_NOT_EXIST = new ErrorCode(1_003_004_341, "协议单位不存在");
    ErrorCode ERROR_AR_CREDIT_EXCEED = new ErrorCode(1_003_004_342, "超出协议单位信用额度");
    ErrorCode ERROR_AR_ACCOUNT_DISABLED = new ErrorCode(1_003_004_343, "协议单位已停用");
    ErrorCode ERROR_AR_CODE_DUPLICATE = new ErrorCode(1_003_004_344, "协议单位编码已存在");
    ErrorCode ERROR_AR_AMOUNT_INVALID = new ErrorCode(1_003_004_345, "挂账金额不合法");
    ErrorCode ERROR_AR_SETTLEMENT_NOT_EXIST = new ErrorCode(1_003_004_346, "结算单不存在");
    ErrorCode ERROR_AR_SETTLEMENT_EMPTY = new ErrorCode(1_003_004_347, "账期内没有未结挂账");
    ErrorCode ERROR_AR_RECEIVE_EXCEED = new ErrorCode(1_003_004_348, "收款金额超过待收金额");

    // ========== OTA 1-003-004-4351 ==========
    ErrorCode ERROR_OTA_CONFIG_NOT_EXIST = new ErrorCode(1_003_004_351, "OTA 配置不存在");
    ErrorCode ERROR_OTA_ORDER_EXIST = new ErrorCode(1_003_004_352, "该 OTA 订单已存在");
    ErrorCode ERROR_OTA_SYNC_FAILED = new ErrorCode(1_003_004_353, "OTA 同步失败");

}
