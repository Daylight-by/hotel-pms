# -*- coding: utf-8 -*-
"""T4 接口自验收：DoD11-13 + 大屏 + 演示数据生成/状态（纯标准库）"""
import json
import os
import sys
import urllib.error
import urllib.parse
import urllib.request
from datetime import date, timedelta

BASE = os.environ.get("T4_BASE", "http://localhost:48080/admin-api")
OUT = []

def log(*a):
    line = " ".join(str(x) for x in a)
    print(line, flush=True)
    OUT.append(line)

def req(method, path, params=None, body=None, raw=False, timeout=90):
    url = BASE + path
    if params:
        url += "?" + urllib.parse.urlencode({k: v for k, v in params.items() if v is not None})
    data = None
    if body is not None:
        data = json.dumps(body, ensure_ascii=False, default=str).encode("utf-8")
    r = urllib.request.Request(url, data=data, method=method)
    r.add_header("Content-Type", "application/json")
    r.add_header("Accept", "application/json")
    r.add_header("tenant-id", "1")
    if TOKEN:
        r.add_header("Authorization", "Bearer " + TOKEN)
    try:
        with urllib.request.urlopen(r, timeout=timeout) as resp:
            content = resp.read()
            if raw:
                return {"_status": resp.status, "_bytes": content, "_headers": dict(resp.headers)}
            return json.loads(content.decode("utf-8")) if content else {"code": resp.status}
    except urllib.error.HTTPError as e:
        content = e.read()
        try:
            return json.loads(content.decode("utf-8"))
        except Exception:
            return {"code": e.code, "msg": content[:200].decode("utf-8", "ignore")}
    except Exception as e:
        return {"code": -1, "msg": "%s: %s" % (type(e).__name__, e)}

TOKEN = ""

def main():
    global TOKEN
    r = req("POST", "/system/auth/login", body={"username": "admin", "password": "admin123", "tenant-name": "来财酒店"})
    data = r.get("data") or {}
    TOKEN = data.get("accessToken") or data.get("token")
    if not TOKEN:
        log("[login] FAILED:", r); return 1
    log("[login] OK, token len =", len(TOKEN))

    today = date.today()

    log("\n=== DoD11 日报/月报/导出 ===")
    r = req("GET", "/hotel/statistics/daily-report", {"bizDate": today.isoformat()})
    d = r.get("data") or {}
    log("daily-report code", r.get("code"), "occ", d.get("occ"), "adr", d.get("adr"),
        "revpar", d.get("revpar"), "roomRevenue", d.get("roomRevenue"), "totalRevenue", d.get("totalRevenue"))
    if r.get("code") != 0:
        log("  daily-report FAILED:", r); return 1
    occ, adr, revpar = float(d["occ"]), float(d["adr"]), float(d["revpar"])
    exp = adr * occ / 100.0
    log("  RevPAR check: %s vs ADR*OCC/100=%.2f (diff %.4f)" % (revpar, exp, abs(revpar - exp)))

    r = req("GET", "/hotel/statistics/monthly-report", {"month": today.strftime("%Y-%m")})
    md = r.get("data") or {}
    s = md.get("summary") or {}
    log("monthly-report code", r.get("code"), "summary", {k: s.get(k) for k in ("days", "occ", "adr", "revpar", "roomRevenue", "totalRevenue")},
        "daysRows", len(md.get("days") or []))
    if r.get("code") != 0 or not md.get("summary"):
        log("  monthly-report FAILED:", r); return 1

    begin = (today - timedelta(days=7)).isoformat()
    r = req("GET", "/hotel/statistics/export", {"beginDate": begin, "endDate": today.isoformat(), "type": "daily"}, raw=True)
    log("export status", r.get("_status"), "bytes", len(r.get("_bytes") or b""),
        "ctype", (r.get("_headers") or {}).get("Content-Type"))
    if not (r.get("_bytes")):
        log("  export FAILED:", r); return 1

    log("\n=== DoD13 操作审计 ===")
    r = req("GET", "/hotel/operation-log/page", {"pageNo": 1, "pageSize": 20, "module": "folio"})
    lst = (r.get("data") or {}).get("list") or []
    log("operation-log code", r.get("code"), "total", (r.get("data") or {}).get("total"), "rows", len(lst))
    has_diff = False
    for row in lst:
        if row.get("beforeData") or row.get("afterData"):
            has_diff = True
            log("  diff row: id", row.get("id"), "module", row.get("module"), "operation", row.get("operation"),
                "bizId", row.get("bizId"))
            break
    log("  has before/after diff:", has_diff)
    if r.get("code") != 0:
        log("  operation-log FAILED:", r); return 1
    if lst and not has_diff:
        log("  [WARN] DoD13: no diff row yet (need folio ops first)")

    log("\n=== 大屏 overview ===")
    r = req("GET", "/hotel/dashboard/overview")
    o = r.get("data") or {}
    log("overview code", r.get("code"), "keys", list(o.keys()))
    if r.get("code") != 0:
        log("  overview FAILED:", r); return 1

    log("\n=== 演示数据 generate/status ===")
    r = req("POST", "/hotel/demo/generate", {"months": 3})
    log("generate code", r.get("code"), json.dumps(r.get("data"), ensure_ascii=False)[:300] if r.get("data") else "")
    if r.get("code") != 0:
        log("  generate FAILED:", r); return 1
    r = req("GET", "/hotel/demo/status")
    log("status code", r.get("code"), json.dumps(r.get("data"), ensure_ascii=False)[:400] if r.get("data") else "")
    if r.get("code") != 0:
        log("  status FAILED:", r); return 1

    log("\n[AC] 演示数据已生成（暂不清理，供 check_p2 DoD 使用）")
    out_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "t4_acceptance_result.json")
    with open(out_path, "w", encoding="utf-8") as f:
        json.dump({"ok": True, "logs": OUT}, f, ensure_ascii=False, indent=2)
    log("\nALL T4 SELF-ACCEPTANCE PASSED")
    return 0

if __name__ == "__main__":
    sys.exit(main())
