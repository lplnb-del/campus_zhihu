<template>
  <div class="my-notices-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">
        消息通知
      </h1>
      <div class="header-actions">
        <el-badge
          :value="unreadCount"
          :max="99"
          class="unread-badge"
        >
          <el-button
            type="primary"
            @click="handleMarkAllRead"
          >
            <el-icon><Check /></el-icon>
            <span>全部已读</span>
          </el-button>
        </el-badge>
      </div>
    </div>

    <!-- 通知分类 -->
    <div class="notice-tabs">
      <el-tabs
        v-model="activeTab"
        @tab-change="handleTabChange"
      >
        <el-tab-pane name="all">
          <template #label>
            <span class="tab-label">
              <el-icon><Bell /></el-icon>
              <span>全部通知</span>
              <el-badge
                v-if="tabCounts.all > 0"
                :value="tabCounts.all"
                :max="99"
                class="tab-badge"
              />
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane name="like">
          <template #label>
            <span class="tab-label">
              <el-icon><Star /></el-icon>
              <span>点赞</span>
              <el-badge
                v-if="tabCounts.like > 0"
                :value="tabCounts.like"
                :max="99"
                class="tab-badge"
              />
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane name="comment">
          <template #label>
            <span class="tab-label">
              <el-icon><ChatLineSquare /></el-icon>
              <span>评论</span>
              <el-badge
                v-if="tabCounts.comment > 0"
                :value="tabCounts.comment"
                :max="99"
                class="tab-badge"
              />
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane name="answer">
          <template #label>
            <span class="tab-label">
              <el-icon><ChatDotRound /></el-icon>
              <span>回答</span>
              <el-badge
                v-if="tabCounts.answer > 0"
                :value="tabCounts.answer"
                :max="99"
                class="tab-badge"
              />
            </span>
          </template>
        </el-tab-pane>
        <el-tab-pane name="system">
          <template #label>
            <span class="tab-label">
              <el-icon><Message /></el-icon>
              <span>系统</span>
              <el-badge
                v-if="tabCounts.system > 0"
                :value="tabCounts.system"
                :max="99"
                class="tab-badge"
              />
            </span>
          </template>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 通知列表 -->
    <div
      v-loading="loading"
      class="notices-container"
    >
      <div
        v-if="noticeList.length > 0"
        class="notice-list"
      >
        <div
          v-for="notice in noticeList"
          :key="notice.id"
          class="notice-card"
          :class="{ 'unread-notice': !notice.isRead }"
          @click="handleNoticeClick(notice)"
        >
          <!-- 通知图标 -->
          <div
            class="notice-icon"
            :class="`icon-${notice.type}`"
          >
            <el-icon v-if="notice.type === 'like'">
              <Star />
            </el-icon>
            <el-icon v-else-if="notice.type === 'comment'">
              <ChatLineSquare />
            </el-icon>
            <el-icon v-else-if="notice.type === 'answer'">
              <ChatDotRound />
            </el-icon>
            <el-icon v-else-if="notice.type === 'system'">
              <Message />
            </el-icon>
            <el-icon v-else-if="notice.type === 'accepted'">
              <CircleCheck />
            </el-icon>
            <el-icon v-else>
              <Bell />
            </el-icon>
          </div>

          <!-- 通知内容 -->
          <div class="notice-content">
            <!-- 用户信息 -->
            <div
              v-if="notice.fromUser"
              class="notice-user"
            >
              <el-avatar
                :size="32"
                :src="notice.fromUser.avatar"
              >
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="user-name">{{ notice.fromUser.username }}</span>
              <span class="user-action">{{ getActionText(notice.type) }}</span>
            </div>

            <!-- 通知标题 -->
            <div class="notice-title">
              {{ notice.title }}
            </div>

            <!-- 通知描述 -->
            <div
              v-if="notice.content"
              class="notice-desc"
              v-html="getContentPreview(notice.content)"
            />

            <!-- 通知元信息 -->
            <div class="notice-meta">
              <span class="meta-time">
                <el-icon><Clock /></el-icon>
                <span>{{ formatTime(notice.createTime) }}</span>
              </span>
              <el-tag
                v-if="!notice.isRead"
                type="danger"
                size="small"
              >
                未读
              </el-tag>
            </div>
          </div>

          <!-- 通知操作 -->
          <div class="notice-actions">
            <el-button
              v-if="!notice.isRead"
              text
              type="primary"
              size="small"
              @click.stop="handleMarkRead(notice.id)"
            >
              <el-icon><Check /></el-icon>
              <span>标为已读</span>
            </el-button>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div
        v-else
        class="empty-container"
      >
        <el-icon
          class="empty-icon"
          :size="80"
        >
          <Bell />
        </el-icon>
        <p class="empty-text">
          {{ emptyText }}
        </p>
      </div>

      <!-- 分页 -->
      <div
        v-if="total > 0"
        class="pagination"
      >
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import {
  Bell,
  Star,
  ChatLineSquare,
  ChatDotRound,
  Message,
  CircleCheck,
  User,
  Clock,
  Check,
} from '@element-plus/icons-vue'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()

// 通知类型
interface NoticeUser {
  id: number
  username: string
  avatar?: string
}

interface Notice {
  id: number
  type: 'like' | 'comment' | 'answer' | 'system' | 'accepted' | 'collection'
  title: string
  content?: string
  targetType?: string
  targetId?: number
  fromUser?: NoticeUser
  isRead: boolean
  createTime: string
}

// 当前标签页
const activeTab = ref('all')

// 未读数量
const unreadCount = ref(0)

// 各标签页未读数量
const tabCounts = ref({
  all: 0,
  like: 0,
  comment: 0,
  answer: 0,
  system: 0,
})

// 通知列表
const noticeList = ref<Notice[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 空状态文案
const emptyText = computed(() => {
  if (activeTab.value === 'like') {
    return '暂无点赞通知'
  }
  if (activeTab.value === 'comment') {
    return '暂无评论通知'
  }
  if (activeTab.value === 'answer') {
    return '暂无回答通知'
  }
  if (activeTab.value === 'system') {
    return '暂无系统通知'
  }
  return '暂无通知'
})

/**
 * localStorage 键
 */
const STORAGE_KEYS = {
  READ_NOTICE_IDS: 'campus_notice_read_ids',
  DELETED_NOTICE_IDS: 'campus_notice_deleted_ids',
}

/**
 * 从 localStorage 获取已读通知 ID 列表
 */
const getReadNoticeIds = (): Set<number> => {
  const data = localStorage.getItem(STORAGE_KEYS.READ_NOTICE_IDS)
  return data ? new Set(JSON.parse(data)) : new Set()
}

/**
 * 保存已读通知 ID 列表到 localStorage
 */
const saveReadNoticeIds = (ids: Set<number>) => {
  localStorage.setItem(STORAGE_KEYS.READ_NOTICE_IDS, JSON.stringify([...ids]))
}

/**
 * 从 localStorage 获取已删除通知 ID 列表
 */
const getDeletedNoticeIds = (): Set<number> => {
  const data = localStorage.getItem(STORAGE_KEYS.DELETED_NOTICE_IDS)
  return data ? new Set(JSON.parse(data)) : new Set()
}

/**
 * 保存已删除通知 ID 列表到 localStorage
 */
const saveDeletedNoticeIds = (ids: Set<number>) => {
  localStorage.setItem(STORAGE_KEYS.DELETED_NOTICE_IDS, JSON.stringify([...ids]))
}

/**
 * 加载通知列表
 */
const loadNotices = async () => {
  loading.value = true
  try {
    // 模拟数据 - 实际应该调用 API
    // const result = await getMyNotices(currentPage.value, pageSize.value, activeTab.value)
    
    // 模拟数据 - 使用真实的问题和回答 ID
    const mockData: Notice[] = [
      {
        id: 1,
        type: 'like',
        title: '你的回答收到了点赞',
        content: '关于"如何学习 Vue 3"的回答',
        targetType: 'answer',
        targetId: 1,
        fromUser: {
          id: 2,
          username: '张三',
          avatar: '',
        },
        isRead: false,
        createTime: new Date(Date.now() - 3600000).toISOString(),
      },
      {
        id: 2,
        type: 'comment',
        title: '你的问题收到了新评论',
        content: '非常好的问题！我也遇到过类似的情况...',
        targetType: 'question',
        targetId: 1,
        fromUser: {
          id: 3,
          username: '李四',
          avatar: '',
        },
        isRead: false,
        createTime: new Date(Date.now() - 7200000).toISOString(),
      },
      {
        id: 3,
        type: 'answer',
        title: '你的问题收到了新回答',
        content: '关于"如何优化网站性能"',
        targetType: 'question',
        targetId: 1,
        fromUser: {
          id: 4,
          username: '王五',
          avatar: '',
        },
        isRead: false,
        createTime: new Date(Date.now() - 10800000).toISOString(),
      },
      {
        id: 4,
        type: 'accepted',
        title: '你的回答被采纳了',
        content: '恭喜！你获得了 50 积分奖励',
        targetType: 'answer',
        targetId: 1,
        fromUser: {
          id: 5,
          username: '赵六',
          avatar: '',
        },
        isRead: true,
        createTime: new Date(Date.now() - 86400000).toISOString(),
      },
      {
        id: 5,
        type: 'system',
        title: '系统维护通知',
        content: '系统将于今晚 22:00-24:00 进行维护升级，期间部分功能可能无法使用...',
        fromUser: null,
        isRead: true,
        createTime: new Date(Date.now() - 172800000).toISOString(),
      },
      {
        id: 6,
        type: 'collection',
        title: '你的问题被收藏了',
        content: '关于"JavaScript 异步编程"的问题',
        targetType: 'question',
        targetId: 1,
        fromUser: {
          id: 6,
          username: '孙七',
          avatar: '',
        },
        isRead: true,
        createTime: new Date(Date.now() - 259200000).toISOString(),
      },
    ]
    
    noticeList.value = mockData
    total.value = mockData.length
    unreadCount.value = mockData.filter(n => !n.isRead).length
    
    // 更新各标签页未读数量
    tabCounts.value = {
      all: mockData.filter(n => !n.isRead).length,
      like: mockData.filter(n => !n.isRead && n.type === 'like').length,
      comment: mockData.filter(n => !n.isRead && n.type === 'comment').length,
      answer: mockData.filter(n => !n.isRead && n.type === 'answer').length,
      system: mockData.filter(n => !n.isRead && n.type === 'system').length,
    }
  } catch (error) {
    console.error('加载通知列表失败：', error)
    ElMessage.error('加载通知列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 处理标签页切换
 */
const handleTabChange = () => {
  currentPage.value = 1
  loadNotices()
}

/**
 * 处理通知点击
 */
const handleNoticeClick = (notice: Notice) => {
  // 标记为已读
  if (!notice.isRead) {
    handleMarkRead(notice.id)
  }
  
  // 跳转到相关页面
  if (notice.targetType === 'question' && notice.targetId) {
    router.push(`/question/${notice.targetId}`)
  } else if (notice.targetType === 'answer' && notice.targetId) {
    router.push(`/question/${notice.targetId}`)
  }
}

/**
 * 处理标记已读
 */
const handleMarkRead = async (noticeId: number) => {
  try {
    // 实际应该调用 API
    // await markNoticeRead(noticeId)
    
    // 更新本地状态
    const notice = noticeList.value.find(n => n.id === noticeId)
    if (notice) {
      notice.isRead = true
      unreadCount.value--
      tabCounts.value.all--
      if (notice.type === 'like') tabCounts.value.like--
      if (notice.type === 'comment') tabCounts.value.comment--
      if (notice.type === 'answer') tabCounts.value.answer--
      if (notice.type === 'system') tabCounts.value.system--
      
      // 保存到 localStorage
      const readIds = getReadNoticeIds()
      readIds.add(noticeId)
      saveReadNoticeIds(readIds)
    }
    ElMessage.success('已标记为已读')
  } catch (error) {
    console.error('标记已读失败：', error)
    ElMessage.error('操作失败')
  }
}

/**
 * 处理全部已读
 */
const handleMarkAllRead = async () => {
  try {
    await ElMessageBox.confirm('确定要将所有通知标记为已读吗？', '全部已读', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
    })

    // 更新本地状态
    noticeList.value.forEach(notice => {
      notice.isRead = true
    })
    unreadCount.value = 0
    tabCounts.value = {
      all: 0,
      like: 0,
      comment: 0,
      answer: 0,
      system: 0,
    }
    
    // 保存到 localStorage
    const allIds = new Set(noticeList.value.map(n => n.id))
    saveReadNoticeIds(allIds)
    
    ElMessage.success('已全部标记为已读')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('全部已读失败：', error)
      ElMessage.error('操作失败')
    }
  }
}

/**
 * 处理删除
 */
const handleDelete = async (noticeId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这条通知吗？', '删除通知', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      center: true,
    })

    // 更新本地状态
    const index = noticeList.value.findIndex(n => n.id === noticeId)
    if (index > -1) {
      const notice = noticeList.value[index]
      if (!notice.isRead) {
        unreadCount.value--
        tabCounts.value.all--
        if (notice.type === 'like') tabCounts.value.like--
        if (notice.type === 'comment') tabCounts.value.comment--
        if (notice.type === 'answer') tabCounts.value.answer--
        if (notice.type === 'system') tabCounts.value.system--
      }
      noticeList.value.splice(index, 1)
      total.value--
      
      // 保存到 localStorage
      const deletedIds = getDeletedNoticeIds()
      deletedIds.add(noticeId)
      saveDeletedNoticeIds(deletedIds)
    }

    ElMessage.success('删除成功')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除通知失败：', error)
      ElMessage.error('删除失败')
    }
  }
}

/**
 * 处理页码变化
 */
const handlePageChange = () => {
  loadNotices()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

/**
 * 处理每页大小变化
 */
const handleSizeChange = () => {
  currentPage.value = 1
  loadNotices()
}

/**
 * 获取操作文案
 */
const getActionText = (type: string) => {
  const actionMap: Record<string, string> = {
    like: '赞了你',
    comment: '评论了你',
    answer: '回答了你的问题',
    accepted: '采纳了你的回答',
    collection: '收藏了你的内容',
  }
  return actionMap[type] || ''
}

/**
 * 获取内容预览
 */
const getContentPreview = (content: string) => {
  const text = content.replace(/<[^>]+>/g, '')
  return text.length > 100 ? text.substring(0, 100) + '...' : text
}

/**
 * 格式化时间
 */
const formatTime = (time: string) => {
  return dayjs(time).fromNow()
}

onMounted(() => {
  // 恢复已读状态
  const readIds = getReadNoticeIds()
  const deletedIds = getDeletedNoticeIds()
  
  loadNotices()
  
  // 应用已读和已删除状态
  setTimeout(() => {
    noticeList.value.forEach(notice => {
      if (readIds.has(notice.id)) {
        notice.isRead = true
      }
    })
    
    // 过滤掉已删除的通知
    if (deletedIds.size > 0) {
      noticeList.value = noticeList.value.filter(n => !deletedIds.has(n.id))
      total.value = noticeList.value.length
    }
  }, 100)
})
</script>

<style lang="scss" scoped>
.my-notices-page {
  width: 100%;
  min-height: calc(100vh - 64px);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 24px;

  .page-title {
    font-size: 28px;
    font-weight: 700;
    color: #303133;
  }

  .header-actions {
    .unread-badge {
      :deep(.el-button) {
        display: flex;
        align-items: center;
        gap: 6px;
      }
    }
  }
}

.notice-tabs {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 24px;
  padding: 0 24px;

  :deep(.el-tabs) {
    .el-tabs__header {
      margin-bottom: 0;
    }

    .el-tabs__nav-wrap::after {
      display: none;
    }
  }
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
  position: relative;

  .tab-badge {
    position: absolute;
    top: -8px;
    right: -16px;
  }
}

.notices-container {
  min-height: 400px;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notice-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;

  &.unread-notice {
    background: linear-gradient(90deg, #f0f9ff 0%, #ffffff 100%);
    border-left: 4px solid #0056d2;

    &::before {
      content: '';
      position: absolute;
      top: 50%;
      left: -2px;
      transform: translateY(-50%);
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #0056d2;
      box-shadow: 0 0 8px rgba(0, 86, 210, 0.5);
    }
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }

  .notice-icon {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    flex-shrink: 0;

    &.icon-like {
      background: rgba(255, 193, 7, 0.1);
      color: #ffc107;
    }

    &.icon-comment {
      background: rgba(33, 150, 243, 0.1);
      color: #2196f3;
    }

    &.icon-answer {
      background: rgba(76, 175, 80, 0.1);
      color: #4caf50;
    }

    &.icon-system {
      background: rgba(156, 39, 176, 0.1);
      color: #9c27b0;
    }

    &.icon-accepted {
      background: rgba(103, 194, 58, 0.1);
      color: #67c23a;
    }

    &.icon-collection {
      background: rgba(255, 87, 34, 0.1);
      color: #ff5722;
    }
  }

  .notice-content {
    flex: 1;
    min-width: 0;

    .notice-user {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 8px;

      .user-name {
        font-size: 14px;
        font-weight: 600;
        color: #303133;
      }

      .user-action {
        font-size: 14px;
        color: #909399;
      }
    }

    .notice-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 8px;
      line-height: 1.5;
    }

    .notice-desc {
      font-size: 14px;
      color: #606266;
      line-height: 1.6;
      margin-bottom: 8px;
      padding: 8px 12px;
      background: #f5f7fa;
      border-radius: 6px;
    }

    .notice-meta {
      display: flex;
      align-items: center;
      gap: 12px;

      .meta-time {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 13px;
        color: #909399;

        .el-icon {
          font-size: 14px;
        }
      }
    }
  }

  .notice-actions {
    display: flex;
    flex-direction: column;
    gap: 8px;
    flex-shrink: 0;

    :deep(.el-button) {
      display: flex;
      align-items: center;
      gap: 4px;
      padding: 8px 12px;
    }
  }
}

.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .empty-icon {
    color: #dcdfe6;
    margin-bottom: 16px;
  }

  .empty-text {
    font-size: 14px;
    color: #909399;
  }
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;

    .header-actions {
      width: 100%;

      .unread-badge {
        width: 100%;

        :deep(.el-button) {
          width: 100%;
          justify-content: center;
        }
      }
    }
  }

  .notice-tabs {
    padding: 0 12px;

    :deep(.el-tabs) {
      .el-tabs__nav {
        display: flex;
        flex-wrap: wrap;
      }

      .el-tabs__item {
        font-size: 13px;
        padding: 0 12px;
      }
    }
  }

  .notice-card {
    flex-direction: column;
    padding: 16px;

    .notice-icon {
      width: 40px;
      height: 40px;
      font-size: 20px;
    }
  }

  .notice-actions {
    flex-direction: row;
    justify-content: flex-start;

    :deep(.el-button) {
      flex: 1;
    }
  }
}
</style>
