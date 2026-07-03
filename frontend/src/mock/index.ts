/**
 * Mock 服务入口
 * 用于 GitHub Pages 静态部署演示
 *
 * 通过 axios-mock-adapter 拦截前端请求，返回本地 Mock 数据。
 * 仅在 VITE_USE_MOCK=true 时启用。
 */

import MockAdapter from 'axios-mock-adapter'
import service from '@/utils/request'
import type { AxiosRequestConfig } from 'axios'
import {
  mockLoginResponse,
  mockCurrentUser,
  mockTags,
  mockQuestions,
  mockAnswers,
  mockComments,
  getQuestionById,
  getAnswersByQuestionId,
} from './data'
import type { PageResponse } from '@/api/question'

let mockAdapter: MockAdapter | null = null

/**
 * 解析 URL 查询参数
 */
function parseParams(config: AxiosRequestConfig): Record<string, any> {
  const params: Record<string, any> = {}

  // 1. 从 config.params 获取
  if (config.params) {
    Object.assign(params, config.params)
  }

  // 2. 从 URL 查询字符串获取
  if (config.url) {
    const queryIndex = config.url.indexOf('?')
    if (queryIndex !== -1) {
      const queryString = config.url.substring(queryIndex + 1)
      queryString.split('&').forEach((pair) => {
        const [key, value] = pair.split('=')
        if (key) {
          params[decodeURIComponent(key)] = value ? decodeURIComponent(value) : ''
        }
      })
    }
  }

  return params
}

/**
 * 解析 URL 路径（去掉查询字符串）
 */
function getPath(config: AxiosRequestConfig): string {
  if (!config.url) return ''
  const queryIndex = config.url.indexOf('?')
  return queryIndex === -1 ? config.url : config.url.substring(0, queryIndex)
}

/**
 * 创建分页响应
 */
function createPageResponse<T>(list: T[], page: number, size: number): PageResponse<T> {
  const total = list.length
  const start = (page - 1) * size
  const end = start + size
  return {
    list: list.slice(start, end),
    total,
    page,
    size,
    pages: Math.ceil(total / size),
  }
}

/**
 * 包装统一响应格式
 */
function success<T>(data: T) {
  return [200, { code: 200, msg: 'success', data, timestamp: Date.now() }]
}

/**
 * 设置 Mock
 */
export function setupMock() {
  if (mockAdapter) return

  mockAdapter = new MockAdapter(service, { delayResponse: 200 })

  console.log('🎭 Mock 模式已启用：所有 API 请求将由本地 Mock 数据响应')

  // ==================== 用户相关 ====================

  // 登录
  mockAdapter.onPost('/api/user/login').reply((config) => {
    const body = JSON.parse(config.data || '{}')
    if (body.username && body.password) {
      return success(mockLoginResponse)
    }
    return [400, { code: 400, msg: '用户名或密码错误', data: null, timestamp: Date.now() }]
  })

  // 注册
  mockAdapter.onPost('/api/user/register').reply(() => {
    return success(mockCurrentUser)
  })

  // 获取当前用户信息
  mockAdapter.onGet(/\/api\/user\/info/).reply(() => {
    return success(mockCurrentUser)
  })

  // 根据 ID 获取用户
  mockAdapter.onGet(/\/api\/user\/(\d+)$/).reply((config) => {
    const path = getPath(config)
    const match = path.match(/\/api\/user\/(\d+)$/)
    if (match) {
      const userId = Number(match[1])
      const user = userId === mockCurrentUser.id ? mockCurrentUser : null
      return success(user)
    }
    return success(null)
  })

  // 刷新 Token
  mockAdapter.onPost('/api/user/refresh-token').reply(() => {
    return success(mockLoginResponse)
  })

  // 登出
  mockAdapter.onPost('/api/user/logout').reply(() => {
    return success(null)
  })

  // ==================== 问题相关 ====================

  // 最新问题
  mockAdapter.onGet(/\/api\/question\/latest/).reply((config) => {
    const params = parseParams(config)
    const page = Number(params.page) || 1
    const size = Number(params.size) || 20
    const sorted = [...mockQuestions].sort(
      (a, b) => new Date(b.createTime).getTime() - new Date(a.createTime).getTime(),
    )
    return success(createPageResponse(sorted, page, size))
  })

  // 热门问题
  mockAdapter.onGet(/\/api\/question\/hot/).reply((config) => {
    const params = parseParams(config)
    const page = Number(params.page) || 1
    const size = Number(params.size) || 20
    const sorted = [...mockQuestions].sort((a, b) => b.viewCount - a.viewCount)
    return success(createPageResponse(sorted, page, size))
  })

  // 待解决问题
  mockAdapter.onGet(/\/api\/question\/unsolved/).reply((config) => {
    const params = parseParams(config)
    const page = Number(params.page) || 1
    const size = Number(params.size) || 20
    const filtered = mockQuestions.filter((q) => !q.isSolved)
    return success(createPageResponse(filtered, page, size))
  })

  // 悬赏问题
  mockAdapter.onGet(/\/api\/question\/reward/).reply((config) => {
    const params = parseParams(config)
    const page = Number(params.page) || 1
    const size = Number(params.size) || 20
    const filtered = mockQuestions.filter((q) => q.rewardPoints > 0)
    const sorted = [...filtered].sort((a, b) => b.rewardPoints - a.rewardPoints)
    return success(createPageResponse(sorted, page, size))
  })

  // 问题列表（通用查询，支持 keyword/tagId/orderBy/page/size）
  mockAdapter.onGet(/\/api\/question\/list/).reply((config) => {
    const params = parseParams(config)
    const page = Number(params.page) || 1
    const size = Number(params.size) || 20
    const keyword = params.keyword ? String(params.keyword).toLowerCase() : ''
    const tagId = params.tagId ? Number(params.tagId) : undefined
    const orderBy = params.orderBy || 'latest'

    let filtered = [...mockQuestions]

    if (keyword) {
      filtered = filtered.filter(
        (q) =>
          q.title.toLowerCase().includes(keyword) ||
          q.content.toLowerCase().includes(keyword),
      )
    }

    if (tagId) {
      filtered = filtered.filter((q) => q.tags.some((t) => t.id === tagId))
    }

    switch (orderBy) {
      case 'hot':
        filtered.sort((a, b) => b.viewCount - a.viewCount)
        break
      case 'reward':
        filtered.sort((a, b) => b.rewardPoints - a.rewardPoints)
        break
      case 'unsolved':
        filtered = filtered.filter((q) => !q.isSolved)
        break
      case 'latest':
      default:
        filtered.sort(
          (a, b) =>
            new Date(b.createTime).getTime() - new Date(a.createTime).getTime(),
        )
    }

    return success(createPageResponse(filtered, page, size))
  })

  // 问题详情
  mockAdapter.onGet(/\/api\/question\/(\d+)$/).reply((config) => {
    const path = getPath(config)
    const match = path.match(/\/api\/question\/(\d+)$/)
    if (match) {
      const question = getQuestionById(Number(match[1]))
      if (question) return success(question)
    }
    return [404, { code: 404, msg: '问题不存在', data: null, timestamp: Date.now() }]
  })

  // 问题详情（不增加浏览次数）
  mockAdapter.onGet(/\/api\/question\/(\d+)\/detail$/).reply((config) => {
    const path = getPath(config)
    const match = path.match(/\/api\/question\/(\d+)\/detail$/)
    if (match) {
      const question = getQuestionById(Number(match[1]))
      if (question) return success(question)
    }
    return [404, { code: 404, msg: '问题不存在', data: null, timestamp: Date.now() }]
  })

  // 发布问题
  mockAdapter.onPost('/api/question/publish').reply((config) => {
    const body = JSON.parse(config.data || '{}')
    const newQuestion = {
      id: Date.now(),
      userId: mockCurrentUser.id,
      username: mockCurrentUser.username,
      userAvatar: mockCurrentUser.avatar,
      title: body.title || '新建问题',
      content: body.content || '',
      tags: body.tagIds
        ? mockTags.filter((t) => body.tagIds.includes(t.id))
        : [],
      rewardPoints: body.rewardPoints || 0,
      viewCount: 0,
      answerCount: 0,
      collectionCount: 0,
      likeCount: 0,
      status: 1,
      isDraft: body.isDraft || false,
      isSolved: false,
      createTime: new Date().toISOString(),
      updateTime: new Date().toISOString(),
    }
    return success(newQuestion)
  })

  // 更新问题
  mockAdapter.onPut(/\/api\/question\/(\d+)$/).reply(() => {
    return success(null)
  })

  // 删除问题
  mockAdapter.onDelete(/\/api\/question\/(\d+)$/).reply(() => {
    return success(null)
  })

  // 相关问题推荐
  mockAdapter.onGet(/\/api\/question\/(\d+)\/related$/).reply((config) => {
    const path = getPath(config)
    const match = path.match(/\/api\/question\/(\d+)\/related$/)
    if (match) {
      const currentId = Number(match[1])
      const related = mockQuestions.filter((q) => q.id !== currentId).slice(0, 5)
      return success(related)
    }
    return success([])
  })

  // 根据标签获取问题列表
  mockAdapter.onGet(/\/api\/question\/tag\/(\d+)$/).reply((config) => {
    const path = getPath(config)
    const match = path.match(/\/api\/question\/tag\/(\d+)$/)
    if (match) {
      const tagId = Number(match[1])
      const params = parseParams(config)
      const page = Number(params.page) || 1
      const size = Number(params.size) || 20
      const filtered = mockQuestions.filter((q) => q.tags.some((t) => t.id === tagId))
      return success(createPageResponse(filtered, page, size))
    }
    return success(createPageResponse([], 1, 20))
  })

  // 搜索问题
  mockAdapter.onGet(/\/api\/question\/search/).reply((config) => {
    const params = parseParams(config)
    const keyword = params.keyword || ''
    const page = Number(params.page) || 1
    const size = Number(params.size) || 20
    const filtered = mockQuestions.filter(
      (q) =>
        q.title.toLowerCase().includes(keyword.toLowerCase()) ||
        q.content.toLowerCase().includes(keyword.toLowerCase()),
    )
    return success(createPageResponse(filtered, page, size))
  })

  // 用户的问题列表
  mockAdapter.onGet(/\/api\/question\/user\/(\d+)$/).reply((config) => {
    const path = getPath(config)
    const match = path.match(/\/api\/question\/user\/(\d+)$/)
    if (match) {
      const userId = Number(match[1])
      const params = parseParams(config)
      const page = Number(params.page) || 1
      const size = Number(params.size) || 20
      const filtered = mockQuestions.filter((q) => q.userId === userId)
      return success(createPageResponse(filtered, page, size))
    }
    return success(createPageResponse([], 1, 20))
  })

  // 我的问题列表
  mockAdapter.onGet(/\/api\/question\/my/).reply((config) => {
    const params = parseParams(config)
    const page = Number(params.page) || 1
    const size = Number(params.size) || 20
    const filtered = mockQuestions.filter((q) => q.userId === mockCurrentUser.id)
    return success(createPageResponse(filtered, page, size))
  })

  // 分页查询问题（POST 方式）
  mockAdapter.onPost('/api/question/page').reply((config) => {
    const body = JSON.parse(config.data || '{}')
    const page = body.page || 1
    const size = body.size || 20
    let filtered = [...mockQuestions]
    if (body.keyword) {
      filtered = filtered.filter(
        (q) =>
          q.title.toLowerCase().includes(body.keyword.toLowerCase()) ||
          q.content.toLowerCase().includes(body.keyword.toLowerCase()),
      )
    }
    if (body.tagId) {
      filtered = filtered.filter((q) => q.tags.some((t) => t.id === body.tagId))
    }
    return success(createPageResponse(filtered, page, size))
  })

  // ==================== 回答相关 ====================

  // 获取问题的回答列表
  mockAdapter.onGet(/\/api\/answer\/question\/(\d+)$/).reply((config) => {
    const path = getPath(config)
    const match = path.match(/\/api\/answer\/question\/(\d+)$/)
    if (match) {
      const questionId = Number(match[1])
      const params = parseParams(config)
      const page = Number(params.page) || 1
      const size = Number(params.size) || 20
      const answers = getAnswersByQuestionId(questionId)
      return success(createPageResponse(answers, page, size))
    }
    return success(createPageResponse([], 1, 20))
  })

  // 回答详情
  mockAdapter.onGet(/\/api\/answer\/(\d+)$/).reply((config) => {
    const path = getPath(config)
    const match = path.match(/\/api\/answer\/(\d+)$/)
    if (match) {
      const answerId = Number(match[1])
      const answer = mockAnswers.find((a) => a.id === answerId)
      if (answer) return success(answer)
    }
    return [404, { code: 404, msg: '回答不存在', data: null, timestamp: Date.now() }]
  })

  // 发布回答
  mockAdapter.onPost('/api/answer/publish').reply((config) => {
    const body = JSON.parse(config.data || '{}')
    const newAnswer = {
      id: Date.now(),
      questionId: body.questionId,
      questionTitle: '',
      userId: mockCurrentUser.id,
      username: mockCurrentUser.username,
      userAvatar: mockCurrentUser.avatar,
      content: body.content || '',
      likeCount: 0,
      commentCount: 0,
      isAccepted: 0,
      status: 1,
      createTime: new Date().toISOString(),
      updateTime: new Date().toISOString(),
      isLiked: false,
      userPoints: mockCurrentUser.points,
    }
    return success(newAnswer)
  })

  // 更新回答
  mockAdapter.onPut(/\/api\/answer\/(\d+)$/).reply(() => {
    return success(null)
  })

  // 删除回答
  mockAdapter.onDelete(/\/api\/answer\/(\d+)$/).reply(() => {
    return success(null)
  })

  // 采纳回答
  mockAdapter.onPut(/\/api\/answer\/(\d+)\/accept$/).reply(() => {
    return success(null)
  })

  // 取消采纳回答
  mockAdapter.onPut(/\/api\/answer\/(\d+)\/cancel-accept$/).reply(() => {
    return success(null)
  })

  // 我的回答列表
  mockAdapter.onGet(/\/api\/answer\/my/).reply((config) => {
    const params = parseParams(config)
    const page = Number(params.page) || 1
    const size = Number(params.size) || 20
    const filtered = mockAnswers.filter((a) => a.userId === mockCurrentUser.id)
    return success(createPageResponse(filtered, page, size))
  })

  // ==================== 评论相关 ====================

  // 获取评论列表
  mockAdapter.onGet(/\/api\/comment\/target/).reply((config) => {
    const params = parseParams(config)
    const targetType = Number(params.targetType)
    const targetId = Number(params.targetId)
    const comments = mockComments.filter(
      (c) => c.targetType === (targetType === 1 ? 'question' : targetType === 2 ? 'answer' : 'comment') &&
        c.targetId === targetId,
    )
    return success({
      list: comments,
      total: comments.length,
      page: 1,
      size: 50,
      pages: 1,
    })
  })

  // 发布评论
  mockAdapter.onPost('/api/comment/publish').reply((config) => {
    const body = JSON.parse(config.data || '{}')
    const newComment = {
      id: Date.now(),
      targetType: body.targetType === 1 ? 'question' : body.targetType === 2 ? 'answer' : 'comment',
      targetId: body.targetId,
      userId: mockCurrentUser.id,
      username: mockCurrentUser.username,
      userAvatar: mockCurrentUser.avatar,
      content: body.content || '',
      parentId: body.parentId,
      replyToUserId: body.replyToUserId,
      likeCount: 0,
      status: 1,
      createTime: new Date().toISOString(),
      updateTime: new Date().toISOString(),
      isLiked: false,
    }
    return success(newComment)
  })

  // ==================== 点赞/收藏相关 ====================

  // 点赞/取消点赞
  mockAdapter.onPost('/api/like/toggle').reply((config) => {
    const params = parseParams(config)
    const targetType = params.targetType
    const targetId = params.targetId
    console.log(`🎭 Mock 点赞切换：targetType=${targetType}, targetId=${targetId}`)
    return success(true)
  })

  // 检查是否已点赞
  mockAdapter.onGet(/\/api\/like\/check/).reply(() => {
    return success(false)
  })

  // 收藏/取消收藏
  mockAdapter.onPost('/api/collection/toggle').reply((config) => {
    const params = parseParams(config)
    console.log(`🎭 Mock 收藏切换：targetType=${params.targetType}, targetId=${params.targetId}`)
    return success(true)
  })

  // 检查是否已收藏
  mockAdapter.onGet(/\/api\/collection\/check/).reply(() => {
    return success(false)
  })

  // ==================== 标签相关 ====================

  // 热门标签
  mockAdapter.onGet(/\/api\/tag\/hot/).reply((config) => {
    const params = parseParams(config)
    const limit = Number(params.limit) || 20
    return success(mockTags.slice(0, limit))
  })

  // 标签列表
  mockAdapter.onGet(/\/api\/tag\/list/).reply((config) => {
    const params = parseParams(config)
    const page = Number(params.page) || 1
    const size = Number(params.size) || 50
    const category = params.category
    let filtered = category
      ? mockTags.filter((t) => t.category && t.category.toLowerCase() === category.toLowerCase())
      : [...mockTags]
    return success({
      list: filtered.slice((page - 1) * size, page * size),
      total: filtered.length,
      page,
      size,
      pages: Math.ceil(filtered.length / size),
      totalQuestions: mockQuestions.length,
    })
  })

  // 所有标签
  mockAdapter.onGet(/\/api\/tag\/all/).reply(() => {
    return success(mockTags)
  })

  // 标签详情
  mockAdapter.onGet(/\/api\/tag\/(\d+)$/).reply((config) => {
    const path = getPath(config)
    const match = path.match(/\/api\/tag\/(\d+)$/)
    if (match) {
      const tag = mockTags.find((t) => t.id === Number(match[1]))
      if (tag) return success(tag)
    }
    return [404, { code: 404, msg: '标签不存在', data: null, timestamp: Date.now() }]
  })

  // 搜索标签
  mockAdapter.onGet(/\/api\/tag\/search/).reply((config) => {
    const params = parseParams(config)
    const keyword = (params.keyword || '').toLowerCase()
    const page = Number(params.page) || 1
    const size = Number(params.size) || 20
    const filtered = keyword
      ? mockTags.filter(
          (t) =>
            t.name.toLowerCase().includes(keyword) ||
            (t.description && t.description.toLowerCase().includes(keyword)),
        )
      : [...mockTags]
    return success({
      list: filtered.slice((page - 1) * size, page * size),
      total: filtered.length,
      page,
      size,
      pages: Math.ceil(filtered.length / size),
      totalQuestions: mockQuestions.length,
    })
  })

  // 关注标签
  mockAdapter.onPost(/\/api\/tag\/(\d+)\/follow$/).reply((config) => {
    const path = getPath(config)
    const match = path.match(/\/api\/tag\/(\d+)\/follow$/)
    if (match) {
      const tagId = Number(match[1])
      const tag = mockTags.find((t) => t.id === tagId)
      if (tag) {
        tag.isFollowed = true
        tag.followCount++
      }
    }
    return success(null)
  })

  // 取消关注标签
  mockAdapter.onDelete(/\/api\/tag\/(\d+)\/follow$/).reply((config) => {
    const path = getPath(config)
    const match = path.match(/\/api\/tag\/(\d+)\/follow$/)
    if (match) {
      const tagId = Number(match[1])
      const tag = mockTags.find((t) => t.id === tagId)
      if (tag) {
        tag.isFollowed = false
        tag.followCount = Math.max(0, tag.followCount - 1)
      }
    }
    return success(null)
  })

  // 检查是否已关注标签
  mockAdapter.onGet(/\/api\/tag\/(\d+)\/followed$/).reply((config) => {
    const path = getPath(config)
    const match = path.match(/\/api\/tag\/(\d+)\/followed$/)
    if (match) {
      const tagId = Number(match[1])
      const tag = mockTags.find((t) => t.id === tagId)
      return success(tag?.isFollowed || false)
    }
    return success(false)
  })

  // 获取我关注的标签列表
  mockAdapter.onGet(/\/api\/tag\/my\/followed/).reply(() => {
    return success(mockTags.filter((t) => t.isFollowed))
  })

  // ==================== 文件上传 ====================

  mockAdapter.onPost('/api/file/upload').reply(() => {
    return success({
      filename: 'mock-file.png',
      url: 'https://via.placeholder.com/400x300?text=Mock+Image',
      size: 10240,
      contentType: 'image/png',
      extension: 'png',
      bucketName: 'mock',
      objectName: 'mock/mock-file.png',
    })
  })

  // ==================== 兜底：未匹配的请求 ====================

  mockAdapter.onAny().reply((config) => {
    console.warn(`🎭 Mock 未匹配请求：${config.method?.toUpperCase()} ${config.url}`)
    return [200, { code: 200, msg: 'mock not implemented', data: null, timestamp: Date.now() }]
  })
}

/**
 * 重置 Mock
 */
export function resetMock() {
  mockAdapter?.restore()
  mockAdapter = null
}
