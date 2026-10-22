import request from '@/config/axios'

export interface FolioItemVO {
  id?: number
  folioId?: number
  orderId?: number
  businessDate?: string
  itemType?: number
  itemTypeName?: string
  itemName?: string
  direction?: number
  directionName?: string
  quantity?: number
  unitPrice?: number
  amount?: number
  paymentMethod?: number
  paymentMethodName?: string
  status?: number
  statusName?: string
  voidId?: number
  transferToFolioId?: number
  operatorId?: number
  operatorName?: string
  remark?: string
  createTime?: number | string
}

export interface FolioVO {
  id?: number
  folioNo?: string
  orderId?: number
  orderNo?: string
  guestId?: number
  guestName?: string
  roomId?: number
  roomNo?: string
  type?: number
  status?: number
  statusName?: string
  totalCharge?: number
  totalPayment?: number
  balance?: number
  remark?: string
  items?: FolioItemVO[]
  createTime?: number | string
}

export interface FolioPostVO {
  folioId: number
  itemType: number
  itemName?: string
  quantity?: number
  unitPrice?: number
  amount?: number
  businessDate?: string
  remark?: string
}

export interface FolioPaymentVO {
  folioId: number
  amount: number
  paymentMethod: number
  tradeNo?: string
  remark?: string
}

// 账页分页
export const getFolioPage = async (params: PageParam) => {
  return await request.get({ url: '/hotel/folio/page', params })
}

// 账页详情（含明细）
export const getFolio = async (id: number): Promise<FolioVO> => {
  return await request.get({ url: '/hotel/folio/get?id=' + id })
}

// 按订单取主账页
export const getFolioByOrder = async (orderId: number): Promise<FolioVO> => {
  return await request.get({ url: '/hotel/folio/get-by-order', params: { orderId } })
}

// 开账页
export const createFolio = async (data: any) => {
  return await request.post({ url: '/hotel/folio/create', data })
}

// 入账消费
export const postCharge = async (data: FolioPostVO) => {
  return await request.post({ url: '/hotel/folio/post-charge', data })
}

// 收款
export const postPayment = async (data: FolioPaymentVO) => {
  return await request.post({ url: '/hotel/folio/post-payment', data })
}

// 冲销明细
export const voidFolioItem = async (id: number, reason: string) => {
  return await request.put({ url: '/hotel/folio/void-item', params: { id }, data: { itemId: id, reason } })
}

// 转账
export const transferFolioItem = async (itemId: number, targetFolioId: number) => {
  return await request.put({ url: '/hotel/folio/transfer-item', data: { itemId, targetFolioId } })
}

// 拆账页
export const splitFolio = async (folioId: number, itemIds: number[], remark?: string) => {
  return await request.post({ url: '/hotel/folio/split', data: { folioId, itemIds, remark } })
}

// 结账
export const settleFolio = async (id: number) => {
  return await request.put({ url: '/hotel/folio/settle', params: { id } })
}

// 账务明细分页
export const getFolioItemPage = async (params: PageParam) => {
  return await request.get({ url: '/hotel/folio/item-page', params })
}
