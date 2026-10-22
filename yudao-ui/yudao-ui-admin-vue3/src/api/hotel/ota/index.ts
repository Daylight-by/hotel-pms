import request from '@/config/axios'

export interface OtaConfigVO {
  id?: number
  storeId?: number
  channelId?: number
  otaCode?: string
  appKey?: string
  appSecret?: string
  apiUrl?: string
  pushRate?: boolean
  pullOrder?: boolean
  syncInterval?: number
  mockEnabled?: boolean
  status?: number
  lastSyncTime?: Date
  remark?: string
}

export interface OtaOrderVO {
  id?: number
  storeId?: number
  otaCode?: string
  otaOrderNo?: string
  orderId?: number
  syncStatus?: number
  rawData?: string
  failReason?: string
  syncTime?: Date
}

export const getOtaConfigPage = async (params: PageParam) => {
  return await request.get({ url: '/hotel/ota/config/page', params })
}

export const createOtaConfig = async (data: OtaConfigVO) => {
  return await request.post({ url: '/hotel/ota/config/create', data })
}

export const updateOtaConfig = async (data: OtaConfigVO) => {
  return await request.put({ url: '/hotel/ota/config/update', data })
}

export const syncOtaOrder = async (configId: number) => {
  return await request.post({ url: '/hotel/ota/sync-order?configId=' + configId })
}

export const pushOtaRate = async (configId: number) => {
  return await request.post({ url: '/hotel/ota/push-rate?configId=' + configId })
}

export const getOtaOrderPage = async (params: PageParam) => {
  return await request.get({ url: '/hotel/ota/order/page', params })
}
