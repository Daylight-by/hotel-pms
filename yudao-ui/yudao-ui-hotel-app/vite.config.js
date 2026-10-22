import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

// H5 开发端口固定 5183（任务书 T3 号段），/app-api 代理到本机后端 48080。
export default defineConfig({
  plugins: [uni()],
  server: {
    host: '0.0.0.0',
    port: 5183,
    strictPort: true,
    proxy: {
      '/app-api': {
        target: 'http://localhost:48080',
        changeOrigin: true
      }
    }
  }
})
