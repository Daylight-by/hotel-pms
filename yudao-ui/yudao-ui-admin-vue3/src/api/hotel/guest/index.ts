import request from '@/config/axios'

export interface GuestVO {
  id?: number
  name?: string
  mobile?: string
  idCard?: string
  email?: string
  sex?: number
  birthday?: string
  remark?: string
  orderCount?: number
  createTime?: Date
}

// 查询住客分页
export const getGuestPage = async (params: PageParam) => {
  return await request.get({ url: '/hotel/guest/page', params })
}

// 获得住客精简列表
export const getGuestSimpleList = async (): Promise<GuestVO[]> => {
  return await request.get({ url: '/hotel/guest/list-all-simple' })
}

// 查询住客详情
export const getGuest = async (id: number) => {
  return await request.get({ url: '/hotel/guest/get?id=' + id })
}

// 新增住客
export const createGuest = async (data: GuestVO) => {
  return await request.post({ url: '/hotel/guest/create', data })
}

// 修改住客
export const updateGuest = async (data: GuestVO) => {
  return await request.put({ url: '/hotel/guest/update', data })
}

// 删除住客
export const deleteGuest = async (id: number) => {
  return await request.delete({ url: '/hotel/guest/delete?id=' + id })
}
