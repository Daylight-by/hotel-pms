# 来财酒店 PMS · 管理后台

基于 [yudao-ui-admin-vue3](https://github.com/yudaocode/yudao-ui-admin-vue3)（MIT）改造。
芋道自带的 AI / BPM / CRM / ERP / MES / 商城 / OA 等模块的页面与接口封装已移除，
只保留 `system`（权限与租户）、`infra`（基础设施）与自研 `hotel` 业务。

## 技术栈

Vue 3.5 · Vite · TypeScript · Element Plus · ECharts · UnoCSS · Pinia

## 跑起来

```bash
pnpm install
pnpm dev            # 默认 5173，接口指向 http://localhost:48080
```

本地环境变量在 `.env.local`（不入库）。默认账号 `admin / admin123`，租户「来财酒店」。

> 首次 `pnpm install` 若报 `ERR_PNPM_IGNORED_BUILDS`，需在 `pnpm-workspace.yaml` 的
> `allowBuilds` 里放行 `esbuild` / `@parcel/watcher`，否则 Vite 启动会找不到 esbuild 可执行文件。

## 目录

```
src/api/hotel/      后端接口封装（按模块拆分）
src/views/hotel/    业务页面
  board/            房态看板
  roomType/ room/   房型、房间
  guest/            住客
  order/            订单
  folio/            账务账页
  cleaning/         清洁任务
  pricing/ ratePlan/  收益定价
  report/ statistics/ 经营报表
  ar/               协议单位应收
  invoice/ psb/ lock/ ota/  发票、公安上传、门锁、OTA
src/utils/hotel.ts  枚举常量与金额格式化
```

## 业务规则

订单状态机、房态流转、清洁任务联动等规则见仓库根目录的
[README-hotel.md](../../README-hotel.md)。
