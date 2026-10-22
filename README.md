# 来财酒店 PMS

面向中小酒店的轻量级 PMS（Property Management System）—— 大创赛参赛作品。

覆盖房态管理、预订与入住、账务结算（Folio）、夜审与营业日推进、收益定价、经营报表，
并支持多门店数据隔离、协议单位 AR 挂账，以及公安上传 / 门锁 / OTA 渠道对接（后三者为适配层，可用模拟数据演示）。

在 [芋道 yudao-boot-mini](https://github.com/yudaocode/yudao-boot-mini)（`master-jdk17`，MIT）基础上增量开发，
业务代码全部在新模块 `yudao-module-hotel` 内，未改动芋道 `system` / `infra` 的原有代码。

## 技术栈

| 层 | 技术 |
| --- | --- |
| 后端 | Spring Boot 3.5 · Java 21 · MyBatis-Plus · Sa-Token · Redisson · Quartz |
| 管理后台 | Vue 3.5 · Vite · Element Plus · TypeScript · ECharts |
| C 端 | uni-app（可编译为 H5 与微信小程序） |
| 数据 | MySQL 8 / MariaDB 10.6 · Redis |

## 规模

- 后端业务代码 316 个 Java 文件，27 个功能模块
- 数据库 33 张业务表（`hotel_*`），全部继承 `TenantBaseDO` 走多租户隔离
- 管理后台 29 个业务页面模块

## 快速开始

完整的环境要求、建库、启动步骤见 **[环境与运行说明.md](环境与运行说明.md)**，
业务规则与接口细节见 **[README-hotel.md](README-hotel.md)**。

```bash
# 1. 建库（MySQL / MariaDB）—— 必须按顺序导入，见 sql/mysql/README.md
for f in ruoyi-vue-pro.sql quartz.sql hotel.sql hotel-t1.sql hotel-t2.sql hotel-t3.sql hotel-t4.sql hotel-t5.sql; do
  mysql -uroot -p ruoyi-vue-pro < "sql/mysql/$f"
done

# 2. 后端（端口 48080，接口文档 /doc.html）
mvn -pl yudao-server -am -DskipTests package
java -jar yudao-server/target/yudao-server.jar --spring.profiles.active=local --server.port=48080

# 3. 管理后台（端口 5173，登录 admin / admin123）
cd yudao-ui/yudao-ui-admin-vue3 && pnpm install && pnpm dev
```

Windows 下也可以直接双击 **`一键启动.bat`**。

## 目录

```
yudao-module-hotel/     自研业务模块（controller / service / dal / enums）
yudao-module-system/    芋道：用户、权限、租户
yudao-module-infra/     芋道：基础设施（文件、配置、定时任务）
yudao-server/           启动入口与配置
yudao-ui/yudao-ui-admin-vue3/   管理后台（Vue3）
yudao-ui/yudao-ui-hotel-app/    C 端（uni-app）
sql/mysql/              建表脚本与演示数据
script/acceptance/      接口级验收脚本（零第三方依赖）
docs/                   参赛方案、任务书、演示剧本
```

## 文档

- [环境与运行说明.md](环境与运行说明.md) —— 环境、建库、启动、常见问题
- [README-hotel.md](README-hotel.md) —— 业务规则、表结构、权限码
- [docs/参赛方案.md](docs/参赛方案.md) —— 选题背景与方案
- [docs/演示剧本.md](docs/演示剧本.md) —— 答辩演示路径

## 致谢

后端脚手架来自 [芋道 ruoyi-vue-pro](https://github.com/YunaiV/ruoyi-vue-pro) / [yudao-boot-mini](https://github.com/yudaocode/yudao-boot-mini)（MIT），
管理后台模板来自 [yudao-ui-admin-vue3](https://github.com/yudaocode/yudao-ui-admin-vue3)。
本仓库仅保留 `system`（权限与租户）、`infra`（基础设施）与自研 `hotel` 三个模块，
芋道自带的 AI / BPM / CRM / ERP / MES / 商城 / OA 等无关模块的前端源码与数据库菜单均已移除。
