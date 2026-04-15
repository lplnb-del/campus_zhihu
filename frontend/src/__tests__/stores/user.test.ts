/**
 * 用户状态管理 Store 单元测试
 * 测试用户登录状态、Token、用户信息等状态管理
 */

import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '@/stores/user'
import type { UserInfo, LoginResponse } from '@/stores/user'

describe('UserStore - 用户状态管理测试', () => {
  beforeEach(() => {
    // 创建新的 Pinia 实例
    setActivePinia(createPinia())
    // 清除 localStorage
    localStorage.clear()
  })

  afterEach(() => {
    localStorage.clear()
  })

  describe('初始状态', () => {
    it('应该有正确的初始状态', () => {
      const store = useUserStore()

      expect(store.token).toBe('')
      expect(store.userInfo).toBeNull()
      expect(store.isLoggedIn).toBe(false)
    })

    it('应该有正确的初始 getters', () => {
      const store = useUserStore()

      expect(store.userId).toBe(0)
      expect(store.username).toBe('')
      expect(store.avatar).toBe('')
      expect(store.points).toBe(0)
      expect(store.major).toBe('')
      expect(store.grade).toBe('')
      expect(store.studentId).toBe('')
      expect(store.hasToken).toBe(false)
    })
  })

  describe('setToken - 设置 Token', () => {
    it('应该成功设置 Token', () => {
      const store = useUserStore()

      store.setToken('test-token-123')

      expect(store.token).toBe('test-token-123')
      expect(store.hasToken).toBe(true)
    })

    it('应该支持更新 Token', () => {
      const store = useUserStore()

      store.setToken('old-token')
      store.setToken('new-token')

      expect(store.token).toBe('new-token')
    })
  })

  describe('setUserInfo - 设置用户信息', () => {
    it('应该成功设置用户信息', () => {
      const store = useUserStore()

      const userInfo: UserInfo = {
        id: 1,
        username: 'testuser',
        avatar: 'avatar.png',
        points: 100,
        major: '计算机科学',
        grade: '2024级',
        studentId: '20240001',
      }

      store.setUserInfo(userInfo)

      expect(store.userInfo).toEqual(userInfo)
      expect(store.isLoggedIn).toBe(true)
    })

    it('应该正确更新 getters', () => {
      const store = useUserStore()

      const userInfo: UserInfo = {
        id: 1,
        username: 'testuser',
        avatar: 'avatar.png',
        points: 100,
        major: '计算机科学',
        grade: '2024级',
        studentId: '20240001',
      }

      store.setUserInfo(userInfo)

      expect(store.userId).toBe(1)
      expect(store.username).toBe('testuser')
      expect(store.avatar).toBe('avatar.png')
      expect(store.points).toBe(100)
      expect(store.major).toBe('计算机科学')
      expect(store.grade).toBe('2024级')
      expect(store.studentId).toBe('20240001')
    })
  })

  describe('login - 登录', () => {
    it('应该成功登录', () => {
      const store = useUserStore()

      const loginData: LoginResponse = {
        token: 'jwt-token-123',
        userInfo: {
          id: 1,
          username: 'testuser',
          avatar: 'avatar.png',
          points: 100,
          major: '计算机科学',
          grade: '2024级',
          studentId: '20240001',
        },
      }

      store.login(loginData)

      expect(store.token).toBe('jwt-token-123')
      expect(store.userInfo).toEqual(loginData.userInfo)
      expect(store.isLoggedIn).toBe(true)
      expect(store.hasToken).toBe(true)
    })

    it('应该覆盖之前的登录状态', () => {
      const store = useUserStore()

      // 第一次登录
      store.login({
        token: 'token-1',
        userInfo: {
          id: 1,
          username: 'user1',
          points: 50,
        },
      })

      // 第二次登录
      store.login({
        token: 'token-2',
        userInfo: {
          id: 2,
          username: 'user2',
          points: 100,
        },
      })

      expect(store.token).toBe('token-2')
      expect(store.userId).toBe(2)
      expect(store.username).toBe('user2')
      expect(store.points).toBe(100)
    })
  })

  describe('logout - 登出', () => {
    it('应该成功登出', () => {
      const store = useUserStore()

      // 先登录
      store.login({
        token: 'jwt-token-123',
        userInfo: {
          id: 1,
          username: 'testuser',
          points: 100,
        },
      })

      // 登出
      store.logout()

      expect(store.token).toBe('')
      expect(store.userInfo).toBeNull()
      expect(store.isLoggedIn).toBe(false)
      expect(store.hasToken).toBe(false)
    })

    it('应该清除 localStorage', () => {
      const store = useUserStore()

      // 先登录
      store.login({
        token: 'jwt-token-123',
        userInfo: {
          id: 1,
          username: 'testuser',
          points: 100,
        },
      })

      // 登出
      store.logout()

      expect(localStorage.getItem('user')).toBeNull()
    })

    it('应该可以多次调用登出', () => {
      const store = useUserStore()

      store.logout()
      store.logout()
      store.logout()

      expect(store.token).toBe('')
      expect(store.userInfo).toBeNull()
      expect(store.isLoggedIn).toBe(false)
    })
  })

  describe('updateUserInfo - 更新用户信息', () => {
    it('应该成功更新用户信息', () => {
      const store = useUserStore()

      // 先设置用户信息
      store.setUserInfo({
        id: 1,
        username: 'testuser',
        avatar: 'old-avatar.png',
        points: 100,
        major: '计算机科学',
        grade: '2024级',
        studentId: '20240001',
      })

      // 更新部分信息
      store.updateUserInfo({
        avatar: 'new-avatar.png',
        major: '软件工程',
      })

      expect(store.userInfo?.avatar).toBe('new-avatar.png')
      expect(store.userInfo?.major).toBe('软件工程')
      expect(store.userInfo?.username).toBe('testuser') // 未更新的字段保持不变
    })

    it('应该处理没有用户信息的情况', () => {
      const store = useUserStore()

      // 没有用户信息时更新
      store.updateUserInfo({
        avatar: 'avatar.png',
      })

      expect(store.userInfo).toBeNull()
    })
  })

  describe('updatePoints - 更新用户积分', () => {
    it('应该成功更新用户积分', () => {
      const store = useUserStore()

      store.setUserInfo({
        id: 1,
        username: 'testuser',
        points: 100,
      })

      store.updatePoints(200)

      expect(store.points).toBe(200)
    })

    it('应该处理没有用户信息的情况', () => {
      const store = useUserStore()

      store.updatePoints(100)

      expect(store.points).toBe(0)
    })
  })

  describe('addPoints - 增加用户积分', () => {
    it('应该成功增加用户积分', () => {
      const store = useUserStore()

      store.setUserInfo({
        id: 1,
        username: 'testuser',
        points: 100,
      })

      store.addPoints(50)

      expect(store.points).toBe(150)
    })

    it('应该支持多次增加积分', () => {
      const store = useUserStore()

      store.setUserInfo({
        id: 1,
        username: 'testuser',
        points: 100,
      })

      store.addPoints(10)
      store.addPoints(20)
      store.addPoints(30)

      expect(store.points).toBe(160)
    })

    it('应该支持增加负数积分', () => {
      const store = useUserStore()

      store.setUserInfo({
        id: 1,
        username: 'testuser',
        points: 100,
      })

      store.addPoints(-20)

      expect(store.points).toBe(80)
    })
  })

  describe('subtractPoints - 减少用户积分', () => {
    it('应该成功减少用户积分', () => {
      const store = useUserStore()

      store.setUserInfo({
        id: 1,
        username: 'testuser',
        points: 100,
      })

      store.subtractPoints(30)

      expect(store.points).toBe(70)
    })

    it('应该防止积分变为负数', () => {
      const store = useUserStore()

      store.setUserInfo({
        id: 1,
        username: 'testuser',
        points: 50,
      })

      store.subtractPoints(100)

      expect(store.points).toBe(0) // 最小为 0
    })

    it('应该处理没有用户信息的情况', () => {
      const store = useUserStore()

      store.subtractPoints(10)

      expect(store.points).toBe(0)
    })
  })

  describe('reset - 重置状态', () => {
    it('应该成功重置所有状态', () => {
      const store = useUserStore()

      // 设置状态
      store.login({
        token: 'jwt-token-123',
        userInfo: {
          id: 1,
          username: 'testuser',
          points: 100,
        },
      })

      // 重置状态
      store.reset()

      expect(store.token).toBe('')
      expect(store.userInfo).toBeNull()
      expect(store.isLoggedIn).toBe(false)
      expect(store.hasToken).toBe(false)
    })

    it('应该可以多次调用重置', () => {
      const store = useUserStore()

      store.reset()
      store.reset()
      store.reset()

      expect(store.token).toBe('')
      expect(store.userInfo).toBeNull()
    })
  })

  describe('持久化', () => {
    it('应该持久化 token、userInfo 和 isLoggedIn', () => {
      const store = useUserStore()

      store.login({
        token: 'jwt-token-123',
        userInfo: {
          id: 1,
          username: 'testuser',
          points: 100,
        },
      })

      // 检查 localStorage
      const storedData = localStorage.getItem('user')
      expect(storedData).toBeTruthy()

      const parsedData = JSON.parse(storedData!)
      expect(parsedData.token).toBe('jwt-token-123')
      expect(parsedData.userInfo).toBeDefined()
      expect(parsedData.isLoggedIn).toBe(true)
    })

    it('应该从 localStorage 恢复状态', () => {
      // 先保存数据到 localStorage
      localStorage.setItem(
        'user',
        JSON.stringify({
          token: 'saved-token',
          userInfo: {
            id: 1,
            username: 'saveduser',
            points: 200,
          },
          isLoggedIn: true,
        })
      )

      // 创建新的 store 实例
      setActivePinia(createPinia())
      const store = useUserStore()

      expect(store.token).toBe('saved-token')
      expect(store.userInfo?.username).toBe('saveduser')
      expect(store.points).toBe(200)
      expect(store.isLoggedIn).toBe(true)
    })
  })

  describe('边界情况', () => {
    it('应该处理空的用户名', () => {
      const store = useUserStore()

      store.setUserInfo({
        id: 1,
        username: '',
        points: 0,
      })

      expect(store.username).toBe('')
    })

    it('应该处理零积分', () => {
      const store = useUserStore()

      store.setUserInfo({
        id: 1,
        username: 'testuser',
        points: 0,
      })

      expect(store.points).toBe(0)

      store.subtractPoints(10)

      expect(store.points).toBe(0)
    })

    it('应该处理非常大的积分', () => {
      const store = useUserStore()

      store.setUserInfo({
        id: 1,
        username: 'testuser',
        points: 0,
      })

      store.addPoints(999999)

      expect(store.points).toBe(999999)
    })

    it('应该处理可选字段为 undefined 的情况', () => {
      const store = useUserStore()

      store.setUserInfo({
        id: 1,
        username: 'testuser',
        points: 100,
      })

      expect(store.avatar).toBe('')
      expect(store.major).toBe('')
      expect(store.grade).toBe('')
      expect(store.studentId).toBe('')
    })
  })

  describe('组合操作', () => {
    it('应该支持登录后更新信息', () => {
      const store = useUserStore()

      store.login({
        token: 'jwt-token-123',
        userInfo: {
          id: 1,
          username: 'testuser',
          points: 100,
        },
      })

      store.updateUserInfo({
        avatar: 'new-avatar.png',
      })

      expect(store.token).toBe('jwt-token-123')
      expect(store.avatar).toBe('new-avatar.png')
    })

    it('应该支持登录后增加积分', () => {
      const store = useUserStore()

      store.login({
        token: 'jwt-token-123',
        userInfo: {
          id: 1,
          username: 'testuser',
          points: 100,
        },
      })

      store.addPoints(50)

      expect(store.points).toBe(150)
      expect(store.isLoggedIn).toBe(true)
    })

    it('应该支持更新后重置', () => {
      const store = useUserStore()

      store.setUserInfo({
        id: 1,
        username: 'testuser',
        points: 100,
      })

      store.updateUserInfo({
        avatar: 'avatar.png',
      })

      store.reset()

      expect(store.userInfo).toBeNull()
      expect(store.isLoggedIn).toBe(false)
    })
  })
})