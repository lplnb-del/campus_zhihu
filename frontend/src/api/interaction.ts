/**
 * 交互相关 API 接口（评论、点赞、收藏）
 */

import { get, post, put, del } from '@/utils/request'

// ==================== 类型定义 ====================

/**
 * 目标类型常量
 */
export const TargetType = {
  QUESTION: 1,    // 问题
  ANSWER: 2,     // 回答
  COMMENT: 3,     // 评论
} as const

/**
 * 评论发布 DTO
 */
export interface CommentPublishDTO {
  targetType: number
  targetId: number
  content: string
  parentId?: number
  replyToUserId?: number
}

/**
 * 评论 VO
 */
export interface CommentVO {
  id: number
  targetType: string
  targetId: number
  userId: number
  username: string
  userAvatar?: string
  content: string
  parentId?: number
  replyToUserId?: number
  replyToUsername?: string
  likeCount: number
  status: number
  createTime: string
  updateTime: string
  // 扩展字段
  replies?: CommentVO[]
  isLiked?: boolean
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

/**
 * 发布评论
 */
export function publishComment(data: CommentPublishDTO) {
  return post<CommentVO>('/comment/publish', data)
}

/**
 * 删除评论
 */
export function deleteComment(id: number) {
  return del<void>(`/comment/${id}`)
}

/**
 * 获取评论详情
 */
export function getCommentById(id: number) {
  return get<CommentVO>(`/comment/${id}`)
}

/**
 * 获取目标的评论列表（带嵌套回复）
 */
export function getCommentsByTarget(
  targetType: string | number,
  targetId: number,
  page = 1,
  size = 20
) {
  // 将字符串类型转换为数字
  let targetTypeNum: number
  if (typeof targetType === 'string') {
    switch (targetType) {
      case 'question':
        targetTypeNum = TargetType.QUESTION
        break
      case 'answer':
        targetTypeNum = TargetType.ANSWER
        break
      case 'comment':
        targetTypeNum = TargetType.COMMENT
        break
      default:
        targetTypeNum = TargetType.ANSWER
    }
  } else {
    targetTypeNum = targetType
  }

  return get<PageResponse<CommentVO>>('/comment/target', {
    targetType: targetTypeNum,
    targetId,
    page,
    size,
  })
}

/**
 * 获取用户的评论列表
 */
export function getCommentsByUser(userId: number, page = 1, size = 20) {
  return get<PageResponse<CommentVO>>(`/comment/user/${userId}`, { page, size })
}

/**
 * 获取我的评论列表
 */
export function getMyComments(page = 1, size = 20) {
  return get<PageResponse<CommentVO>>('/comment/my', { page, size })
}

/**
 * 屏蔽评论（管理员）
 */
export function blockComment(id: number) {
  return put<void>(`/comment/${id}/block`)
}

/**
 * 审核通过评论（管理员）
 */
export function approveComment(id: number) {
  return put<void>(`/comment/${id}/approve`)
}

// ==================== 点赞相关 ====================

/**
 * 点赞/取消点赞
 * @param targetType 目标类型（1-问题，2-回答，3-评论）
 * @param targetId 目标ID
 */
export function toggleLike(targetType: number, targetId: number) {
  return post<boolean>('/like/toggle', null, { params: { targetType, targetId } })
}

/**
 * 点赞
 * @param targetType 目标类型（1-问题，2-回答，3-评论）
 * @param targetId 目标ID
 */
export function addLike(targetType: number, targetId: number) {
  return post<void>('/like', null, { params: { targetType, targetId } })
}

/**
 * 取消点赞
 * @param targetType 目标类型（1-问题，2-回答，3-评论）
 * @param targetId 目标ID
 */
export function removeLike(targetType: number, targetId: number) {
  return del<void>('/like', { params: { targetType, targetId } })
}

/**
 * 检查是否已点赞
 * @param targetType 目标类型（1-问题，2-回答，3-评论）
 * @param targetId 目标ID
 */
export function checkLiked(targetType: number, targetId: number) {
  return get<boolean>('/like/check', { targetType, targetId })
}

/**
 * 批量检查是否已点赞
 * @param targetType 目标类型（1-问题，2-回答，3-评论）
 * @param targetIds 目标ID列表
 */
export function checkLikedBatch(targetType: number, targetIds: number[]) {
  return post<Record<number, boolean>>('/like/check-batch', {
    targetType,
    targetIds,
  })
}

/**
 * 获取用户的点赞列表
 */
export function getUserLikes(userId: number, page = 1, size = 20) {
  return get<PageResponse<any>>(`/like/user/${userId}`, { page, size })
}

/**
 * 获取我的点赞列表
 */
export function getMyLikes(page = 1, size = 20) {
  return get<PageResponse<any>>('/like/my', { page, size })
}

/**
 * 获取目标的点赞数
 * @param targetType 目标类型（1-问题，2-回答，3-评论）
 * @param targetId 目标ID
 */
export function getLikeCount(targetType: number, targetId: number) {
  return get<number>('/like/count/target', { targetType, targetId })
}

/**
 * 获取用户的点赞总数
 */
export function getUserLikeCount(userId: number) {
  return get<number>(`/like/count/user/${userId}`)
}

// ==================== 收藏相关 ====================

/**
 * 收藏/取消收藏
 * @param targetType 目标类型（1-问题，2-回答）
 * @param targetId 目标ID
 */
export function toggleCollection(targetType: number, targetId: number) {
  return post<boolean>('/collection/toggle', null, { params: { targetType, targetId } })
}

/**
 * 收藏
 * @param targetType 目标类型（1-问题，2-回答）
 * @param targetId 目标ID
 */
export function addCollection(targetType: number, targetId: number) {
  return post<void>('/collection', null, { params: { targetType, targetId } })
}

/**
 * 取消收藏
 * @param targetType 目标类型（1-问题，2-回答）
 * @param targetId 目标ID
 */
export function removeCollection(targetType: number, targetId: number) {
  return del<void>('/collection', { params: { targetType, targetId } })
}

/**
 * 检查是否已收藏
 * @param targetType 目标类型（1-问题，2-回答）
 * @param targetId 目标ID
 */
export function checkCollected(targetType: number, targetId: number) {
  return get<boolean>('/collection/check', { targetType, targetId })
}

/**
 * 批量检查是否已收藏
 * @param targetType 目标类型（1-问题，2-回答）
 * @param targetIds 目标ID列表
 */
export function checkCollectedBatch(targetType: number, targetIds: number[]) {
  return post<Record<number, boolean>>('/collection/check-batch', {
    targetType,
    targetIds,
  })
}

/**
 * 获取用户的收藏列表
 */
export function getUserCollections(userId: number, page = 1, size = 20) {
  return get<PageResponse<any>>(`/collection/user/${userId}`, { page, size })
}

/**
 * 获取我的收藏列表
 */
export function getMyCollections(page = 1, size = 20) {
  return get<PageResponse<any>>('/collection/my', { page, size })
}

/**
 * 获取目标的收藏数
 * @param targetType 目标类型（1-问题，2-回答）
 * @param targetId 目标ID
 */
export function getCollectionCount(targetType: number, targetId: number) {
  return get<number>('/collection/count/target', { targetType, targetId })
}

/**
 * 获取用户的收藏总数
 */
export function getUserCollectionCount(userId: number) {
  return get<number>(`/collection/count/user/${userId}`)
}
