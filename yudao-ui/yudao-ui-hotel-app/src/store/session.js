import { reactive } from 'vue'
import { MEMBER_KEY, TOKEN_KEY } from '@/utils/config'
import { setToken } from '@/utils/request'

export const session = reactive({
  token: '',
  member: null
})

export function loadSession() {
  try {
    session.token = uni.getStorageSync(TOKEN_KEY) || ''
    session.member = uni.getStorageSync(MEMBER_KEY) || null
  } catch (e) {
    session.token = ''
    session.member = null
  }
  return session
}

export function saveSession(token, member) {
  session.token = token || ''
  session.member = member || null
  setToken(token)
  try {
    if (member) uni.setStorageSync(MEMBER_KEY, member)
  } catch (e) {}
}

export function clearSession() {
  session.token = ''
  session.member = null
  setToken('')
  try {
    uni.removeStorageSync(MEMBER_KEY)
  } catch (e) {}
}

export function isLoggedIn() {
  return !!session.token
}

/** 未登录时跳登录页，登录后回跳 redirect（页面路径，可带 query） */
export function requireLogin(redirect) {
  if (isLoggedIn()) return true
  const target = redirect ? encodeURIComponent(redirect) : ''
  uni.navigateTo({ url: '/pages/login/index' + (target ? '?redirect=' + target : '') })
  return false
}
