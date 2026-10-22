# yudao-ui-hotel-app · C 端预订站

uni-app + Vue3，一套代码编译 **H5** 与 **微信小程序**。属于 T3 包，仅本目录内文件由 T3 维护。

## 启动

```powershell
# H5 开发（端口 5183，代理 /app-api -> localhost:48080）
$env:NODE_OPTIONS=""
npm install
npm run dev:h5

# 小程序（产物 dist/build/mp-weixin）
npm run build:mp-weixin
```

## 目录

```
src/
  api/            # booking / member 接口封装（结构归一化在这里）
  components/     # h-cover 封面、h-empty 空态
  mock/           # 本地演示数据 + 接口同构 mock（后端未就绪时自动启用）
  pages/
    index/        # 首页 / 房型列表（日期、人数、房量）
    room/detail   # 房型详情（轮播、设施、逐日价、房价计划）
    order/fill    # 填写订单
    order/pay     # 支付（含模拟支付成功、结果轮询）
    order/list    # 我的订单（筛选、取消、发票、自助入住/退房）
    order/detail  # 订单详情（费用、进度、发票、自助）
    login/        # 手机号 + 验证码
  store/session.js# 会员态
  utils/          # request（token/租户头/自动降级）、format、config
```

## 与后端联调

- `src/utils/config.js`：`APP_MODE = 'auto' | 'api' | 'mock'`。
- `auto`：优先真实接口，网络失败 / 5xx / 404 自动切 mock，页面右上角显示「演示数据」。
- `api`：只走真实接口，联调时用这个，避免接口问题被 mock 掩盖。
- 小程序直连地址在 config.js 的 `MP-WEIXIN` 条件编译段里，真机预览改成局域网 IP。

接口契约见 `docs/任务书/P1-面向客户.md` §2.2 / §2.4，字段差异只在 `src/api/booking.js` 的 `normalizeRoom` 里适配。
