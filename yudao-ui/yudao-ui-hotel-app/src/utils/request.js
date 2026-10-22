/**
 * 统一请求层
 * - 自动携带会员 token（Authorization: Bearer）与租户头（tenant-id）
 * - 统一解包 { code, msg, data }，非 0 抛业务异常
 * - APP_MODE=auto 时，接口不可用（网络失败 / 5xx / 404）自动降级到本地 mock
 */
import { APP_MODE, BASE_URL, TENANT_ID, REQUEST_TIMEOUT, TOKEN_KEY } from './config'
import { mockRequest } from '@/mock/index'

let mockActive = APP_MODE === 'mock'
const listeners = []

export function isMockMode() {
  return mockActive
}

export function onMockModeChange(fn) {
  listeners.push(fn)
  return () => {
    const i = listeners.indexOf(fn)
    if (i >= 0) listeners.splice(i, 1)
  }
}

function enterMock(reason) {
  if (mockActive) return
  mockActive = true
  console.warn('[hotel-app] 接口不可用，已切换本地演示数据：', reason)
  listeners.forEach((fn) => {
    try {
      fn(true)
    } catch (e) {}
  })
}

export function getToken() {
  try {
    return uni.getStorageSync(TOKEN_KEY) || ''
  } catch (e) {
    return ''
  }
}

export function setToken(token) {
  try {
    if (token) uni.setStorageSync(TOKEN_KEY, token)
    else uni.removeStorageSync(TOKEN_KEY)
  } catch (e) {}
}

function buildQuery(data) {
  if (!data) return ''
  const parts = Object.keys(data)
    .filter((k) => data[k] !== undefined && data[k] !== null && data[k] !== '')
    .map((k) => `${encodeURIComponent(k)}=${encodeURIComponent(data[k])}`)
  return parts.length ? '?' + parts.join('&') : ''
}

function requestReal({ url, method, data }) {
  return new Promise((resolve, reject) => {
    const isGet = method === 'GET'
    const fullUrl = BASE_URL + url + (isGet ? buildQuery(data) : '')
    const header = {
      'tenant-id': TENANT_ID,
      'Content-Type': 'application/json'
    }
    const token = getToken()
    if (token) header.Authorization = 'Bearer ' + token

    uni.request({
      url: fullUrl,
      method,
      data: isGet ? undefined : data || {},
      header,
      timeout: REQUEST_TIMEOUT,
      success: (res) => {
        if (res.statusCode === 404 || res.statusCode >= 500) {
          const err = new Error('服务不可用（HTTP ' + res.statusCode + '）')
          err.unavailable = true
          reject(err)
          return
        }
        const body = res.data || {}
        if (body.code === 0) {
          resolve(body.data)
          return
        }
        const err = new Error(body.msg || '请求失败')
        err.code = body.code
        err.unavailable = body.code === 404
        reject(err)
      },
      fail: (err) => {
        const e = new Error(err.errMsg || '网络异常')
        e.unavailable = true
        reject(e)
      }
    })
  })
}

export async function request(options) {
  const opts = { method: 'GET', ...options }

  if (!mockActive) {
    try {
      return await requestReal(opts)
    } catch (err) {
      if (APP_MODE === 'auto' && err.unavailable) {
        enterMock(err.message)
      } else {
        throw err
      }
    }
  }
  if (APP_MODE === 'api' && !mockActive) {
    throw new Error('接口不可用')
  }
  const res = await mockRequest(opts)
  if (res.code !== 0) {
    const err = new Error(res.msg || '请求失败')
    err.code = res.code
    throw err
  }
  return res.data
}

/** 仅用于联调：强制重试真实接口 */
export function resetMockMode() {
  mockActive = APP_MODE === 'mock'
  listeners.forEach((fn) => {
    try {
      fn(mockActive)
    } catch (e) {}
  })
}

export function toast(title, icon = 'none') {
  uni.showToast({ title, icon, duration: 2200 })
}
