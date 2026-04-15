/**
 * 文件上传相关 API 接口
 */

import { post, get, del } from '@/utils/request'

// ==================== 类型定义 ====================

/**
 * 文件上传响应 DTO
 */
export interface FileUploadDTO {
  filename: string
  url: string
  size: number
  contentType: string
  extension: string
  bucketName: string
  objectName: string
}

/**
 * 上传进度回调
 */
export type UploadProgressCallback = (progress: number) => void

// ==================== API 接口 ====================

/**
 * 上传文件
 */
export function uploadFile(
  file: File,
  onProgress?: UploadProgressCallback
): Promise<FileUploadDTO> {
  return new Promise((resolve, reject) => {
    const formData = new FormData()
    formData.append('file', file)

    post<FileUploadDTO>('/file/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: (progressEvent) => {
        if (progressEvent.total && onProgress) {
          const progress = Math.round((progressEvent.loaded * 100) / progressEvent.total)
          onProgress(progress)
        }
      },
    })
      .then(resolve)
      .catch(reject)
  })
}

/**
 * 删除文件
 */
export function deleteFile(objectName: string) {
  return del<void>('/file/delete', {
    params: { objectName },
  })
}

/**
 * 获取文件URL
 */
export function getFileUrl(objectName: string) {
  return get<string>('/file/url', {
    params: { objectName },
  })
}

/**
 * 获取临时访问URL
 */
export function getPresignedUrl(objectName: string, expires = 3600) {
  return get<string>('/file/presigned-url', {
    params: { objectName, expires },
  })
}

// ==================== 工具函数 ====================

/**
 * 验证文件类型
 */
export function isValidFileType(file: File, allowedTypes: string[] = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']): boolean {
  return allowedTypes.includes(file.type)
}

/**
 * 验证文件大小
 */
export function isValidFileSize(file: File, maxSize: number = 10 * 1024 * 1024): boolean {
  return file.size > 0 && file.size <= maxSize
}

/**
 * 格式化文件大小
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round((bytes / Math.pow(k, i)) * 100) / 100 + ' ' + sizes[i]
}

/**
 * 获取文件扩展名
 */
export function getFileExtension(filename: string): string {
  const lastDotIndex = filename.lastIndexOf('.')
  if (lastDotIndex === -1) return ''
  return filename.substring(lastDotIndex + 1).toLowerCase()
}

/**
 * 生成预览URL（用于本地预览）
 */
export function getPreviewUrl(file: File): string {
  return URL.createObjectURL(file)
}

/**
 * 释放预览URL
 */
export function revokePreviewUrl(url: string): void {
  URL.revokeObjectURL(url)
}