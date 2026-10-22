/**
 * 本地 mock 服务：按 P1 §2.2 契约返回同构数据。
 * 数据保存在 uni storage，刷新页面不丢单，答辩可完整走 下单→支付→订单状态变化。
 */
import { HOTEL, ROOM_TYPES, MOCK_AVAILABILITY, ratePlansOf, dailyPrices } from './data'
import { addDays, fmtDate, nightsBetween } from '@/utils/format'
import { MEMBER_KEY, ORDER_CACHE_KEY } from '@/utils/config'

const HOLD_KEY = 'hotel_app_holds'
const PAY_KEY = 'hotel_app_pays'
const SEQ_KEY = 'hotel_app_seq'

const wait = (ms) => new Promise((resolve) => setTimeout(resolve, ms))

function read(key, fallback) {
  try {
    const v = uni.getStorageSync(key)
    return v === '' || v === undefined || v === null ? fallback : v
  } catch (e) {
    return fallback
  }
}

function write(key, value) {
  try {
    uni.setStorageSync(key, value)
  } catch (e) {}
}

function nextSeq() {
  const n = Number(read(SEQ_KEY, 1000)) + 1
  write(SEQ_KEY, n)
  return n
}

function orderNo() {
  const d = new Date()
  const stamp = `${d.getFullYear()}${String(d.getMonth() + 1).padStart(2, '0')}${String(
    d.getDate()
  ).padStart(2, '0')}`
  return `QY${stamp}${nextSeq()}`
}

function payNo() {
  return `PAY${Date.now()}${Math.floor(Math.random() * 90 + 10)}`
}

function orderList() {
  return read(ORDER_CACHE_KEY, [])
}

function saveOrders(list) {
  write(ORDER_CACHE_KEY, list)
}

function findOrder(id) {
  return orderList().find((o) => String(o.id) === String(id))
}

function upsertOrder(order) {
  const list = orderList()
  const idx = list.findIndex((o) => String(o.id) === String(order.id))
  if (idx >= 0) list[idx] = order
  else list.unshift(order)
  saveOrders(list)
}

function findRoom(id) {
  return ROOM_TYPES.find((r) => String(r.id) === String(id)) || ROOM_TYPES[1]
}

function buildAvailability(query) {
  const checkIn = query.checkInDate || fmtDate(new Date())
  const checkOut = query.checkOutDate || fmtDate(addDays(checkIn, 1))
  const nights = nightsBetween(checkIn, checkOut) || 1
  return {
    checkInDate: checkIn,
    checkOutDate: checkOut,
    nights,
    list: ROOM_TYPES.map((room) => {
      const daily = dailyPrices(room, checkIn, checkOut)
      const total = daily.reduce((sum, d) => sum + d.price, 0)
      const plans = ratePlansOf(room).map((p) => ({
        ...p,
        totalPrice: Math.round(p.price * nights * 100) / 100
      }))
      return {
        roomTypeId: room.id,
        roomTypeName: room.name,
        roomTypeCode: room.code,
        coverImage: '',
        images: [],
        bedType: room.bedType,
        breakfast: room.breakfast,
        window: room.window,
        area: room.area,
        maxOccupancy: room.maxOccupancy,
        amenities: room.amenities,
        intro: room.intro,
        price: plans[0].price,
        totalPrice: total,
        availableCount: MOCK_AVAILABILITY[room.id] || 0,
        ratePlans: plans,
        dailyPrices: daily
      }
    })
  }
}

export async function mockRequest({ url, method = 'GET', data = {} }) {
  await wait(180 + Math.random() * 160)
  const [rawPath, rawQuery = ''] = String(url).split('?')
  const path = rawPath.replace('/app-api', '')
  const query = { ...parseQuery(rawQuery), ...(data || {}) }
  const body = data || {}

  // ---------------- 会员 ----------------
  if (path === '/hotel/member/login' && method === 'POST') {
    const mobile = body.mobile || body.phone || ''
    if (!/^1\d{10}$/.test(mobile)) {
      return fail(1003004240, '手机号格式不正确')
    }
    if (String(body.code) !== '123456') {
      return fail(1003004241, '验证码不正确，演示环境请输入 123456')
    }
    const member = {
      id: 90001,
      memberNo: 'M' + mobile.slice(-4),
      nickname: '栖屿会员',
      mobile,
      points: 1280,
      balance: 0,
      level: '银卡会员'
    }
    write(MEMBER_KEY, member)
    return ok({ accessToken: 'mock-token-' + mobile, member })
  }

  if (path === '/hotel/member/info') {
    return ok(read(MEMBER_KEY, null))
  }

  // ---------------- 预订引擎 ----------------
  if (path === '/hotel/booking/availability') {
    return ok(buildAvailability(query))
  }

  if (path === '/hotel/booking/hold' && method === 'POST') {
    const room = findRoom(body.roomTypeId)
    const count = Number(body.roomCount || 1)
    const stock = MOCK_AVAILABILITY[room.id] || 0
    if (stock < count) {
      return fail(1003004221, '所选日期房量不足，请调整入住日期或房型')
    }
    const holdNo = 'HOLD' + Date.now()
    const holds = read(HOLD_KEY, [])
    holds.unshift({
      holdNo,
      roomTypeId: room.id,
      ratePlanId: body.ratePlanId,
      checkInDate: body.checkInDate,
      checkOutDate: body.checkOutDate,
      roomCount: count,
      guestName: body.guestName || '',
      guestMobile: body.guestMobile || '',
      expireTime: Date.now() + 15 * 60 * 1000,
      status: 10
    })
    write(HOLD_KEY, holds.slice(0, 30))
    return ok({ holdNo, expireTime: Date.now() + 15 * 60 * 1000 })
  }

  if (path === '/hotel/booking/submit' && method === 'POST') {
    const holds = read(HOLD_KEY, [])
    const hold = holds.find((h) => h.holdNo === body.holdNo)
    if (!hold) return fail(1003004222, '占位已失效，请重新选择房型')
    if (hold.expireTime < Date.now()) return fail(1003004222, '占位已过期，请重新选择房型')

    const room = findRoom(hold.roomTypeId)
    const plan = ratePlansOf(room).find((p) => String(p.id) === String(hold.ratePlanId)) ||
      ratePlansOf(room)[0]
    const checkIn = hold.checkInDate
    const checkOut = hold.checkOutDate
    const nights = nightsBetween(checkIn, checkOut) || 1
    const daily = dailyPrices(room, checkIn, checkOut)
    const amount = Math.round(plan.price * nights * hold.roomCount * 100) / 100

    const order = {
      id: Number('9' + String(Date.now()).slice(-9)),
      orderNo: orderNo(),
      status: 10,
      paymentStatus: 10,
      roomTypeId: room.id,
      roomTypeName: room.name,
      coverImage: '',
      bedType: room.bedType,
      breakfast: plan.breakfast,
      window: room.window,
      ratePlanId: plan.id,
      ratePlanName: plan.name,
      cancelPolicy: plan.cancelPolicy,
      checkInDate: checkIn,
      checkOutDate: checkOut,
      nights,
      roomCount: hold.roomCount,
      guestName: body.guestName || hold.guestName || '栖屿客人',
      guestMobile: body.guestMobile || hold.guestMobile || '',
      arrivalTime: body.arrivalTime || '18:00',
      remark: body.remark || '',
      invoiceNeeded: !!body.invoiceNeeded,
      invoiceStatus: body.invoiceNeeded ? 10 : 0,
      dailyPrices: daily,
      roomAmount: amount,
      payableAmount: amount,
      paidAmount: 0,
      payMethod: null,
      createTime: Date.now(),
      payTime: null,
      actualCheckInTime: null,
      actualCheckOutTime: null
    }
    hold.status = 20
    write(HOLD_KEY, holds)
    upsertOrder(order)
    return ok({ orderId: order.id, orderNo: order.orderNo, payableAmount: amount, expireTime: Date.now() + 30 * 60 * 1000 })
  }

  if (path === '/hotel/booking/pay' && method === 'POST') {
    const order = findOrder(body.orderId)
    if (!order) return fail(1003004231, '订单不存在')
    if (order.paymentStatus === 30) return fail(1003004232, '订单已支付')
    const payOrderNo = payNo()
    const pays = read(PAY_KEY, [])
    pays.unshift({
      payOrderNo,
      orderId: order.id,
      amount: order.payableAmount,
      channel: body.payMethod || body.channel || 'wxpay',
      status: 10,
      createTime: Date.now()
    })
    write(PAY_KEY, pays.slice(0, 50))
    return ok({
      payOrderNo,
      amount: order.payableAmount,
      mock: true,
      payParams: JSON.stringify({ appId: 'mock', timeStamp: String(Date.now()), nonceStr: 'mock', package: 'prepay_id=mock', signType: 'RSA', paySign: 'mock' })
    })
  }

  if (path === '/hotel/booking/pay-result') {
    const pays = read(PAY_KEY, [])
    const pay = pays.find((p) => p.payOrderNo === query.payOrderNo)
    if (!pay) return fail(1003004233, '支付单不存在')
    const order = findOrder(pay.orderId)
    return ok({
      payOrderNo: pay.payOrderNo,
      status: pay.status,
      paidTime: pay.payTime || null,
      amount: pay.amount,
      orderStatus: order ? order.status : null
    })
  }

  if (path === '/hotel/pay/mock-success' && method === 'POST') {
    const pays = read(PAY_KEY, [])
    const pay = pays.find((p) => p.payOrderNo === query.payOrderNo)
    if (!pay) return fail(1003004233, '支付单不存在')
    if (pay.status === 20) return ok(true)
    pay.status = 20
    pay.payTime = Date.now()
    write(PAY_KEY, pays)

    const order = findOrder(pay.orderId)
    if (order) {
      order.status = 20
      order.paymentStatus = 30
      order.paidAmount = order.payableAmount
      order.payMethod = pay.channel
      order.payTime = Date.now()
      upsertOrder(order)
      const member = read(MEMBER_KEY, null)
      if (member) {
        member.points = Number(member.points || 0) + Math.round(order.payableAmount)
        write(MEMBER_KEY, member)
      }
    }
    return ok(true)
  }

  // ---------------- 我的订单 ----------------
  if (path === '/hotel/booking/my-order/page') {
    const pageNo = Number(query.pageNo || 1)
    const pageSize = Number(query.pageSize || 20)
    let list = orderList().slice().sort((a, b) => b.createTime - a.createTime)
    if (query.status !== undefined && query.status !== '' && query.status !== null && Number(query.status) !== 0) {
      list = list.filter((o) => Number(o.status) === Number(query.status))
    }
    const start = (pageNo - 1) * pageSize
    return ok({ list: list.slice(start, start + pageSize), total: list.length })
  }

  if (path === '/hotel/booking/my-order/get') {
    const order = findOrder(query.id)
    return order ? ok(order) : fail(1003004231, '订单不存在')
  }

  if (path === '/hotel/booking/my-order/cancel' && method === 'PUT') {
    const order = findOrder(query.id)
    if (!order) return fail(1003004231, '订单不存在')
    if (order.status === 30) return fail(1003004234, '已入住订单不可取消，请联系前台')
    if (order.status === 50) return ok(true)
    order.status = 50
    order.cancelReason = query.reason || '客人取消'
    order.cancelTime = Date.now()
    order.paymentStatus = order.paymentStatus === 30 ? 40 : order.paymentStatus
    upsertOrder(order)
    return ok(true)
  }

  // ---------------- 发票与自助服务 ----------------
  if (path === '/hotel/member/invoice-apply' && method === 'POST') {
    const order = findOrder(body.orderId)
    if (!order) return fail(1003004231, '订单不存在')
    order.invoiceStatus = 20
    order.invoiceTitle = body.title
    order.invoiceTaxNo = body.taxNo || ''
    order.invoiceEmail = body.email || ''
    upsertOrder(order)
    return ok(order.id)
  }

  if (path === '/hotel/member/self-check-in' && method === 'PUT') {
    const order = findOrder(query.orderId)
    if (!order) return fail(1003004231, '订单不存在')
    if (order.status !== 20) return fail(1003004235, '仅已确认订单可自助入住')
    if (order.paymentStatus !== 30) return fail(1003004236, '订单未支付，无法自助入住')
    order.status = 30
    order.actualCheckInTime = Date.now()
    upsertOrder(order)
    return ok(true)
  }

  if (path === '/hotel/member/self-check-out' && method === 'PUT') {
    const order = findOrder(query.orderId)
    if (!order) return fail(1003004231, '订单不存在')
    if (order.status !== 30) return fail(1003004237, '仅已入住订单可自助退房')
    order.status = 40
    order.actualCheckOutTime = Date.now()
    upsertOrder(order)
    return ok(true)
  }

  // ---------------- 基础信息 ----------------
  if (path === '/hotel/booking/hotel-info') {
    return ok(HOTEL)
  }

  return fail(404, `mock 未实现：${method} ${path}`)
}

function parseQuery(str) {
  const out = {}
  if (!str) return out
  str.split('&').forEach((pair) => {
    if (!pair) return
    const [k, v = ''] = pair.split('=')
    out[decodeURIComponent(k)] = decodeURIComponent(v)
  })
  return out
}

function ok(data) {
  return { code: 0, msg: 'success', data }
}

function fail(code, msg) {
  return { code, msg, data: null }
}

export function resetMockData() {
  write(ORDER_CACHE_KEY, [])
  write(HOLD_KEY, [])
  write(PAY_KEY, [])
}
