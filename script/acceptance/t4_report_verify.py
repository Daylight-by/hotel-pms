# -*- coding: utf-8 -*-
"""用演示数据验证日报/月报非空 + RevPAR 恒等 + 操作审计生成"""
import json
import os
import sys
import urllib.request
from datetime import date, timedelta

BASE = "http://localhost:48082/admin-api"

def req(method, path, params=None, body=None, raw=False, timeout=90):
    url = BASE + path
    if params:
        url += "?" + urllib.parse.urlencode({k: v for k, v in params.items() if v is not None})
    data = None
    if body is not None:
        data = json.dumps(body, ensure_ascii=False).encode("utf-8")
    r = urllib.request.Request(url, data=data, method=method)
    r.add_header("Content-Type", "application/json")
    r.add_header("tenant-id", "1")
    r.add_header("Authorization", "Bearer " + TOKEN)
    try:
        with urllib.request.urlopen(r, timeout=timeout) as resp:
            c = resp.read()
            if raw:
                return {"_s": resp.status, "_b": c}
            return json.loads(c.decode("utf-8"))
    except urllib.error.HTTPError as e:
        return {"code": e.code, "msg": "http err"}
    except Exception as e:
        return {"code": -1, "msg": str(e)}

import urllib.parse, urllib.error

TOKEN = ""
r = req("POST", "/system/auth/login", body={"username": "admin", "password": "admin123", "tenant-name": "来财酒店"})
TOKEN = (r.get("data") or {}).get("accessToken")
print("login code", r.get("code"))

y = (date.today() - timedelta(days=1)).isoformat()
r = req("GET", "/hotel/statistics/daily-report", {"bizDate": y})
d = r.get("data") or {}
print("daily", y, "->", json.dumps({k: d.get(k) for k in ("occ", "adr", "revpar", "roomRevenue", "otherRevenue", "totalRevenue", "soldNights", "availableNights", "arrivals", "departures", "arBalance")}, ensure_ascii=False))
if r.get("code") == 0 and d.get("revpar") is not None:
    exp = float(d["adr"]) * float(d["occ"]) / 100.0
    print("RevPAR check:", d["revpar"], "vs", round(exp, 2), "diff", abs(float(d["revpar"]) - exp))

r = req("GET", "/hotel/statistics/monthly-report", {"month": date.today().strftime("%Y-%m")})
md = r.get("data") or {}
s = md.get("summary") or {}
print("monthly summary:", json.dumps({k: s.get(k) for k in ("days", "occ", "adr", "revpar", "roomRevenue", "totalRevenue")}, ensure_ascii=False))
print("monthly days rows:", len(md.get("days") or []))

r = req("GET", "/hotel/operation-log/page", {"pageNo": 1, "pageSize": 20, "module": "order"})
lst = (r.get("data") or {}).get("list") or []
print("operation-log order module rows:", len(lst), "first:", (lst[0] if lst else None))

r = req("GET", "/hotel/dashboard/overview")
o = r.get("data") or {}
print("overview today:", json.dumps(o.get("today"), ensure_ascii=False))
print("overview roomStatus:", json.dumps(o.get("roomStatus"), ensure_ascii=False))
print("overview trend len:", len(o.get("trend") or []), "todo:", json.dumps(o.get("todo"), ensure_ascii=False)[:200])
