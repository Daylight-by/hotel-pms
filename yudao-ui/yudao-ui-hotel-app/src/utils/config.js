/**
 * C 端运行配置
 * APP_MODE:
 *   auto —— 优先请求真实后端，接口不可用时自动降级到本地 mock（默认，便于后端未就绪时演示）
 *   api  —— 只走真实接口，失败即报错（联调必选，避免把接口问题藏起来）
 *   mock —— 只走本地 mock（答辩彩排/断网兜底）
 */
export const APP_MODE = 'auto'

let baseUrl = '/app-api'
// #ifdef MP-WEIXIN
// 小程序没有 devServer 代理，直连后端；微信开发者工具需勾选「不校验合法域名」。
// 真机预览时改成电脑局域网 IP，例如 http://192.168.1.10:48080/app-api
baseUrl = 'http://localhost:48080/app-api'
// #endif

export const BASE_URL = baseUrl
export const TENANT_ID = '1'
export const REQUEST_TIMEOUT = 5000

/** 会员登录后 token 的本地缓存键 */
export const TOKEN_KEY = 'hotel_app_token'
export const MEMBER_KEY = 'hotel_app_member'
export const ORDER_CACHE_KEY = 'hotel_app_orders'
export const SELECTED_ROOM_KEY = 'hotel_app_selected_room'
