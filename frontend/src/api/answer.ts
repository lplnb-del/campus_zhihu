/**
 * 回答相关 API 接口
 */

import { get, post, put, del } from '@/utils/request'

// ==================== 类型定义 ====================

/**
 * 回答发布 DTO
 */
export interface AnswerPublishDTO {
  questionId: number
  content: string
}

/**
 * 回答更新 DTO
 */
export interface AnswerUpdateDTO {
  content: string
}

/**
 * 回答查询 DTO
 */
export interface AnswerQueryDTO {
  questionId?: number
  userId?: number
  keyword?: string
  orderBy?: 'latest' | 'hot' | 'accepted'
  page?: number
  size?: number
}

/**
 * 回答 VO
 */
export interface AnswerVO {
  id: number
  questionId: number
  questionTitle: string
  userId: number
  username: string
  userAvatar?: string
  content: string
  likeCount: number
  commentCount: number
  isAccepted: number // 0-未采纳 1-已采纳
  status: number
  createTime: string
  updateTime: string
  // 扩展字段
  isLiked?: boolean
  userPoints?: number
}

/**
 * 分页响应
 */
export interface PageResponse<T> {
  list: T[]
  total: number
  page: number
  size: number
  pages: number
}

// ==================== API 接口 ====================

/**
 * 发布回答
 */
export function publishAnswer(data: AnswerPublishDTO) {
  return post<AnswerVO>('/answer/publish', data)
}

/**
 * 更新回答
 */
export function updateAnswer(id: number, data: AnswerUpdateDTO) {
  return put<AnswerVO>(`/answer/${id}`, data)
}

/**
 * 删除回答
 */
export function deleteAnswer(id: number) {
  return del<void>(`/answer/${id}`)
}

/**
 * 获取回答详情
 */
export function getAnswerById(id: number) {
  return get<AnswerVO>(`/answer/${id}`)
}

/**
 * 获取问题的回答列表
 */
export function getAnswersByQuestion(questionId: number, page = 1, size = 20) {
  return get<PageResponse<AnswerVO>>(`/answer/question/${questionId}`, { page, size })
}

/**
 * 获取用户的回答列表
 */
export function getAnswersByUser(userId: number, page = 1, size = 20) {
  return get<PageResponse<AnswerVO>>(`/answer/user/${userId}`, { page, size })
}

/**
 * 获取我的回答列表
 */
export function getMyAnswers(page = 1, size = 20) {
  return get<PageResponse<AnswerVO>>('/answer/my', { page, size })
}

/**
 * 采纳回答
 */
export function acceptAnswer(answerId: number, questionId: number) {
  return put<void>(`/answer/${answerId}/accept`, null, { params: { questionId } })
}

/**
 * 取消采纳回答
 */
export function cancelAcceptAnswer(answerId: number, questionId: number) {
  return put<void>(`/answer/${answerId}/cancel-accept`, null, { params: { questionId } })
}

/**
 * 搜索回答
 */
export function searchAnswers(keyword: string, page = 1, size = 20) {
  return get<PageResponse<AnswerVO>>('/answer/search', { keyword, page, size })
}
