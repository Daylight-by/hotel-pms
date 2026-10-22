# -*- coding: utf-8 -*-
"""T4 演示数据 clean/regenerate 幂等闭环验证（针对 48082）"""
import json
import urllib.error
import urllib.parse
import urllib.request

BASE = "http://localhost:48082/admin-api"
TOKEN = ""

def req(method, path, params=None, body=None, timeout=180):
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
            return json.loads(resp.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        return {"code": e.code, "msg": "http err"}
    except Exception as e:
        return {"code": -1, "msg": str(e)}

r = req("POST", "/system/auth/login", body={"username": "admin", "password": "admin123", "tenant-name": "来财酒店"})
TOKEN = (r.get("data") or {}).get("accessToken")
print("login", r.get("code"))

# 1) clean
r = req("POST", "/hotel/demo/clean")
print("clean ->", json.dumps(r.get("data") or r, ensure_ascii=False)[:200])
r = req("GET", "/hotel/demo/status")
print("status after clean ->", json.dumps(r.get("data"), ensure_ascii=False)[:200])

# 2) regenerate（幂等：先自动清残留再生成）
r = req("POST", "/hotel/demo/generate", {"months": 3})
print("regenerate ->", json.dumps(r.get("data") or r, ensure_ascii=False)[:220])

# 3) clean again
r = req("POST", "/hotel/demo/clean")
print("clean2 ->", json.dumps(r.get("data") or r, ensure_ascii=False)[:200])
r = req("GET", "/hotel/demo/status")
print("final status ->", json.dumps(r.get("data"), ensure_ascii=False)[:200])
print("IDEMPOTENCY LOOP DONE")
