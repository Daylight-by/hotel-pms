# 酒店管理系统（基于芋道 yudao-boot-mini）

在 [芋道 yudao-boot-mini](https://github.com/yudaocode/yudao-boot-mini)（`master-jdk17` 分支，MIT）基础上新增的酒店业务模块 `yudao-module-hotel`，
沿用芋道的分层规范、权限体系与前端工程，**不修改芋道原有业务代码**，只做增量。

- 后端：Spring Boot 3.5 + Java 17/21 + MyBatis-Plus + MySQL/MariaDB + Redis
- 前端：Vue3 + TypeScript + Element Plus + ECharts（`yudao-ui/yudao-ui-admin-vue3`）

## 一、功能

| 模块 | 能力 |
|---|---|
| 房态看板 | 按楼层分组的房态网格（空闲/已入住/待清洁/维修中）、出租率、今日预抵、今日待退房，支持一键入住/退房、改房态、开单 |
| 房型管理 | 增删改查、门市价、可住人数、面积、设施、上下架 |
| 房间管理 | 增删改查、按房型/楼层/房态筛选、单独调整房态 |
| 住客管理 | 建档、手机号/姓名检索、一键跳转下单 |
| 订单管理 | 预订、确认、入住、退房、取消、收款、退款；日期冲突校验；状态机约束 |
| 清洁任务 | 退房自动生成任务、任务状态流转（待处理→处理中→已完成→已核实）、指派保洁、完成/核实联动房态 |
| 经营统计 | 今日/本月收入、出租率、预估在住金额、收入与出租率趋势、订单量趋势、支付方式占比、房态分布 |

### 业务规则（已实现并有接口级测试）

- 订单状态机：`10 待确认 → 20 已确认 → 30 已入住 → 40 已退房`；`10/20 → 50 已取消`
- 同一房间在 `[入住日, 离店日)` 区间内不允许重复占用（`ORDER_ROOM_CONFLICT`）
- 办理入住：房间置为「已入住」；办理退房：房间置为「待清洁」**并自动生成退房清洁任务**
- 清洁任务「已核实」：房间自动回到「空闲」
- 房间存在未关闭的清洁任务时，不允许直接改为「空闲」
- 房间存在活跃订单时，不允许删除
- 已支付订单不允许直接取消，必须先走退款；退款后按状态自动取消并释放房间
- 金额由后端按房间实时售价 × 晚数计算，前端仅做预估展示

## 二、目录

```
yudao-module-hotel/                              # 新增后端模块
  src/main/java/cn/iocoder/yudao/module/hotel/
    controller/admin/{roomtype,room,guest,order,cleaning,statistics}/
    service/{roomtype,room,guest,order,cleaning,statistics}/
    dal/dataobject/                              # 5 个 DO，继承 TenantBaseDO
    dal/mysql/                                   # 5 个 Mapper，继承 BaseMapperX
    enums/                                       # 房态/订单/支付/清洁枚举 + 错误码
yudao-ui/yudao-ui-admin-vue3/src/
  api/hotel/                                     # 6 个接口封装
  views/hotel/                                   # 7 个页面 + 6 个表单弹窗
  utils/hotel.ts                                 # 前端枚举常量与金额格式化
sql/mysql/hotel.sql                              # 建表 + 演示数据 + 菜单权限
```

## 三、数据库

`sql/mysql/hotel.sql` 会创建 5 张表并写入演示数据，同时注册菜单（`system_menu` ID 段 20000–20199，不与芋道内置菜单冲突）：

| 表 | 说明 |
|---|---|
| `hotel_room_type` | 房型 |
| `hotel_room` | 房间，`status`：10 空闲 / 20 已入住 / 30 待清洁 / 40 维修中 |
| `hotel_guest` | 住客（与后台 `system_users` 分离，住客不是登录用户） |
| `hotel_order` | 订单，`status`：10/20/30/40/50，`payment_status`：10/20/30/40 |
| `hotel_cleaning_task` | 清洁任务，`type`：10 退房 / 20 日常 / 30 深度，`status`：10/20/30/40 |

演示数据：4 个房型、20 间房（2–5 层）、4 位住客。

导入顺序：

```bash
mysql -uroot -p ruoyi-vue-pro < sql/mysql/ruoyi-vue-pro.sql   # 芋道基础表
mysql -uroot -p ruoyi-vue-pro < sql/mysql/hotel.sql           # 酒店表 + 菜单
```

## 四、启动

### 1. 依赖服务

- MySQL 8 / MariaDB 10.6+（默认库名 `ruoyi-vue-pro`，账号 `root/123456`）
- Redis 6+（默认 `127.0.0.1:6379`，无密码）

配置见 `yudao-server/src/main/resources/application-local.yaml`。

### 2. 后端

```bash
mvn -pl yudao-server -am -DskipTests package
java -jar yudao-server/target/yudao-server.jar --spring.profiles.active=local --server.port=48080
```

> 显式带 `--server.port=48080`，避免环境变量里残留的 `SERVER_PORT` 覆盖配置。

接口文档：<http://localhost:48080/doc.html>

### 3. 前端

```bash
cd yudao-ui/yudao-ui-admin-vue3
pnpm install
pnpm dev --port 5173
```

访问 <http://localhost:5173>，登录 `admin / admin123`，左侧菜单「酒店管理」。

> 首次 `pnpm install` 若出现 `ERR_PNPM_IGNORED_BUILDS`，需确认 `pnpm-workspace.yaml` 中的 `allowBuilds` 已放行
> `esbuild` / `@parcel/watcher`，否则 Vite 启动会报找不到 esbuild 可执行文件。

## 五、权限

菜单权限码均为 `hotel:<模块>:<动作>`，已注册到 `system_menu`，`super_admin` 角色默认可见：

```
hotel:room-type:{query,create,update,delete}
hotel:room:{query,create,update,delete}
hotel:guest:{query,create,update,delete}
hotel:order:{query,create,update,delete}
hotel:cleaning:{query,create,update,delete}
hotel:statistics:query
```

## 六、三期开发路线

已完成的只是**坯子**。完整的酒店 PMS 分三期推进，任务书可直接派发给执行 agent：

| 期 | 目标 | 关键交付 |
|---|---|---|
| **P0 能开门** | 单店内部可用 | 房价日历、账务账页（Folio）、夜审与营业日、续住/换房/钟点房、交班、发票、ADR/RevPAR/OCC |
| **P1 面向客户** | 开放直销 | 房价计划与渠道、预订引擎（H5/小程序）、在线支付、会员、自助入住退房、电子发票 |
| **P2 商业化** | 合规与生态 | 旅馆业治安上传、身份证读卡、门锁、OTA 直连、协议单位 AR、多门店、报表中心 |

- 任务书：[`docs/任务书/`](docs/任务书/)（总纲 + P0/P1/P2 + 派发模板）
- 差距分析：[`docs/功能调研与差距分析.md`](docs/功能调研与差距分析.md)

派发方式：把 `docs/任务书/派发模板.md` 里对应期次的指令连任务书一起给执行 agent；
交付回来后用验收脚本做最终验收：

```bash
python script/acceptance/run_all.py --phase p0   # 依次 p0/p1/p2
python script/acceptance/run_all.py --phase all
```

脚本零第三方依赖，详见 [`script/acceptance/README.md`](script/acceptance/README.md)。

## 七、说明

- 本模块为独立业务模块，未改动芋道 `system` / `infra` 的任何代码；接入方式是在根 `pom.xml` 增加
  `<module>yudao-module-hotel</module>`，并在 `yudao-server/pom.xml` 中引入该依赖。
- 数据隔离沿用芋道多租户机制，所有 DO 继承 `TenantBaseDO`，请求需携带 `tenant-id` 头。
- 芋道自带的 AI / BPM / CRM / ERP / 财务 / HRM / IM / IoT / 商城 / 会员 / MES / 公众号 / OA /
  支付 / 报表 / WMS 等无关模块，前端源码与数据库菜单均已清除，只保留
  `system`（权限与租户）、`infra`（基础设施）与自研 `hotel`。
- Excel 导出、发票管理、会员体系、渠道对接等在 P0–P2 中分批落地，见上表。
