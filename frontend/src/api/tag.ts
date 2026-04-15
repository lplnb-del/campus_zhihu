/**
 * 标签相关 API 接口
 */

import { get, post, put, del } from '@/utils/request'

// ==================== 类型定义 ====================

/**
 * 标签 VO
 */
export interface TagVO {
  id: number
  name: string
  description?: string
  icon?: string
  color?: string
  category?: string
  questionCount: number
  followCount: number
  createTime: string
  updateTime: string
  isFollowed?: boolean
}

/**
 * 标签创建 DTO
 */
export interface TagCreateDTO {
  name: string
  description?: string
  icon?: string
  color?: string
}

/**
 * 标签更新 DTO
 */
export interface TagUpdateDTO {
  name?: string
  description?: string
  icon?: string
  color?: string
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
  totalQuestions?: number
}

// ==================== API 接口 ====================

/**
 * 创建标签
 */
export function createTag(data: TagCreateDTO) {
  return post<TagVO>('/tag/create', data)
}

/**
 * 更新标签
 */
export function updateTag(id: number, data: TagUpdateDTO) {
  return put<TagVO>(`/tag/${id}`, data)
}

/**
 * 删除标签
 */
export function deleteTag(id: number) {
  return del<void>(`/tag/${id}`)
}

/**
 * 获取标签详情
 */
export function getTagById(id: number) {
  return get<TagVO>(`/tag/${id}`)
}

/**
 * 获取标签列表
 */
export function getTagList(page = 1, size = 50, category?: string, sortBy?: string) {
  return get<PageResponse<TagVO>>('/tag/list', { page, size, category, sortBy })
}

/**
 * 获取所有标签（不分页）
 */
export function getAllTags() {
  return get<TagVO[]>('/tag/all')
}

/**
 * 获取热门标签
 */
export function getHotTags(limit = 20) {
  return get<TagVO[]>('/tag/hot', { limit })
}

/**
 * 搜索标签
 */
export function searchTags(keyword: string, page = 1, size = 20, sortBy?: string) {
  return get<PageResponse<TagVO>>('/tag/search', { keyword, page, size, sortBy })
}

/**
 * 关注标签
 */
export function followTag(tagId: number) {
  return post<void>(`/tag/${tagId}/follow`)
}

/**
 * 取消关注标签
 */
export function unfollowTag(tagId: number) {
  return del<void>(`/tag/${tagId}/follow`)
}

/**
 * 检查是否已关注标签
 */
export function checkFollowed(tagId: number) {
  return get<boolean>(`/tag/${tagId}/followed`)
}

/**
 * 获取用户关注的标签列表
 */
export function getFollowedTags(userId: number) {
  return get<TagVO[]>(`/tag/user/${userId}/followed`)
}

/**
 * 获取我关注的标签列表
 */
export function getMyFollowedTags() {
  return get<TagVO[]>('/tag/my/followed')
}

/**
 * 获取标签下的问题数量
 */
export function getTagQuestionCount(tagId: number) {
  return get<number>(`/tag/${tagId}/question-count`)
}
