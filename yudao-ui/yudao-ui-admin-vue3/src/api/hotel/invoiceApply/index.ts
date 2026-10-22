import request from '@/config/axios'

export interface InvoiceApplyVO {
  id?: number
  applyNo?: string
  orderId?: number
  orderNo?: string
  memberId?: number
  title?: string
  taxNo?: string
  email?: string
  type?: number
  typeName?: string
  amount?: number
  status?: number
  statusName?: string
  invoiceId?: number
  rejectReason?: string
  handleTime?: string
  createTime?: string
}

export const getInvoiceApplyPage = async (params: PageParam) => {
  return await request.get({ url: '/hotel/invoice-apply/page', params })
}

export const handleInvoiceApply = async (data: { id: number; status: number; rejectReason?: string }) => {
  return await request.put({ url: '/hotel/invoice-apply/handle', data, params: { id: data.id } })
}
