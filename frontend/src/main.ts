/**
 * Vue 3 主入口文件
 * CampusZhihu Frontend Application
 */

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'
import router from './router'
import App from './App.vue'

// 全局样式
import './assets/styles/global.scss'
import 'nprogress/nprogress.css'

// 创建 Vue 应用实例
const app = createApp(App)

// 创建 Pinia 实例并添加持久化插件
const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)

// 使用插件
app.use(pinia)
app.use(router)

// 挂载应用
app.mount('#app')

// 开发环境日志
if (import.meta.env.DEV) {
  console.log('🚀 CampusZhihu 前端应用启动成功！')
  console.log('📦 当前环境：', import.meta.env.MODE)
  console.log('🔗 API 地址：', import.meta.env.VITE_API_BASE_URL || '/api')
}
