# T3 · C 端 uni-app（H5 + 微信小程序）

> ## ⚠️ 动手前先读这一段
>
> **你不是一个人在开发。** 另外 3–4 个 agent 正在**同一台电脑、同一个 Git 仓库、同一个数据库**上同时干活。
>
> 开工前必须先读 [`并行协作须知.md`](../并行协作须知.md)，7 条铁律必须遵守：
> **禁止 git 写操作**（checkout/reset/stash/clean 会毁掉别人几小时的代码）·
> **禁止改公共文件**（尤其 `sql/mysql/hotel.sql`、`ErrorCodeConstants`、`pom.xml`、`application*.yaml`）·
> **禁止 `mvn clean`**（会删 target，别人的服务直接崩）·
> **禁止全局格式化**（会改写别人的文件）·
> **只碰自己包分配的目录**·
> **SQL 只写自己的 `hotel-tN.sql`**·
> **禁止 TRUNCATE / 清库**。
>
> 违反任意一条，视为不合格，整包返工。
>
> 端口：后端用你包内分配到的（T1=48081 / T2=48082 / T4=48083 / T5=48084），
> **48080 和 5173 是派发者验收用，不要占**；前端 5173 只有一个实例，不要另起 Vite。


> 项目背景：酒店 PMS，大创赛参赛作品。**这是客人看到的部分，演示时直接投屏/扫码，做得好不好最直观。**

## 一、必读

1. `docs/任务书/00-总纲与派发说明.md` —— 环境、启动命令、坑位、规范。**必读 §2 §4。**
2. `docs/任务书/P1-面向客户.md` §3.2 —— C 端页面规格
3. `docs/参赛方案.md` —— 项目定位与演示设计

## 二、技术栈（已定稿，不要改）

**uni-app + Vue3**，目录 `yudao-ui/yudao-ui-hotel-app/`

- 一套代码同时编译 **H5** 和 **微信小程序**
- H5 开发端口 `5183`，代理到 `/app-api`
- 小程序编译产物 `dist/build/mp-weixin`（微信开发者工具可导入）

**为什么不用 Vant H5**：本项目要「扫码演示小程序」这个加分项。先用 Vant 写 H5 再补小程序 = 两套前端。

**上线说明（了解即可）**：个人主体小程序过不了「住宿服务」类目审核，**本项目只出体验版用于答辩演示，不上线**。所以不需要真实商户号、不需要备案。

## 三、你要做的页面（P1 §3.2 有详细要点）

| 页面 | 要点 |
|---|---|
| 首页 / 房型列表 | 日期区间选择、房型卡片（封面图、床型、早餐、含税价、「仅剩 x 间」） |
| 房型详情 | 轮播图、设施、逐日价格明细、房价计划选择（含早/不可取消） |
| 填写订单 | 入住人、手机、预计到店、备注、金额明细 |
| 支付页 | 微信/支付宝选择、mock 模式下「模拟支付成功」按钮、结果轮询 |
| 我的订单 | 列表 + 详情 + 取消 + 申请发票 + 自助入住/退房 |
| 登录 | 手机号 + 验证码 |

## 四、接口契约（后端由 T1/T2 并行开发，你按契约先写）

- `GET /app-api/hotel/booking/availability` —— 可售房型（含 `price` / `availableCount`）
- `POST /app-api/hotel/booking/hold` —— 创建占位，返回 `holdNo`
- `POST /app-api/hotel/booking/submit` —— 提交，返回 `orderId`
- `POST /app-api/hotel/booking/pay` —— 返回 `payOrderNo` + `payParams`
- `GET /app-api/hotel/booking/pay-result?payOrderNo=`
- `POST /app-api/hotel/pay/mock-success` —— **演示用，必须有入口**
- `POST /app-api/hotel/member/login` / `GET /member/info`
- `PUT /app-api/hotel/member/self-check-in?orderId=` / `self-check-out`
- `GET /app-api/hotel/booking/my-order/page` / `get?id=`
- `PUT /app-api/hotel/booking/my-order/cancel?id=`

**契约细节见 P1 §2.2 / §2.4。** 若后端还没好，先用本地 mock 数据把页面跑通，接口好了再对接。

## 五、SQL 归属

`hotel_room_type` 加字段：**归你**（C 端列表要用）：
`cover_image`、`images`、`bed_type`、`breakfast`、`window`
写进 **`sql/mysql/hotel-t3.sql`**（新建，不许改 `hotel.sql`）

## 六、号段

- 菜单 ID：无（C 端不进后台菜单）
- 错误码：无
- 前端目录：`yudao-ui/yudao-ui-hotel-app/`（整个目录都是你的）

## 七、禁止碰

- `sql/mysql/hotel.sql`
- 后台 `yudao-ui-admin-vue3/src/` 下任何文件（那是 T1/T2/T4/T5 的）
- 后端 Java 代码（你只做前端）

## 八、自验收

```bash
python script/acceptance/check_p1.py   # DoD 16 归你
```
另外自己验证：
- `npm run dev:h5`（或 pnpm）能起，curl `http://localhost:5183` 返回 200
- `npm run build:mp-weixin` 能产出 `dist/build/mp-weixin`
- 6 个页面都能打开，不白屏
- mock 支付流程能走通（下单 → 支付 → 订单状态变已确认）

## 九、交付清单

- [ ] uni-app 工程可运行，H5 端 5183 可访问
- [ ] 小程序编译产物存在
- [ ] 6 个页面完整
- [ ] 请求层封装（`uni.request`，带会员 token `Authorization: Bearer`、租户头 `tenant-id: 1`；
      **不要用 axios**——小程序端需要额外适配器，`uni.request` 才能一套代码双端跑）
- [ ] `hotel_room_type` 字段写在 `hotel-t3.sql`
- [ ] **写一份 1 页的《扫码演示说明》**（怎么起服务、怎么预览、演示点哪几步）——答辩要用
