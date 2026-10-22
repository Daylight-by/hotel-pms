import request from '@/config/axios'

export interface BusinessDateVO {
  bizDate?: string
  status?: number
  statusName?: string
  auditTime?: number | string
  today?: string
}

export interface NightAuditVO {
  id?: number
  bizDate?: string
  status?: number
  statusName?: string
  startTime?: number | string
  endTime?: number | string
  durationMs?: number
  roomChargeTotal?: number
  postedRoomCount?: number
  postedItemCount?: number
  failReason?: string
  createTime?: number | string
}

// 当前营业日
export const getBusinessDate = async (): Promise<BusinessDateVO> => {
  return await request.get({ url: '/hotel/audit/business-date' })
}

// 执行夜审
export const runNightAudit = async () => {
  return await request.post({ url: '/hotel/audit/run' })
}

// 夜审历史分页
export const getNightAuditPage = async (params: PageParam) => {
  return await request.get({ url: '/hotel/audit/page', params })
}

// 最近一次夜审
export const getLastNightAudit = async (): Promise<NightAuditVO> => {
  return await request.get({ url: '/hotel/audit/last' })
}
