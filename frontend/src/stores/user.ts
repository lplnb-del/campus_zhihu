/**
 * 用户状态管理 Store
 * 使用 Pinia 管理用户登录状态、Token、用户信息等
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { RouteLocationNormalizedLoaded } from 'vue-router'

// 用户信息接口
export interface UserInfo {
  id: number
  username: string
  avatar?: string
  points: number
  major?: string
  grade?: string
  studentId?: string
}

// 登录响应接口
export interface LoginResponse {
  token: string
  userInfo: UserInfo
}

export const useUserStore = defineStore(
  'user',
  () => {
    // ==========================================
    // State
    // ==========================================
    const token = ref<string>('')
    const userInfo = ref<UserInfo | null>(null)
    const isLoggedIn = ref<boolean>(false)

    // ==========================================
    // Getters (使用 computed)
    // ==========================================

    /**
     * 获取用户 ID
     */
    const userId = computed(() => userInfo.value?.id || 0)

    /**
     * 获取用户名
     */
    const username = computed(() => userInfo.value?.username || '')

    /**
     * 获取用户头像
     */
    const avatar = computed(() => userInfo.value?.avatar || '')

    /**
     * 获取用户积分
     */
    const points = computed(() => userInfo.value?.points || 0)

    /**
     * 获取用户专业
     */
    const major = computed(() => userInfo.value?.major || '')

    /**
     * 获取用户年级
     */
    const grade = computed(() => userInfo.value?.grade || '')

    /**
     * 获取学号
     */
    const studentId = computed(() => userInfo.value?.studentId || '')

    /**
     * 判断是否已登录
     */
    const hasToken = computed(() => !!token.value)

    // ==========================================
    // Actions
    // ==========================================

    /**
     * 设置 Token
     */
    const setToken = (newToken: string) => {
      token.value = newToken
    }

    /**
     * 设置用户信息
     */
    const setUserInfo = (info: UserInfo) => {
      userInfo.value = info
      isLoggedIn.value = true
    }

    /**
     * 登录
     */
    const login = (loginData: LoginResponse) => {
      token.value = loginData.token
      userInfo.value = loginData.userInfo
      isLoggedIn.value = true
    }

    /**
     * 登出
     */
    const logout = () => {
      token.value = ''
      userInfo.value = null
      isLoggedIn.value = false
    }

    /**
     * 更新用户信息
     */
    const updateUserInfo = (info: Partial<UserInfo>) => {
      if (userInfo.value) {
        userInfo.value = {
          ...userInfo.value,
          ...info,
        }
      }
    }

    /**
     * 更新用户积分
     */
    const updatePoints = (newPoints: number) => {
      if (userInfo.value) {
        userInfo.value.points = newPoints
      }
    }

    /**
     * 增加用户积分
     */
    const addPoints = (points: number) => {
      if (userInfo.value) {
        userInfo.value.points += points
      }
    }

    /**
     * 减少用户积分
     */
    const subtractPoints = (points: number) => {
      if (userInfo.value) {
        userInfo.value.points = Math.max(0, userInfo.value.points - points)
      }
    }

    /**
     * 重置状态
     */
    const reset = () => {
      token.value = ''
      userInfo.value = null
      isLoggedIn.value = false
    }

    return {
      // State
      token,
      userInfo,
      isLoggedIn,

      // Getters
      userId,
      username,
      avatar,
      points,
      major,
      grade,
      studentId,
      hasToken,

      // Actions
      setToken,
      setUserInfo,
      login,
      logout,
      updateUserInfo,
      updatePoints,
      addPoints,
      subtractPoints,
      reset,
    }
  },
  {
    // 持久化配置
    persist: {
      key: 'user',
      storage: localStorage,
      paths: ['token', 'userInfo', 'isLoggedIn'],
    },
  }
)
