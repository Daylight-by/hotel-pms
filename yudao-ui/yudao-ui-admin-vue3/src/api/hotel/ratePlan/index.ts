import request from '@/config/axios'

export interface RatePlanVO {
  id?: number
  name: string
  code: string
  channelId?: number
  channelName?: string
  roomTypeId?: number
  roomTypeName?: string
  priceRuleType: number
  priceRuleTypeName?: string
  priceRuleValue?: number
  breakfast?: number
  minNights?: number
  maxNights?: number
  minAdvanceHours?: number
  cancelPolicy?: number
  cancelPolicyName?: string
  cancelFreeHours?: number
  needDeposit?: boolean
  depositRate?: number
  status: number
  statusName?: string
  sort?: number
  remark?: string
  createTime?: string
}

export interface CalcPriceDayVO {
  bizDate: string
  basePrice: number
  price: number
  remaining: number
}

export interface CalcPriceVO {
  ratePlanId: number
  ratePlanName: string
  roomTypeId: number
  roomTypeName: string
  nights: number
  totalAmount: number
  dailyPrices: CalcPriceDayVO[]
}

/** 房价计划分页 */
export const getRatePlanPage = async (params: any) => {
  return await request.get({ url: '/hotel/rate-plan/page', params })
}

/** 房价计划详情 */
export const getRatePlan = async (id: number) => {
  return await request.get({ url: '/hotel/rate-plan/get', params: { id } })
}

/** 创建房价计划 */
export const createRatePlan = async (data: RatePlanVO) => {
  return await request.post({ url: '/hotel/rate-plan/create', data })
}

/** 更新房价计划 */
export const updateRatePlan = async (data: RatePlanVO) => {
  return await request.put({ url: '/hotel/rate-plan/update', data })
}

/** 删除房价计划 */
export const deleteRatePlan = async (id: number) => {
  return await request.delete({ url: '/hotel/rate-plan/delete', params: { id } })
}

/** 试算：逐日价格 + 总价（DoD3：日历价 300 × 90% = 270） */
export const calcRatePlanPrice = async (data: {
  ratePlanId: number
  roomTypeId?: number
  beginDate: string
  endDate: string
}): Promise<CalcPriceVO> => {
  return await request.post({ url: '/hotel/rate-plan/calc-price', data })
}
