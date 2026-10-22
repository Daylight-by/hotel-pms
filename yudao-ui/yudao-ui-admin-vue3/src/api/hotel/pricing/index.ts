import request from '@/config/axios'

export interface PriceSuggestionVO {
  id?: number
  roomTypeId: number
  roomTypeName?: string
  bizDate: string
  basePrice: number
  suggestedPrice: number
  occFactor: number
  advanceFactor: number
  weekendFactor: number
  histOccupancy?: number
  bookingPace?: number
  leadDays?: number
  reason?: string
  status: number
  statusName?: string
  adoptedPrice?: number
  createTime?: string
}

export interface BacktestDayVO {
  bizDate: string
  basePrice: number
  suggestedPrice: number
  occupancy: number
  actualRevpar: number
  modelRevpar: number
  occupancyEstimated?: boolean
}

export interface BacktestVO {
  id?: number
  name: string
  roomTypeId: number
  roomTypeName?: string
  beginDate: string
  endDate: string
  actualRevpar: number
  modelRevpar: number
  upliftRate: number
  assumption?: string
  days?: BacktestDayVO[]
  createTime?: string
}

/** 生成定价建议 */
export const generateSuggestions = async (data: {
  roomTypeId: number
  beginDate: string
  endDate: string
}) => {
  return await request.post({ url: '/hotel/pricing/generate', data })
}

/** 查询定价建议 */
export const getSuggestionList = async (params: {
  roomTypeId: number
  beginDate: string
  endDate: string
}): Promise<PriceSuggestionVO[]> => {
  return await request.get({ url: '/hotel/pricing/list', params })
}

/** 采纳建议（写回房价日历） */
export const adoptSuggestion = async (id: number, price?: number) => {
  return await request.put({ url: '/hotel/pricing/adopt', params: { id, price } })
}

/** 忽略建议 */
export const ignoreSuggestion = async (id: number) => {
  return await request.put({ url: '/hotel/pricing/ignore', params: { id } })
}

/** 回测：原价 vs 建议价 RevPAR */
export const runBacktest = async (data: {
  name?: string
  roomTypeId: number
  beginDate: string
  endDate: string
}): Promise<BacktestVO> => {
  return await request.post({ url: '/hotel/pricing/backtest', data })
}

/** 回测记录分页 */
export const getBacktestPage = async (params: any) => {
  return await request.get({ url: '/hotel/pricing/backtest/page', params })
}
