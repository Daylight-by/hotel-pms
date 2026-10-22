import request from '@/config/axios'

export interface LockLogVO {
  id?: number
  storeId?: number
  orderId?: number
  roomId?: number
  roomNo?: string
  cardNo?: string
  actionType?: number
  validFrom?: Date
  validTo?: Date
  status?: number
  failReason?: string
  handleTime?: Date
}

export const getLockPage = async (params: PageParam) => {
  return await request.get({ url: '/hotel/lock/page', params })
}

export const issueCard = async (orderId: number) => {
  return await request.post({ url: '/hotel/lock/issue?orderId=' + orderId })
}

export const reissueCard = async (cardLogId: number) => {
  return await request.post({ url: '/hotel/lock/reissue?cardLogId=' + cardLogId })
}

export const cancelCard = async (orderId: number) => {
  return await request.post({ url: '/hotel/lock/cancel?orderId=' + orderId })
}

export const extendCard = async (orderId: number, newCheckOutDate?: string) => {
  return await request.post({
    url: '/hotel/lock/extend',
    params: { orderId, newCheckOutDate }
  })
}
