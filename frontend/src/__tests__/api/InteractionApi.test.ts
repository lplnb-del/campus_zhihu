/**
 * 交互 API 单元测试
 * 测试评论、点赞、收藏相关的 API 接口
 */

import { describe, it, expect, beforeEach, afterEach } from 'vitest'
import { rest } from 'msw'
import { setupServer } from 'msw/node'
import {
  // 评论相关
  publishComment,
  deleteComment,
  getCommentById,
  getCommentsByTarget,
  getCommentsByUser,
  getMyComments,
  blockComment,
  approveComment,
  // 点赞相关
  toggleLike,
  addLike,
  removeLike,
  checkLiked,
  checkLikedBatch,
  getUserLikes,
  getMyLikes,
  getLikeCount,
  getUserLikeCount,
  // 收藏相关
  toggleCollection,
  addCollection,
  removeCollection,
  checkCollected,
  checkCollectedBatch,
  getUserCollections,
  getMyCollections,
  getCollectionCount,
  getUserCollectionCount,
} from '@/api/interaction'
import type {
  CommentPublishDTO,
  CommentVO,
  PageResponse,
} from '@/api/interaction'

// 创建 MSW 服务器
const server = setupServer()

// 在所有测试开始前启动服务器
beforeAll(() => server.listen())

// 每个测试结束后重置处理器
afterEach(() => server.resetHandlers())

// 所有测试结束后关闭服务器
afterAll(() => server.close())

// 创建模拟评论数据
const mockComment: CommentVO = {
  id: 1,
  targetType: 'answer',
  targetId: 1,
  userId: 1,
  username: 'testuser',
  userAvatar: 'avatar.png',
  content: '这是一个很好的回答！',
  likeCount: 5,
  status: 1,
  createTime: '2024-01-01T00:00:00',
  updateTime: '2024-01-02T00:00:00',
}

// 创建模拟分页响应数据
const mockPageResponse: PageResponse<CommentVO> = {
  list: [mockComment],
  total: 1,
  page: 1,
  size: 20,
  pages: 1,
}

describe('InteractionApi - 交互 API 测试', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  // ==================== 评论相关测试 ====================

  describe('publishComment - 发布评论', () => {
    it('应该成功发布评论', async () => {
      server.use(
        rest.post('/api/comment/publish', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '发布成功',
              data: mockComment,
              timestamp: Date.now(),
            })
          )
        })
      )

      const publishData: CommentPublishDTO = {
        targetType: 'answer',
        targetId: 1,
        content: '这是一个很好的回答！',
      }

      const result = await publishComment(publishData)
      expect(result).toBeDefined()
      expect(result.id).toBe(1)
      expect(result.content).toBe('这是一个很好的回答！')
    })

    it('应该成功发布回复评论', async () => {
      server.use(
        rest.post('/api/comment/publish', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '发布成功',
              data: {
                ...mockComment,
                parentId: 1,
                replyToUserId: 2,
                replyToUsername: 'otheruser',
              },
              timestamp: Date.now(),
            })
          )
        })
      )

      const publishData: CommentPublishDTO = {
        targetType: 'comment',
        targetId: 1,
        content: '我同意你的观点',
        parentId: 1,
        replyToUserId: 2,
      }

      const result = await publishComment(publishData)
      expect(result).toBeDefined()
      expect(result.parentId).toBe(1)
      expect(result.replyToUserId).toBe(2)
    })

    it('应该处理评论内容为空的情况', async () => {
      server.use(
        rest.post('/api/comment/publish', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 4001,
              msg: '评论内容不能为空',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      const publishData: CommentPublishDTO = {
        targetType: 'answer',
        targetId: 1,
        content: '',
      }

      await expect(publishComment(publishData)).rejects.toThrow('评论内容不能为空')
    })
  })

  describe('deleteComment - 删除评论', () => {
    it('应该成功删除评论', async () => {
      server.use(
        rest.delete('/api/comment/1', (req, res, ctx) => {
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

      const result = await deleteComment(1)
      expect(result).toBeUndefined()
    })

    it('应该处理删除他人评论的情况', async () => {
      server.use(
        rest.delete('/api/comment/2', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 4002,
              msg: '无权删除该评论',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      await expect(deleteComment(2)).rejects.toThrow('无权删除该评论')
    })
  })

  describe('getCommentById - 获取评论详情', () => {
    it('应该成功获取评论详情', async () => {
      server.use(
        rest.get('/api/comment/1', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '获取成功',
              data: mockComment,
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await getCommentById(1)
      expect(result).toBeDefined()
      expect(result.id).toBe(1)
    })

    it('应该处理评论不存在的情况', async () => {
      server.use(
        rest.get('/api/comment/999', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 4003,
              msg: '评论不存在',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      await expect(getCommentById(999)).rejects.toThrow('评论不存在')
    })
  })

  describe('getCommentsByTarget - 获取目标的评论列表', () => {
    it('应该成功获取目标的评论列表', async () => {
      server.use(
        rest.get('/api/comment/target', (req, res, ctx) => {
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

      const result = await getCommentsByTarget('answer', 1, 1, 20)
      expect(result).toBeDefined()
      expect(result.list).toHaveLength(1)
    })
  })

  describe('getCommentsByUser - 获取用户的评论列表', () => {
    it('应该成功获取用户的评论列表', async () => {
      server.use(
        rest.get('/api/comment/user/1', (req, res, ctx) => {
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

      const result = await getCommentsByUser(1, 1, 20)
      expect(result).toBeDefined()
      expect(result.list).toHaveLength(1)
    })
  })

  describe('getMyComments - 获取我的评论列表', () => {
    it('应该成功获取我的评论列表', async () => {
      server.use(
        rest.get('/api/comment/my', (req, res, ctx) => {
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

      const result = await getMyComments(1, 20)
      expect(result).toBeDefined()
      expect(result.list).toHaveLength(1)
    })
  })

  describe('blockComment - 屏蔽评论（管理员）', () => {
    it('应该成功屏蔽评论', async () => {
      server.use(
        rest.put('/api/comment/1/block', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '屏蔽成功',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await blockComment(1)
      expect(result).toBeUndefined()
    })
  })

  describe('approveComment - 审核通过评论（管理员）', () => {
    it('应该成功审核通过评论', async () => {
      server.use(
        rest.put('/api/comment/1/approve', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '审核通过',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await approveComment(1)
      expect(result).toBeUndefined()
    })
  })

  // ==================== 点赞相关测试 ====================

  describe('toggleLike - 点赞/取消点赞', () => {
    it('应该成功点赞', async () => {
      server.use(
        rest.post('/api/like/toggle', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '点赞成功',
              data: { liked: true },
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await toggleLike('answer', 1)
      expect(result).toBeDefined()
      expect(result.liked).toBe(true)
    })

    it('应该成功取消点赞', async () => {
      server.use(
        rest.post('/api/like/toggle', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '取消点赞成功',
              data: { liked: false },
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await toggleLike('answer', 1)
      expect(result).toBeDefined()
      expect(result.liked).toBe(false)
    })
  })

  describe('addLike - 点赞', () => {
    it('应该成功点赞', async () => {
      server.use(
        rest.post('/api/like', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '点赞成功',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await addLike('answer', 1)
      expect(result).toBeUndefined()
    })

    it('应该处理重复点赞的情况', async () => {
      server.use(
        rest.post('/api/like', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 5001,
              msg: '已经点赞过了',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      await expect(addLike('answer', 1)).rejects.toThrow('已经点赞过了')
    })
  })

  describe('removeLike - 取消点赞', () => {
    it('应该成功取消点赞', async () => {
      server.use(
        rest.delete('/api/like', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '取消点赞成功',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await removeLike('answer', 1)
      expect(result).toBeUndefined()
    })
  })

  describe('checkLiked - 检查是否已点赞', () => {
    it('应该返回 true 表示已点赞', async () => {
      server.use(
        rest.get('/api/like/check', (req, res, ctx) => {
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

      const result = await checkLiked('answer', 1)
      expect(result).toBe(true)
    })

    it('应该返回 false 表示未点赞', async () => {
      server.use(
        rest.get('/api/like/check', (req, res, ctx) => {
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

      const result = await checkLiked('answer', 1)
      expect(result).toBe(false)
    })
  })

  describe('checkLikedBatch - 批量检查是否已点赞', () => {
    it('应该成功批量检查点赞状态', async () => {
      server.use(
        rest.post('/api/like/check-batch', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '检查成功',
              data: { 1: true, 2: false, 3: true },
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await checkLikedBatch('answer', [1, 2, 3])
      expect(result).toBeDefined()
      expect(result[1]).toBe(true)
      expect(result[2]).toBe(false)
      expect(result[3]).toBe(true)
    })
  })

  describe('getUserLikes - 获取用户的点赞列表', () => {
    it('应该成功获取用户的点赞列表', async () => {
      server.use(
        rest.get('/api/like/user/1', (req, res, ctx) => {
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

      const result = await getUserLikes(1, 1, 20)
      expect(result).toBeDefined()
      expect(result.list).toHaveLength(1)
    })
  })

  describe('getMyLikes - 获取我的点赞列表', () => {
    it('应该成功获取我的点赞列表', async () => {
      server.use(
        rest.get('/api/like/my', (req, res, ctx) => {
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

      const result = await getMyLikes(1, 20)
      expect(result).toBeDefined()
      expect(result.list).toHaveLength(1)
    })
  })

  describe('getLikeCount - 获取目标的点赞数', () => {
    it('应该成功获取目标的点赞数', async () => {
      server.use(
        rest.get('/api/like/count/target', (req, res, ctx) => {
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

      const result = await getLikeCount('answer', 1)
      expect(result).toBe(100)
    })
  })

  describe('getUserLikeCount - 获取用户的点赞总数', () => {
    it('应该成功获取用户的点赞总数', async () => {
      server.use(
        rest.get('/api/like/count/user/1', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '获取成功',
              data: 50,
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await getUserLikeCount(1)
      expect(result).toBe(50)
    })
  })

  // ==================== 收藏相关测试 ====================

  describe('toggleCollection - 收藏/取消收藏', () => {
    it('应该成功收藏', async () => {
      server.use(
        rest.post('/api/collection/toggle', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '收藏成功',
              data: { collected: true },
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await toggleCollection('question', 1)
      expect(result).toBeDefined()
      expect(result.collected).toBe(true)
    })

    it('应该成功取消收藏', async () => {
      server.use(
        rest.post('/api/collection/toggle', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '取消收藏成功',
              data: { collected: false },
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await toggleCollection('question', 1)
      expect(result).toBeDefined()
      expect(result.collected).toBe(false)
    })
  })

  describe('addCollection - 收藏', () => {
    it('应该成功收藏', async () => {
      server.use(
        rest.post('/api/collection', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '收藏成功',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await addCollection('question', 1)
      expect(result).toBeUndefined()
    })

    it('应该处理重复收藏的情况', async () => {
      server.use(
        rest.post('/api/collection', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 6001,
              msg: '已经收藏过了',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      await expect(addCollection('question', 1)).rejects.toThrow('已经收藏过了')
    })
  })

  describe('removeCollection - 取消收藏', () => {
    it('应该成功取消收藏', async () => {
      server.use(
        rest.delete('/api/collection', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '取消收藏成功',
              data: null,
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await removeCollection('question', 1)
      expect(result).toBeUndefined()
    })
  })

  describe('checkCollected - 检查是否已收藏', () => {
    it('应该返回 true 表示已收藏', async () => {
      server.use(
        rest.get('/api/collection/check', (req, res, ctx) => {
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

      const result = await checkCollected('question', 1)
      expect(result).toBe(true)
    })

    it('应该返回 false 表示未收藏', async () => {
      server.use(
        rest.get('/api/collection/check', (req, res, ctx) => {
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

      const result = await checkCollected('question', 1)
      expect(result).toBe(false)
    })
  })

  describe('checkCollectedBatch - 批量检查是否已收藏', () => {
    it('应该成功批量检查收藏状态', async () => {
      server.use(
        rest.post('/api/collection/check-batch', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '检查成功',
              data: { 1: true, 2: false, 3: true },
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await checkCollectedBatch('question', [1, 2, 3])
      expect(result).toBeDefined()
      expect(result[1]).toBe(true)
      expect(result[2]).toBe(false)
      expect(result[3]).toBe(true)
    })
  })

  describe('getUserCollections - 获取用户的收藏列表', () => {
    it('应该成功获取用户的收藏列表', async () => {
      server.use(
        rest.get('/api/collection/user/1', (req, res, ctx) => {
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

      const result = await getUserCollections(1, 1, 20)
      expect(result).toBeDefined()
      expect(result.list).toHaveLength(1)
    })
  })

  describe('getMyCollections - 获取我的收藏列表', () => {
    it('应该成功获取我的收藏列表', async () => {
      server.use(
        rest.get('/api/collection/my', (req, res, ctx) => {
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

      const result = await getMyCollections(1, 20)
      expect(result).toBeDefined()
      expect(result.list).toHaveLength(1)
    })
  })

  describe('getCollectionCount - 获取目标的收藏数', () => {
    it('应该成功获取目标的收藏数', async () => {
      server.use(
        rest.get('/api/collection/count/target', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '获取成功',
              data: 50,
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await getCollectionCount('question', 1)
      expect(result).toBe(50)
    })
  })

  describe('getUserCollectionCount - 获取用户的收藏总数', () => {
    it('应该成功获取用户的收藏总数', async () => {
      server.use(
        rest.get('/api/collection/count/user/1', (req, res, ctx) => {
          return res(
            ctx.status(200),
            ctx.json({
              code: 200,
              msg: '获取成功',
              data: 25,
              timestamp: Date.now(),
            })
          )
        })
      )

      const result = await getUserCollectionCount(1)
      expect(result).toBe(25)
    })
  })
})