import request from '@/config/axios'

/** 操作审计日志分页 */
export const getOperationLogPageApi = (params: any) =>
  request.get({ url: '/hotel/operation-log/page', params })
