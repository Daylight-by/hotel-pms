import request from '@/config/axios'

export interface InvoiceVO {
  id?: number
  invoiceNo?: string
  orderId?: number
  orderNo?: string
  guestId?: number
  guestName?: string
  title?: string
  taxNo?: string
  type?: number
  typeName?: string
  amount?: number
  status?: number
  statusName?: string
  issueTime?: number | string
  voidTime?: number | string
  remark?: string
  createTime?: number | string
}

export interface InvoiceSaveVO {
  orderId?: number
  guestId?: number
  invoiceNo?: string
  title: string
  taxNo?: string
  type?: number
  amount: number
  remark?: string
}

// 开票
export const issueInvoice = async (data: InvoiceSaveVO) => {
  return await request.post({ url: '/hotel/invoice/issue', data })
}

// 红冲
export const voidInvoice = async (id: number, reason: string) => {
  return await request.put({ url: '/hotel/invoice/void', params: { id, reason } })
}

// 发票分页
export const getInvoicePage = async (params: PageParam) => {
  return await request.get({ url: '/hotel/invoice/page', params })
}
