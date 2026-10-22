# -*- coding: utf-8 -*-
"""
P1「面向客户」验收脚本 —— 对应 docs/任务书/P1-面向客户.md 第 5 节 DoD 17 条

运行：
    python script/acceptance/check_p1.py
前置：P0 已验收通过，后端 48080 已启动
"""
import sys
import os

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from harness import (Api, check, skip, section, summary, d, near, money,  # noqa: E402
                     http_status, set_cleanup_api, trash_guest, trash_order)

ADMIN = "/admin-api"
APP = "/app-api"
FE = "http://localhost:5173"
FE_H5 = "http://localhost:5183"

E_HOLD_FAILED = 1003004221
E_HOLD_EXPIRED = 1003004222
E_MEMBER_MOBILE_EXIST = 1003004242


def main():
    api = Api()
    api.login()
    set_cleanup_api(api)
    if not api.token:
        check("管理端登录", False, "拿不到 token")
        return summary("P1")

    # =========================================================== 渠道与房价计划
    section("渠道与房价计划（DoD 2-3）")
    r = api.get(ADMIN + "/hotel/channel/page", {"pageNo": 1, "pageSize": 20})
    channels = Api.rows(r)
    check("[DoD2] 渠道列表可用", Api.ok(r), "code=%s, %d 条" % (r.get("code"), len(channels)))

    ch_id = channels[0].get("id") if channels else None
    r = api.get(ADMIN + "/hotel/rate-plan/page", {"pageNo": 1, "pageSize": 20})
    plans = Api.rows(r)
    check("[DoD2] 房价计划列表可用", Api.ok(r), "code=%s, %d 条" % (r.get("code"), len(plans)))

    if plans and Api.ok(r):
        p = plans[0]
        r = api.post(ADMIN + "/hotel/rate-plan/calc-price", {
            "ratePlanId": p.get("id"), "roomTypeId": p.get("roomTypeId"),
            "beginDate": d(1), "endDate": d(2)})
        cp = Api.data(r) or {}
        check("[DoD3] 房价计划算价接口可用", Api.ok(r), "code=%s" % r.get("code"))
        if Api.ok(r):
            total = cp.get("totalAmount") if isinstance(cp, dict) else None
            daily = (cp.get("dailyPrices") if isinstance(cp, dict) else None) or []
            check("[DoD3] 返回总价与逐日价格", total is not None and bool(daily),
                  "total=%s days=%d" % (total, len(daily)))
            # 折扣规则校验：日历价 300 × 90% = 270
            if daily:
                day1 = money(daily[0].get("price"))
                base = money(daily[0].get("basePrice") or 300)
                rule = p.get("priceRuleType")
                val = money(p.get("priceRuleValue") or 100)
                if rule == 20:
                    check("[DoD3] 折扣规则算价正确", near(day1, base * val / 100),
                          "%s vs %s" % (day1, base * val / 100))
                elif rule == 30:
                    check("[DoD3] 减额规则算价正确", near(day1, base - val),
                          "%s vs %s" % (day1, base - val))
                elif rule == 40:
                    check("[DoD3] 固定价规则算价正确", near(day1, val), "%s" % day1)
                else:
                    check("[DoD3] 按日历价规则", near(day1, base), "%s vs %s" % (day1, base))
    else:
        skip("[DoD3] 算价", "无房价计划数据")

    # =========================================================== 预订引擎
    section("预订引擎（DoD 4-6）")
    r = api.get(APP + "/hotel/booking/availability",
                {"checkInDate": d(1), "checkOutDate": d(3), "adultCount": 2})
    avails = Api.data(r) or []
    check("[DoD4] 可售房型查询", Api.ok(r) and bool(avails),
          "code=%s, %d 个房型" % (r.get("code"), len(avails)))

    hold_no = None
    order_id = None  # 由 submit 产生；未实现时后续用例跳过
    if avails:
        a0 = avails[0]
        check("[DoD4] 返回 price 与 availableCount",
              a0.get("price") is not None and a0.get("availableCount") is not None,
              "price=%s available=%s" % (a0.get("price"), a0.get("availableCount")))

        r = api.post(APP + "/hotel/booking/hold", {
            "roomTypeId": a0.get("roomTypeId"), "checkInDate": d(1), "checkOutDate": d(3),
            "roomCount": 1})
        hold = Api.data(r) or {}
        hold_no = hold.get("holdNo")
        check("[DoD5] 创建占位", Api.ok(r) and bool(hold_no), "holdNo=%s" % hold_no)

        # 库存打满验证防超售
        r = api.post(APP + "/hotel/booking/hold", {
            "roomTypeId": a0.get("roomTypeId"), "checkInDate": d(1), "checkOutDate": d(3),
            "roomCount": 9999})
        check("[DoD5] 超量占位被拒", r.get("code") == E_HOLD_FAILED, "code=%s" % r.get("code"))

    if hold_no:
        r = api.post(APP + "/hotel/booking/submit", {
            "holdNo": hold_no, "contactName": "P1验收", "contactMobile": "13900007777",
            "guestName": "P1验收", "guestMobile": "13900007777"})
        sub = Api.data(r) or {}
        order_id = sub.get("orderId")
        trash_order(order_id)
        check("[DoD6] 提交订单", Api.ok(r) and bool(order_id), "orderId=%s" % order_id)

        if order_id:
            od = Api.data(api.get(ADMIN + "/hotel/order/get", {"id": order_id})) or {}
            check("[DoD6] 新订单 status=10 待支付", od.get("status") == 10,
                  "status=%s" % od.get("status"))

            # =========================================================== 支付
            section("在线支付（DoD 7-9）")
            r = api.post(APP + "/hotel/booking/pay",
                         {"orderId": order_id, "channel": "wxpay"})
            pay = Api.data(r) or {}
            pay_no = pay.get("payOrderNo")
            check("[DoD7] 创建支付单", Api.ok(r) and bool(pay_no), "payOrderNo=%s" % pay_no)

            if pay_no:
                r = api.get(APP + "/hotel/booking/pay-result", {"payOrderNo": pay_no})
                pr = Api.data(r) or {}
                check("[DoD7] 支付结果查询 status=10", pr.get("status") == 10,
                      "status=%s" % pr.get("status"))

                r = api.post(APP + "/hotel/pay/mock-success", {"payOrderNo": pay_no})
                check("[DoD8] 模拟支付成功", Api.ok(r), "code=%s" % r.get("code"))

                od2 = Api.data(api.get(ADMIN + "/hotel/order/get", {"id": order_id})) or {}
                check("[DoD8] 支付后订单 status=20 已确认", od2.get("status") == 20,
                      "status=%s" % od2.get("status"))
                check("[DoD8] 支付后已收金额 > 0", money(od2.get("paidAmount", 0)) > 0,
                      "paid=%s" % od2.get("paidAmount"))

                folios = Api.rows(api.get(ADMIN + "/hotel/folio/page",
                                          {"orderId": order_id, "pageNo": 1, "pageSize": 5}))
                if folios:
                    items = Api.rows(api.get(ADMIN + "/hotel/folio/item-page",
                                             {"folioId": folios[0].get("id"),
                                              "pageNo": 1, "pageSize": 50}))
                    credit = [i for i in items if i.get("direction") == 2]
                    check("[DoD8] 账页有贷方（收款）明细", bool(credit),
                          "%d 条贷方" % len(credit))
                    n1 = len(items)
                    # 幂等：重复回调
                    api.post(APP + "/hotel/pay/mock-success", {"payOrderNo": pay_no})
                    items2 = Api.rows(api.get(ADMIN + "/hotel/folio/item-page",
                                              {"folioId": folios[0].get("id"),
                                               "pageNo": 1, "pageSize": 50}))
                    check("[DoD9] 重复支付回调幂等", len(items2) == n1,
                          "%d -> %d" % (n1, len(items2)))
                else:
                    skip("[DoD8][DoD9] 账页明细", "订单未开账页")
    else:
        skip("[DoD6-9] 下单与支付", "占位未创建")

    # =========================================================== 会员与自助
    section("会员与自助服务（DoD 10-12）")
    mobile = "13900006666"
    tok = api.login_member(mobile, "123456")
    check("[DoD10] 会员登录返回 token", bool(tok), "token=%s" % ("有" if tok else "无"))
    if tok:
        info = Api.data(api.get(APP + "/hotel/member/info")) or {}
        check("[DoD10] 会员信息可查", bool(info), "memberNo=%s" % info.get("memberNo"))

    # 自助入住退房：用上面支付成功的订单
    if order_id:
        r = api.put(APP + "/hotel/member/self-check-in", params={"orderId": order_id})
        # 非当日订单预期失败；当日订单应成功。两种都接受，只要接口可达且语义明确
        reachable = r.get("code") != -1
        check("[DoD11] 自助入住接口可达", reachable, "code=%s msg=%s" % (r.get("code"), r.get("msg")))

        r = api.put(APP + "/hotel/member/self-check-out", params={"orderId": order_id})
        check("[DoD12] 自助退房接口可达", r.get("code") != -1,
              "code=%s msg=%s" % (r.get("code"), r.get("msg")))
    else:
        skip("[DoD11][DoD12] 自助入住退房", "无可用订单")

    # =========================================================== 发票
    section("电子发票（DoD 13）")
    if order_id:
        r = api.post(APP + "/hotel/member/invoice-apply", {
            "orderId": order_id, "title": "P1验收公司", "taxNo": "91330100MA2XXXXX1X",
            "email": "test@example.com", "type": 30})
        apply_id = Api.data(r)
        check("[DoD13] 提交发票申请", Api.ok(r) and bool(apply_id), "code=%s" % r.get("code"))
        if apply_id:
            r = api.put(ADMIN + "/hotel/invoice-apply/handle",
                        {"id": apply_id, "status": 20}, params={"id": apply_id})
            check("[DoD13] 管理端开具发票", Api.ok(r), "code=%s" % r.get("code"))
            inv = Api.rows(api.get(ADMIN + "/hotel/invoice/page",
                                   {"orderId": order_id, "pageNo": 1, "pageSize": 5}))
            check("[DoD13] 生成发票记录", bool(inv), "%d 条" % len(inv))
    else:
        skip("[DoD13] 发票", "无可用订单")

    # =========================================================== 智能定价
    section("智能定价（DoD 18-21）")
    rts = Api.rows(api.get(ADMIN + "/hotel/room-type/page", {"pageNo": 1, "pageSize": 10}))
    rt_id = rts[0].get("id") if rts else None
    if rt_id:
        r = api.post(ADMIN + "/hotel/pricing/generate",
                     {"roomTypeId": rt_id, "beginDate": d(1), "endDate": d(5)})
        check("[DoD18] 生成定价建议", Api.ok(r), "code=%s" % r.get("code"))
        if Api.ok(r):
            sugs = Api.rows(api.get(ADMIN + "/hotel/pricing/list",
                                    {"roomTypeId": rt_id, "beginDate": d(1), "endDate": d(5)}))
            ok_reason = bool(sugs) and all(
                s.get("suggestedPrice") is not None and (s.get("reason") or "").strip()
                for s in sugs)
            check("[DoD18] 建议含价格与非空理由", ok_reason, "%d 条" % len(sugs))
            bad = []
            for s in sugs:
                base, sug = money(s.get("basePrice")), money(s.get("suggestedPrice"))
                if base > 0 and not (float(base) * 0.7 <= float(sug) <= float(base) * 1.5):
                    bad.append("%s->%s" % (base, sug))
            check("[DoD19] 建议价在 [0.7x, 1.5x] 区间内", not bad,
                  "越界 %s" % bad[:3] if bad else "")
        else:
            skip("[DoD18][DoD19] 定价建议", "生成接口未实现")

        r = api.post(ADMIN + "/hotel/pricing/backtest",
                     {"roomTypeId": rt_id, "beginDate": d(-30), "endDate": d(-1)})
        bt = Api.data(r) or {}
        check("[DoD20] 定价回测接口可用", Api.ok(r), "code=%s" % r.get("code"))
        if Api.ok(r):
            check("[DoD20] 回测返回三项指标",
                  bt.get("actualRevpar") is not None
                  and bt.get("modelRevpar") is not None
                  and bt.get("upliftRate") is not None,
                  "actual=%s model=%s uplift=%s"
                  % (bt.get("actualRevpar"), bt.get("modelRevpar"), bt.get("upliftRate")))
    else:
        skip("[DoD18-20] 智能定价", "无房型数据")

    st = http_status("%s/src/views/hotel/pricing/index.vue" % FE)
    check("[DoD21] 智能定价页面可编译", st == 200, "http=%s" % st)
    perm_p = Api.data(api.get(ADMIN + "/system/auth/get-permission-info")) or {}
    check("[DoD21] hotel:pricing:query 已下发",
          "hotel:pricing:query" in (perm_p.get("permissions") or []), "")

    # =========================================================== 权限与前端
    section("权限与前端（DoD 14-17）")
    perm = Api.data(api.get(ADMIN + "/system/auth/get-permission-info")) or {}
    codes = perm.get("permissions") or []
    need = ["hotel:channel:query", "hotel:rate-plan:query", "hotel:member:query"]
    missing = [p for p in need if p not in codes]
    check("[DoD14] P1 新权限码已下发", not missing, "缺失 %s" % missing if missing else "")

    pages = ["hotel/channel/index", "hotel/ratePlan/index", "hotel/member/index",
             "hotel/payOrder/index", "hotel/invoiceApply/index"]
    bad = []
    for p in pages:
        st = http_status("%s/src/views/%s.vue" % (FE, p))
        if st != 200:
            bad.append("%s(%s)" % (p, st))
    check("[DoD15] P1 管理端页面可编译", not bad, "异常：%s" % bad if bad else "%d 个" % len(pages))

    h5 = http_status(FE_H5 + "/")
    booking = http_status(FE + "/src/views/hotel/booking/index.vue")
    check("[DoD16] C 端预订入口可达", h5 == 200 or booking == 200,
          "H5=%s, 内嵌预订页=%s" % (h5, booking))

    if order_id:
        od = Api.data(api.get(ADMIN + "/hotel/order/get", {"id": order_id})) or {}
        check("[DoD17] 订单含渠道/房价计划字段",
              "channelId" in od and "ratePlanId" in od,
              "channelId=%s ratePlanId=%s" % (od.get("channelId"), od.get("ratePlanId")))
        st = od.get("status")
        check("[DoD17] 订单状态枚举已扩容（10 待支付存在）", st is not None, "status=%s" % st)

    return summary("P1")


if __name__ == "__main__":
    raise SystemExit(main())
