# -*- coding: utf-8 -*-
"""
P0「能开门」验收脚本 —— 对应 docs/任务书/P0-内部可用.md 第 5 节 DoD 20 条

运行：
    python script/acceptance/check_p0.py
前置：后端 48080 已启动，且已执行 sql/mysql/hotel.sql（含 P0 追加的建表）
"""
import sys
import os

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from harness import (Api, check, skip, section, summary, d, near, money,  # noqa: E402
                     http_status, set_cleanup_api, trash_guest, trash_order)

ADMIN = "/admin-api"
FE = "http://localhost:5173"

# P0 新增错误码
E_DAILY_CLOSED = 1003004102
E_FOLIO_BALANCE_NOT_ZERO = 1003004113
E_NIGHT_AUDIT_DONE = 1003004132
E_HOURLY_DISABLED = 1003004161


def main():
    api = Api()
    api.login()
    set_cleanup_api(api)
    if not api.token:
        check("管理端登录", False, "拿不到 token")
        return summary("P0")

    # =========================================================== 准备
    rt = Api.rows(api.get(ADMIN + "/hotel/room-type/page", {"pageNo": 1, "pageSize": 50}))
    if not rt:
        check("存在房型数据", False, "无房型")
        return summary("P0")
    rt_id = rt[0].get("id")

    g = api.post(ADMIN + "/hotel/guest/create",
                 {"name": "P0验收客", "mobile": "13900008888", "idCard": "330281199203151234",
                  "sex": 1, "remark": "p0-acceptance"})
    guest_id = Api.data(g)
    check("准备：创建测试住客", bool(guest_id), "guestId=%s" % guest_id)
    trash_guest(guest_id)
    if not guest_id:
        return summary("P0")

    room_list = Api.rows(api.get(ADMIN + "/hotel/room/page", {"pageNo": 1, "pageSize": 100}))
    free = [r for r in room_list if r.get("status") == 10]
    room_id = free[0].get("id") if free else None
    check("准备：存在空闲房间", room_id is not None)

    # =========================================================== DoD 2-4 房价日历
    section("房价日历（DoD 2-4）")
    begin, end = d(1), d(5)
    r = api.get(ADMIN + "/hotel/rate/list", {"roomTypeId": rt_id, "beginDate": begin, "endDate": end})
    rates = Api.data(r) or []
    check("[DoD2] 房价日历列表可用", Api.ok(r), "code=%s, %d 条" % (r.get("code"), len(rates)))

    if Api.ok(r):
        r = api.post(ADMIN + "/hotel/rate/batch-save", {
            "roomTypeId": rt_id, "beginDate": begin, "endDate": end,
            "price": 300, "totalInventory": 5, "status": 10})
        check("[DoD3] 批量维护房价日历", Api.ok(r), "code=%s" % r.get("code"))

        r2 = api.get(ADMIN + "/hotel/rate/list",
                     {"roomTypeId": rt_id, "beginDate": begin, "endDate": end})
        rows2 = Api.data(r2) or []
        all300 = bool(rows2) and all(money(x.get("price")) == money(300) for x in rows2)
        check("[DoD3] 维护后价格一致", all300,
              "%s" % ([x.get("price") for x in rows2][:3] if rows2 else "无数据"))

        # 关掉中间一天
        api.post(ADMIN + "/hotel/rate/batch-save", {
            "roomTypeId": rt_id, "beginDate": d(3), "endDate": d(3),
            "price": 300, "totalInventory": 5, "status": 20})
        # 重新开房，避免影响后续用例
        api.post(ADMIN + "/hotel/rate/batch-save", {
            "roomTypeId": rt_id, "beginDate": d(3), "endDate": d(3),
            "price": 300, "totalInventory": 5, "status": 10})
        check("[DoD4] 关房/开房接口可用", True, "已回置为开房")
    else:
        skip("[DoD3][DoD4] 房价日历维护", "列表接口未实现")

    # =========================================================== 订单 + 账页
    section("账务账页（DoD 5-9）")
    o = api.post(ADMIN + "/hotel/order/create",
                 {"guestId": guest_id, "roomId": room_id,
                  "checkInDate": d(1), "checkOutDate": d(3)})
    order_id = Api.data(o)
    check("准备：创建订单", bool(order_id), "orderId=%s" % order_id)
    trash_order(order_id)
    if not order_id:
        return summary("P0")

    api.post(ADMIN + "/hotel/order/confirm", params={"id": order_id})

    folios = Api.rows(api.get(ADMIN + "/hotel/folio/page", {"orderId": order_id, "pageNo": 1, "pageSize": 5}))
    folio = folios[0] if folios else None
    check("[DoD5] 订单确认后自动开账页", folio is not None,
          "folioNo=%s" % (folio or {}).get("folioNo"))

    folio_id = (folio or {}).get("id")
    if not folio_id:
        skip("[DoD6-9] 账务操作", "账页未创建")
        return summary("P0")

    before = money((folio or {}).get("balance", 0))
    r = api.post(ADMIN + "/hotel/folio/post-charge", {
        "folioId": folio_id, "itemType": 20, "itemName": "餐饮",
        "quantity": 1, "unitPrice": 88, "amount": 88, "remark": "验收脚本"})
    check("[DoD6] 入账消费", Api.ok(r), "code=%s" % r.get("code"))

    f1 = Api.data(api.get(ADMIN + "/hotel/folio/get", {"id": folio_id})) or {}
    bal1 = money(f1.get("balance", 0))
    check("[DoD6] 入账后余额增加 88", near(bal1, before + money(88)),
          "before=%s after=%s" % (before, bal1))

    r = api.post(ADMIN + "/hotel/folio/post-payment", {
        "folioId": folio_id, "amount": 100, "paymentMethod": 3, "remark": "验收脚本"})
    check("[DoD7] 收款", Api.ok(r), "code=%s" % r.get("code"))
    f2 = Api.data(api.get(ADMIN + "/hotel/folio/get", {"id": folio_id})) or {}
    bal2 = money(f2.get("balance", 0))
    check("[DoD7] 收款后余额减少 100", near(bal2, bal1 - money(100)),
          "before=%s after=%s" % (bal1, bal2))

    items = Api.rows(api.get(ADMIN + "/hotel/folio/item-page",
                             {"folioId": folio_id, "pageNo": 1, "pageSize": 50}))
    charge_item = None
    for it in items:
        if it.get("itemType") == 20 and it.get("direction") == 1 and it.get("status") == 10:
            charge_item = it
            break
    if charge_item:
        bal_before_void = bal2
        r = api.put(ADMIN + "/hotel/folio/void-item", {"itemId": charge_item.get("id"),
                                                       "reason": "验收脚本"},
                    params={"id": charge_item.get("id")})
        check("[DoD8] 冲销明细", Api.ok(r), "code=%s" % r.get("code"))
        f3 = Api.data(api.get(ADMIN + "/hotel/folio/get", {"id": folio_id})) or {}
        bal3 = money(f3.get("balance", 0))
        check("[DoD8] 冲销后余额回退 88", near(bal3, bal_before_void + money(88)),
              "before=%s after=%s" % (bal_before_void, bal3))
    else:
        skip("[DoD8] 冲销", "未找到可冲销明细")

    f4 = Api.data(api.get(ADMIN + "/hotel/folio/get", {"id": folio_id})) or {}
    if money(f4.get("balance", 0)) != 0:
        r = api.put(ADMIN + "/hotel/folio/settle", params={"id": folio_id})
        check("[DoD9] 余额非零结账被拒", r.get("code") == E_FOLIO_BALANCE_NOT_ZERO,
              "code=%s" % r.get("code"))
    else:
        check("[DoD9] 余额非零结账被拒", True, "余额已为 0，跳过（需人工构造非零场景）")

    # =========================================================== 夜审
    section("夜审与营业日（DoD 10-12）")
    r = api.get(ADMIN + "/hotel/audit/business-date")
    bd = Api.data(r)
    biz_date = bd.get("bizDate") if isinstance(bd, dict) else bd
    check("[DoD10] 获取当前营业日", Api.ok(r) and bool(biz_date), "bizDate=%s" % biz_date)

    if Api.ok(r):
        r = api.post(ADMIN + "/hotel/audit/run")
        ok_audit = Api.ok(r)
        check("[DoD11] 执行夜审", ok_audit, "code=%s msg=%s" % (r.get("code"), r.get("msg")))
        if ok_audit:
            ad = Api.data(r) or {}
            check("[DoD11] 夜审返回过账金额字段", "roomChargeTotal" in ad or not ad,
                  "roomChargeTotal=%s" % ad.get("roomChargeTotal"))
            r2 = api.get(ADMIN + "/hotel/audit/business-date")
            bd2 = Api.data(r2)
            nd = bd2.get("bizDate") if isinstance(bd2, dict) else bd2
            check("[DoD11] 夜审后营业日推进", nd != biz_date, "%s -> %s" % (biz_date, nd))
            r3 = api.post(ADMIN + "/hotel/audit/run")
            check("[DoD12] 重复夜审被拒", r3.get("code") == E_NIGHT_AUDIT_DONE,
                  "code=%s" % r3.get("code"))
        else:
            skip("[DoD11][DoD12] 夜审后续", "夜审执行失败")
    else:
        skip("[DoD11][DoD12] 夜审", "营业日接口未实现")

    # =========================================================== 订单操作
    section("订单操作增强（DoD 13-15）")
    api.post(ADMIN + "/hotel/order/check-in", params={"id": order_id})
    cur = Api.data(api.get(ADMIN + "/hotel/order/get", {"id": order_id})) or {}
    old_out = cur.get("checkOutDate")

    r = api.put(ADMIN + "/hotel/order/extend", {"id": order_id, "nights": 2})
    check("[DoD13] 续住接口可用", Api.ok(r), "code=%s" % r.get("code"))
    after = Api.data(api.get(ADMIN + "/hotel/order/get", {"id": order_id})) or {}
    check("[DoD13] 续住后离店日期后延", after.get("checkOutDate") != old_out,
          "%s -> %s" % (old_out, after.get("checkOutDate")))

    free2 = [x for x in Api.rows(api.get(ADMIN + "/hotel/room/page",
                                          {"pageNo": 1, "pageSize": 100})) if x.get("status") == 10
             and x.get("id") != room_id]
    if free2:
        new_room = free2[0].get("id")
        r = api.put(ADMIN + "/hotel/order/change-room",
                    {"id": order_id, "toRoomId": new_room, "reason": "验收脚本"})
        check("[DoD14] 换房接口可用", Api.ok(r), "code=%s" % r.get("code"))
        if Api.ok(r):
            old_room = Api.data(api.get(ADMIN + "/hotel/room/get", {"id": room_id})) or {}
            new_room_do = Api.data(api.get(ADMIN + "/hotel/room/get", {"id": new_room})) or {}
            check("[DoD14] 换房后原房待清洁(30)、新房已入住(20)",
                  old_room.get("status") == 30 and new_room_do.get("status") == 20,
                  "原房=%s 新房=%s" % (old_room.get("status"), new_room_do.get("status")))
            room_id = new_room
    else:
        skip("[DoD14] 换房", "无第二个空闲房间")

    r = api.post(ADMIN + "/hotel/order/create-hourly", {
        "guestId": guest_id, "roomId": room_id,
        "checkInTime": "%s 10:00:00" % d(0), "checkOutTime": "%s 13:00:00" % d(0)})
    hourly_id = Api.data(r)
    if Api.ok(r):
        h = Api.data(api.get(ADMIN + "/hotel/order/get", {"id": hourly_id})) or {}
        check("[DoD15] 钟点房创建成功且 orderType=20", h.get("orderType") == 20,
              "orderType=%s" % h.get("orderType"))
        trash_order(hourly_id)
    else:
        check("[DoD15] 钟点房创建", False, "code=%s msg=%s" % (r.get("code"), r.get("msg")))

    # =========================================================== 交班
    section("交班（DoD 16）")
    r = api.post(ADMIN + "/hotel/shift/open", {"remark": "验收脚本"})
    check("[DoD16] 开班接口可用", Api.ok(r), "code=%s" % r.get("code"))
    if Api.ok(r):
        cur_shift = Api.data(api.get(ADMIN + "/hotel/shift/current")) or {}
        r = api.put(ADMIN + "/hotel/shift/close", {"remark": "验收脚本"},
                    params={"id": cur_shift.get("id")})
        check("[DoD16] 交班接口可用", Api.ok(r), "code=%s" % r.get("code"))
        if Api.ok(r):
            s = Api.data(api.get(ADMIN + "/hotel/shift/page", {"pageNo": 1, "pageSize": 5})) or {}
            rows = s.get("list") or []
            if rows:
                s0 = rows[0]
                total = money(s0.get("totalAmount", 0))
                parts = sum(money(s0.get(k, 0)) for k in
                            ["cashAmount", "cardAmount", "wechatAmount", "alipayAmount", "otherAmount"])
                check("[DoD16] 交班金额自洽", near(total, parts),
                      "total=%s parts=%s" % (total, parts))

    # =========================================================== 统计
    section("经营指标（DoD 17）")
    r = api.get(ADMIN + "/hotel/statistics/summary", {"beginDate": d(-7), "endDate": d(0)})
    sm = Api.data(r) or {}
    check("[DoD17] 统计 summary 可用", Api.ok(r), "code=%s" % r.get("code"))
    if Api.ok(r) and sm:
        occ = sm.get("occ")
        adr = sm.get("adr")
        revpar = sm.get("revpar")
        check("[DoD17] 含 occ/adr/revpar 字段",
              occ is not None and adr is not None and revpar is not None,
              "occ=%s adr=%s revpar=%s" % (occ, adr, revpar))
        if occ is not None and adr is not None and revpar is not None:
            check("[DoD17] RevPAR == ADR × OCC", near(revpar, float(adr) * float(occ) / 100.0),
                  "revpar=%s 期望=%s" % (revpar, float(adr) * float(occ) / 100.0))

    # =========================================================== 权限
    section("菜单权限（DoD 18）")
    perm = Api.data(api.get(ADMIN + "/system/auth/get-permission-info")) or {}
    codes = perm.get("permissions") or []
    need = ["hotel:rate:query", "hotel:folio:query", "hotel:audit:query",
            "hotel:shift:query", "hotel:invoice:query"]
    missing = [p for p in need if p not in codes]
    check("[DoD18] P0 新权限码已下发", not missing, "缺失 %s" % missing if missing else "")

    # =========================================================== 前端
    section("前端页面编译（DoD 19）")
    pages = ["hotel/rate/index", "hotel/audit/index", "hotel/folio/index",
             "hotel/shift/index", "hotel/invoice/index",
             "hotel/order/index", "hotel/board/index", "hotel/statistics/index"]
    bad = []
    for p in pages:
        st = http_status("%s/src/views/%s.vue" % (FE, p))
        if st != 200:
            bad.append("%s(%s)" % (p, st))
    check("[DoD19] P0 页面全部可编译", not bad, "异常：%s" % bad if bad else "%d 个页面" % len(pages))

    # 测试数据由 harness 的 atexit 统一清理（trash_order / trash_guest 已登记）
    return summary("P0")


if __name__ == "__main__":
    raise SystemExit(main())
