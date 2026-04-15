/**
 * 回答 API 单元测试
 * 测试回答相关的 API 接口
 */

import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import { rest } from 'msw'
import { setupServer } from 'msw/node'
import {
  publishAnswer,
  updateAnswer,
  deleteAnswer,
  getAnswerById,
  getAnswersByQuestion,
  getAnswersByUser,
  getMyAnswers,
  acceptAnswer,
  cancelAcceptAnswer,
  searchAnswers,
} from '@/api/answer'
import type {
  AnswerPublishDTO,
  AnswerUpdateDTO,
  AnswerVO,
  PageResponse,
} from '@/api/answer'

// 创建 MSW 服务器
const server = setupServer()

// 在所有测试开始前启动服务器
beforeAll(() => server.listen())

// 每个测试结束后重置处理器
afterEach(() => server.resetHandlers())

// 所有测试结束后关闭服务器
afterAll(() => server.close())

// 创建模拟回答数据
const mockAnswer: AnswerVO = {
  id: 1,
  questionId: 1,
  questionTitle: '如何学习 Vue 3？',
  userId: 1,
  username: 'testuser',
  userAvatar: 'avatar.png',
  content: 'Vue 3 是一个渐进式 JavaScript 框架，建议从官方文档开始学习...',
  likeCount: 10,
  commentCount: 5,
  isAccepted: 0,
  status: 1,
  createTime: '2024-01-01T00:00:00',
  updateTime: '2024-01-02T00:00:00',
  userPoints: 150,
}

// 创建模拟分页响应数据
const mockPageResponse: PageResponse<AnswerVO> = {
  list: [mockAnswer],
  total: 1,
  page: 1,
  size: 20,
  pages: 1,
}

describe('AnswerApi - 回答 API 测试', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  describe('publishAnswer - 发布回答', () => {
    it('应该成功发布新回答', async () => {
      server.use(
        rest.post('/api/answer/publish', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '发布成功',
              data: mockAnswer,
              timestamp: Date.now(),
            })
          )
        })
      )

      const publishData: AnswerPublishDTO = {
        questionId: 1,
        content: 'Vue 3 是一个渐进式 JavaScript 框架，建议从官方文档开始学习...',
      }

      const result = await publishAnswer(publishData)
      expect(result).toBeDefined()
      expect(result.id).toBe(1)
      expect(result.questionId).toBe(1)
      expect(result.content).toContain('Vue 3')
    })

    it('应该处理问题不存在的情况', async () => {
      server.use(
        rest.post('/api/answer/publish', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 3001,
              msg: '问题不存在',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      const publishData: AnswerPublishDTO = {
        questionId: 999,
        content: '这是我的回答',
      }

      await expect(publishAnswer(publishData)).rejects.toThrow('问题不存在')
    })

    it('应该处理回答内容为空的情况', async () => {
      server.use(
        rest.post('/api/answer/publish', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 3002,
              msg: '回答内容不能为空',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      const publishData: AnswerPublishDTO = {
        questionId: 1,
        content: '',
      }

      await expect(publishAnswer(publishData)).rejects.toThrow('回答内容不能为空')
    })
  })

  describe('updateAnswer - 更新回答', () => {
    it('应该成功更新回答', async () => {
      server.use(
        rest.put('/api/answer/1', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '更新成功',
              data: {
                ...mockAnswer,
                content: 'Vue 3 是一个渐进式 JavaScript 框架，建议从官方文档开始学习，然后多实践...',
                updateTime: '2024-01-03T00:00:00',
              },
              timestamp: Date.now(),
            })
          )
        })
      )

      const updateData: AnswerUpdateDTO = {
        content: 'Vue 3 是一个渐进式 JavaScript 框架，建议从官方文档开始学习，然后多实践...',
      }

      const result = await updateAnswer(1, updateData)
      expect(result).toBeDefined()
      expect(result.content).toContain('然后多实践')
    })

    it('应该处理回答不存在的情况', async () => {
      server.use(
        rest.put('/api/answer/999', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 3003,
              msg: '回答不存在',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      await expect(updateAnswer(999, { content: '新内容' })).rejects.toThrow('回答不存在')
    })

    it('应该处理更新他人回答的情况', async () => {
      server.use(
        rest.put('/api/answer/2', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 3004,
              msg: '无权修改该回答',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      await expect(updateAnswer(2, { content: '新内容' })).rejects.toThrow('无权修改该回答')
    })
  })

  describe('deleteAnswer - 删除回答', () => {
    it('应该成功删除回答', async () => {
      server.use(
        rest.delete('/api/answer/1', (req, res, ctx) => {
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

      const result = await deleteAnswer(1)
      expect(result).toBeUndefined()
    })

    it('应该处理删除他人回答的情况', async () => {
      server.use(
        rest.delete('/api/answer/2', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 3005,
              msg: '无权删除该回答',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      await expect(deleteAnswer(2)).rejects.toThrow('无权删除该回答')
    })
  })

  describe('getAnswerById - 获取回答详情', () => {
    it('应该成功获取回答详情', async () => {
      server.use(
        rest.get('/api/answer/1', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '获取成功',
              data: mockAnswer,
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await getAnswerById(1)
      expect(result).toBeDefined()
      expect(result.id).toBe(1)
      expect(result.questionTitle).toBe('如何学习 Vue 3？')
    })

    it('应该处理回答不存在的情况', async () => {
      server.use(
        rest.get('/api/answer/999', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 3003,
              msg: '回答不存在',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      await expect(getAnswerById(999)).rejects.toThrow('回答不存在')
    })
  })

  describe('getAnswersByQuestion - 获取问题的回答列表', () => {
    it('应该成功获取问题的回答列表', async () => {
      server.use(
        rest.get('/api/answer/question/1', (req, res, ctx) => {
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

      const result = await getAnswersByQuestion(1, 1, 20)
      expect(result).toBeDefined()
      expect(result.list).toHaveLength(1)
      expect(result.list[0].questionId).toBe(1)
    })

    it('应该支持分页', async () => {
      server.use(
        rest.get('/api/answer/question/1', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '获取成功',
              data: {
                ...mockPageResponse,
                total: 50,
                pages: 3,
              },
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await getAnswersByQuestion(1, 2, 20)
      expect(result).toBeDefined()
      expect(result.total).toBe(50)
      expect(result.pages).toBe(3)
    })
  })

  describe('getAnswersByUser - 获取用户的回答列表', () => {
    it('应该成功获取指定用户的回答列表', async () => {
      server.use(
        rest.get('/api/answer/user/1', (req, res, ctx) => {
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

      const result = await getAnswersByUser(1, 1, 20)
      expect(result).toBeDefined()
      expect(result.list).toHaveLength(1)
      expect(result.list[0].userId).toBe(1)
    })
  })

  describe('getMyAnswers - 获取我的回答列表', () => {
    it('应该成功获取我的回答列表', async () => {
      server.use(
        rest.get('/api/answer/my', (req, res, ctx) => {
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

      const result = await getMyAnswers(1, 20)
      expect(result).toBeDefined()
      expect(result.list).toHaveLength(1)
    })

    it('应该处理未登录的情况', async () => {
      server.use(
        rest.get('/api/answer/my', (req, res, ctx) => {
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

      await expect(getMyAnswers(1, 20)).rejects.toThrow('未登录')
    })
  })

  describe('acceptAnswer - 采纳回答', () => {
    it('应该成功采纳回答', async () => {
      server.use(
        rest.put('/api/answer/1/accept', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '采纳成功',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await acceptAnswer(1, 1)
      expect(result).toBeUndefined()
    })

    it('应该处理积分不足的情况', async () => {
      server.use(
        rest.put('/api/answer/1/accept', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 3006,
              msg: '积分不足，无法采纳',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      await expect(acceptAnswer(1, 1)).rejects.toThrow('积分不足，无法采纳')
    })

    it('应该处理采纳自己的回答的情况', async () => {
      server.use(
        rest.put('/api/answer/1/accept', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 3007,
              msg: '不能采纳自己的回答',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      await expect(acceptAnswer(1, 1)).rejects.toThrow('不能采纳自己的回答')
    })

    it('应该处理已采纳回答的情况', async () => {
      server.use(
        rest.put('/api/answer/1/accept', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 3008,
              msg: '该问题已有采纳的回答',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      await expect(acceptAnswer(1, 1)).rejects.toThrow('该问题已有采纳的回答')
    })
  })

  describe('cancelAcceptAnswer - 取消采纳回答', () => {
    it('应该成功取消采纳回答', async () => {
      server.use(
        rest.put('/api/answer/1/cancel-accept', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '取消采纳成功',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await cancelAcceptAnswer(1, 1)
      expect(result).toBeUndefined()
    })

    it('应该处理无权取消采纳的情况', async () => {
      server.use(
        rest.put('/api/answer/2/cancel-accept', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 3009,
              msg: '无权取消采纳该回答',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      await expect(cancelAcceptAnswer(2, 1)).rejects.toThrow('无权取消采纳该回答')
    })
  })

  describe('searchAnswers - 搜索回答', () => {
    it('应该成功搜索回答', async () => {
      server.use(
        rest.get('/api/answer/search', (req, res, ctx) => {
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

      const result = await searchAnswers('Vue', 1, 20)
      expect(result).toBeDefined()
      expect(result.list).toHaveLength(1)
    })

    it('应该处理搜索无结果的情况', async () => {
      server.use(
        rest.get('/api/answer/search', (req, res, ctx) => {
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

      const result = await searchAnswers('不存在的关键词', 1, 20)
      expect(result.list).toHaveLength(0)
      expect(result.total).toBe(0)
    })
  })
})