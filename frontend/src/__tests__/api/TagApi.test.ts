/**
 * 标签 API 单元测试
 * 测试标签相关的 API 接口
 */

import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import { rest } from 'msw'
import { setupServer } from 'msw/node'
import {
  createTag,
  updateTag,
  deleteTag,
  getTagById,
  getTagList,
  getAllTags,
  getHotTags,
  searchTags,
  followTag,
  unfollowTag,
  checkFollowed,
  getFollowedTags,
  getMyFollowedTags,
  getTagQuestionCount,
} from '@/api/tag'
import type {
  TagCreateDTO,
  TagUpdateDTO,
  TagVO,
  PageResponse,
} from '@/api/tag'

// 创建 MSW 服务器
const server = setupServer()

// 在所有测试开始前启动服务器
beforeAll(() => server.listen())

// 每个测试结束后重置处理器
afterEach(() => server.resetHandlers())

// 所有测试结束后关闭服务器
afterAll(() => server.close())

// 创建模拟标签数据
const mockTag: TagVO = {
  id: 1,
  name: '计算机科学',
  description: '计算机科学相关话题',
  icon: 'computer',
  color: '#0056D2',
  questionCount: 100,
  followCount: 50,
  createTime: '2024-01-01T00:00:00',
  updateTime: '2024-01-02T00:00:00',
}

// 创建模拟分页响应数据
const mockPageResponse: PageResponse<TagVO> = {
  list: [mockTag],
  total: 1,
  page: 1,
  size: 50,
  pages: 1,
}

describe('TagApi - 标签 API 测试', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  describe('createTag - 创建标签', () => {
    it('应该成功创建新标签', async () => {
      server.use(
        rest.post('/api/tag/create', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '创建成功',
              data: mockTag,
              timestamp: Date.now(),
            })
          )
        })
      )

      const createData: TagCreateDTO = {
        name: '计算机科学',
        description: '计算机科学相关话题',
        icon: 'computer',
        color: '#0056D2',
      }

      const result = await createTag(createData)
      expect(result).toBeDefined()
      expect(result.id).toBe(1)
      expect(result.name).toBe('计算机科学')
      expect(result.icon).toBe('computer')
    })

    it('应该处理标签名已存在的情况', async () => {
      server.use(
        rest.post('/api/tag/create', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 7001,
              msg: '标签名已存在',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      const createData: TagCreateDTO = {
        name: '已存在的标签',
      }

      await expect(createTag(createData)).rejects.toThrow('标签名已存在')
    })

    it('应该处理标签名为空的情况', async () => {
      server.use(
        rest.post('/api/tag/create', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 7002,
              msg: '标签名不能为空',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      const createData: TagCreateDTO = {
        name: '',
      }

      await expect(createTag(createData)).rejects.toThrow('标签名不能为空')
    })
  })

  describe('updateTag - 更新标签', () => {
    it('应该成功更新标签', async () => {
      server.use(
        rest.put('/api/tag/1', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '更新成功',
              data: {
                ...mockTag,
                description: '更新后的描述',
                color: '#FF6B6B',
                updateTime: '2024-01-03T00:00:00',
              },
              timestamp: Date.now(),
            })
          )
        })
      )

      const updateData: TagUpdateDTO = {
        description: '更新后的描述',
        color: '#FF6B6B',
      }

      const result = await updateTag(1, updateData)
      expect(result).toBeDefined()
      expect(result.description).toBe('更新后的描述')
      expect(result.color).toBe('#FF6B6B')
    })

    it('应该处理标签不存在的情况', async () => {
      server.use(
        rest.put('/api/tag/999', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 7003,
              msg: '标签不存在',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      await expect(updateTag(999, { name: '新名称' })).rejects.toThrow('标签不存在')
    })
  })

  describe('deleteTag - 删除标签', () => {
    it('应该成功删除标签', async () => {
      server.use(
        rest.delete('/api/tag/1', (req, res, ctx) => {
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

      const result = await deleteTag(1)
      expect(result).toBeUndefined()
    })

    it('应该处理标签下有问题的情况', async () => {
      server.use(
        rest.delete('/api/tag/1', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 7004,
              msg: '该标签下有问题，无法删除',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      await expect(deleteTag(1)).rejects.toThrow('该标签下有问题，无法删除')
    })
  })

  describe('getTagById - 获取标签详情', () => {
    it('应该成功获取标签详情', async () => {
      server.use(
        rest.get('/api/tag/1', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '获取成功',
              data: mockTag,
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await getTagById(1)
      expect(result).toBeDefined()
      expect(result.id).toBe(1)
      expect(result.name).toBe('计算机科学')
    })

    it('应该处理标签不存在的情况', async () => {
      server.use(
        rest.get('/api/tag/999', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 7003,
              msg: '标签不存在',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      await expect(getTagById(999)).rejects.toThrow('标签不存在')
    })
  })

  describe('getTagList - 获取标签列表', () => {
    it('应该成功获取标签列表（分页）', async () => {
      server.use(
        rest.get('/api/tag/list', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '获取成功',
              data: mockPageResponse,
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await getTagList(1, 50)
      expect(result).toBeDefined()
      expect(result.list).toHaveLength(1)
      expect(result.total).toBe(1)
    })

    it('应该支持不同的分页参数', async () => {
      server.use(
        rest.get('/api/tag/list', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '获取成功',
              data: {
                ...mockPageResponse,
                total: 100,
                pages: 2,
              },
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await getTagList(2, 50)
      expect(result).toBeDefined()
      expect(result.total).toBe(100)
      expect(result.pages).toBe(2)
    })
  })

  describe('getAllTags - 获取所有标签', () => {
    it('应该成功获取所有标签（不分页）', async () => {
      server.use(
        rest.get('/api/tag/all', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '获取成功',
              data: [mockTag],
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await getAllTags()
      expect(result).toBeDefined()
      expect(result).toHaveLength(1)
      expect(result[0].id).toBe(1)
    })

    it('应该返回空数组当没有标签时', async () => {
      server.use(
        rest.get('/api/tag/all', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '获取成功',
              data: [],
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await getAllTags()
      expect(result).toHaveLength(0)
    })
  })

  describe('getHotTags - 获取热门标签', () => {
    it('应该成功获取热门标签', async () => {
      server.use(
        rest.get('/api/tag/hot', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '获取成功',
              data: [mockTag],
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await getHotTags(20)
      expect(result).toBeDefined()
      expect(result).toHaveLength(1)
    })

    it('应该支持限制数量', async () => {
      server.use(
        rest.get('/api/tag/hot', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '获取成功',
              data: Array(10).fill(mockTag).map((tag, index) => ({
                ...tag,
                id: index + 1,
                name: `标签${index + 1}`,
              })),
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await getHotTags(10)
      expect(result).toHaveLength(10)
    })
  })

  describe('searchTags - 搜索标签', () => {
    it('应该成功搜索标签', async () => {
      server.use(
        rest.get('/api/tag/search', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '搜索成功',
              data: mockPageResponse,
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await searchTags('计算机', 1, 20)
      expect(result).toBeDefined()
      expect(result.list).toHaveLength(1)
    })

    it('应该处理搜索无结果的情况', async () => {
      server.use(
        rest.get('/api/tag/search', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '搜索成功',
              data: {
                list: [],
                total: 0,
                page: 1,
                size: 20,
                pages: 0,
              },
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await searchTags('不存在的标签', 1, 20)
      expect(result.list).toHaveLength(0)
      expect(result.total).toBe(0)
    })
  })

  describe('followTag - 关注标签', () => {
    it('应该成功关注标签', async () => {
      server.use(
        rest.post('/api/tag/1/follow', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '关注成功',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await followTag(1)
      expect(result).toBeUndefined()
    })

    it('应该处理已关注的情况', async () => {
      server.use(
        rest.post('/api/tag/1/follow', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 7005,
              msg: '已经关注过了',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      await expect(followTag(1)).rejects.toThrow('已经关注过了')
    })

    it('应该处理标签不存在的情况', async () => {
      server.use(
        rest.post('/api/tag/999/follow', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 7003,
              msg: '标签不存在',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      await expect(followTag(999)).rejects.toThrow('标签不存在')
    })
  })

  describe('unfollowTag - 取消关注标签', () => {
    it('应该成功取消关注标签', async () => {
      server.use(
        rest.delete('/api/tag/1/follow', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '取消关注成功',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await unfollowTag(1)
      expect(result).toBeUndefined()
    })

    it('应该处理未关注的情况', async () => {
      server.use(
        rest.delete('/api/tag/1/follow', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 7006,
              msg: '未关注该标签',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      await expect(unfollowTag(1)).rejects.toThrow('未关注该标签')
    })
  })

  describe('checkFollowed - 检查是否已关注标签', () => {
    it('应该返回 true 表示已关注', async () => {
      server.use(
        rest.get('/api/tag/1/followed', (req, res, ctx) => {
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

      const result = await checkFollowed(1)
      expect(result).toBe(true)
    })

    it('应该返回 false 表示未关注', async () => {
      server.use(
        rest.get('/api/tag/1/followed', (req, res, ctx) => {
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

      const result = await checkFollowed(1)
      expect(result).toBe(false)
    })
  })

  describe('getFollowedTags - 获取用户关注的标签列表', () => {
    it('应该成功获取用户关注的标签列表', async () => {
      server.use(
        rest.get('/api/tag/user/1/followed', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '获取成功',
              data: [mockTag],
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await getFollowedTags(1)
      expect(result).toBeDefined()
      expect(result).toHaveLength(1)
    })

    it('应该返回空数组当用户没有关注任何标签时', async () => {
      server.use(
        rest.get('/api/tag/user/1/followed', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '获取成功',
              data: [],
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await getFollowedTags(1)
      expect(result).toHaveLength(0)
    })
  })

  describe('getMyFollowedTags - 获取我关注的标签列表', () => {
    it('应该成功获取我关注的标签列表', async () => {
      server.use(
        rest.get('/api/tag/my/followed', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '获取成功',
              data: [mockTag],
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await getMyFollowedTags()
      expect(result).toBeDefined()
      expect(result).toHaveLength(1)
    })

    it('应该处理未登录的情况', async () => {
      server.use(
        rest.get('/api/tag/my/followed', (req, res, ctx) => {
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

      await expect(getMyFollowedTags()).rejects.toThrow('未登录')
    })
  })

  describe('getTagQuestionCount - 获取标签下的问题数量', () => {
    it('应该成功获取标签下的问题数量', async () => {
      server.use(
        rest.get('/api/tag/1/question-count', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '获取成功',
              data: 100,
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await getTagQuestionCount(1)
      expect(result).toBe(100)
    })

    it('应该返回 0 当标签下没有问题时', async () => {
      server.use(
        rest.get('/api/tag/1/question-count', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '获取成功',
              data: 0,
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await getTagQuestionCount(1)
      expect(result).toBe(0)
    })
  })
})