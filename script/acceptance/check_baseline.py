# -*- coding: utf-8 -*-
"""
基线回归检查（P0/P1/P2 每期验收前都必须全绿）

保护「P0 之前已打通」的能力不被后续开发破坏：
  1. 登录与权限
  2. 房型 / 房间 / 住客 / 订单 / 清洁任务 五个列表接口
  3. 房态看板
  4. 经营统计
  5. 端到端链路：下单 → 确认 → 入住 → 退房 → 生成清洁任务 → 清洁完成
  6. 日期重叠下单被拦截（错误码 1003004002）

运行：
    python script/acceptance/check_baseline.py
"""
import sys
import os

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from harness import (Api, check, skip, section, summary, d,  # noqa: E402
                     set_cleanup_api, trash_guest, trash_order)

ADMIN = "/admin-api"
OCCUPIED_CONFLICT_CODE = 1003004002  # 所选房间在该日期已被占用


def main():
    api = Api()
    api.login()
    set_cleanup_api(api)  # 注册 atexit 清理，保证中途 return 也不留脏数据
    if not api.token:
        check("管理端登录", False, "拿不到 token，后端未启动？")
        return summary("基线回归")

    # ---------------------------------------------------------- 1 权限
    section("登录与权限")
    perm = api.get(ADMIN + "/system/auth/get-permission-info")
    perms = Api.data(perm) or {}
    codes = perms.get("permissions") if isinstance(perms, dict) else None
    if codes is None:
        codes = []
    check("获取权限信息", Api.ok(perm), "权限码 %d 个" % len(codes))
    need = ["hotel:room:query", "hotel:order:query", "hotel:guest:query",
            "hotel:cleaning:query", "hotel:room-type:query", "hotel:statistics:query"]
    missing = [p for p in need if p not in codes]
    check("酒店核心权限码齐全", not missing, "缺失 %s" % missing if missing else "")

    # ---------------------------------------------------------- 2 列表接口
    section("基础列表接口")
    for name, path in [("房型列表", "/hotel/room-type/page"),
                       ("房间列表", "/hotel/room/page"),
                       ("住客列表", "/hotel/guest/page"),
                       ("订单列表", "/hotel/order/page"),
                       ("清洁任务列表", "/hotel/cleaning-task/page")]:
        r = api.get(ADMIN + path, {"pageNo": 1, "pageSize": 10})
        rows = Api.rows(r)
        check(name, Api.ok(r), "code=%s, %d 行" % (r.get("code"), len(rows)))

    board = api.get(ADMIN + "/hotel/room/board")
    bd = Api.data(board)
    board_n = len(bd) if isinstance(bd, (list, dict)) else 0
    check("房态看板", Api.ok(board) and board_n > 0, "%s 条房态数据" % board_n)

    ov = api.get(ADMIN + "/hotel/statistics/overview")
    check("经营统计概览", Api.ok(ov), str(ov.get("code")))

    # ---------------------------------------------------------- 3 准备数据
    section("端到端链路")
    rt = Api.rows(api.get(ADMIN + "/hotel/room-type/page", {"pageNo": 1, "pageSize": 50}))
    if not rt:
        check("存在房型数据", False, "无房型，请先导入 sql/mysql/hotel.sql 演示数据")
        return summary("基线回归")
    room_type_id = rt[0].get("id")

    # 创建专属住客，避免污染
    g = api.post(ADMIN + "/hotel/guest/create", {
        "name": "验收脚本测试客", "mobile": "13900009999",
        "idCard": "330281199203151234", "sex": 1, "remark": "acceptance-script"})
    guest_id = Api.data(g)
    check("创建测试住客", Api.ok(g) and bool(guest_id), "guestId=%s" % guest_id)
    trash_guest(guest_id)
    if not guest_id:
        return summary("基线回归")

    # 找一个空闲房间。注意：房态空闲 ≠ 该日期无订单，因此逐个尝试下单，
    # 避开上一次跑脚本残留的订单（脚本可重复运行，不能依赖干净库）。
    room_list = Api.rows(api.get(ADMIN + "/hotel/room/page", {"pageNo": 1, "pageSize": 100}))
    free = [r for r in room_list if r.get("status") == 10]
    if not free:
        check("存在空闲房间", False, "没有空闲房，请先重置演示数据")
        return summary("基线回归")

    order_id, room_id = None, None
    order_body = None
    for room in free:
        rid = room.get("id")
        ob = {"guestId": guest_id, "roomId": rid,
              "checkInDate": d(1), "checkOutDate": d(3)}
        o = api.post(ADMIN + "/hotel/order/create", ob)
        oid = Api.data(o)
        if oid:
            order_id, room_id, order_body = oid, rid, ob
            break
    check("创建订单", order_id is not None, "orderId=%s roomId=%s" % (order_id, room_id))
    trash_order(order_id)
    if not order_id:
        return summary("基线回归")

    # 日期重叠应被拦截
    dup = api.post(ADMIN + "/hotel/order/create", dict(order_body, remark="重叠测试"))
    check("日期重叠下单被拦截", dup.get("code") == OCCUPIED_CONFLICT_CODE,
          "code=%s" % dup.get("code"))

    # ---------------------------------------------------------- 5 确认 → 入住
    r = api.post(ADMIN + "/hotel/order/confirm", params={"id": order_id})
    check("确认订单", Api.ok(r), str(r.get("code")))

    r = api.post(ADMIN + "/hotel/order/check-in", params={"id": order_id})
    check("办理入住", Api.ok(r), str(r.get("code")))

    after_in = Api.data(api.get(ADMIN + "/hotel/room/get", {"id": room_id})) or {}
    check("入住后房态=已入住(20)", after_in.get("status") == 20,
          "status=%s" % after_in.get("status"))

    # ---------------------------------------------------------- 6 退房 → 清洁
    r = api.post(ADMIN + "/hotel/order/check-out", params={"id": order_id})
    check("办理退房", Api.ok(r), str(r.get("code")))

    after_out = Api.data(api.get(ADMIN + "/hotel/room/get", {"id": room_id})) or {}
    check("退房后房态=待清洁(30)", after_out.get("status") == 30,
          "status=%s" % after_out.get("status"))

    open_tasks = Api.data(api.get(ADMIN + "/hotel/cleaning-task/list-open")) or []
    task = None
    for t in open_tasks:
        if t.get("roomId") == room_id:
            task = t
            break
    check("退房自动生成清洁任务", task is not None,
          "%d 个待处理任务" % len(open_tasks))

    if task:
        # 状态流转为「已完成」时房态不变；只有「已验收(40)」才把房态置回空闲(10)
        r = api.put(ADMIN + "/hotel/cleaning-task/update-status",
                    {"status": 30, "remark": "验收脚本"}, params={"id": task.get("id")})
        check("清洁任务标记已完成", Api.ok(r), str(r.get("code")))

        r = api.put(ADMIN + "/hotel/cleaning-task/update-status",
                    {"status": 40, "remark": "验收脚本"}, params={"id": task.get("id")})
        check("清洁任务标记已验收", Api.ok(r), str(r.get("code")))

        after_clean = Api.data(api.get(ADMIN + "/hotel/room/get", {"id": room_id})) or {}
        check("清洁验收后房态=空闲(10)", after_clean.get("status") == 10,
              "status=%s" % after_clean.get("status"))
    else:
        skip("清洁任务状态流转", "未找到任务")

    # ---------------------------------------------------------- 7 清理与业务约束
    section("清理测试数据")
    # 业务规则：只有已取消的订单可删除（1_003_004_007）。历史单不可删。
    r = api.delete(ADMIN + "/hotel/order/delete", {"id": order_id})
    check("已退房订单不可删除（业务约束）", r.get("code") == 1003004007,
          "code=%s" % r.get("code"))

    o2 = api.post(ADMIN + "/hotel/order/create",
                  {"guestId": guest_id, "roomId": room_id,
                   "checkInDate": d(6), "checkOutDate": d(8)})
    o2_id = Api.data(o2)
    if o2_id:
        trash_order(o2_id)
        api.post(ADMIN + "/hotel/order/cancel", params={"id": o2_id})
        r = api.delete(ADMIN + "/hotel/order/delete", {"id": o2_id})
        check("已取消订单可删除", Api.ok(r), "code=%s" % r.get("code"))
    else:
        skip("已取消订单可删除", "创建第二笔订单失败")

    r = api.delete(ADMIN + "/hotel/guest/delete", {"id": guest_id})
    check("删除测试住客", Api.ok(r), str(r.get("code")))

    return summary("基线回归")


if __name__ == "__main__":
    raise SystemExit(main())
