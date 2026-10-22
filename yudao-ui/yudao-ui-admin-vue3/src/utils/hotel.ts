/**
 * 酒店模块前端公共常量与格式化工具
 */

/** 金额格式化：1234.5 -> 1,234.50 */
export const formatMoney = (value?: number | string | null, digits = 2): string => {
  if (value === null || value === undefined || value === '') return '-'
  const num = Number(value)
  if (Number.isNaN(num)) return '-'
  return num.toLocaleString('zh-CN', {
    minimumFractionDigits: digits,
    maximumFractionDigits: digits
  })
}

/** 房态：10 空闲 / 20 已入住 / 30 待清洁 / 40 维修中 */
export const ROOM_STATUS = [
  { value: 10, label: '空闲', type: 'success', color: '#67c23a' },
  { value: 20, label: '已入住', type: 'danger', color: '#f56c6c' },
  { value: 30, label: '待清洁', type: 'warning', color: '#e6a23c' },
  { value: 40, label: '维修中', type: 'info', color: '#909399' }
]

/** 订单状态：10 待支付 / 11 待确认 / 20 已确认 / 30 已入住 / 40 已退房 / 50 已取消 / 60 已超时关闭 */
export const ORDER_STATUS = [
  { value: 10, label: '待支付', type: 'warning' },
  { value: 11, label: '待确认', type: 'info' },
  { value: 20, label: '已确认', type: 'primary' },
  { value: 30, label: '已入住', type: 'danger' },
  { value: 40, label: '已退房', type: 'success' },
  { value: 50, label: '已取消', type: 'info' },
  { value: 60, label: '已超时关闭', type: 'info' }
]

/** 支付状态：10 未支付 / 20 部分支付 / 30 已支付 / 40 已退款 */
export const PAYMENT_STATUS = [
  { value: 10, label: '未支付', type: 'info' },
  { value: 20, label: '部分支付', type: 'warning' },
  { value: 30, label: '已支付', type: 'success' },
  { value: 40, label: '已退款', type: 'danger' }
]

/** 支付方式：1 现金 / 2 银行卡 / 3 微信 / 4 支付宝 / 5 在线支付（与后端 PaymentMethodEnum 一致） */
export const PAYMENT_METHOD = [
  { value: 1, label: '现金' },
  { value: 2, label: '银行卡' },
  { value: 3, label: '微信' },
  { value: 4, label: '支付宝' },
  { value: 5, label: '在线支付' }
]

/** 清洁类型：10 退房清洁 / 20 日常清洁 / 30 深度清洁 */
export const CLEANING_TYPE = [
  { value: 10, label: '退房清洁' },
  { value: 20, label: '日常清洁' },
  { value: 30, label: '深度清洁' }
]

/** 清洁任务状态：10 待处理 / 20 处理中 / 30 已完成 / 40 已取消 */
export const CLEANING_STATUS = [
  { value: 10, label: '待处理', type: 'info' },
  { value: 20, label: '处理中', type: 'warning' },
  { value: 30, label: '已完成', type: 'success' },
  { value: 40, label: '已取消', type: 'info' }
]

type OptionItem = { value: number | string; label: string; type?: string; color?: string }

/** 从常量表中取中文名 */
export const labelOf = (options: OptionItem[], value?: number | string | null): string => {
  if (value === null || value === undefined) return '-'
  return options.find((item) => item.value === value)?.label ?? String(value)
}

/** 从常量表中取 Tag 类型 */
export const tagTypeOf = (options: OptionItem[], value?: number | string | null): any => {
  if (value === null || value === undefined) return 'info'
  return options.find((item) => item.value === value)?.type ?? 'info'
}

/** 从常量表中取颜色 */
export const colorOf = (options: OptionItem[], value?: number | string | null): string => {
  if (value === null || value === undefined) return '#909399'
  return options.find((item) => item.value === value)?.color ?? '#909399'
}

/** 计算两个日期之间的晚数 */
export const nightsBetween = (checkIn?: string, checkOut?: string): number => {
  if (!checkIn || !checkOut) return 0
  const start = new Date(checkIn).getTime()
  const end = new Date(checkOut).getTime()
  if (Number.isNaN(start) || Number.isNaN(end) || end <= start) return 0
  return Math.round((end - start) / (24 * 60 * 60 * 1000))
}
