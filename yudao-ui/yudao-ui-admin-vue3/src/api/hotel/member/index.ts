import request from '@/config/axios'

export interface MemberVO {
  id?: number
  memberNo?: string
  guestId?: number
  name?: string
  mobile?: string
  level?: number
  levelName?: string
  points?: number
  balance?: number
  totalNights?: number
  totalAmount?: number
  status?: number
  statusName?: string
  registerTime?: string
  createTime?: string
}

export interface MemberSaveVO {
  id?: number
  name: string
  mobile: string
  password?: string
  level?: number
  status?: number
  guestId?: number
}

export interface MemberPointsLogVO {
  id?: number
  memberId?: number
  type?: number
  typeName?: string
  points?: number
  balance?: number
  orderId?: number
  remark?: string
  createTime?: string
}

export const getMemberPage = async (params: PageParam) => {
  return await request.get({ url: '/hotel/member/page', params })
}

export const getMember = async (id: number) => {
  return await request.get({ url: '/hotel/member/get', params: { id } })
}

export const createMember = async (data: MemberSaveVO) => {
  return await request.post({ url: '/hotel/member/create', data })
}

export const updateMember = async (data: MemberSaveVO) => {
  return await request.put({ url: '/hotel/member/update', data })
}

export const adjustPoints = async (data: { memberId: number; points: number; remark?: string }) => {
  return await request.put({ url: '/hotel/member/adjust-points', data })
}

export const adjustBalance = async (data: { memberId: number; amount: number; remark?: string }) => {
  return await request.put({ url: '/hotel/member/adjust-balance', data })
}

export const getPointsLog = async (memberId: number) => {
  return await request.get({ url: '/hotel/member/points-log', params: { memberId } })
}
