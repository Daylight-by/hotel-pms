import request from '@/config/axios'

export interface ShiftVO {
  id?: number
  shiftNo?: string
  bizDate?: string
  startTime?: number | string
  endTime?: number | string
  operatorId?: number
  operatorName?: string
  cashAmount?: number
  cardAmount?: number
  wechatAmount?: number
  alipayAmount?: number
  otherAmount?: number
  totalAmount?: number
  status?: number
  statusName?: string
  remark?: string
  createTime?: number | string
}

// 开班
export const openShift = async (data: { remark?: string }) => {
  return await request.post({ url: '/hotel/shift/open', data })
}

// 交班
export const closeShift = async (id: number, data: { remark?: string; actualCash?: number }) => {
  return await request.put({ url: '/hotel/shift/close', params: { id }, data })
}

// 当前班次
export const getCurrentShift = async (): Promise<ShiftVO> => {
  return await request.get({ url: '/hotel/shift/current' })
}

// 班次分页
export const getShiftPage = async (params: PageParam) => {
  return await request.get({ url: '/hotel/shift/page', params })
}
