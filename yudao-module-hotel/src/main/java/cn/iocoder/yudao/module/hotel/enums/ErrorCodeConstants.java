package cn.iocoder.yudao.module.hotel.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * Hotel 模块错误码，使用 1-003-000-000 段
 */
public interface ErrorCodeConstants {

    // ========== 房型 1-003-001-000 ==========
    ErrorCode ROOM_TYPE_NOT_EXISTS = new ErrorCode(1_003_001_000, "房型不存在");
    ErrorCode ROOM_TYPE_CODE_DUPLICATE = new ErrorCode(1_003_001_001, "房型编码已存在");
    ErrorCode ROOM_TYPE_HAS_ROOM = new ErrorCode(1_003_001_002, "该房型下还有房间，无法删除");

    // ========== 房间 1-003-002-000 ==========
    ErrorCode ROOM_NOT_EXISTS = new ErrorCode(1_003_002_000, "房间不存在");
    ErrorCode ROOM_NO_DUPLICATE = new ErrorCode(1_003_002_001, "房间号已存在");
    ErrorCode ROOM_HAS_ACTIVE_ORDER = new ErrorCode(1_003_002_002, "该房间还有未完成订单，无法删除");
    ErrorCode ROOM_HAS_OPEN_CLEANING = new ErrorCode(1_003_002_003, "该房间还有未完成的清洁任务");
    ErrorCode ROOM_UNDER_MAINTENANCE = new ErrorCode(1_003_002_004, "房间正在维修，无法安排入住");

    // ========== 住客 1-003-003-000 ==========
    ErrorCode GUEST_NOT_EXISTS = new ErrorCode(1_003_003_000, "住客不存在");

    // ========== 订单 1-003-004-000 ==========
    ErrorCode ORDER_NOT_EXISTS = new ErrorCode(1_003_004_000, "订单不存在");
    ErrorCode ORDER_DATE_INVALID = new ErrorCode(1_003_004_001, "离店日期必须晚于入住日期");
    ErrorCode ORDER_ROOM_CONFLICT = new ErrorCode(1_003_004_002, "所选房间在该日期已被占用");
    ErrorCode ORDER_STATUS_NOT_ALLOW = new ErrorCode(1_003_004_003, "订单当前状态不允许该操作");
    ErrorCode ORDER_HAS_PAID = new ErrorCode(1_003_004_004, "订单已收款，请先退款");
    ErrorCode ORDER_NOT_PAID = new ErrorCode(1_003_004_005, "订单没有可退款的支付记录");
    ErrorCode ORDER_REFUND_EXCEED = new ErrorCode(1_003_004_006, "退款金额不能超过已收金额");
    ErrorCode ORDER_DELETE_NOT_ALLOW = new ErrorCode(1_003_004_007, "只有已取消的订单可以删除");

    // ========== 清洁任务 1-003-005-000 ==========
    ErrorCode CLEANING_TASK_NOT_EXISTS = new ErrorCode(1_003_005_000, "清洁任务不存在");
    ErrorCode CLEANING_STATUS_NOT_ALLOW = new ErrorCode(1_003_005_001, "清洁任务状态流转不合法");
    ErrorCode CLEANING_DELETE_NOT_ALLOW = new ErrorCode(1_003_005_002, "只有待处理的任务可以删除");

    // ========== P0 房价日历 1-003-004-101 ==========
    ErrorCode ERROR_DAILY_RATE_NOT_EXIST = new ErrorCode(1_003_004_101, "该日期房价未维护");
    ErrorCode ERROR_DAILY_RATE_CLOSED = new ErrorCode(1_003_004_102, "该日期已关房，不可预订");
    ErrorCode ERROR_DAILY_RATE_INVENTORY_NOT_ENOUGH = new ErrorCode(1_003_004_103, "该日期房量不足");

    // ========== P0 账务账页 1-003-004-111 ==========
    ErrorCode ERROR_FOLIO_NOT_EXIST = new ErrorCode(1_003_004_111, "账页不存在");
    ErrorCode ERROR_FOLIO_ALREADY_SETTLED = new ErrorCode(1_003_004_112, "账页已结账，不可操作");
    ErrorCode ERROR_FOLIO_BALANCE_NOT_ZERO = new ErrorCode(1_003_004_113, "账页余额不为零，无法结账");
    ErrorCode ERROR_FOLIO_ITEM_NOT_EXIST = new ErrorCode(1_003_004_114, "账务明细不存在");
    ErrorCode ERROR_FOLIO_ITEM_ALREADY_VOID = new ErrorCode(1_003_004_115, "该明细已冲销");

    // ========== P0 支付 1-003-004-121 ==========
    ErrorCode ERROR_PAYMENT_AMOUNT_INVALID = new ErrorCode(1_003_004_121, "支付金额非法");

    // ========== P0 夜审 1-003-004-131 ==========
    ErrorCode ERROR_NIGHT_AUDIT_NOT_ALLOWED = new ErrorCode(1_003_004_131, "存在未处理的在住订单，不能夜审");
    ErrorCode ERROR_NIGHT_AUDIT_ALREADY_DONE = new ErrorCode(1_003_004_132, "该营业日已完成夜审");
    ErrorCode ERROR_BUSINESS_DATE_NOT_EXIST = new ErrorCode(1_003_004_133, "营业日未初始化");

    // ========== P0 交班 1-003-004-141 ==========
    ErrorCode ERROR_SHIFT_NOT_OPEN = new ErrorCode(1_003_004_141, "当前无进行中的班次");
    ErrorCode ERROR_SHIFT_ALREADY_OPEN = new ErrorCode(1_003_004_142, "已有进行中的班次，请先交班");

    // ========== P0 发票 1-003-004-151 ==========
    ErrorCode ERROR_INVOICE_AMOUNT_INVALID = new ErrorCode(1_003_004_151, "开票金额超出可开金额");
    ErrorCode ERROR_INVOICE_ALREADY_VOID = new ErrorCode(1_003_004_152, "该发票已红冲");

    // ========== P0 订单操作 1-003-004-161 ==========
    ErrorCode ERROR_HOURLY_NOT_ENABLED = new ErrorCode(1_003_004_161, "该房型未开放钟点房");
    ErrorCode ERROR_ORDER_CANNOT_EXTEND = new ErrorCode(1_003_004_171, "该订单状态不可续住");
    ErrorCode ERROR_ORDER_CANNOT_CHANGE_ROOM = new ErrorCode(1_003_004_172, "该订单状态不可换房");
}
