/**
 * 问题相关 API 接口
 */

import { get, post, put, del } from "@/utils/request";

// ==================== 类型定义 ====================

/**
 * 标签 VO
 */
export interface TagVO {
  id: number;
  name: string;
  description?: string;
  icon?: string;
  color?: string;
  questionCount: number;
  followCount: number;
  createTime: string;
}

/**
 * 问题发布 DTO
 */
export interface QuestionPublishDTO {
  title: string;
  content: string;
  tagIds: number[];
  rewardPoints?: number;
  isDraft?: boolean;
}

/**
 * 问题更新 DTO
 */
export interface QuestionUpdateDTO {
  title?: string;
  content?: string;
  tagIds?: number[];
  rewardPoints?: number;
  isDraft?: boolean;
}

/**
 * 问题查询 DTO
 */
export interface QuestionQueryDTO {
  keyword?: string;
  tagId?: number;
  userId?: number;
  status?: number;
  orderBy?: "latest" | "hot" | "reward" | "unsolved";
  page?: number;
  size?: number;
}

/**
 * 问题 VO
 */
export interface QuestionVO {
  id: number;
  userId: number;
  username: string;
  userAvatar?: string;
  title: string;
  content: string;
  tags: TagVO[];
  rewardPoints: number;
  viewCount: number;
  answerCount: number;
  collectionCount: number;
  likeCount: number;
  status: number;
  isDraft: boolean;
  isSolved: boolean;
  acceptedAnswerId?: number;
  createTime: string;
  updateTime: string;
  // 扩展字段
  isCollected?: boolean;
  isLiked?: boolean;
}

/**
 * 分页响应
 */
export interface PageResponse<T> {
  list: T[];
  total: number;
  page: number;
  size: number;
  pages: number;
}

// ==================== API 接口 ====================

/**
 * 发布问题
 */
export function publishQuestion(data: QuestionPublishDTO) {
  return post<QuestionVO>("/question/publish", data);
}

/**
 * 更新问题
 */
export function updateQuestion(id: number, data: QuestionUpdateDTO) {
  return put<QuestionVO>(`/question/${id}`, data);
}

/**
 * 删除问题
 */
export function deleteQuestion(id: number) {
  return del<void>(`/question/${id}`);
}

/**
 * 获取问题详情
 */
export function getQuestionById(id: number) {
  return get<QuestionVO>(`/question/${id}`);
}

/**
 * 获取问题列表（分页）
 */
export function getQuestionList(params: QuestionQueryDTO) {
  return get<PageResponse<QuestionVO>>("/question/list", params);
}

/**
 * 分页查询问题（POST方式，支持复杂查询）
 */
export function getQuestionPage(data: QuestionQueryDTO) {
  return post<PageResponse<QuestionVO>>('/question/page', data)
}

/**
 * 获取问题详情（不增加浏览次数，用于编辑）
 */
export function getQuestionDetail(id: number) {
  return get<QuestionVO>(`/question/${id}/detail`)
}

/**
 * 获取最新问题
 */
export function getLatestQuestions(page = 1, size = 20) {
  return get<PageResponse<QuestionVO>>("/question/latest", { page, size });
}

/**
 * 获取热门问题
 */
export function getHotQuestions(page = 1, size = 20) {
  return get<PageResponse<QuestionVO>>("/question/hot", { page, size });
}

/**
 * 获取待解决问题
 */
export function getUnsolvedQuestions(page = 1, size = 20) {
  return get<PageResponse<QuestionVO>>("/question/unsolved", { page, size });
}

/**
 * 获取悬赏问题
 */
export function getRewardQuestions(page = 1, size = 20) {
  return get<PageResponse<QuestionVO>>("/question/reward", { page, size });
}

/**
 * 搜索问题
 */
export function searchQuestions(keyword: string, page = 1, size = 20) {
  return get<PageResponse<QuestionVO>>("/question/search", {
    keyword,
    page,
    size,
  });
}

/**
 * 获取用户的问题列表
 */
export function getUserQuestions(userId: number, page = 1, size = 20) {
  return get<PageResponse<QuestionVO>>(`/question/user/${userId}`, {
    page,
    size,
  });
}

/**
 * 获取我的问题列表
 */
export function getMyQuestions(page = 1, size = 20) {
  return get<PageResponse<QuestionVO>>("/question/my", { page, size });
}

/**
 * 关闭问题
 */
export function closeQuestion(id: number) {
  return put<void>(`/question/${id}/close`);
}

/**
 * 获取相关问题推荐
 */
export function getRelatedQuestions(id: number, size = 5) {
  return get<QuestionVO[]>(`/question/${id}/related`, { size });
}

/**
 * 根据标签获取问题列表
 */
export function getQuestionsByTag(
  tagId: number,
  page = 1,
  size = 20,
  orderBy: "hot" | "latest" | "unsolved" | "reward" = "hot",
) {
  return get<PageResponse<QuestionVO>>(`/question/tag/${tagId}`, {
    orderBy,
    page,
    size,
  });
}
