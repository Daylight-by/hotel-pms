import request from '@/config/axios'

export interface RoomDailyRateVO {
  id?: number
  roomTypeId?: number
  roomTypeName?: string
  bizDate?: string
  price?: number
  totalInventory?: number
  soldInventory?: number
  remaining?: number
  status?: number
  statusName?: string
  remark?: string
  updateTime?: string
}

export interface RateBatchSaveVO {
  roomTypeId: number
  beginDate: string
  endDate: string
  price: number
  totalInventory?: number
  status?: number
  remark?: string
}

export interface RateBoardDayVO {
  bizDate: string
  price: number
  totalInventory: number
  soldInventory: number
  remaining: number
  status: number
}

export interface RateBoardVO {
  roomTypeId: number
  roomTypeName: string
  basePrice: number
  roomCount: number
  days: RateBoardDayVO[]
}

// 批量维护房价日历
export const batchSaveRate = async (data: RateBatchSaveVO) => {
  return await request.post({ url: '/hotel/rate/batch-save', data })
}

// 按日期区间查询房价日历（缺失日期也会返回）
export const getRateList = async (params: {
  roomTypeId: number
  beginDate: string
  endDate: string
}): Promise<RoomDailyRateVO[]> => {
  return await request.get({ url: '/hotel/rate/list', params })
}

// 房态盘（房型 × 日期）
export const getRateBoard = async (params: {
  beginDate: string
  endDate: string
}): Promise<RateBoardVO[]> => {
  return await request.get({ url: '/hotel/rate/board', params })
}

// 房价日历分页
export const getRatePage = async (params: PageParam) => {
  return await request.get({ url: '/hotel/rate/page', params })
}
