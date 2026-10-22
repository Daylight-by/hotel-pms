import request from '@/config/axios'

export interface ChannelVO {
  id?: number
  name: string
  code: string
  type: number
  typeName?: string
  commissionRate?: number
  status: number
  statusName?: string
  sort?: number
  remark?: string
  createTime?: string
}

/** 渠道分页 */
export const getChannelPage = async (params: any) => {
  return await request.get({ url: '/hotel/channel/page', params })
}

/** 渠道详情 */
export const getChannel = async (id: number) => {
  return await request.get({ url: '/hotel/channel/get', params: { id } })
}

/** 创建渠道 */
export const createChannel = async (data: ChannelVO) => {
  return await request.post({ url: '/hotel/channel/create', data })
}

/** 更新渠道 */
export const updateChannel = async (data: ChannelVO) => {
  return await request.put({ url: '/hotel/channel/update', data })
}

/** 删除渠道 */
export const deleteChannel = async (id: number) => {
  return await request.delete({ url: '/hotel/channel/delete', params: { id } })
}

/** 开启中的渠道（下拉） */
export const getEnabledChannelList = async (): Promise<ChannelVO[]> => {
  return await request.get({ url: '/hotel/channel/list-enabled' })
}
