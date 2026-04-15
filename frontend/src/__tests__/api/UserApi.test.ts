/**
 * 用户 API 单元测试
 * 测试用户相关的 API 接口
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { rest } from 'msw'
import { setupServer } from 'msw/node'
import {
  register,
  login,
  getCurrentUser,
  getUserById,
  getUserByUsername,
  updateUser,
  updatePassword,
  checkUsername,
  checkStudentId,
  logout,
  refreshToken,
  verifyEmail,
  resendVerificationEmail,
  getUserStats,
} from '@/api/user'
import type { RegisterDTO, LoginDTO, UserUpdateDTO, UserVO, LoginVO } from '@/api/user'

// 创建 MSW 服务器
const server = setupServer()

// 在所有测试开始前启动服务器
beforeAll(() => server.listen())

// 每个测试结束后重置处理器
afterEach(() => server.resetHandlers())

// 所有测试结束后关闭服务器
afterAll(() => server.close())

describe('UserApi - 用户 API 测试', () => {
  beforeEach(() => {
    // 清除 localStorage
    localStorage.clear()
  })

  describe('register - 用户注册', () => {
    it('应该成功注册新用户', async () => {
      // 模拟注册成功响应
      server.use(
        rest.post('/api/user/register', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '注册成功',
              data: {
                id: 1,
                username: 'testuser',
                email: 'test@example.com',
                points: 0,
                questionCount: 0,
                answerCount: 0,
                collectionCount: 0,
                status: 1,
                createTime: '2024-01-01T00:00:00',
                updateTime: '2024-01-01T00:00:00',
              },
              timestamp: Date.now(),
            })
          )
        })
      )

      const registerData: RegisterDTO = {
        username: 'testuser',
        password: 'password123',
        email: 'test@example.com',
        studentId: '20240001',
        realName: 'Test User',
      }

      const result = await register(registerData)
      expect(result).toBeDefined()
      expect(result.id).toBe(1)
      expect(result.username).toBe('testuser')
      expect(result.email).toBe('test@example.com')
    })

    it('应该处理用户名已存在的情况', async () => {
      server.use(
        rest.post('/api/user/register', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 1005,
              msg: '用户名已存在',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      const registerData: RegisterDTO = {
        username: 'existinguser',
        password: 'password123',
        email: 'test@example.com',
      }

      await expect(register(registerData)).rejects.toThrow('用户名已存在')
    })

    it('应该处理邮箱已存在的情况', async () => {
      server.use(
        rest.post('/api/user/register', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 1006,
              msg: '邮箱已被注册',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      const registerData: RegisterDTO = {
        username: 'newuser',
        password: 'password123',
        email: 'existing@example.com',
      }

      await expect(register(registerData)).rejects.toThrow('邮箱已被注册')
    })
  })

  describe('login - 用户登录', () => {
    it('应该成功登录', async () => {
      server.use(
        rest.post('/api/user/login', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '登录成功',
              data: {
                token: 'mock-jwt-token',
                tokenType: 'Bearer',
                expiresIn: 604800,
                userInfo: {
                  id: 1,
                  username: 'testuser',
                  email: 'test@example.com',
                  avatar: 'avatar.png',
                  points: 100,
                  questionCount: 5,
                  answerCount: 10,
                  collectionCount: 3,
                  status: 1,
                  createTime: '2024-01-01T00:00:00',
                  updateTime: '2024-01-01T00:00:00',
                },
              },
              timestamp: Date.now(),
            })
          )
        })
      )

      const loginData: LoginDTO = {
        username: 'testuser',
        password: 'password123',
      }

      const result = await login(loginData)
      expect(result).toBeDefined()
      expect(result.token).toBe('mock-jwt-token')
      expect(result.userInfo.username).toBe('testuser')
      expect(result.userInfo.points).toBe(100)
    })

    it('应该处理用户名或密码错误的情况', async () => {
      server.use(
        rest.post('/api/user/login', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 1007,
              msg: '用户名或密码错误',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      const loginData: LoginDTO = {
        username: 'wronguser',
        password: 'wrongpassword',
      }

      await expect(login(loginData)).rejects.toThrow('用户名或密码错误')
    })

    it('应该处理账号未激活的情况', async () => {
      server.use(
        rest.post('/api/user/login', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 1008,
              msg: '账号未激活，请先验证邮箱',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      const loginData: LoginDTO = {
        username: 'inactiveuser',
        password: 'password123',
      }

      await expect(login(loginData)).rejects.toThrow('账号未激活，请先验证邮箱')
    })
  })

  describe('getCurrentUser - 获取当前用户信息', () => {
    it('应该成功获取当前登录用户信息', async () => {
      server.use(
        rest.get('/api/user/info', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '获取成功',
              data: {
                id: 1,
                username: 'testuser',
                email: 'test@example.com',
                avatar: 'avatar.png',
                bio: '这是我的简介',
                school: '清华大学',
                major: '计算机科学',
                grade: '2024级',
                studentId: '20240001',
                realName: 'Test User',
                points: 150,
                questionCount: 10,
                answerCount: 20,
                collectionCount: 5,
                status: 1,
                createTime: '2024-01-01T00:00:00',
                updateTime: '2024-01-02T00:00:00',
              },
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await getCurrentUser()
      expect(result).toBeDefined()
      expect(result.id).toBe(1)
      expect(result.username).toBe('testuser')
      expect(result.school).toBe('清华大学')
      expect(result.major).toBe('计算机科学')
      expect(result.points).toBe(150)
    })

    it('应该处理未登录的情况', async () => {
      server.use(
        rest.get('/api/user/info', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 1001,
              msg: '未登录',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      await expect(getCurrentUser()).rejects.toThrow('未登录')
    })
  })

  describe('getUserById - 根据 ID 获取用户信息', () => {
    it('应该成功获取指定用户信息', async () => {
      server.use(
        rest.get('/api/user/2', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '获取成功',
              data: {
                id: 2,
                username: 'otheruser',
                email: 'other@example.com',
                avatar: 'other-avatar.png',
                bio: '另一个用户',
                points: 200,
                questionCount: 15,
                answerCount: 30,
                collectionCount: 10,
                status: 1,
                createTime: '2024-01-01T00:00:00',
                updateTime: '2024-01-02T00:00:00',
              },
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await getUserById(2)
      expect(result).toBeDefined()
      expect(result.id).toBe(2)
      expect(result.username).toBe('otheruser')
      expect(result.points).toBe(200)
    })

    it('应该处理用户不存在的情况', async () => {
      server.use(
        rest.get('/api/user/999', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 1009,
              msg: '用户不存在',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      await expect(getUserById(999)).rejects.toThrow('用户不存在')
    })
  })

  describe('getUserByUsername - 根据用户名获取用户信息', () => {
    it('应该成功获取指定用户名的用户信息', async () => {
      server.use(
        rest.get('/api/user/username/anotheruser', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '获取成功',
              data: {
                id: 3,
                username: 'anotheruser',
                email: 'another@example.com',
                avatar: 'another-avatar.png',
                points: 300,
                questionCount: 20,
                answerCount: 40,
                collectionCount: 15,
                status: 1,
                createTime: '2024-01-01T00:00:00',
                updateTime: '2024-01-02T00:00:00',
              },
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await getUserByUsername('anotheruser')
      expect(result).toBeDefined()
      expect(result.username).toBe('anotheruser')
      expect(result.points).toBe(300)
    })
  })

  describe('updateUser - 更新用户信息', () => {
    it('应该成功更新用户信息', async () => {
      server.use(
        rest.put('/api/user/update', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '更新成功',
              data: {
                id: 1,
                username: 'testuser',
                email: 'test@example.com',
                avatar: 'new-avatar.png',
                bio: '更新后的简介',
                school: '北京大学',
                major: '软件工程',
                grade: '2023级',
                points: 150,
                questionCount: 10,
                answerCount: 20,
                collectionCount: 5,
                status: 1,
                createTime: '2024-01-01T00:00:00',
                updateTime: '2024-01-03T00:00:00',
              },
              timestamp: Date.now(),
            })
          )
        })
      )

      const updateData: UserUpdateDTO = {
        avatar: 'new-avatar.png',
        bio: '更新后的简介',
        school: '北京大学',
        major: '软件工程',
        grade: '2023级',
      }

      const result = await updateUser(updateData)
      expect(result).toBeDefined()
      expect(result.avatar).toBe('new-avatar.png')
      expect(result.bio).toBe('更新后的简介')
      expect(result.school).toBe('北京大学')
    })
  })

  describe('updatePassword - 修改密码', () => {
    it('应该成功修改密码', async () => {
      server.use(
        rest.put('/api/user/password', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '密码修改成功',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await updatePassword('oldpass123', 'newpass456')
      expect(result).toBeUndefined()
    })

    it('应该处理原密码错误的情况', async () => {
      server.use(
        rest.put('/api/user/password', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 1010,
              msg: '原密码错误',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      await expect(
        updatePassword('wrongoldpass', 'newpass456')
      ).rejects.toThrow('原密码错误')
    })
  })

  describe('checkUsername - 检查用户名是否存在', () => {
    it('应该返回 false 表示用户名可用', async () => {
      server.use(
        rest.get('/api/user/check/username', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '检查成功',
              data: false,
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await checkUsername('availableusername')
      expect(result).toBe(false)
    })

    it('应该返回 true 表示用户名已存在', async () => {
      server.use(
        rest.get('/api/user/check/username', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '检查成功',
              data: true,
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await checkUsername('existingusername')
      expect(result).toBe(true)
    })
  })

  describe('checkStudentId - 检查学号是否存在', () => {
    it('应该返回 false 表示学号可用', async () => {
      server.use(
        rest.get('/api/user/check/studentId', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '检查成功',
              data: false,
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await checkStudentId('20249999')
      expect(result).toBe(false)
    })

    it('应该返回 true 表示学号已存在', async () => {
      server.use(
        rest.get('/api/user/check/studentId', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '检查成功',
              data: true,
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await checkStudentId('20240001')
      expect(result).toBe(true)
    })
  })

  describe('logout - 用户登出', () => {
    it('应该成功登出', async () => {
      server.use(
        rest.post('/api/user/logout', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '登出成功',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await logout()
      expect(result).toBeUndefined()
    })
  })

  describe('refreshToken - 刷新 Token', () => {
    it('应该成功刷新 Token', async () => {
      server.use(
        rest.post('/api/user/refresh-token', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '刷新成功',
              data: {
                token: 'new-mock-jwt-token',
                tokenType: 'Bearer',
                expiresIn: 604800,
                userInfo: {
                  id: 1,
                  username: 'testuser',
                  email: 'test@example.com',
                  points: 150,
                  questionCount: 10,
                  answerCount: 20,
                  collectionCount: 5,
                  status: 1,
                  createTime: '2024-01-01T00:00:00',
                  updateTime: '2024-01-02T00:00:00',
                },
              },
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await refreshToken()
      expect(result).toBeDefined()
      expect(result.token).toBe('new-mock-jwt-token')
    })
  })

  describe('verifyEmail - 验证邮箱', () => {
    it('应该成功验证邮箱', async () => {
      server.use(
        rest.post('/api/user/verify-email', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '邮箱验证成功',
              data: true,
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await verifyEmail('verification-token-123')
      expect(result).toBe(true)
    })

    it('应该处理验证码无效的情况', async () => {
      server.use(
        rest.post('/api/user/verify-email', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 1011,
              msg: '验证码无效或已过期',
              data: false,
              timestamp: Date.now(),
            })
          )
        })
      )

      await expect(verifyEmail('invalid-token')).rejects.toThrow('验证码无效或已过期')
    })
  })

  describe('resendVerificationEmail - 重新发送验证邮件', () => {
    it('应该成功重新发送验证邮件', async () => {
      server.use(
        rest.post('/api/user/resend-verification-email', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '验证邮件已发送',
              data: true,
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await resendVerificationEmail('test@example.com')
      expect(result).toBe(true)
    })
  })

  describe('getUserStats - 获取用户统计信息', () => {
    it('应该成功获取用户统计信息', async () => {
      server.use(
        rest.get('/api/user/1/stats', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '获取成功',
              data: {
                questionCount: 10,
                answerCount: 20,
                collectionCount: 5,
                likeCount: 30,
                points: 150,
              },
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await getUserStats(1)
      expect(result).toBeDefined()
      expect(result.questionCount).toBe(10)
      expect(result.answerCount).toBe(20)
      expect(result.collectionCount).toBe(5)
      expect(result.likeCount).toBe(30)
      expect(result.points).toBe(150)
    })
  })
})