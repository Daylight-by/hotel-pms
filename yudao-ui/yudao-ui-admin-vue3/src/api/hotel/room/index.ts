import request from '@/config/axios'

export interface RoomVO {
  id?: number
  roomNo?: string
  roomTypeId?: number
  roomTypeName?: string
  floor?: number
  area?: number
  price?: number
  status?: number
  statusName?: string
  lockStatus?: number
  lockStatusName?: string
  sort?: number
  currentGuest?: string
  remark?: string
  createTime?: Date
}

export interface RoomBoardVO {
  total: number
  rooms: RoomVO[]
  counts: {
    available: number
    occupied: number
    cleaning: number
    maintenance: number
  }
}

// 查询房间分页
export const getRoomPage = async (params: PageParam) => {
  return await request.get({ url: '/hotel/room/page', params })
}

// 获得房间精简列表
export const getRoomSimpleList = async (): Promise<RoomVO[]> => {
  return await request.get({ url: '/hotel/room/list-all-simple' })
}

// 查询房间详情
export const getRoom = async (id: number) => {
  return await request.get({ url: '/hotel/room/get?id=' + id })
}

// 新增房间
export const createRoom = async (data: RoomVO) => {
  return await request.post({ url: '/hotel/room/create', data })
}

// 修改房间
export const updateRoom = async (data: RoomVO) => {
  return await request.put({ url: '/hotel/room/update', data })
}

// 删除房间
export const deleteRoom = async (id: number) => {
  return await request.delete({ url: '/hotel/room/delete?id=' + id })
}

// 调整房态
export const updateRoomStatus = async (id: number, status: number) => {
  return await request.put({ url: '/hotel/room/update-status', params: { id, status } })
}

// 锁定 / 解锁房间
export const updateRoomLockStatus = async (id: number, lockStatus: number) => {
  return await request.put({ url: '/hotel/room/update-lock-status', params: { id, lockStatus } })
}

// 房态看板
export const getRoomBoard = async (): Promise<RoomBoardVO> => {
  return await request.get({ url: '/hotel/room/board' })
}
