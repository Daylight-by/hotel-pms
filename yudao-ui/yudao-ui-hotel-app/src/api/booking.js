/**
 * 预订引擎接口（P1 §2.2，/app-api/hotel/booking/**）
 * 统一做一次结构归一化：后端字段与本地 mock 字段不一致时，只改这里。
 */
import { request } from '@/utils/request'
import { addDays, fmtDate, nightsBetween } from '@/utils/format'

function normalizePlan(plan, basePrice, breakfast) {
  return {
    id: plan.id || plan.ratePlanId,
    code: plan.code,
    name: plan.name || plan.planName || '标准价',
    desc: plan.desc || plan.description || '',
    price: Number(plan.price != null ? plan.price : basePrice || 0),
    breakfast: plan.breakfast || breakfast || '',
    cancelPolicy: plan.cancelPolicy != null ? plan.cancelPolicy : 10,
    cancelPolicyText:
      plan.cancelPolicyText ||
      (plan.cancelPolicy === 30 ? '不可取消' : '入住前可免费取消')
  }
}

export function normalizeRoom(item) {
  const basePrice = Number(item.price || item.basePrice || item.minPrice || 0)
  const room = {
    roomTypeId: item.roomTypeId || item.id,
    roomTypeName: item.roomTypeName || item.name || '客房',
    roomTypeCode: item.roomTypeCode || item.code || '',
    coverImage: item.coverImage || item.cover_image || '',
    images: item.images ? String(item.images).split(',').filter(Boolean) : item.imageList || [],
    bedType: item.bedType || item.bed_type || '大床',
    breakfast: item.breakfast || '无早',
    window: item.window || '有窗',
    area: item.area,
    maxOccupancy: item.maxOccupancy || 2,
    amenities: Array.isArray(item.amenities)
      ? item.amenities
      : String(item.amenities || '')
          .split(',')
          .filter(Boolean),
    intro: item.intro || item.description || '',
    price: basePrice,
    totalPrice: Number(item.totalPrice || 0),
    availableCount:
      item.availableCount != null
        ? Number(item.availableCount)
        : item.remainingCount != null
          ? Number(item.remainingCount)
          : 0,
    ratePlans: [],
    dailyPrices: []
  }

  if (Array.isArray(item.ratePlans) && item.ratePlans.length) {
    room.ratePlans = item.ratePlans.map((p, idx) =>
      normalizePlan(
        { ...p, id: p.id || p.ratePlanId || `plan-${room.roomTypeId}-${idx}` },
        basePrice,
        room.breakfast
      )
    )
  } else {
    room.ratePlans = [normalizePlan({ id: item.ratePlanId || null, price: basePrice }, basePrice, room.breakfast)]
  }
  room.price = Math.min(...room.ratePlans.map((p) => p.price))

  if (Array.isArray(item.dailyPrices) && item.dailyPrices.length) {
    room.dailyPrices = item.dailyPrices.map((d) => ({
      date: fmtDate(d.date || d.day),
      price: Number(d.price || 0),
      weekend: !!d.weekend
    }))
  }
  return room
}

/** 可售房型查询；返回 { list, checkInDate, checkOutDate, nights } */
export async function fetchAvailability(params) {
  const data = await request({
    url: '/hotel/booking/availability',
    method: 'GET',
    data: {
      checkInDate: params.checkInDate,
      checkOutDate: params.checkOutDate,
      adultCount: params.adultCount || 2,
      childCount: params.childCount || 0,
      roomCount: params.roomCount || 1
    }
  })
  const list = Array.isArray(data) ? data : (data && (data.list || data.records || data.items)) || []
  const nights = nightsBetween(params.checkInDate, params.checkOutDate) || 1
  const rooms = list.map(normalizeRoom)
  rooms.forEach((room) => {
    if (!room.dailyPrices.length) {
      const dates = []
      for (let i = 0; i < nights; i++) dates.push(fmtDate(addDays(params.checkInDate, i)))
      room.dailyPrices = dates.map((date) => ({ date, price: room.price, weekend: false }))
    }
    if (!room.totalPrice) {
      room.totalPrice = room.dailyPrices.reduce((sum, d) => sum + d.price, 0)
    }
  })
  return {
    list: rooms,
    checkInDate: params.checkInDate,
    checkOutDate: params.checkOutDate,
    nights
  }
}

/** 创建占位，返回 { holdNo, expireTime } */
export function createHold(payload) {
  return request({ url: '/hotel/booking/hold', method: 'POST', data: payload })
}

/** 提交订单，返回 { orderId, orderNo, payableAmount } */
export function submitBooking(payload) {
  return request({ url: '/hotel/booking/submit', method: 'POST', data: payload })
}

/** 发起支付，返回 { payOrderNo, payParams } */
export function createPayOrder(payload) {
  return request({ url: '/hotel/booking/pay', method: 'POST', data: payload })
}

/** 支付结果轮询 */
export function fetchPayResult(payOrderNo) {
  return request({ url: '/hotel/booking/pay-result', method: 'GET', data: { payOrderNo } })
}

/** 演示用：模拟支付成功（mock-enabled 时后端开放） */
export function mockPaySuccess(payOrderNo) {
  return request({ url: '/hotel/pay/mock-success', method: 'POST', data: { payOrderNo } })
}

/** 我的订单分页 */
export async function fetchMyOrders(params = {}) {
  const data = await request({
    url: '/hotel/booking/my-order/page',
    method: 'GET',
    data: { pageNo: params.pageNo || 1, pageSize: params.pageSize || 20, status: params.status || undefined }
  })
  const list = Array.isArray(data) ? data : (data && (data.list || data.records)) || []
  return { list, total: (data && data.total) || list.length }
}

export function fetchOrderDetail(id) {
  return request({ url: '/hotel/booking/my-order/get', method: 'GET', data: { id } })
}

export function cancelOrder(id, reason) {
  return request({ url: '/hotel/booking/my-order/cancel', method: 'PUT', data: { id, reason } })
}
