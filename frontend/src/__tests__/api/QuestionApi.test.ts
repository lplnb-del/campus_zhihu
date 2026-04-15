/**
 * 问题 API 单元测试
 * 测试问题相关的 API 接口
 */

import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import { rest } from 'msw'
import { setupServer } from 'msw/node'
import {
  publishQuestion,
  updateQuestion,
  deleteQuestion,
  getQuestionById,
  getQuestionList,
  getQuestionPage,
  getQuestionDetail,
  getLatestQuestions,
  getHotQuestions,
  getUnsolvedQuestions,
  getRewardQuestions,
  searchQuestions,
  getUserQuestions,
  getMyQuestions,
  closeQuestion,
  getRelatedQuestions,
  getQuestionsByTag,
} from '@/api/question'
import type {
  QuestionPublishDTO,
  QuestionUpdateDTO,
  QuestionQueryDTO,
  QuestionVO,
  PageResponse,
  TagVO,
} from '@/api/question'

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
}

// 创建模拟问题数据
const mockQuestion: QuestionVO = {
  id: 1,
  userId: 1,
  username: 'testuser',
  userAvatar: 'avatar.png',
  title: '如何学习 Vue 3？',
  content: '我想学习 Vue 3，有什么好的学习路径推荐吗？',
  tags: [mockTag],
  rewardPoints: 10,
  viewCount: 100,
  answerCount: 5,
  collectionCount: 3,
  likeCount: 10,
  status: 1,
  isDraft: false,
  isSolved: false,
  createTime: '2024-01-01T00:00:00',
  updateTime: '2024-01-02T00:00:00',
}

// 创建模拟分页响应数据
const mockPageResponse: PageResponse<QuestionVO> = {
  list: [mockQuestion],
  total: 1,
  page: 1,
  size: 20,
  pages: 1,
}

describe('QuestionApi - 问题 API 测试', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  describe('publishQuestion - 发布问题', () => {
    it('应该成功发布新问题', async () => {
      server.use(
        rest.post('/api/question/publish', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '发布成功',
              data: mockQuestion,
              timestamp: Date.now(),
            })
          )
        })
      )

      const publishData: QuestionPublishDTO = {
        title: '如何学习 Vue 3？',
        content: '我想学习 Vue 3，有什么好的学习路径推荐吗？',
        tagIds: [1],
        rewardPoints: 10,
        isDraft: false,
      }

      const result = await publishQuestion(publishData)
      expect(result).toBeDefined()
      expect(result.id).toBe(1)
      expect(result.title).toBe('如何学习 Vue 3？')
      expect(result.rewardPoints).toBe(10)
    })

    it('应该成功保存草稿', async () => {
      server.use(
        rest.post('/api/question/publish', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '保存成功',
              data: {
                ...mockQuestion,
                isDraft: true,
              },
              timestamp: Date.now(),
            })
          )
        })
      )

      const publishData: QuestionPublishDTO = {
        title: '草稿问题',
        content: '这是草稿内容',
        tagIds: [1],
        isDraft: true,
      }

      const result = await publishQuestion(publishData)
      expect(result.isDraft).toBe(true)
    })
  })

  describe('updateQuestion - 更新问题', () => {
    it('应该成功更新问题', async () => {
      server.use(
        rest.put('/api/question/1', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '更新成功',
              data: {
                ...mockQuestion,
                title: '如何深入学习 Vue 3？',
                content: '我想深入学习 Vue 3，有什么好的学习路径推荐吗？',
                updateTime: '2024-01-03T00:00:00',
              },
              timestamp: Date.now(),
            })
          )
        })
      )

      const updateData: QuestionUpdateDTO = {
        title: '如何深入学习 Vue 3？',
        content: '我想深入学习 Vue 3，有什么好的学习路径推荐吗？',
      }

      const result = await updateQuestion(1, updateData)
      expect(result).toBeDefined()
      expect(result.title).toBe('如何深入学习 Vue 3？')
    })

    it('应该处理问题不存在的情况', async () => {
      server.use(
        rest.put('/api/question/999', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 2001,
              msg: '问题不存在',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      await expect(updateQuestion(999, { title: '新标题' })).rejects.toThrow('问题不存在')
    })
  })

  describe('deleteQuestion - 删除问题', () => {
    it('应该成功删除问题', async () => {
      server.use(
        rest.delete('/api/question/1', (req, res, ctx) => {
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

      const result = await deleteQuestion(1)
      expect(result).toBeUndefined()
    })

    it('应该处理删除他人问题的情况', async () => {
      server.use(
        rest.delete('/api/question/2', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 2002,
              msg: '无权删除该问题',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      await expect(deleteQuestion(2)).rejects.toThrow('无权删除该问题')
    })
  })

  describe('getQuestionById - 获取问题详情', () => {
    it('应该成功获取问题详情', async () => {
      server.use(
        rest.get('/api/question/1', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '获取成功',
              data: mockQuestion,
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await getQuestionById(1)
      expect(result).toBeDefined()
      expect(result.id).toBe(1)
      expect(result.title).toBe('如何学习 Vue 3？')
    })

    it('应该处理问题不存在的情况', async () => {
      server.use(
        rest.get('/api/question/999', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 2001,
              msg: '问题不存在',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      await expect(getQuestionById(999)).rejects.toThrow('问题不存在')
    })
  })

  describe('getQuestionList - 获取问题列表', () => {
    it('应该成功获取问题列表（分页）', async () => {
      server.use(
        rest.get('/api/question/list', (req, res, ctx) => {
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

      const result = await getQuestionList({ page: 1, size: 20 })
      expect(result).toBeDefined()
      expect(result.list).toHaveLength(1)
      expect(result.total).toBe(1)
      expect(result.page).toBe(1)
    })

    it('应该支持按标签筛选问题', async () => {
      server.use(
        rest.get('/api/question/list', (req, res, ctx) => {
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

      const result = await getQuestionList({ tagId: 1, page: 1, size: 20 })
      expect(result).toBeDefined()
    })

    it('应该支持按用户筛选问题', async () => {
      server.use(
        rest.get('/api/question/list', (req, res, ctx) => {
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

      const result = await getQuestionList({ userId: 1, page: 1, size: 20 })
      expect(result).toBeDefined()
    })
  })

  describe('getQuestionPage - 分页查询问题', () => {
    it('应该成功分页查询问题', async () => {
      server.use(
        rest.post('/api/question/page', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '查询成功',
              data: mockPageResponse,
              timestamp: Date.now(),
            })
          )
        })
      )

      const queryData: QuestionQueryDTO = {
        keyword: 'Vue',
        page: 1,
        size: 20,
      }

      const result = await getQuestionPage(queryData)
      expect(result).toBeDefined()
      expect(result.list).toHaveLength(1)
    })
  })

  describe('getQuestionDetail - 获取问题详情（不增加浏览次数）', () => {
    it('应该成功获取问题详情（编辑用）', async () => {
      server.use(
        rest.get('/api/question/1/detail', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '获取成功',
              data: mockQuestion,
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await getQuestionDetail(1)
      expect(result).toBeDefined()
      expect(result.id).toBe(1)
    })
  })

  describe('getLatestQuestions - 获取最新问题', () => {
    it('应该成功获取最新问题列表', async () => {
      server.use(
        rest.get('/api/question/latest', (req, res, ctx) => {
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

      const result = await getLatestQuestions(1, 20)
      expect(result).toBeDefined()
      expect(result.list).toHaveLength(1)
    })
  })

  describe('getHotQuestions - 获取热门问题', () => {
    it('应该成功获取热门问题列表', async () => {
      server.use(
        rest.get('/api/question/hot', (req, res, ctx) => {
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

      const result = await getHotQuestions(1, 20)
      expect(result).toBeDefined()
      expect(result.list).toHaveLength(1)
    })
  })

  describe('getUnsolvedQuestions - 获取待解决问题', () => {
    it('应该成功获取待解决问题列表', async () => {
      server.use(
        rest.get('/api/question/unsolved', (req, res, ctx) => {
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

      const result = await getUnsolvedQuestions(1, 20)
      expect(result).toBeDefined()
      expect(result.list).toHaveLength(1)
    })
  })

  describe('getRewardQuestions - 获取悬赏问题', () => {
    it('应该成功获取悬赏问题列表', async () => {
      server.use(
        rest.get('/api/question/reward', (req, res, ctx) => {
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

      const result = await getRewardQuestions(1, 20)
      expect(result).toBeDefined()
      expect(result.list).toHaveLength(1)
    })
  })

  describe('searchQuestions - 搜索问题', () => {
    it('应该成功搜索问题', async () => {
      server.use(
        rest.get('/api/question/search', (req, res, ctx) => {
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

      const result = await searchQuestions('Vue', 1, 20)
      expect(result).toBeDefined()
      expect(result.list).toHaveLength(1)
    })

    it('应该处理搜索无结果的情况', async () => {
      server.use(
        rest.get('/api/question/search', (req, res, ctx) => {
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

      const result = await searchQuestions('不存在的关键词', 1, 20)
      expect(result.list).toHaveLength(0)
      expect(result.total).toBe(0)
    })
  })

  describe('getUserQuestions - 获取用户的问题列表', () => {
    it('应该成功获取指定用户的问题列表', async () => {
      server.use(
        rest.get('/api/question/user/1', (req, res, ctx) => {
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

      const result = await getUserQuestions(1, 1, 20)
      expect(result).toBeDefined()
      expect(result.list).toHaveLength(1)
    })
  })

  describe('getMyQuestions - 获取我的问题列表', () => {
    it('应该成功获取我的问题列表', async () => {
      server.use(
        rest.get('/api/question/my', (req, res, ctx) => {
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

      const result = await getMyQuestions(1, 20)
      expect(result).toBeDefined()
      expect(result.list).toHaveLength(1)
    })

    it('应该处理未登录的情况', async () => {
      server.use(
        rest.get('/api/question/my', (req, res, ctx) => {
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

      await expect(getMyQuestions(1, 20)).rejects.toThrow('未登录')
    })
  })

  describe('closeQuestion - 关闭问题', () => {
    it('应该成功关闭问题', async () => {
      server.use(
        rest.put('/api/question/1/close', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '关闭成功',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await closeQuestion(1)
      expect(result).toBeUndefined()
    })
  })

  describe('getRelatedQuestions - 获取相关问题推荐', () => {
    it('应该成功获取相关问题推荐', async () => {
      server.use(
        rest.get('/api/question/1/related', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '获取成功',
              data: [mockQuestion],
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await getRelatedQuestions(1, 5)
      expect(result).toBeDefined()
      expect(result).toHaveLength(1)
    })
  })

  describe('getQuestionsByTag - 根据标签获取问题列表', () => {
    it('应该成功根据标签获取问题列表', async () => {
      server.use(
        rest.get('/api/question/tag/1', (req, res, ctx) => {
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

      const result = await getQuestionsByTag(1, 1, 20, 'hot')
      expect(result).toBeDefined()
      expect(result.list).toHaveLength(1)
    })

    it('应该支持不同的排序方式', async () => {
      server.use(
        rest.get('/api/question/tag/1', (req, res, ctx) => {
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

      const result1 = await getQuestionsByTag(1, 1, 20, 'hot')
      const result2 = await getQuestionsByTag(1, 1, 20, 'latest')
      const result3 = await getQuestionsByTag(1, 1, 20, 'unsolved')
      const result4 = await getQuestionsByTag(1, 1, 20, 'reward')

      expect(result1).toBeDefined()
      expect(result2).toBeDefined()
      expect(result3).toBeDefined()
      expect(result4).toBeDefined()
    })
  })
})