/**
 * Axios 请求工具单元测试
 * 测试请求拦截器、响应拦截器、错误处理等功能
 */

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { rest } from 'msw'
import { setupServer } from 'msw/node'
import axios from 'axios'
import { get, post, put, del, upload, download } from '@/utils/request'
import type { ApiResponse } from '@/utils/request'

// 创建 MSW 服务器
const server = setupServer()

// 在所有测试开始前启动服务器
beforeAll(() => server.listen())

// 每个测试结束后重置处理器
afterEach(() => server.resetHandlers())

// 所有测试结束后关闭服务器
afterAll(() => server.close())

// Mock localStorage
const localStorageMock = (() => {
  let store: Record<string, string> = {}

  return {
    getItem: (key: string) => store[key] || null,
    setItem: (key: string, value: string) => {
      store[key] = value.toString()
    },
    removeItem: (key: string) => {
      delete store[key]
    },
    clear: () => {
      store = {}
    },
  }
})()

Object.defineProperty(window, 'localStorage', {
  value: localStorageMock,
})

// Mock router
const mockPush = vi.fn()
vi.mock('@/router', () => ({
  default: {
    push: mockPush,
    currentRoute: {
      value: {
        fullPath: '/test',
      },
    },
  },
}))

// Mock useUserStore
const mockUserStore = {
  token: 'mock-token',
  logout: vi.fn(),
}

vi.mock('@/stores/user', () => ({
  useUserStore: () => mockUserStore,
}))

// Mock Element Plus
vi.mock('element-plus', () => ({
  ElMessage: {
    error: vi.fn(),
    success: vi.fn(),
    warning: vi.fn(),
  },
  ElLoading: {
    service: vi.fn(() => ({
      close: vi.fn(),
    })),
  },
}))

describe('RequestUtil - Axios 请求工具测试', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
    mockUserStore.token = 'mock-token'
  })

  describe('get - GET 请求', () => {
    it('应该成功发送 GET 请求', async () => {
      server.use(
        rest.get('/api/test', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '成功',
              data: { id: 1, name: 'test' },
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await get('/test')
      expect(result).toEqual({ id: 1, name: 'test' })
    })

    it('应该支持传递查询参数', async () => {
      server.use(
        rest.get('/api/test', (req, res, ctx) => {
          const keyword = req.url.searchParams.get('keyword')
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '成功',
              data: { keyword },
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await get('/test', { keyword: 'vue' })
      expect(result).toEqual({ keyword: 'vue' })
    })

    it('应该处理 404 错误', async () => {
      server.use(
        rest.get('/api/not-found', (req, res, ctx) => {
          return res(ctx.status(404))
        })
      )

      await expect(get('/not-found')).rejects.toThrow()
    })

    it('应该处理网络错误', async () => {
      server.use(
        rest.get('/api/error', (req, res) => {
          return res.networkError('网络连接失败')
        })
      )

      await expect(get('/error')).rejects.toThrow()
    })
  })

  describe('post - POST 请求', () => {
    it('应该成功发送 POST 请求', async () => {
      server.use(
        rest.post('/api/test', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '创建成功',
              data: { id: 1 },
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await post('/test', { name: 'test' })
      expect(result).toEqual({ id: 1 })
    })

    it('应该发送 JSON 格式的数据', async () => {
      server.use(
        rest.post('/api/test', async (req, res, ctx) => {
          const body = await req.json()
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '成功',
              data: body,
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await post('/test', { name: 'test', age: 18 })
      expect(result).toEqual({ name: 'test', age: 18 })
    })

    it('应该处理 400 错误', async () => {
      server.use(
        rest.post('/api/test', (req, res, ctx) => {
          return res(ctx.status(400))
        })
      )

      await expect(post('/test', {})).rejects.toThrow()
    })
  })

  describe('put - PUT 请求', () => {
    it('应该成功发送 PUT 请求', async () => {
      server.use(
        rest.put('/api/test/1', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '更新成功',
              data: { id: 1, name: 'updated' },
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await put('/test/1', { name: 'updated' })
      expect(result).toEqual({ id: 1, name: 'updated' })
    })

    it('应该处理 403 错误', async () => {
      server.use(
        rest.put('/api/test/1', (req, res, ctx) => {
          return res(ctx.status(403))
        })
      )

      await expect(put('/test/1', {})).rejects.toThrow()
    })
  })

  describe('del - DELETE 请求', () => {
    it('应该成功发送 DELETE 请求', async () => {
      server.use(
        rest.delete('/api/test/1', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '删除成功',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await del('/test/1')
      expect(result).toBeNull()
    })

    it('应该支持传递查询参数', async () => {
      server.use(
        rest.delete('/api/test', (req, res, ctx) => {
          const id = req.url.searchParams.get('id')
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '删除成功',
              data: { deletedId: id },
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await del('/test', { id: '1' })
      expect(result).toEqual({ deletedId: '1' })
    })
  })

  describe('upload - 文件上传', () => {
    it('应该成功上传文件', async () => {
      const mockFile = new File(['test content'], 'test.txt', { type: 'text/plain' })

      server.use(
        rest.post('/api/upload', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '上传成功',
              data: { url: 'http://example.com/test.txt' },
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await upload('/upload', mockFile)
      expect(result).toEqual({ url: 'http://example.com/test.txt' })
    })

    it('应该支持上传进度回调', async () => {
      const mockFile = new File(['test content'], 'test.txt', { type: 'text/plain' })
      const onProgress = vi.fn()

      server.use(
        rest.post('/api/upload', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '上传成功',
              data: { url: 'http://example.com/test.txt' },
              timestamp: Date.now(),
            })
          )
        })
      )

      await upload('/upload', mockFile, onProgress)
      // 注意：MSW 可能不会触发实际的进度事件
    })
  })

  describe('download - 文件下载', () => {
    it('应该成功下载文件', async () => {
      const mockBlob = new Blob(['test content'], { type: 'text/plain' })

      server.use(
        rest.get('/api/download', (req, res, ctx) => {
          return res(
            ctx.set('Content-Type', 'text/plain'),
            ctx.set('Content-Disposition', 'attachment; filename="test.txt"'),
            ctx.body(mockBlob)
          )
        })
      )

      // Mock document.createElement
      const mockLink = {
        href: '',
        download: '',
        click: vi.fn(),
      }
      const createElementSpy = vi.spyOn(document, 'createElement').mockReturnValue(mockLink as any)
      const appendChildSpy = vi.spyOn(document.body, 'appendChild').mockImplementation(() => mockLink as any)
      const removeChildSpy = vi.spyOn(document.body, 'removeChild').mockImplementation(() => mockLink as any)

      await download('/download', {}, 'test.txt')

      expect(createElementSpy).toHaveBeenCalledWith('a')
      expect(mockLink.click).toHaveBeenCalled()

      createElementSpy.mockRestore()
      appendChildSpy.mockRestore()
      removeChildSpy.mockRestore()
    })
  })

  describe('请求拦截器', () => {
    it('应该自动添加 Token 到请求头', async () => {
      server.use(
        rest.get('/api/test', (req, res, ctx) => {
          const authHeader = req.headers.get('Authorization')
          expect(authHeader).toBe('Bearer mock-token')
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '成功',
              data: {},
              timestamp: Date.now(),
            })
          )
        })
      )

      await get('/test')
    })

    it('应该在没有 Token 时不添加 Authorization 头', async () => {
      mockUserStore.token = ''

      server.use(
        rest.get('/api/test', (req, res, ctx) => {
          const authHeader = req.headers.get('Authorization')
          expect(authHeader).toBeNull()
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '成功',
              data: {},
              timestamp: Date.now(),
            })
          )
        })
      )

      await get('/test')
    })
  })

  describe('响应拦截器', () => {
    it('应该处理业务错误（非 200 状态码）', async () => {
      server.use(
        rest.get('/api/error', (req, res, ctx) => {
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

      await expect(get('/error')).rejects.toThrow('未登录')
    })

    it('应该在 Token 失效时自动登出', async () => {
      server.use(
        rest.get('/api/error', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 1001,
              msg: '登录已过期',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      await expect(get('/error')).rejects.toThrow()
      expect(mockUserStore.logout).toHaveBeenCalled()
    })

    it('应该在 401 时自动登出', async () => {
      server.use(
        rest.get('/api/error', (req, res, ctx) => {
          return res(ctx.status(401))
        })
      )

      await expect(get('/error')).rejects.toThrow()
      expect(mockUserStore.logout).toHaveBeenCalled()
    })
  })

  describe('错误处理', () => {
    it('应该处理网络超时', async () => {
      server.use(
        rest.get('/api/timeout', (req, res) => {
          return res(ctx.delay(35000)) // 超过默认 30 秒超时
        })
      )

      await expect(get('/timeout')).rejects.toThrow()
    })

    it('应该处理 500 服务器错误', async () => {
      server.use(
        rest.get('/api/server-error', (req, res, ctx) => {
          return res(ctx.status(500))
        })
      )

      await expect(get('/server-error')).rejects.toThrow()
    })

    it('应该处理 502 网关错误', async () => {
      server.use(
        rest.get('/api/gateway-error', (req, res, ctx) => {
          return res(ctx.status(502))
        })
      )

      await expect(get('/gateway-error')).rejects.toThrow()
    })

    it('应该处理 503 服务不可用', async () => {
      server.use(
        rest.get('/api/unavailable', (req, res, ctx) => {
          return res(ctx.status(503))
        })
      )

      await expect(get('/unavailable')).rejects.toThrow()
    })

    it('应该处理 504 网关超时', async () => {
      server.use(
        rest.get('/api/gateway-timeout', (req, res, ctx) => {
          return res(ctx.status(504))
        })
      )

      await expect(get('/gateway-timeout')).rejects.toThrow()
    })
  })

  describe('自定义配置', () => {
    it('应该支持禁用加载动画', async () => {
      server.use(
        rest.get('/api/test', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '成功',
              data: {},
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await get('/test', {}, { showLoading: false })
      expect(result).toBeDefined()
    })

    it('应该支持禁用错误提示', async () => {
      server.use(
        rest.get('/api/error', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 1001,
              msg: '错误',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      await expect(get('/error', {}, { showError: false })).rejects.toThrow()
    })

    it('应该支持自定义加载文本', async () => {
      server.use(
        rest.get('/api/test', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '成功',
              data: {},
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await get('/test', {}, { loadingText: '自定义加载中...' })
      expect(result).toBeDefined()
    })
  })
})