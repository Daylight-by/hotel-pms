import request from '@/config/axios'

export interface PsbLogVO {
  id?: number
  storeId?: number
  orderId?: number
  guestId?: number
  idCard?: string
  guestName?: string
  roomNo?: string
  actionType?: number
  bizTime?: Date
  status?: number
  retryCount?: number
  failReason?: string
  uploadTime?: Date
}

export const getPsbPage = async (params: PageParam) => {
  return await request.get({ url: '/hotel/psb/page', params })
}

export const retryPsb = async (id: number) => {
  return await request.post({ url: '/hotel/psb/retry?id=' + id })
}

export const getPsbStat = async (params: any) => {
  return await request.get({ url: '/hotel/psb/stat', params })
}
