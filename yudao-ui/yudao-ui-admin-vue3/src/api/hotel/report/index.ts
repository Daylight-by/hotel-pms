import request from '@/config/axios'

/** 经营日报 */
export const getDailyReportApi = (params: any) =>
  request.get({ url: '/hotel/statistics/daily-report', params })

/** 经营月报 */
export const getMonthlyReportApi = (params: any) =>
  request.get({ url: '/hotel/statistics/monthly-report', params })

/** 报表导出（type=daily 日报 / monthly 月报） */
export const exportReportApi = (params: any) =>
  request.download({ url: '/hotel/statistics/export', params })
