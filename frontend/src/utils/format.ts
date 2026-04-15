/**
 * 数字格式化工具函数
 */

/**
 * 格式化数字显示（大数缩写）
 * @param num 要格式化的数字
 * @returns 格式化后的字符串
 * @example
 * formatNumber(1000) // "1.0k"
 * formatNumber(10000) // "1.0w"
 * formatNumber(1234) // "1234"
 */
export function formatNumber(num: number | undefined | null): string {
  if (num === undefined || num === null) return '0'
  if (num >= 10000) return (num / 10000).toFixed(1) + 'w'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k'
  return num.toString()
}

/**
 * 格式化文件大小
 * @param bytes 字节数
 * @returns 格式化后的字符串
 * @example
 * formatFileSize(1024) // "1.00 KB"
 * formatFileSize(1048576) // "1.00 MB"
 */
export function formatFileSize(bytes: number): string {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 格式化金额
 * @param amount 金额
 * @returns 格式化后的字符串
 */
export function formatMoney(amount: number): string {
  return '¥' + amount.toFixed(2)
}

/**
 * 格式化百分比
 * @param value 数值（0-1）
 * @returns 格式化后的字符串
 */
export function formatPercent(value: number): string {
  return (value * 100).toFixed(1) + '%'
}
