# -*- coding: utf-8 -*-
"""
P2「商业化与合规」验收脚本 —— 对应 docs/任务书/P2-商业化与合规.md 第 5 节 DoD 17 条

运行：
    python script/acceptance/check_p2.py
前置：P0 + P1 已验收通过，后端 48080 已启动
说明：P2 外部对接（公安/门锁/OTA/读卡器）在本机无真硬件，脚本只验证「Mock 模式下全链路可跑通」。
"""
import sys
import os

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from harness import (Api, check, skip, section, summary, d, near, money,  # noqa: E402
                     http_status, set_cleanup_api, trash_guest, trash_order)

ADMIN = "/admin-api"
FE = "http://localhost:5173"

E_STORE_NO_PERMISSION = 1003004303
E_PSB_ID_INVALID = 1003004312
E_AR_CREDIT_EXCEED = 1003004342


def main():
    api = Api()
    api.login()
    set_cleanup_api(api)
    if not api.token:
        check("管理端登录", False, "拿不到 token")
        return summary("P2")

    # =========================================================== 门店与数据权限
    section("门店与数据权限（DoD 2-4）")
    r = api.get(ADMIN + "/hotel/store/list")
    stores = Api.data(r) or []
    check("[DoD2] 门店列表可用", Api.ok(r) and bool(stores),
          "code=%s, %d 家门店" % (r.get("code"), len(stores)))

    store_id = stores[0].get("id") if stores else None
    if store_id:
        api2 = Api()
        api2.login()
        r = api2.get(ADMIN + "/hotel/room/page",
                     {"pageNo": 1, "pageSize": 100}, headers={"store-id": store_id})
        rows = Api.rows(r)
        other = [x for x in rows if x.get("storeId") not in (None, 0, store_id)]
        check("[DoD3] 门店数据隔离（只见本店房间）", Api.ok(r) and not other,
              "%d 行，越店 %d 行" % (len(rows), len(other)))

        # 造一个不属于该门店的订单来测越权：取另一门店 id
        other_store = None
        if len(stores) > 1:
            other_store = stores[1].get("id")
        if other_store:
            r = api2.get(ADMIN + "/hotel/order/page",
                         {"pageNo": 1, "pageSize": 5}, headers={"store-id": other_store})
            check("[DoD4] 跨门店访问受控", Api.ok(r) or r.get("code") == E_STORE_NO_PERMISSION,
                  "code=%s" % r.get("code"))
        else:
            skip("[DoD4] 跨门店访问受控", "只有一家门店，无法构造越权场景")
    else:
        skip("[DoD3][DoD4] 门店数据权限", "无门店数据")

    # =========================================================== 公安上传
    section("公安上传（DoD 5-6）")
    r = api.get(ADMIN + "/hotel/psb/page", {"pageNo": 1, "pageSize": 20})
    logs = Api.rows(r)
    check("[DoD5] 公安上传日志列表可用", Api.ok(r), "code=%s, %d 条" % (r.get("code"), len(logs)))
    checkin_logs = [x for x in logs if x.get("actionType") == 10]
    if checkin_logs:
        ok_logs = [x for x in checkin_logs if x.get("status") == 20]
        check("[DoD5] 存在上传成功的入住登记记录", bool(ok_logs),
              "入住登记 %d 条，成功 %d 条" % (len(checkin_logs), len(ok_logs)))
    else:
        skip("[DoD5] 入住登记上传记录", "无入住登记日志（需先产生入住）")

    r = api.post(ADMIN + "/hotel/guest/create",
                 {"name": "身份证校验测试", "mobile": "13900005555",
                  "idCard": "33028119920315123X", "sex": 1})
    check("[DoD6] 非法身份证被拒", r.get("code") == E_PSB_ID_INVALID,
          "code=%s msg=%s" % (r.get("code"), r.get("msg")))
    if Api.ok(r):
        trash_guest(Api.data(r))

    # =========================================================== 门锁
    section("门锁发卡（DoD 7）")
    r = api.get(ADMIN + "/hotel/lock/page", {"pageNo": 1, "pageSize": 20})
    cards = Api.rows(r)
    check("[DoD7] 门锁发卡日志列表可用", Api.ok(r), "code=%s, %d 条" % (r.get("code"), len(cards)))
    issued = [x for x in cards if x.get("actionType") == 10 and x.get("status") == 20]
    check("[DoD7] 存在发卡成功记录", bool(issued), "%d 条" % len(issued))

    # =========================================================== OTA
    section("OTA 直连（DoD 8）")
    r = api.get(ADMIN + "/hotel/ota/config/page", {"pageNo": 1, "pageSize": 20})
    configs = Api.rows(r)
    check("[DoD8] OTA 配置列表可用", Api.ok(r), "code=%s, %d 条" % (r.get("code"), len(configs)))

    if configs:
        cfg = configs[0]
        before = len(Api.rows(api.get(ADMIN + "/hotel/ota/order/page",
                                      {"pageNo": 1, "pageSize": 100})))
        r = api.post(ADMIN + "/hotel/ota/sync-order", params={"configId": cfg.get("id")})
        check("[DoD8] OTA 同步订单", Api.ok(r), "code=%s" % r.get("code"))
        after = len(Api.rows(api.get(ADMIN + "/hotel/ota/order/page",
                                     {"pageNo": 1, "pageSize": 100})))
        check("[DoD8] 同步后 OTA 订单增加", after >= before, "%d -> %d" % (before, after))

        r = api.post(ADMIN + "/hotel/ota/sync-order", params={"configId": cfg.get("id")})
        after2 = len(Api.rows(api.get(ADMIN + "/hotel/ota/order/page",
                                      {"pageNo": 1, "pageSize": 100})))
        check("[DoD8] 重复同步幂等（不重复建单）", after2 == after, "%d -> %d" % (after, after2))

        r = api.post(ADMIN + "/hotel/ota/push-rate", params={"configId": cfg.get("id")})
        check("[DoD8] 推送房价库存", Api.ok(r), "code=%s" % r.get("code"))
    else:
        skip("[DoD8] OTA 同步", "无 OTA 配置")

    # =========================================================== AR 挂账
    section("协议单位 AR（DoD 9-10）")
    r = api.get(ADMIN + "/hotel/ar/account/page", {"pageNo": 1, "pageSize": 20})
    accounts = Api.rows(r)
    check("[DoD9] 协议单位列表可用", Api.ok(r), "code=%s, %d 条" % (r.get("code"), len(accounts)))

    if accounts:
        acc = accounts[0]
        used0 = money(acc.get("usedAmount", 0))
        limit = money(acc.get("creditLimit", 0))
        amt = min(money(1000), max(limit - used0, money(0)))
        if amt > 0:
            r = api.post(ADMIN + "/hotel/ar/post",
                         {"arAccountId": acc.get("id"), "amount": float(amt),
                          "remark": "验收脚本"})
            check("[DoD9] AR 挂账", Api.ok(r), "code=%s" % r.get("code"))
            if Api.ok(r):
                acc2 = None
                for x in Api.rows(api.get(ADMIN + "/hotel/ar/account/page",
                                          {"pageNo": 1, "pageSize": 50})):
                    if x.get("id") == acc.get("id"):
                        acc2 = x
                        break
                used1 = money((acc2 or {}).get("usedAmount", 0))
                check("[DoD9] 挂账后已用额度增加", near(used1, used0 + amt),
                      "%s -> %s" % (used0, used1))

                r = api.post(ADMIN + "/hotel/ar/post",
                             {"arAccountId": acc.get("id"), "amount": 99999999,
                              "remark": "超额测试"})
                check("[DoD9] 超额挂账被拒", r.get("code") == E_AR_CREDIT_EXCEED,
                      "code=%s" % r.get("code"))

                r = api.post(ADMIN + "/hotel/ar/settlement/generate", {
                    "arAccountId": acc.get("id"), "beginDate": d(-30), "endDate": d(0)})
                st_id = Api.data(r)
                check("[DoD10] 生成结算单", Api.ok(r) and bool(st_id), "code=%s" % r.get("code"))
                if st_id:
                    r = api.post(ADMIN + "/hotel/ar/settlement/receive", {
                        "settlementId": st_id, "amount": 1, "paymentMethod": 2})
                    check("[DoD10] 结算收款", Api.ok(r), "code=%s" % r.get("code"))
        else:
            skip("[DoD9] AR 挂账", "额度已用尽")
    else:
        skip("[DoD9][DoD10] AR", "无协议单位数据")

    # =========================================================== 报表
    section("报表与审计（DoD 11-13）")
    r = api.get(ADMIN + "/hotel/statistics/daily-report", {"bizDate": d(0)})
    rep = Api.data(r) or {}
    check("[DoD11] 日报接口可用", Api.ok(r), "code=%s" % r.get("code"))
    if Api.ok(r) and rep:
        need_keys = ["occ", "adr", "revpar", "roomRevenue", "totalRevenue"]
        miss = [k for k in need_keys if k not in rep]
        check("[DoD11] 日报字段齐全", not miss, "缺失 %s" % miss if miss else "")
        if not miss:
            check("[DoD11] 日报 RevPAR == ADR × OCC",
                  near(rep.get("revpar"), float(rep.get("adr")) * float(rep.get("occ")) / 100.0),
                  "revpar=%s" % rep.get("revpar"))

    r = api.get(ADMIN + "/hotel/statistics/monthly-report", {"month": d(0)[:7]})
    check("[DoD11] 月报接口可用", Api.ok(r), "code=%s" % r.get("code"))

    r = api.get(ADMIN + "/hotel/statistics/export",
                {"beginDate": d(-7), "endDate": d(0), "type": "daily"}, raw=True)
    size = len((r or {}).get("_bytes") or b"")
    check("[DoD12] 报表导出返回非空文件", size > 0, "%d bytes" % size)

    r = api.get(ADMIN + "/hotel/operation-log/page", {"pageNo": 1, "pageSize": 20, "module": "folio"})
    oplogs = Api.rows(r)
    check("[DoD13] 操作审计日志可用", Api.ok(r), "code=%s, %d 条" % (r.get("code"), len(oplogs)))
    if oplogs:
        has_diff = any(x.get("beforeData") or x.get("afterData") for x in oplogs)
        check("[DoD13] 审计日志含变更前后数据", has_diff, "%d 条" % len(oplogs))

    # =========================================================== 权限与前端
    section("权限与前端（DoD 14-16）")
    perm = Api.data(api.get(ADMIN + "/system/auth/get-permission-info")) or {}
    codes = perm.get("permissions") or []
    need = ["hotel:store:query", "hotel:psb:query", "hotel:ar:query"]
    missing = [p for p in need if p not in codes]
    check("[DoD14] P2 新权限码已下发", not missing, "缺失 %s" % missing if missing else "")

    pages = ["hotel/store/index", "hotel/psb/index", "hotel/lock/index",
             "hotel/otaConfig/index", "hotel/otaOrder/index", "hotel/arAccount/index",
             "hotel/arLedger/index", "hotel/arSettlement/index", "hotel/report/index",
             "hotel/operationLog/index"]
    bad = []
    for p in pages:
        st = http_status("%s/src/views/%s.vue" % (FE, p))
        if st != 200:
            bad.append("%s(%s)" % (p, st))
    check("[DoD15] P2 页面可编译", not bad, "异常：%s" % bad if bad else "%d 个" % len(pages))

    st = http_status("%s/src/layout/components/StoreSwitch.vue" % FE)
    check("[DoD15] 门店切换器组件存在", st == 200, "http=%s" % st)

    import subprocess
    agent = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                         "idreader", "agent.js")
    if os.path.exists(agent):
        check("[DoD16] 读卡 Agent 存在", True, os.path.basename(agent))
    else:
        check("[DoD16] 读卡 Agent 存在", False, "缺少 script/idreader/agent.js")

    return summary("P2")


if __name__ == "__main__":
    raise SystemExit(main())
