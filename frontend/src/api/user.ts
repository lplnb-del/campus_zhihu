/**
 * 用户相关 API 接口
 */

import { get, post, put } from '@/utils/request'

// ==================== 类型定义 ====================

/**
 * 用户注册 DTO
 */
export interface RegisterDTO {
  username: string
  password: string
  email: string
  studentId?: string
  realName?: string
}

/**
 * 用户登录 DTO
 */
export interface LoginDTO {
  username: string
  password: string
}

/**
 * 用户更新 DTO
 */
export interface UserUpdateDTO {
  email?: string
  avatar?: string
  bio?: string
  school?: string
  major?: string
  grade?: string
}

/**
 * 用户 VO
 */
export interface UserVO {
  id: number
  username: string
  email: string
  avatar?: string
  bio?: string
  school?: string
  major?: string
  grade?: string
  studentId?: string
  realName?: string
  points: number
  questionCount: number
  answerCount: number
  collectionCount: number
  status: number
  createTime: string
  updateTime: string
}

/**
 * 登录响应 VO
 */
export interface LoginVO {
  token: string
  tokenType?: string
  expiresIn?: number
  userInfo: UserVO
}

// ==================== API 接口 ====================

/**
 * 用户注册
 */
export function register(data: RegisterDTO) {
  return post<UserVO>('/user/register', data)
}

/**
 * 用户登录
 */
export function login(data: LoginDTO) {
  return post<LoginVO>('/user/login', data)
}

/**
 * 获取当前登录用户信息
 */
export function getCurrentUser() {
  return get<UserVO>('/user/info')
}

/**
 * 根据 ID 获取用户信息
 */
export function getUserById(id: number) {
  return get<UserVO>(`/user/${id}`)
}

/**
 * 根据用户名获取用户信息
 */
export function getUserByUsername(username: string) {
  return get<UserVO>(`/user/username/${username}`)
}

/**
 * 更新用户信息
 */
export function updateUser(data: UserUpdateDTO) {
  return put<UserVO>('/user/update', data)
}

/**
 * 修改密码
 */
export function updatePassword(oldPassword: string, newPassword: string) {
  return put<void>('/user/password', { oldPassword, newPassword })
}

/**
 * 检查用户名是否存在
 */
export function checkUsername(username: string) {
  return get<boolean>('/user/check/username', { username })
}

/**
 * 检查学号是否存在
 */
export function checkStudentId(studentId: string) {
  return get<boolean>('/user/check/studentId', { studentId })
}

/**
 * 用户登出
 */
export function logout() {
  return post<void>('/user/logout')
}

/**
 * 刷新 Token
 */
export function refreshToken() {
  return post<LoginVO>('/user/refresh-token')
}

/**
 * 验证邮箱
 */
export function verifyEmail(token: string) {
  return post<boolean>('/user/verify-email', { token })
}

/**
 * 重新发送验证邮件
 */
export function resendVerificationEmail(email: string) {
  return post<boolean>('/user/resend-verification-email', null, { params: { email } })
}

/**
 * 获取用户统计信息
 */
export function getUserStats(userId: number) {
  return get<{
    questionCount: number
    answerCount: number
    collectionCount: number
    likeCount: number
    points: number
  }>(`/user/${userId}/stats`)
}
