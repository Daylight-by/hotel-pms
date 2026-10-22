import request from '@/config/axios'

/** 生成演示数据（months：1-12，默认 6） */
export const generateDemoDataApi = (months: number) =>
  request.post({ url: '/hotel/demo/generate', params: { months } })

/** 清理演示数据（幂等） */
export const cleanDemoDataApi = () => request.post({ url: '/hotel/demo/clean' })

/** 演示数据状态 */
export const getDemoStatusApi = () => request.get({ url: '/hotel/demo/status' })
