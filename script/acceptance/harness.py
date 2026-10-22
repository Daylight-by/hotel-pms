# -*- coding: utf-8 -*-
"""
酒店 PMS 验收脚本通用工具（零第三方依赖，仅用 Python 标准库）

用法：
    from harness import Api, check, summary, section

    api = Api()                      # 默认 http://localhost:48080，租户 1
    api.login()                      # admin / admin123
    r = api.get("/hotel/room/page")  # 返回 dict（CommonResult 的 JSON）
    check("房间列表可用", r.get("code") == 0, str(r.get("code")))
    raise SystemExit(summary("P0"))
"""
import atexit
import json
import sys
import urllib.error
import urllib.parse
import urllib.request

BASE = "http://localhost:48080"
TENANT_ID = 1
ADMIN_API = "/admin-api"
APP_API = "/app-api"

_RESULTS = []
_VERBOSE = "--verbose" in sys.argv or "-v" in sys.argv


# ---------------------------------------------------------------- 结果收集
def check(name, ok, detail=""):
    """记录一条验收项。ok 为布尔值。"""
    ok = bool(ok)
    _RESULTS.append((name, ok, detail))
    print("[%s] %s%s" % ("PASS" if ok else "FAIL", name, ("  -> %s" % detail) if detail else ""))
    return ok


def skip(name, reason):
    print("[SKIP] %s  -> %s" % (name, reason))


def section(title):
    print("\n--- %s ---" % title)


def summary(phase):
    """打印汇总并返回进程退出码（有 FAIL 则为 1）。"""
    total = len(_RESULTS)
    failed = [r for r in _RESULTS if not r[1]]
    print("\n" + "=" * 60)
    print("%s 验收结果：%d 项，通过 %d，失败 %d" % (phase, total, total - len(failed), len(failed)))
    if failed:
        print("失败项：")
        for name, _, detail in failed:
            print("  - %s %s" % (name, ("(%s)" % detail) if detail else ""))
    print("=" * 60)
    return 1 if failed else 0


# ---------------------------------------------------------------- HTTP 客户端
class Api(object):
    def __init__(self, base=BASE, tenant_id=TENANT_ID, timeout=20):
        self.base = base.rstrip("/")
        self.tenant_id = tenant_id
        self.timeout = timeout
        self.token = None
        self.member_token = None

    # ---------- 底层 ----------
    def _request(self, method, path, params=None, body=None, headers=None, raw=False):
        url = self.base + path
        if params:
            url += "?" + urllib.parse.urlencode(
                {k: v for k, v in params.items() if v is not None}, doseq=True)
        data = None
        if body is not None:
            data = json.dumps(body, ensure_ascii=False, default=str).encode("utf-8")
        req = urllib.request.Request(url, data=data, method=method)
        req.add_header("Content-Type", "application/json")
        req.add_header("Accept", "application/json")
        req.add_header("tenant-id", str(self.tenant_id))
        tok = self.token
        if "/app-api/" in path:
            # app-api 走会员身份（userType=2），admin token（userType=1）会被网关判为「错误的用户类型」403。
            # 因此在没有会员 token 时**不要**回落到 admin token，让 @PermitAll 的公开接口匿名访问。
            tok = self.member_token
        if tok:
            req.add_header("Authorization", "Bearer " + tok)
        if headers:
            for k, v in headers.items():
                if v is not None:
                    req.add_header(k, str(v))
        try:
            with urllib.request.urlopen(req, timeout=self.timeout) as resp:
                content = resp.read()
                if raw:
                    return {"_status": resp.status, "_bytes": content,
                            "_headers": dict(resp.headers)}
                if not content:
                    return {"code": resp.status}
                return json.loads(content.decode("utf-8"))
        except urllib.error.HTTPError as e:
            content = e.read()
            try:
                return json.loads(content.decode("utf-8"))
            except Exception:
                return {"code": e.code, "msg": (content[:200].decode("utf-8", "ignore")
                                                if content else str(e))}
        except Exception as e:  # 连接失败、超时等
            return {"code": -1, "msg": "%s: %s" % (type(e).__name__, e)}

    def get(self, path, params=None, **kw):
        return self._request("GET", path, params=params, **kw)

    def post(self, path, body=None, params=None, **kw):
        return self._request("POST", path, params=params, body=body if body is not None else {}, **kw)

    def put(self, path, body=None, params=None, **kw):
        return self._request("PUT", path, params=params, body=body if body is not None else {}, **kw)

    def delete(self, path, params=None, **kw):
        return self._request("DELETE", path, params=params, **kw)

    # ---------- 登录 ----------
    def login(self, username="admin", password="admin123"):
        r = self.post(ADMIN_API + "/system/auth/login",
                      {"username": username, "password": password})
        data = (r or {}).get("data") or {}
        self.token = data.get("accessToken") or data.get("token")
        if not self.token:
            print("!! 管理端登录失败：%s" % r)
        return self.token

    def login_member(self, mobile, code="123456"):
        r = self.post(APP_API + "/hotel/member/login", {"mobile": mobile, "code": code})
        data = (r or {}).get("data") or {}
        self.member_token = data.get("accessToken") or data.get("token")
        return self.member_token

    # ---------- 常用断言辅助 ----------
    @staticmethod
    def ok(r):
        return isinstance(r, dict) and r.get("code") == 0

    @staticmethod
    def data(r):
        if not isinstance(r, dict):
            return None
        return r.get("data")

    @staticmethod
    def rows(r):
        """分页返回取 list；非分页返回 data 本身（如果是 list）。"""
        d = Api.data(r)
        if isinstance(d, dict):
            return d.get("list") or d.get("records") or []
        if isinstance(d, list):
            return d
        return []


# ---------------------------------------------------------------- 小工具
def today():
    import datetime
    return datetime.date.today()


def d(offset=0):
    import datetime
    return (datetime.date.today() + datetime.timedelta(days=offset)).isoformat()


def money(v):
    from decimal import Decimal
    return Decimal(str(v if v is not None else 0))


def near(a, b, tol=0.02):
    try:
        return abs(float(a) - float(b)) <= tol
    except Exception:
        return False


def http_status(url, timeout=25):
    """前端 Vite 页面编译校验：返回 HTTP 状态码。"""
    try:
        req = urllib.request.Request(url)
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            return resp.status
    except urllib.error.HTTPError as e:
        return e.code
    except Exception:
        return -1


# ---------------------------------------------------------------- 测试数据清理
# 验收脚本会中途 return（某接口未实现时），若不清理，下次运行会被脏数据干扰。
# 这里用 atexit 兜底：无论正常结束还是提前 return，都会尝试删除测试住客与订单。
_TRASH = {"api": None, "guests": [], "orders": []}


def set_cleanup_api(api):
    _TRASH["api"] = api


def trash_guest(gid):
    if gid:
        _TRASH["guests"].append(gid)


def trash_order(oid):
    if oid:
        _TRASH["orders"].append(oid)


def _do_cleanup():
    api = _TRASH["api"]
    if not api:
        return
    try:
        for oid in set(_TRASH["orders"]):
            # 业务规则：只有已取消的订单可删除，所以先取消再删
            api.post(ADMIN_API + "/hotel/order/cancel", params={"id": oid})
            api.delete(ADMIN_API + "/hotel/order/delete", {"id": oid})
        for gid in set(_TRASH["guests"]):
            api.delete(ADMIN_API + "/hotel/guest/delete", {"id": gid})
        if _TRASH["orders"] or _TRASH["guests"]:
            print("\n[cleanup] 已清理测试订单 %d 笔、测试住客 %d 位"
                  % (len(set(_TRASH["orders"])), len(set(_TRASH["guests"]))))
    except Exception as e:
        print("\n[cleanup] 清理失败（可忽略）：%s" % e)


atexit.register(_do_cleanup)
