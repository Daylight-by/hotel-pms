import request from '@/config/axios'

export interface PayOrderVO {
  id?: number
  payOrderNo?: string
  bizOrderId?: number
  orderNo?: string
  bizType?: number
  bizTypeName?: string
  amount?: number
  channel?: string
  channelName?: string
  channelTradeNo?: string
  status?: number
  statusName?: string
  expireTime?: string
  payTime?: string
  refundAmount?: number
  createTime?: string
}

export const getPayOrderPage = async (params: PageParam) => {
  return await request.get({ url: '/hotel/pay/page', params })
}

export const refundPayOrder = async (data: { payOrderId: number; amount?: number; reason?: string }) => {
  return await request.put({ url: '/hotel/pay/refund', data })
}
