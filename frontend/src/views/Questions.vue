<template>
  <div class="questions-page">
    <!-- 页面标题和操作栏 -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">
          问题广场
        </h1>
        <p class="page-subtitle">
          探索校园中的各类问题
        </p>
      </div>
      <div class="header-right">
        <el-button
          type="primary"
          size="large"
          class="campus-btn campus-btn-primary"
          @click="handleAskQuestion"
        >
          <el-icon><EditPen /></el-icon>
          <span>提问</span>
        </el-button>
      </div>
    </div>

    <!-- 筛选和搜索栏 -->
    <div class="filter-bar">
      <div class="filter-left">
        <!-- 排序方式 -->
        <el-radio-group
          v-model="queryParams.orderBy"
          @change="handleOrderChange"
        >
          <el-radio-button value="latest">
            <el-icon><Clock /></el-icon>
            <span>最新</span>
          </el-radio-button>
          <el-radio-button value="hot">
            <el-icon><TrendCharts /></el-icon>
            <span>热门</span>
          </el-radio-button>
          <el-radio-button value="unsolved">
            <el-icon><Warning /></el-icon>
            <span>待解决</span>
          </el-radio-button>
          <el-radio-button value="reward">
            <el-icon><Coin /></el-icon>
            <span>悬赏</span>
          </el-radio-button>
        </el-radio-group>

        <!-- 标签筛选 -->
        <el-select
          v-model="queryParams.tagId"
          placeholder="选择话题"
          clearable
          filterable
          style="width: 200px"
          @change="handleTagChange"
        >
          <el-option
            v-for="tag in tags"
            :key="tag.id"
            :label="tag.name"
            :value="tag.id"
          >
            <span style="float: left">{{ tag.name }}</span>
            <span style="float: right; color: #909399; font-size: 13px">
              {{ tag.questionCount }}
            </span>
          </el-option>
        </el-select>
      </div>

      <div class="filter-right">
        <!-- 搜索框 -->
        <el-input
          v-model="searchKeyword"
          placeholder="搜索问题..."
          clearable
          style="width: 300px"
          @keyup.enter="handleSearch"
          @clear="handleSearchClear"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button
              :icon="Search"
              @click="handleSearch"
            />
          </template>
        </el-input>
      </div>
    </div>

    <!-- 问题列表 -->
    <div class="questions-container">
      <div class="questions-main">
        <!-- 加载中 -->
        <div
          v-if="loading"
          class="skeleton-list"
        >
          <div
            v-for="i in 5"
            :key="i"
            class="skeleton-card"
          >
            <div class="skeleton-title" />
            <div class="skeleton-text" />
            <div class="skeleton-text" />
            <div
              class="skeleton-text"
              style="width: 60%"
            />
          </div>
        </div>

        <!-- 问题列表 -->
        <div
          v-else-if="questionList.length > 0"
          class="question-list"
        >
          <div
            v-for="question in questionList"
            :key="question.id"
            class="question-card"
            @click="handleQuestionDetail(question.id)"
          >
            <!-- 悬赏标识 -->
            <div
              v-if="question.rewardPoints > 0"
              class="reward-corner"
            >
              <el-icon><Coin /></el-icon>
              <span>{{ question.rewardPoints }}</span>
            </div>

            <!-- 问题状态标识 -->
            <div
              v-if="question.isSolved"
              class="solved-badge"
            >
              <el-icon><CircleCheck /></el-icon>
              <span>已解决</span>
            </div>

            <!-- 问题标题 -->
            <h3 class="question-title">
              {{ question.title }}
            </h3>

            <!-- 问题内容摘要 -->
            <div
              class="question-content"
              v-html="getContentPreview(question.content)"
            />

            <!-- 标签 -->
            <div
              v-if="question.tags && question.tags.length > 0"
              class="question-tags"
            >
              <span
                v-for="tag in question.tags"
                :key="tag.id"
                class="campus-tag"
                @click.stop="handleTagClick(tag.id)"
              >
                {{ tag.name }}
              </span>
            </div>

            <!-- 问题元信息 -->
            <div class="question-meta">
              <div class="meta-left">
                <el-avatar
                  :size="32"
                  :src="question.userAvatar || `https://ui-avatars.com/api/?name=${question.username}&background=0056D2&color=fff&size=64`"
                >
                  <el-icon><User /></el-icon>
                </el-avatar>
                <div class="user-info">
                  <span class="username">{{ question.username }}</span>
                  <span class="time">{{ formatTime(question.createTime) }}</span>
                </div>
              </div>
              <div class="meta-right">
                <span class="stat-item">
                  <el-icon><View /></el-icon>
                  <span>{{ formatNumber(question.viewCount) }}</span>
                </span>
                <span class="stat-item">
                  <el-icon><ChatLineSquare /></el-icon>
                  <span>{{ formatNumber(question.answerCount) }}</span>
                </span>
                <span class="stat-item">
                  <el-icon><Star /></el-icon>
                  <span>{{ formatNumber(question.collectionCount) }}</span>
                </span>
              </div>
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
            <Document />
          </el-icon>
          <p class="empty-text">
            {{ emptyText }}
          </p>
          <el-button
            type="primary"
            @click="handleResetFilter"
          >
            重置筛选
          </el-button>
        </div>

        <!-- 分页 -->
        <div
          v-if="total > 0"
          class="pagination"
        >
          <el-pagination
            v-model:current-page="queryParams.page"
            v-model:page-size="queryParams.size"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </div>

      <!-- 右侧边栏 -->
      <div class="questions-sidebar">
        <!-- 热门标签 -->
        <div class="sidebar-card">
          <div class="card-header">
            <el-icon><PriceTag /></el-icon>
            <span>热门话题</span>
          </div>
          <div class="card-content">
            <div
              v-loading="loadingTags"
              class="tag-list"
            >
              <div
                v-for="tag in hotTags"
                :key="tag.id"
                class="hot-tag-item"
                :class="{ active: queryParams.tagId === tag.id }"
                @click="handleTagClick(tag.id)"
              >
                <span class="tag-name">{{ tag.name }}</span>
                <span class="tag-count">{{ tag.questionCount }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 筛选统计 -->
        <div class="sidebar-card">
          <div class="card-header">
            <el-icon><DataAnalysis /></el-icon>
            <span>统计信息</span>
          </div>
          <div class="card-content">
            <div class="stats-list">
              <div class="stats-item">
                <span class="label">总问题数</span>
                <span class="value">{{ formatNumber(total) }}</span>
              </div>
              <div class="stats-item">
                <span class="label">当前页</span>
                <span class="value">{{ queryParams.page }} / {{ totalPages }}</span>
              </div>
              <div class="stats-item">
                <span class="label">每页显示</span>
                <span class="value">{{ queryParams.size }} 条</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 快捷操作 -->
        <div class="sidebar-card">
          <div class="card-header">
            <el-icon><Guide /></el-icon>
            <span>快捷操作</span>
          </div>
          <div class="card-content">
            <div class="quick-actions">
              <el-button
                type="primary"
                plain
                block
                @click="handleAskQuestion"
              >
                <el-icon><EditPen /></el-icon>
                <span>我要提问</span>
              </el-button>
              <el-button
                plain
                block
                @click="handleMyQuestions"
              >
                <el-icon><QuestionFilled /></el-icon>
                <span>我的提问</span>
              </el-button>
              <el-button
                plain
                block
                @click="handleMyCollections"
              >
                <el-icon><Star /></el-icon>
                <span>我的收藏</span>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { getQuestionList, type QuestionVO, type QuestionQueryDTO } from '@/api/question'
import { getHotTags, getAllTags, type TagVO } from '@/api/tag'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import {
  EditPen,
  Clock,
  TrendCharts,
  Warning,
  Coin,
  Search,
  User,
  View,
  ChatLineSquare,
  Star,
  Document,
  PriceTag,
  DataAnalysis,
  Guide,
  QuestionFilled,
  CircleCheck,
} from '@element-plus/icons-vue'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 查询参数
const queryParams = reactive<QuestionQueryDTO>({
  keyword: '',
  tagId: undefined,
  orderBy: 'latest',
  page: 1,
  size: 20,
})

// 搜索关键词（单独管理，回车或点击搜索按钮时才应用到queryParams）
const searchKeyword = ref('')

// 问题列表
const questionList = ref<QuestionVO[]>([])
const loading = ref(false)
const total = ref(0)

// 标签列表
const tags = ref<TagVO[]>([])
const hotTags = ref<TagVO[]>([])
const loadingTags = ref(false)

// 总页数
const totalPages = computed(() => {
  return Math.ceil(total.value / queryParams.size)
})

// 空状态文案
const emptyText = computed(() => {
  if (queryParams.keyword) {
    return `未找到与"${queryParams.keyword}"相关的问题`
  }
  if (queryParams.tagId) {
    return '该话题下暂无问题'
  }
  return '暂无问题'
})

/**
 * 初始化查询参数（从URL）
 */
const initQueryParams = () => {
  if (route.query.keyword) {
    queryParams.keyword = route.query.keyword as string
    searchKeyword.value = queryParams.keyword
  }
  if (route.query.tagId) {
    queryParams.tagId = Number(route.query.tagId)
  }
  if (route.query.orderBy) {
    queryParams.orderBy = route.query.orderBy as any
  }
}

/**
 * 加载问题列表
 */
const loadQuestions = async () => {
  loading.value = true
  try {
    const result = await getQuestionList(queryParams)
    questionList.value = result.list
    total.value = result.total
  } catch (error) {
    console.error('加载问题列表失败：', error)
    ElMessage.error('加载问题列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 加载标签列表
 */
const loadTags = async () => {
  loadingTags.value = true
  try {
    const [allTags, topTags] = await Promise.all([
      getAllTags(),
      getHotTags(15),
    ])
    tags.value = allTags
    hotTags.value = topTags
  } catch (error) {
    console.error('加载标签列表失败：', error)
  } finally {
    loadingTags.value = false
  }
}

/**
 * 处理排序变化
 */
const handleOrderChange = () => {
  queryParams.page = 1
  updateURL()
  loadQuestions()
}

/**
 * 处理标签变化
 */
const handleTagChange = () => {
  queryParams.page = 1
  updateURL()
  loadQuestions()
}

/**
 * 处理搜索
 */
const handleSearch = () => {
  queryParams.keyword = searchKeyword.value.trim()
  queryParams.page = 1
  updateURL()
  loadQuestions()
}

/**
 * 处理清空搜索
 */
const handleSearchClear = () => {
  searchKeyword.value = ''
  queryParams.keyword = ''
  queryParams.page = 1
  updateURL()
  loadQuestions()
}

/**
 * 处理标签点击
 */
const handleTagClick = (tagId: number) => {
  if (queryParams.tagId === tagId) {
    queryParams.tagId = undefined
  } else {
    queryParams.tagId = tagId
  }
  queryParams.page = 1
  updateURL()
  loadQuestions()
}

/**
 * 处理页码变化
 */
const handlePageChange = (page: number) => {
  queryParams.page = page
  updateURL()
  loadQuestions()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

/**
 * 处理每页大小变化
 */
const handleSizeChange = (size: number) => {
  queryParams.size = size
  queryParams.page = 1
  updateURL()
  loadQuestions()
}

/**
 * 处理重置筛选
 */
const handleResetFilter = () => {
  queryParams.keyword = ''
  queryParams.tagId = undefined
  queryParams.orderBy = 'latest'
  queryParams.page = 1
  searchKeyword.value = ''
  updateURL()
  loadQuestions()
}

/**
 * 更新URL
 */
const updateURL = () => {
  const query: any = {}
  if (queryParams.keyword) query.keyword = queryParams.keyword
  if (queryParams.tagId) query.tagId = queryParams.tagId.toString()
  if (queryParams.orderBy !== 'latest') query.orderBy = queryParams.orderBy

  router.replace({ query })
}

/**
 * 处理问题详情
 */
const handleQuestionDetail = (id: number) => {
  router.push(`/question/${id}`)
}

/**
 * 处理提问
 */
const handleAskQuestion = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push('/ask')
}

/**
 * 处理我的提问
 */
const handleMyQuestions = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push('/user/questions')
}

/**
 * 处理我的收藏
 */
const handleMyCollections = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push('/user/collections')
}

/**
 * 获取内容预览
 */
const getContentPreview = (content: string) => {
  // 移除HTML标签
  const text = content.replace(/<[^>]+>/g, '')
  // 截取前150个字符
  return text.length > 150 ? text.substring(0, 150) + '...' : text
}

/**
 * 格式化时间
 */
const formatTime = (time: string) => {
  return dayjs(time).fromNow()
}

/**
 * 格式化数字
 */
const formatNumber = (num: number | undefined | null) => {
  if (num === undefined || num === null) {
    return '0'
  }
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

// 监听路由变化
watch(() => route.query, () => {
  if (route.path === '/questions') {
    initQueryParams()
    loadQuestions()
  }
})

onMounted(() => {
  initQueryParams()
  loadQuestions()
  loadTags()
})
</script>

<style lang="scss" scoped>
.questions-page {
  width: 100%;
  min-height: calc(100vh - 64px);
}

// ==========================================
// 页面头部
// ==========================================
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  padding: 24px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .header-left {
    .page-title {
      font-size: 28px;
      font-weight: 700;
      color: #303133;
      margin-bottom: 4px;
    }

    .page-subtitle {
      font-size: 14px;
      color: #909399;
    }
  }

  .header-right {
    :deep(.el-button) {
      display: flex;
      align-items: center;
      gap: 6px;
    }
  }
}

// ==========================================
// 筛选栏
// ==========================================
.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px 24px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .filter-left {
    display: flex;
    align-items: center;
    gap: 16px;
    flex: 1;

    :deep(.el-radio-button__inner) {
      display: flex;
      align-items: center;
      gap: 4px;
      padding: 10px 16px;
    }
  }

  .filter-right {
    display: flex;
    align-items: center;
    gap: 12px;
  }
}

// ==========================================
// 问题容器
// ==========================================
.questions-container {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
  align-items: start;
}

// ==========================================
// 主要区域
// ==========================================
.questions-main {
  .question-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .question-card {
    background: #ffffff;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    transition: all 0.3s ease;
    cursor: pointer;
    position: relative;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
    }

    .reward-corner {
      position: absolute;
      top: 16px;
      right: 16px;
      display: flex;
      align-items: center;
      gap: 4px;
      padding: 6px 12px;
      background: linear-gradient(135deg, #ff6b6b 0%, #ff8e53 100%);
      color: #ffffff;
      border-radius: 20px;
      font-size: 12px;
      font-weight: 600;
      box-shadow: 0 2px 8px rgba(255, 107, 107, 0.3);
    }

    .solved-badge {
      position: absolute;
      top: 16px;
      right: 16px;
      display: flex;
      align-items: center;
      gap: 4px;
      padding: 6px 12px;
      background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
      color: #ffffff;
      border-radius: 20px;
      font-size: 12px;
      font-weight: 600;
    }

    .question-title {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 12px;
      line-height: 1.6;
      padding-right: 100px;
      transition: color 0.3s ease;

      &:hover {
        color: #0056d2;
      }
    }

    .question-content {
      font-size: 14px;
      color: #606266;
      line-height: 1.8;
      margin-bottom: 16px;
    }

    .question-tags {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
      margin-bottom: 16px;
    }

    .question-meta {
      display: flex;
      align-items: center;
      justify-content: space-between;

      .meta-left {
        display: flex;
        align-items: center;
        gap: 12px;

        .user-info {
          display: flex;
          flex-direction: column;
          gap: 4px;

          .username {
            font-size: 14px;
            font-weight: 500;
            color: #303133;
          }

          .time {
            font-size: 12px;
            color: #909399;
          }
        }
      }

      .meta-right {
        display: flex;
        align-items: center;
        gap: 16px;

        .stat-item {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 13px;
          color: #909399;

          .el-icon {
            font-size: 16px;
          }
        }
      }
    }
  }

  .pagination {
    margin-top: 24px;
    display: flex;
    justify-content: center;
  }
}

// ==========================================
// 侧边栏
// ==========================================
.questions-sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: sticky;
  top: 88px;

  .sidebar-card {
    background: #ffffff;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    overflow: hidden;

    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 16px 20px;
      border-bottom: 1px solid #e4e7ed;
      font-size: 15px;
      font-weight: 600;
      color: #303133;

      .el-icon {
        color: #0056d2;
      }
    }

    .card-content {
      padding: 16px 20px;
    }
  }

  .tag-list {
    display: flex;
    flex-direction: column;
    gap: 8px;

    .hot-tag-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 10px 12px;
      background: #f5f7fa;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s ease;

      &:hover {
        background: #e6f0ff;
        transform: translateX(4px);
      }

      &.active {
        background: #0056d2;
        color: #ffffff;

        .tag-count {
          color: rgba(255, 255, 255, 0.8);
        }
      }

      .tag-name {
        font-size: 14px;
        font-weight: 500;
      }

      .tag-count {
        font-size: 12px;
        color: #909399;
      }
    }
  }

  .stats-list {
    display: flex;
    flex-direction: column;
    gap: 12px;

    .stats-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 8px 0;
      border-bottom: 1px dashed #e4e7ed;

      &:last-child {
        border-bottom: none;
      }

      .label {
        font-size: 13px;
        color: #606266;
      }

      .value {
        font-size: 15px;
        font-weight: 600;
        color: #0056d2;
      }
    }
  }

  .quick-actions {
    display: flex;
    flex-direction: column;
    gap: 8px;

    :deep(.el-button) {
      justify-content: flex-start;
      gap: 8px;
    }
  }
}

// ==========================================
// 响应式设计
// ==========================================
@media (max-width: 1024px) {
  .questions-container {
    grid-template-columns: 1fr;
  }

  .questions-sidebar {
    display: none;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;

    .header-right {
      width: 100%;

      :deep(.el-button) {
        width: 100%;
        justify-content: center;
      }
    }
  }

  .filter-bar {
    flex-direction: column;
    align-items: stretch;

    .filter-left {
      flex-direction: column;

      :deep(.el-radio-group) {
        width: 100%;
      }

      :deep(.el-select) {
        width: 100% !important;
      }
    }

    .filter-right {
      width: 100%;

      :deep(.el-input) {
        width: 100% !important;
      }
    }
  }

  .question-card {
    .question-title {
      padding-right: 0 !important;
    }

    .reward-corner,
    .solved-badge {
      position: static;
      margin-bottom: 12px;
      display: inline-flex;
    }
  }
}
</style>
