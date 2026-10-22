/** 会员与自助服务接口（P1 §2.4，/app-api/hotel/member/**） */
import { request } from '@/utils/request'

export function loginByMobile(mobile, code) {
  return request({ url: '/hotel/member/login', method: 'POST', data: { mobile, code } })
}

export function fetchMemberInfo() {
  return request({ url: '/hotel/member/info', method: 'GET' })
}

export function applyInvoice(payload) {
  return request({ url: '/hotel/member/invoice-apply', method: 'POST', data: payload })
}

export function selfCheckIn(orderId) {
  return request({ url: '/hotel/member/self-check-in', method: 'PUT', data: { orderId } })
}

export function selfCheckOut(orderId) {
  return request({ url: '/hotel/member/self-check-out', method: 'PUT', data: { orderId } })
}
