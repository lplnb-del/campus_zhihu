/**
 * Axios 请求封装
 * 包含请求拦截器、响应拦截器、错误处理
 */

import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, AxiosError } from 'axios'
import { ElMessage, ElLoading } from 'element-plus'
import type { LoadingInstance } from 'element-plus/es/components/loading/src/loading'
import { useUserStore } from '@/stores/user'
import router from '@/router'

// 响应数据结构
export interface ApiResponse<T = any> {
  code: number
  msg: string
  data: T
  timestamp: number
}

// 请求配置扩展
interface CustomRequestConfig extends AxiosRequestConfig {
  showLoading?: boolean // 是否显示加载动画
  showError?: boolean // 是否显示错误提示
  loadingText?: string // 加载提示文本
}

// 加载实例
let loadingInstance: LoadingInstance | null = null
let requestCount = 0

/**
 * 显示加载动画
 */
const showLoading = (text = '加载中...') => {
  if (requestCount === 0) {
    loadingInstance = ElLoading.service({
      lock: true,
      text,
      background: 'rgba(0, 0, 0, 0.7)',
    })
  }
  requestCount++
}

/**
 * 隐藏加载动画
 */
const hideLoading = () => {
  requestCount--
  if (requestCount <= 0) {
    requestCount = 0
    loadingInstance?.close()
    loadingInstance = null
  }
}

/**
 * 创建 Axios 实例
 */
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8',
  },
})

/**
 * 请求拦截器
 */
service.interceptors.request.use(
  (config: any) => {
    const customConfig = config as CustomRequestConfig

    // 显示加载动画（默认开启）
    if (customConfig.showLoading !== false) {
      showLoading(customConfig.loadingText)
    }

    // 添加 Token
    const userStore = useUserStore()
    const token = userStore.token
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    // 处理 GET 请求参数
    if (config.method === 'get' && config.params) {
      let url = config.url as string
      url += '?'
      const keys = Object.keys(config.params)
      for (const key of keys) {
        if (config.params[key] !== null && config.params[key] !== undefined) {
          url += `${key}=${encodeURIComponent(config.params[key])}&`
        }
      }
      url = url.substring(0, url.length - 1)
      config.params = {}
      config.url = url
    }

    return config
  },
  (error: AxiosError) => {
    hideLoading()
    console.error('请求错误：', error)
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 */
service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const customConfig = response.config as CustomRequestConfig

    // 隐藏加载动画
    if (customConfig.showLoading !== false) {
      hideLoading()
    }

    const res = response.data

    // 成功响应
    if (res.code === 200) {
      return res.data
    }

    // Token 失效或未授权
    if (res.code === 1001 || res.code === 1002 || res.code === 1003) {
      const userStore = useUserStore()
      userStore.logout()

      ElMessage.error(res.msg || '登录已过期，请重新登录')

      // 跳转到登录页
      router.push({
        path: '/login',
        query: { redirect: router.currentRoute.value.fullPath },
      })

      return Promise.reject(new Error(res.msg || '未授权'))
    }

    // 权限不足
    if (res.code === 1004) {
      ElMessage.error(res.msg || '权限不足')
      return Promise.reject(new Error(res.msg || '权限不足'))
    }

    // 其他业务错误
    if (customConfig.showError !== false) {
      ElMessage.error(res.msg || '操作失败')
    }

    return Promise.reject(new Error(res.msg || '操作失败'))
  },
  (error: AxiosError) => {
    const customConfig = error.config as CustomRequestConfig

    // 隐藏加载动画
    if (customConfig?.showLoading !== false) {
      hideLoading()
    }

    // 处理 HTTP 错误
    let message = '网络请求失败'

    if (error.response) {
      const status = error.response.status

      switch (status) {
        case 400:
          message = '请求参数错误'
          break
        case 401: {
          message = '未授权，请登录'
          const userStore = useUserStore()
          userStore.logout()
          router.push('/login')
          break
        }
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求地址不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        case 502:
          message = '网关错误'
          break
        case 503:
          message = '服务不可用'
          break
        case 504:
          message = '网关超时'
          break
        default:
          message = `连接错误 ${status}`
      }
    } else if (error.request) {
      // 请求已发出，但没有收到响应
      message = '网络连接失败，请检查您的网络'
    } else {
      // 发送请求时出了点问题
      message = error.message || '请求失败'
    }

    // 超时处理
    if (error.code === 'ECONNABORTED' && error.message.includes('timeout')) {
      message = '请求超时，请稍后重试'
    }

    // 显示错误提示（默认开启）
    if (customConfig?.showError !== false) {
      ElMessage.error(message)
    }

    console.error('响应错误：', error)
    return Promise.reject(error)
  }
)

/**
 * 导出请求方法
 */
export default service

/**
 * GET 请求
 */
export function get<T = any>(
  url: string,
  params?: any,
  config?: CustomRequestConfig
): Promise<T> {
  return service.get(url, { params, ...config })
}

/**
 * POST 请求
 */
export function post<T = any>(
  url: string,
  data?: any,
  config?: CustomRequestConfig
): Promise<T> {
  return service.post(url, data, config)
}

/**
 * PUT 请求
 */
export function put<T = any>(
  url: string,
  data?: any,
  config?: CustomRequestConfig
): Promise<T> {
  return service.put(url, data, config)
}

/**
 * DELETE 请求
 */
export function del<T = any>(
  url: string,
  params?: any,
  config?: CustomRequestConfig
): Promise<T> {
  return service.delete(url, { params, ...config })
}

/**
 * 文件上传
 */
export function upload<T = any>(
  url: string,
  file: File,
  onProgress?: (progressEvent: any) => void,
  config?: CustomRequestConfig
): Promise<T> {
  const formData = new FormData()
  formData.append('file', file)

  return service.post(url, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    onUploadProgress: onProgress,
    ...config,
  })
}

/**
 * 文件下载
 */
export function download(
  url: string,
  params?: any,
  filename?: string,
  config?: CustomRequestConfig
): Promise<void> {
  return service
    .get(url, {
      params,
      responseType: 'blob',
      ...config,
    })
    .then((response: any) => {
      // 创建 blob 对象
      const blob = new Blob([response])
      const downloadUrl = window.URL.createObjectURL(blob)

      // 创建下载链接
      const link = document.createElement('a')
      link.href = downloadUrl
      link.download = filename || '下载文件'
      document.body.appendChild(link)
      link.click()

      // 释放资源
      document.body.removeChild(link)
      window.URL.revokeObjectURL(downloadUrl)
    })
}
