import { get, post, del } from '@/utils/request'

/**
 * 收藏VO
 */
export interface CollectionVO {
  id: number
  userId: number
  targetType: number // 1: 问题, 2: 回答
  targetId: number
  createTime: string
  question?: {
    id: number
    title: string
    content: string
    createTime: string
  }
  answer?: {
    id: number
    content: string
    questionId: number
    questionTitle: string
    createTime: string
  }
}

/**
 * 获取我的收藏列表
 */
export function getMyCollections(page = 1, size = 20) {
  return get<{
    list: CollectionVO[]
    total: number
  }>('/collection/my', {
    params: { page, size }
  })
}

/**
 * 获取用户收藏列表
 */
export function getUserCollections(userId: number, page = 1, size = 20) {
  return get<{
    list: CollectionVO[]
    total: number
  }>(`/collection/user/${userId}`, {
    params: { page, size }
  })
}

/**
 * 收藏
 */
export function addCollection(targetType: number, targetId: number) {
  return post<void>('/collection/add', null, {
    params: { targetType, targetId }
  })
}

/**
 * 取消收藏
 */
export function removeCollection(targetType: number, targetId: number) {
  return del<void>('/collection/remove', null, {
    params: { targetType, targetId }
  })
}

/**
 * 批量检查是否已收藏
 */
export function checkCollectedBatch(targetType: number, targetIds: number[]) {
  return post<Record<number, boolean>>('/collection/check-batch', {
    targetIds
  }, {
    params: { targetType }
  })
}

/**
 * 检查单个是否已收藏
 */
export function checkCollected(targetType: number, targetId: number) {
  return get<boolean>('/collection/check', {
    params: { targetType, targetId }
  })
}
