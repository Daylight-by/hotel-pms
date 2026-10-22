import request from '@/config/axios'

export interface ArAccountVO {
  id?: number
  storeId?: number
  storeName?: string
  name?: string
  code?: string
  taxNo?: string
  contactName?: string
  contactMobile?: string
  creditLimit?: number
  usedAmount?: number
  availableAmount?: number
  settleCycle?: number
  status?: number
  remark?: string
  createTime?: Date
}

export interface ArLedgerVO {
  id?: number
  storeId?: number
  arAccountId?: number
  arAccountName?: string
  orderId?: number
  folioId?: number
  amount?: number
  type?: number
  bizDate?: string
  settlementId?: number
  status?: number
  remark?: string
  createTime?: Date
}

export interface ArSettlementVO {
  id?: number
  storeId?: number
  settlementNo?: string
  arAccountId?: number
  arAccountName?: string
  beginDate?: string
  endDate?: string
  totalAmount?: number
  paidAmount?: number
  remainAmount?: number
  status?: number
  settleTime?: Date
  remark?: string
  createTime?: Date
}

export const getArAccountPage = async (params: PageParam) => {
  return await request.get({ url: '/hotel/ar/account/page', params })
}

export const getArAccountList = async (): Promise<ArAccountVO[]> => {
  return await request.get({ url: '/hotel/ar/account/list' })
}

export const createArAccount = async (data: ArAccountVO) => {
  return await request.post({ url: '/hotel/ar/account/create', data })
}

export const updateArAccount = async (data: ArAccountVO) => {
  return await request.put({ url: '/hotel/ar/account/update', data })
}

export const postToAr = async (data: any) => {
  return await request.post({ url: '/hotel/ar/post', data })
}

export const generateSettlement = async (data: any) => {
  return await request.post({ url: '/hotel/ar/settlement/generate', data })
}

export const receiveSettlement = async (data: any) => {
  return await request.post({ url: '/hotel/ar/settlement/receive', data })
}

export const getArLedgerPage = async (params: PageParam) => {
  return await request.get({ url: '/hotel/ar/ledger/page', params })
}

export const getArSettlementPage = async (params: PageParam) => {
  return await request.get({ url: '/hotel/ar/settlement/page', params })
}
