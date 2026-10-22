import request from '@/config/axios'

export interface HotelOverviewVO {
  todayRevenue: number
  monthRevenue: number
  todayOrders: number
  activeOrders: number
  totalRooms: number
  availableRooms: number
  occupiedRooms: number
  cleaningRooms: number
  maintenanceRooms: number
  occupancyRate: number
  expectedRevenue: number
  todayCheckIns: number
  dueCheckOuts: number
  openCleaningTasks: number
}

export interface TrendPointVO {
  date: string
  revenue: number
  orders: number
  occupancyRate: number
}

export interface PaymentShareVO {
  name: string
  value: number
}

export interface HotelSummaryVO {
  beginDate: string
  endDate: string
  occ: number
  adr: number
  revpar: number
  roomRevenue: number
  totalRevenue: number
  availableNights: number
  soldNights: number
  orderCount: number
  roomNights: number
  avgStay: number
}

export interface SourceVO {
  guestSource: { name: string; value: number; amount: number }[]
  paymentSource: { name: string; value: number }[]
}

export interface TrendRangePointVO extends TrendPointVO {
  soldNights?: number
  totalNights?: number
}

// 经营概览
export const getOverview = async (): Promise<HotelOverviewVO> => {
  return await request.get({ url: '/hotel/statistics/overview' })
}

// 收入与入住率趋势
export const getTrend = async (days = 14): Promise<TrendPointVO[]> => {
  return await request.get({ url: '/hotel/statistics/trend', params: { days } })
}

// 支付方式占比
export const getPaymentShare = async (): Promise<PaymentShareVO[]> => {
  return await request.get({ url: '/hotel/statistics/payment-share' })
}

// 核心经营指标（OCC / ADR / RevPAR）
export const getSummary = async (params: {
  beginDate: string
  endDate: string
}): Promise<HotelSummaryVO> => {
  return await request.get({ url: '/hotel/statistics/summary', params })
}

// 区间趋势（日 / 月）
export const getTrendRange = async (params: {
  beginDate: string
  endDate: string
  type: 'day' | 'month'
}): Promise<TrendRangePointVO[]> => {
  return await request.get({ url: '/hotel/statistics/trend', params })
}

// 客源结构 + 收款结构
export const getSource = async (params: {
  beginDate: string
  endDate: string
}): Promise<SourceVO> => {
  return await request.get({ url: '/hotel/statistics/source', params })
}
