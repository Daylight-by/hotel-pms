# 三期验收脚本

零第三方依赖（只用 Python 标准库），直接可跑。派发者用它对其他 agent 的交付做最终验收。

## 运行

```bash
cd D:\AAA\hotel-management-yudao

# 只跑基线回归（P0 之前已打通的能力，任何一期都必须不回归）
python script/acceptance/run_all.py --phase baseline

# 验收某一期（会自动先跑 baseline，再跑 p0…直到该期）
python script/acceptance/run_all.py --phase p0
python script/acceptance/run_all.py --phase p1
python script/acceptance/run_all.py --phase p2
python script/acceptance/run_all.py --phase all      # 全部跑一遍

# 也可以单独跑
python script/acceptance/check_p0.py
```

推荐用本机托管 Python：
`C:\Users\27346\.workbuddy\binaries\python\versions\3.13.12\python.exe`

## 前置条件

1. 后端已启动在 `48080`（`java -jar yudao-server.jar --server.port=48080`）
2. 前端已启动在 `5173`（P1/P2 还要 `5183` 的 H5）
3. 数据库已导入 `sql/mysql/hotel.sql`
4. 账号 `admin / admin123`，租户 `1`

## 文件

| 文件 | 作用 |
|---|---|
| `harness.py` | 公共库：HTTP 客户端（登录/token/租户头）、PASS/FAIL 断言与汇总、测试数据 atexit 清理 |
| `check_baseline.py` | 基线回归：5 个列表接口 + 房态看板 + 统计 + 端到端链路（下单→确认→入住→退房→清洁→验收）+ 重叠下单拦截 |
| `check_p0.py` | P0 二十条 DoD：房价日历、账务账页、夜审营业日、续住换房钟点房、交班、ADR/RevPAR |
| `check_p1.py` | P1 十七条 DoD：渠道、房价计划算价、预订引擎占位防超售、在线支付幂等、会员、自助入住退房、电子发票 |
| `check_p2.py` | P2 十七条 DoD：门店数据隔离、公安上传、门锁、OTA 幂等同步、AR 挂账结算、日报导出、操作审计 |
| `run_all.py` | 串联入口，按依赖顺序跑，任一 FAIL 则退出码 1 |

## 设计约定

- **脚本可重复运行**：每次会自建测试住客/订单，结束后 `atexit` 统一清理；选房时逐个尝试下单以避开上次残留，不依赖"干净库"。
- **接口未实现不会崩溃**：返回 404/500 只记 FAIL，不抛异常。因此脚本在 P0 未开发时也能跑，输出一份"未完成清单"。
- **退出码**：全绿 0，有 FAIL 为 1。可接 CI。
- **黑盒验收**：脚本只验接口行为；SQL 结构与代码规范由派发者按任务书第 2.4 节人工复核。

## 新增检查项

在对应 `check_xx.py` 里用 `check("名称", 布尔, "详情")` 追加即可，编号建议沿用 `[DoDn]` 前缀，与任务书条目一一对应。
