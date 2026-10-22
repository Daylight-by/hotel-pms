import request from '@/config/axios'

export interface OrderVO {
  id?: number
  orderNo?: string
  guestId?: number
  guestName?: string
  guestMobile?: string
  roomId?: number
  roomNo?: string
  roomTypeId?: number
  roomTypeName?: string
  orderType?: number
  orderTypeName?: string
  businessDate?: string
  checkInDate?: string
  checkOutDate?: string
  checkInTime?: string
  checkOutTime?: string
  nights?: number
  adultCount?: number
  childCount?: number
  parentOrderId?: number
  amount?: number
  deposit?: number
  paidAmount?: number
  status?: number
  statusName?: string
  paymentStatus?: number
  paymentStatusName?: string
  paymentMethod?: number
  paymentMethodName?: string
  invoiceNo?: string
  invoiceRequested?: boolean
  remark?: string
  actualCheckInTime?: Date
  actualCheckOutTime?: Date
  createTime?: Date
}

export interface OrderPaymentVO {
  paymentStatus: number
  paymentMethod?: number
  amount?: number
  invoiceNo?: string
  invoiceRequested?: boolean
  reason?: string
}

// 查询订单分页
export const getOrderPage = async (params: PageParam) => {
  return await request.get({ url: '/hotel/order/page', params })
}

// 查询订单详情
export const getOrder = async (id: number) => {
  return await request.get({ url: '/hotel/order/get?id=' + id })
}

// 创建订单（预订）
export const createOrder = async (data: OrderVO) => {
  return await request.post({ url: '/hotel/order/create', data })
}

// 修改订单
export const updateOrder = async (data: OrderVO) => {
  return await request.put({ url: '/hotel/order/update', data })
}

// 办理入住
export const checkInOrder = async (id: number) => {
  return await request.post({ url: '/hotel/order/check-in', params: { id } })
}

// 办理退房
export const checkOutOrder = async (id: number) => {
  return await request.post({ url: '/hotel/order/check-out', params: { id } })
}

// 确认订单
export const confirmOrder = async (id: number) => {
  return await request.post({ url: '/hotel/order/confirm', params: { id } })
}

// 取消订单
export const cancelOrder = async (id: number) => {
  return await request.post({ url: '/hotel/order/cancel', params: { id } })
}

// 登记支付
export const payOrder = async (id: number, data: OrderPaymentVO) => {
  return await request.post({ url: '/hotel/order/payment', params: { id }, data })
}

// 订单退款
export const refundOrder = async (id: number, data: OrderPaymentVO) => {
  return await request.post({ url: '/hotel/order/refund', params: { id }, data })
}

// 删除订单
export const deleteOrder = async (id: number) => {
  return await request.delete({ url: '/hotel/order/delete?id=' + id })
}

// 最近订单
export const getRecentOrders = async (limit = 8): Promise<OrderVO[]> => {
  return await request.get({ url: '/hotel/order/recent', params: { limit } })
}

// 今日预抵
export const getTodayCheckIns = async (): Promise<OrderVO[]> => {
  return await request.get({ url: '/hotel/order/today-check-ins' })
}

// 今日待退房
export const getDueCheckOuts = async (): Promise<OrderVO[]> => {
  return await request.get({ url: '/hotel/order/due-check-outs' })
}

// 订单详情（订单 + 账页 + 明细 + 换房记录）
export const getOrderDetail = async (id: number) => {
  return await request.get({ url: '/hotel/order/get-detail?id=' + id })
}

// 续住
export const extendOrder = async (data: { id: number; nights: number; remark?: string }) => {
  return await request.put({ url: '/hotel/order/extend', data })
}

// 换房
export const changeRoom = async (data: { id: number; toRoomId: number; reason?: string }) => {
  return await request.put({ url: '/hotel/order/change-room', data })
}

// 钟点房开单
export const createHourlyOrder = async (data: {
  guestId: number
  roomId: number
  checkInTime: string
  checkOutTime: string
  deposit?: number
  remark?: string
}) => {
  return await request.post({ url: '/hotel/order/create-hourly', data })
}

// 提前退房
export const earlyCheckout = async (id: number) => {
  return await request.put({ url: '/hotel/order/early-checkout', params: { id } })
}

// 取消订单（带原因与违约金）
export const cancelOrderWithReason = async (
  id: number,
  data: { reason?: string; penalty?: number }
) => {
  return await request.put({ url: '/hotel/order/cancel', params: { id }, data })
}
