import request from '@/config/axios'

/** 经营数据大屏：总览 */
export const getDashboardOverviewApi = () =>
  request.get({ url: '/hotel/dashboard/overview' })
