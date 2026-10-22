/** 通用格式化工具（H5 / 小程序通用，避免 new Date('yyyy-MM-dd') 的兼容坑） */

export function pad(n) {
  return n < 10 ? '0' + n : '' + n
}

export function parseDate(value) {
  if (!value) return new Date()
  if (value instanceof Date) return new Date(value.getTime())
  if (typeof value === 'number') return new Date(value)
  if (Array.isArray(value)) {
    // 后端 LocalDate 可能序列化成 [2026, 10, 21]
    return new Date(value[0], (value[1] || 1) - 1, value[2] || 1)
  }
  const m = String(value).match(/^(\d{4})-(\d{1,2})-(\d{1,2})/)
  if (m) return new Date(Number(m[1]), Number(m[2]) - 1, Number(m[3]))
  const d = new Date(value)
  return isNaN(d.getTime()) ? new Date() : d
}

export function fmtDate(value) {
  const d = parseDate(value)
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

export function fmtShort(value) {
  const d = parseDate(value)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

export function fmtDateDot(value) {
  const d = parseDate(value)
  return `${d.getFullYear()}.${pad(d.getMonth() + 1)}.${pad(d.getDate())}`
}

export const WEEK_CN = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

export function weekday(value) {
  return WEEK_CN[parseDate(value).getDay()]
}

export function isWeekend(value) {
  const day = parseDate(value).getDay()
  return day === 5 || day === 6
}

export function addDays(value, days) {
  const d = parseDate(value)
  d.setDate(d.getDate() + days)
  return d
}

export function today() {
  return fmtDate(new Date())
}

/** 两个日期之间的间夜数（离店 - 入住） */
export function nightsBetween(checkIn, checkOut) {
  const a = parseDate(checkIn)
  const b = parseDate(checkOut)
  const diff = Math.round((b.getTime() - a.getTime()) / 86400000)
  return diff > 0 ? diff : 0
}

/** 枚举住宿日期区间 */
export function dateRange(checkIn, checkOut) {
  const list = []
  const nights = nightsBetween(checkIn, checkOut)
  for (let i = 0; i < nights; i++) {
    list.push(fmtDate(addDays(checkIn, i)))
  }
  return list
}

export function money(value) {
  const n = Number(value || 0)
  if (Number.isInteger(n)) return String(n)
  return n.toFixed(2)
}

export function statusText(status) {
  const map = {
    10: '待支付',
    20: '已确认',
    21: '待确认',
    30: '已入住',
    40: '已退房',
    50: '已取消',
    60: '已退款'
  }
  return map[status] || '未知状态'
}

export function statusTone(status) {
  if (status === 10) return 'gold'
  if (status === 20) return 'green'
  if (status === 30) return 'blue'
  if (status === 40) return 'muted'
  return 'muted'
}
