import request from '@/config/axios'

export interface CleaningTaskVO {
  id?: number
  roomId?: number
  roomNo?: string
  floor?: number
  type?: number
  typeName?: string
  status?: number
  statusName?: string
  assigneeId?: number
  assigneeName?: string
  planDate?: string
  finishTime?: Date
  remark?: string
  createTime?: Date
}

// 查询清洁任务分页
export const getCleaningTaskPage = async (params: PageParam) => {
  return await request.get({ url: '/hotel/cleaning-task/page', params })
}

// 待处理清洁任务列表
export const getOpenCleaningTasks = async (): Promise<CleaningTaskVO[]> => {
  return await request.get({ url: '/hotel/cleaning-task/list-open' })
}

// 创建清洁任务
export const createCleaningTask = async (data: CleaningTaskVO) => {
  return await request.post({ url: '/hotel/cleaning-task/create', data })
}

// 流转清洁任务状态
export const updateCleaningTaskStatus = async (id: number, status: number, remark?: string) => {
  return await request.put({
    url: '/hotel/cleaning-task/update-status',
    params: { id },
    data: { status, remark }
  })
}

// 指派保洁人员
export const assignCleaningTask = async (id: number, assigneeId?: number) => {
  return await request.put({
    url: '/hotel/cleaning-task/assign',
    params: { id, assigneeId }
  })
}

// 删除清洁任务
export const deleteCleaningTask = async (id: number) => {
  return await request.delete({ url: '/hotel/cleaning-task/delete?id=' + id })
}
