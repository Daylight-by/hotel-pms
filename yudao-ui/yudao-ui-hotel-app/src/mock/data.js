/**
 * 本地演示数据（后端 /app-api 未就绪时兜底，字段与 P1 §2.2 契约保持一致）
 * 图片字段留空，由前端封面组件用品牌色块渲染，避免答辩现场断网出现裂图。
 */
import { addDays, fmtDate, isWeekend } from '@/utils/format'

export const HOTEL = {
  name: '栖屿酒店',
  brand: 'QIYU HOTEL',
  city: '杭州 · 西湖',
  address: '杭州市西湖区留白路 18 号',
  phone: '0571-8888 6666',
  intro: '十二间客房，藏在城市与山水的缝隙里。'
}

export const ROOM_TYPES = [
  {
    id: 1,
    name: '单人间',
    code: 'SINGLE',
    basePrice: 268,
    maxOccupancy: 1,
    area: 22,
    bedType: '1.2m 单人床',
    breakfast: '无早',
    window: '有窗',
    amenities: ['WiFi', '空调', '电视', '独立卫浴', '书桌'],
    intro: '紧凑而安静，一张床、一盏灯，适合独行的旅人。',
    grade: 1
  },
  {
    id: 2,
    name: '标准双人间',
    code: 'DOUBLE',
    basePrice: 398,
    maxOccupancy: 2,
    area: 28,
    bedType: '1.5m 大床',
    breakfast: '含双早',
    window: '有窗',
    amenities: ['WiFi', '空调', '电视', '独立卫浴', '双人沙发'],
    intro: '朝南的大床房，晨光落在木质窗台上，是一晚好眠的开始。',
    grade: 2
  },
  {
    id: 3,
    name: '豪华双床房',
    code: 'TWIN',
    basePrice: 528,
    maxOccupancy: 2,
    area: 36,
    bedType: '1.2m 双床',
    breakfast: '含双早',
    window: '落地窗',
    amenities: ['WiFi', '空调', '电视', '浴缸', '迷你吧', '落地窗'],
    intro: '两张独立床位，落地窗外是庭院的树。适合朋友或同事同行。',
    grade: 3
  },
  {
    id: 4,
    name: '行政套房',
    code: 'SUITE',
    basePrice: 888,
    maxOccupancy: 3,
    area: 55,
    bedType: '2m 大床 + 沙发床',
    breakfast: '含双早',
    window: '落地窗',
    amenities: ['WiFi', '空调', '电视', '浴缸', '客厅', '行政酒廊', '迷你吧'],
    intro: '独立客厅与卧室，行政酒廊全天供应茶点。适合长住与接待。',
    grade: 4
  }
]

/** 每个房型的房价计划（含早 / 不可取消），与后端 rateplan 语义一致 */
export function ratePlansOf(room) {
  const base = room.basePrice
  return [
    {
      id: room.id * 10 + 1,
      code: 'STD',
      name: room.breakfast === '含双早' ? '标准价 · 含双早' : '标准价 · 不含早',
      desc: '免费取消 · 到店付款',
      price: base,
      breakfast: room.breakfast,
      cancelPolicy: 10,
      cancelPolicyText: '入住前可免费取消'
    },
    {
      id: room.id * 10 + 2,
      code: 'BF',
      name: '含早套餐',
      desc: '双人自助早餐 · 限时免费取消',
      price: base + 60,
      breakfast: '含双早',
      cancelPolicy: 20,
      cancelPolicyText: '入住前 24 小时免费取消'
    },
    {
      id: room.id * 10 + 3,
      code: 'NRF',
      name: '不可取消 · 特惠 9 折',
      desc: '价格更低，订后不可退改',
      price: Math.round(base * 0.9),
      breakfast: room.breakfast,
      cancelPolicy: 30,
      cancelPolicyText: '不可取消'
    }
  ]
}

/** 逐日房价：周末上浮 15% */
export function dailyPrices(room, checkIn, checkOut) {
  const list = []
  let cursor = checkIn
  let guard = 0
  while (fmtDate(cursor) < fmtDate(checkOut) && guard < 60) {
    const date = fmtDate(cursor)
    const price = Math.round(room.basePrice * (isWeekend(date) ? 1.15 : 1))
    list.push({ date, price, weekend: isWeekend(date) })
    cursor = addDays(cursor, 1)
    guard++
  }
  return list
}

export const MOCK_AVAILABILITY = {
  1: 3,
  2: 2,
  3: 1,
  4: 2
}
