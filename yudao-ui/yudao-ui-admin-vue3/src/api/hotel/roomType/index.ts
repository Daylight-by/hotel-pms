import request from '@/config/axios'

export interface RoomTypeVO {
  id?: number
  name?: string
  code?: string
  basePrice?: number
  hourlyPrice?: number
  hourlyHours?: number
  hourlyEnabled?: boolean
  deposit?: number
  maxOccupancy?: number
  area?: number
  amenities?: string
  description?: string
  status?: number
  sort?: number
  roomCount?: number
  createTime?: Date
}

// 查询房型分页
export const getRoomTypePage = async (params: PageParam) => {
  return await request.get({ url: '/hotel/room-type/page', params })
}

// 获得房型精简列表
export const getRoomTypeSimpleList = async (): Promise<RoomTypeVO[]> => {
  return await request.get({ url: '/hotel/room-type/list-all-simple' })
}

// 查询房型详情
export const getRoomType = async (id: number) => {
  return await request.get({ url: '/hotel/room-type/get?id=' + id })
}

// 新增房型
export const createRoomType = async (data: RoomTypeVO) => {
  return await request.post({ url: '/hotel/room-type/create', data })
}

// 修改房型
export const updateRoomType = async (data: RoomTypeVO) => {
  return await request.put({ url: '/hotel/room-type/update', data })
}

// 删除房型
export const deleteRoomType = async (id: number) => {
  return await request.delete({ url: '/hotel/room-type/delete?id=' + id })
}
