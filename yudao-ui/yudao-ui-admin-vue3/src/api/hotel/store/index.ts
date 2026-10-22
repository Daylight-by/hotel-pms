import request from '@/config/axios'

export interface StoreVO {
  id?: number
  name?: string
  code?: string
  province?: string
  city?: string
  district?: string
  address?: string
  phone?: string
  legalPerson?: string
  psbCode?: string
  checkInTime?: string
  checkOutTime?: string
  status?: number
  sort?: number
  remark?: string
  createTime?: Date
}

export const getStoreList = async (): Promise<StoreVO[]> => {
  return await request.get({ url: '/hotel/store/list' })
}

export const getStorePage = async (params: PageParam) => {
  return await request.get({ url: '/hotel/store/page', params })
}

export const getStore = async (id: number) => {
  return await request.get({ url: '/hotel/store/get?id=' + id })
}

export const createStore = async (data: StoreVO) => {
  return await request.post({ url: '/hotel/store/create', data })
}

export const updateStore = async (data: StoreVO) => {
  return await request.put({ url: '/hotel/store/update', data })
}

export const deleteStore = async (id: number) => {
  return await request.delete({ url: '/hotel/store/delete?id=' + id })
}
