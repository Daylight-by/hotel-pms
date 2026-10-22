package cn.iocoder.yudao.module.hotel.enums;

import cn.iocoder.yudao.framework.common.exception.ErrorCode;

/**
 * T2 收钱与客（支付 / 会员 / 自助 / 发票申请）错误码。
 *
 * <p>号段：1-003-004-4235 ~ 1-003-004-4260。
 * 说明：P1 规格里 4231-4234 原分配给支付，但 T1 已占用于智能定价，
 * 为避免并行冲突，T2 实际使用 4235-4239；会员 4241-4247；发票/自助 4251-4258。
 * 交付后由派发者统一合并进 {@link ErrorCodeConstants}。</p>
 */
public interface HotelT2ErrorCodeConstants {

    // ========== 在线支付 1-003-004-4235 ==========
    ErrorCode ERROR_PAY_ORDER_NOT_EXIST = new ErrorCode(1_003_004_235, "支付单不存在");
    ErrorCode ERROR_PAY_ORDER_STATUS_INVALID = new ErrorCode(1_003_004_236, "支付单状态异常");
    ErrorCode ERROR_PAY_AMOUNT_MISMATCH = new ErrorCode(1_003_004_237, "支付金额与订单金额不一致");
    ErrorCode ERROR_PAY_CHANNEL_NOT_SUPPORT = new ErrorCode(1_003_004_238, "暂不支持该支付方式");
    ErrorCode ERROR_PAY_MOCK_DISABLED = new ErrorCode(1_003_004_239, "模拟支付未开启");

    // ========== 会员 1-003-004-4241 ==========
    ErrorCode ERROR_MEMBER_NOT_EXIST = new ErrorCode(1_003_004_241, "会员不存在");
    ErrorCode ERROR_MEMBER_MOBILE_EXIST = new ErrorCode(1_003_004_242, "该手机号已注册会员");
    ErrorCode ERROR_MEMBER_POINTS_NOT_ENOUGH = new ErrorCode(1_003_004_243, "积分不足");
    ErrorCode ERROR_MEMBER_BALANCE_NOT_ENOUGH = new ErrorCode(1_003_004_244, "储值余额不足");
    ErrorCode ERROR_MEMBER_LOGIN_FAIL = new ErrorCode(1_003_004_245, "手机号或验证码不正确");
    ErrorCode ERROR_MEMBER_FROZEN = new ErrorCode(1_003_004_246, "会员已被冻结");
    ErrorCode ERROR_MEMBER_PASSWORD_ERROR = new ErrorCode(1_003_004_247, "会员密码不正确");

    // ========== 发票申请 / 自助服务 1-003-004-4251 ==========
    ErrorCode ERROR_INVOICE_APPLY_NOT_EXIST = new ErrorCode(1_003_004_251, "发票申请不存在");
    ErrorCode ERROR_INVOICE_APPLY_STATUS_INVALID = new ErrorCode(1_003_004_252, "发票申请状态异常");
    ErrorCode ERROR_INVOICE_APPLY_AMOUNT_EXCEED = new ErrorCode(1_003_004_253, "开票金额超出可申请金额");
    ErrorCode ERROR_SELF_CHECK_IN_NOT_ALLOWED = new ErrorCode(1_003_004_254, "当前订单不可自助入住（需已支付且为当日订单）");
    ErrorCode ERROR_SELF_CHECK_OUT_NOT_ALLOWED = new ErrorCode(1_003_004_255, "当前订单不可自助退房");
    ErrorCode ERROR_PAY_NOTIFY_INVALID = new ErrorCode(1_003_004_256, "支付回调验签失败");
    ErrorCode ERROR_ORDER_NOT_PAID_FOR_INVOICE = new ErrorCode(1_003_004_257, "订单未支付，暂不能申请发票");
    ErrorCode ERROR_PAY_REFUND_EXCEED = new ErrorCode(1_003_004_258, "退款金额超过可退金额");
}
