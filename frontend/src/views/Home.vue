<template>
  <div class="home-page">
    <!-- Banner 区域 -->
    <section class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">
          <span class="gradient-text">CampusZhihu</span>
        </h1>
        <p class="hero-subtitle">
          连接校园智慧 · 分享知识力量
        </p>
        <p class="hero-description">
          一个集学术交流、生活互助、积分悬赏于一体的高校知识共享平台
        </p>
        <div class="hero-actions">
          <el-button
            type="primary"
            size="large"
            class="campus-btn campus-btn-primary"
            @click="handleAskQuestion"
          >
            <el-icon><EditPen /></el-icon>
            <span>立即提问</span>
          </el-button>
          <el-button
            size="large"
            @click="handleExplore"
          >
            <el-icon><Compass /></el-icon>
            <span>探索问题</span>
          </el-button>
        </div>
      </div>
    </section>

    <!-- 统计数据 -->
    <section class="stats-section">
      <div class="stats-grid">
        <div class="stat-card">
          <el-icon
            class="stat-icon"
            :size="32"
          >
            <QuestionFilled />
          </el-icon>
          <div class="stat-info">
            <div class="stat-number">
              {{ stats.questionCount }}
            </div>
            <div class="stat-label">
              问题总数
            </div>
          </div>
        </div>
        <div class="stat-card">
          <el-icon
            class="stat-icon"
            :size="32"
          >
            <ChatLineSquare />
          </el-icon>
          <div class="stat-info">
            <div class="stat-number">
              {{ stats.answerCount }}
            </div>
            <div class="stat-label">
              回答总数
            </div>
          </div>
        </div>
        <div class="stat-card">
          <el-icon
            class="stat-icon"
            :size="32"
          >
            <User />
          </el-icon>
          <div class="stat-info">
            <div class="stat-number">
              {{ stats.userCount }}
            </div>
            <div class="stat-label">
              活跃用户
            </div>
          </div>
        </div>
        <div class="stat-card">
          <el-icon
            class="stat-icon"
            :size="32"
          >
            <Check />
          </el-icon>
          <div class="stat-info">
            <div class="stat-number">
              {{ stats.solvedCount }}
            </div>
            <div class="stat-label">
              已解决
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 主要内容区 -->
    <section class="main-content">
      <div class="content-container">
        <!-- 左侧：问题列表 -->
        <div class="left-content">
          <!-- Tab 切换 -->
          <div class="content-tabs">
            <el-tabs
              v-model="activeTab"
              @tab-change="handleTabChange"
            >
              <el-tab-pane
                label="最新问题"
                name="latest"
              >
                <template #label>
                  <span class="tab-label">
                    <el-icon><Clock /></el-icon>
                    <span>最新问题</span>
                  </span>
                </template>
              </el-tab-pane>
              <el-tab-pane
                label="热门问题"
                name="hot"
              >
                <template #label>
                  <span class="tab-label">
                    <el-icon><TrendCharts /></el-icon>
                    <span>热门问题</span>
                  </span>
                </template>
              </el-tab-pane>
              <el-tab-pane
                label="待解决"
                name="unsolved"
              >
                <template #label>
                  <span class="tab-label">
                    <el-icon><Warning /></el-icon>
                    <span>待解决</span>
                  </span>
                </template>
              </el-tab-pane>
              <el-tab-pane
                label="悬赏问题"
                name="reward"
              >
                <template #label>
                  <span class="tab-label">
                    <el-icon><Coin /></el-icon>
                    <span>悬赏问题</span>
                  </span>
                </template>
              </el-tab-pane>
            </el-tabs>
          </div>

          <!-- 问题列表 -->
          <div
            v-loading="loading"
            class="question-list"
          >
            <!-- 加载中骨架屏 -->
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
              </div>
            </div>

            <!-- 问题卡片 -->
            <div
              v-else-if="questionList.length > 0"
              class="questions"
            >
              <div
                v-for="question in questionList"
                :key="question.id"
                class="question-card"
                @click="handleQuestionDetail(question.id)"
              >
                <!-- 问题标题 -->
                <h3 class="question-title">
                  {{ question.title }}
                </h3>

                <!-- 问题内容预览 -->
                <div
                  class="question-content"
                  v-html="question.content"
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
                      :size="24"
                      :src="question.userAvatar || `https://ui-avatars.com/api/?name=${question.username}&background=0056D2&color=fff&size=48`"
                    >
                      <el-icon><User /></el-icon>
                    </el-avatar>
                    <span class="username">{{ question.username }}</span>
                    <span class="time">{{ formatTime(question.createTime) }}</span>
                  </div>
                  <div class="meta-right">
                    <!-- 悬赏标识 -->
                    <span
                      v-if="question.rewardPoints > 0"
                      class="reward-badge"
                    >
                      <el-icon><Coin /></el-icon>
                      <span>{{ question.rewardPoints }}</span>
                    </span>
                    <!-- 浏览数 -->
                    <span class="stat-item">
                      <el-icon><View /></el-icon>
                      <span>{{ question.viewCount }}</span>
                    </span>
                    <!-- 回答数 -->
                    <span class="stat-item">
                      <el-icon><ChatLineSquare /></el-icon>
                      <span>{{ question.answerCount }}</span>
                    </span>
                    <!-- 收藏数 -->
                    <span class="stat-item">
                      <el-icon><Star /></el-icon>
                      <span>{{ question.collectionCount }}</span>
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
              <el-icon class="empty-icon">
                <Document />
              </el-icon>
              <p class="empty-text">
                暂无问题
              </p>
            </div>
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
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handlePageChange"
            />
          </div>
        </div>

        <!-- 右侧：热门标签和推荐 -->
        <div class="right-sidebar">
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
                  @click="handleTagClick(tag.id)"
                >
                  <span class="tag-name">{{ tag.name }}</span>
                  <span class="tag-count">{{ tag.questionCount }} 问题</span>
                </div>
              </div>
            </div>
          </div>

          <!-- 今日之星 -->
          <div class="sidebar-card">
            <div class="card-header">
              <el-icon><Trophy /></el-icon>
              <span>今日之星</span>
            </div>
            <div class="card-content">
              <div class="user-rank-list">
                <div
                  v-for="(user, index) in topUsers"
                  :key="user.id"
                  class="user-rank-item"
                >
                  <div
                    class="rank-badge"
                    :class="`rank-${index + 1}`"
                  >
                    {{ index + 1 }}
                  </div>
                  <el-avatar
                    :size="32"
                    :src="user.avatar || `https://ui-avatars.com/api/?name=${user.username}&background=0056D2&color=fff&size=64`"
                  >
                    <el-icon><User /></el-icon>
                  </el-avatar>
                  <div class="user-info">
                    <div class="username">
                      {{ user.username }}
                    </div>
                    <div class="user-points">
                      {{ user.points }} 积分
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { getLatestQuestions, getHotQuestions, getUnsolvedQuestions, getRewardQuestions, type QuestionVO } from '@/api/question'
import { getHotTags, type TagVO } from '@/api/tag'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import {
  EditPen,
  Compass,
  QuestionFilled,
  ChatLineSquare,
  User,
  Check,
  Clock,
  TrendCharts,
  Warning,
  Coin,
  View,
  Star,
  Document,
  PriceTag,
  Trophy,
} from '@element-plus/icons-vue'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()
const userStore = useUserStore()

// 统计数据
const stats = ref({
  questionCount: 1234,
  answerCount: 5678,
  userCount: 890,
  solvedCount: 456,
})

// 当前 Tab
const activeTab = ref('latest')

// 问题列表
const questionList = ref<QuestionVO[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 热门标签
const hotTags = ref<TagVO[]>([])
const loadingTags = ref(false)

// 今日之星
const topUsers = ref([
  { id: 1, username: '学霸小明', avatar: '', points: 1250 },
  { id: 2, username: '代码女神', avatar: '', points: 980 },
  { id: 3, username: '算法大佬', avatar: '', points: 850 },
])

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
 * 处理探索
 */
const handleExplore = () => {
  router.push('/questions')
}

/**
 * 处理 Tab 切换
 */
const handleTabChange = (tabName: string) => {
  activeTab.value = tabName
  currentPage.value = 1
  loadQuestions()
}

/**
 * 加载问题列表
 */
const loadQuestions = async () => {
  loading.value = true
  try {
    let result
    switch (activeTab.value) {
      case 'latest':
        result = await getLatestQuestions(currentPage.value, pageSize.value)
        break
      case 'hot':
        result = await getHotQuestions(currentPage.value, pageSize.value)
        break
      case 'unsolved':
        result = await getUnsolvedQuestions(currentPage.value, pageSize.value)
        break
      case 'reward':
        result = await getRewardQuestions(currentPage.value, pageSize.value)
        break
      default:
        result = await getLatestQuestions(currentPage.value, pageSize.value)
    }

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
 * 加载热门标签
 */
const loadHotTags = async () => {
  loadingTags.value = true
  try {
    hotTags.value = await getHotTags(10)
  } catch (error) {
    console.error('加载热门标签失败：', error)
  } finally {
    loadingTags.value = false
  }
}

/**
 * 处理问题详情
 */
const handleQuestionDetail = (id: number) => {
  router.push(`/question/${id}`)
}

/**
 * 处理标签点击
 */
const handleTagClick = (tagId: number) => {
  router.push({
    path: '/questions',
    query: { tagId },
  })
}

/**
 * 格式化时间
 */
const formatTime = (time: string) => {
  return dayjs(time).fromNow()
}

/**
 * 处理页码变化
 */
const handlePageChange = (page: number) => {
  currentPage.value = page
  loadQuestions()
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

/**
 * 处理每页大小变化
 */
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadQuestions()
}

onMounted(() => {
  loadQuestions()
  loadHotTags()
})
</script>

<style lang="scss" scoped>
.home-page {
  width: 100%;
  min-height: calc(100vh - 64px);
}

// ==========================================
// Hero 区域
// ==========================================
.hero-section {
  background: linear-gradient(135deg, #0056d2 0%, #0070f3 100%);
  padding: 80px 24px;
  text-align: center;
  color: #ffffff;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: url('data:image/svg+xml,<svg width="100" height="100" xmlns="http://www.w3.org/2000/svg"><defs><pattern id="grid" width="100" height="100" patternUnits="userSpaceOnUse"><path d="M 100 0 L 0 0 0 100" fill="none" stroke="rgba(255,255,255,0.1)" stroke-width="1"/></pattern></defs><rect width="100%" height="100%" fill="url(%23grid)" /></svg>');
    opacity: 0.3;
  }

  .hero-content {
    position: relative;
    z-index: 1;
    max-width: 800px;
    margin: 0 auto;
  }

  .hero-title {
    font-size: 48px;
    font-weight: 800;
    margin-bottom: 16px;
    letter-spacing: -1px;

    .gradient-text {
      background: linear-gradient(135deg, #ffffff 0%, #e6f0ff 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
  }

  .hero-subtitle {
    font-size: 24px;
    font-weight: 500;
    margin-bottom: 16px;
    opacity: 0.9;
  }

  .hero-description {
    font-size: 16px;
    opacity: 0.8;
    margin-bottom: 32px;
    line-height: 1.6;
  }

  .hero-actions {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 16px;

    :deep(.el-button) {
      height: 48px;
      padding: 0 32px;
      font-size: 16px;
      font-weight: 600;
      border-radius: 24px;
    }

    :deep(.el-button--primary) {
      background: #ffffff;
      color: #0056d2;
      border: none;

      &:hover {
        background: #f0f7ff;
        box-shadow: 0 4px 16px rgba(255, 255, 255, 0.3);
      }
    }

    :deep(.el-button--default) {
      background: rgba(255, 255, 255, 0.2);
      color: #ffffff;
      border: 1px solid rgba(255, 255, 255, 0.3);

      &:hover {
        background: rgba(255, 255, 255, 0.3);
        border-color: rgba(255, 255, 255, 0.5);
      }
    }
  }
}

// ==========================================
// 统计数据区域
// ==========================================
.stats-section {
  max-width: 1200px;
  margin: -40px auto 40px;
  padding: 0 24px;
  position: relative;
  z-index: 2;

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;

    .stat-card {
      background: #ffffff;
      border-radius: 12px;
      padding: 24px;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
      display: flex;
      align-items: center;
      gap: 16px;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      }

      .stat-icon {
        color: #0056d2;
      }

      .stat-info {
        flex: 1;

        .stat-number {
          font-size: 28px;
          font-weight: 700;
          color: #303133;
          line-height: 1;
          margin-bottom: 8px;
        }

        .stat-label {
          font-size: 13px;
          color: #909399;
        }
      }
    }
  }
}

// ==========================================
// 主要内容区
// ==========================================
.main-content {
  max-width: 1440px;
  margin: 0 auto;
  padding: 0 24px;

  .content-container {
    display: grid;
    grid-template-columns: 1fr 320px;
    gap: 24px;
    align-items: start;
  }
}

// ==========================================
// 左侧内容
// ==========================================
.left-content {
  .content-tabs {
    background: #ffffff;
    border-radius: 12px;
    padding: 16px 20px 0;
    margin-bottom: 16px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

    :deep(.el-tabs__header) {
      margin-bottom: 0;
      border-bottom: none;
    }

    :deep(.el-tabs__nav-wrap::after) {
      display: none;
    }

    :deep(.el-tabs__item) {
      font-size: 15px;
      font-weight: 500;
      color: #606266;
      padding: 12px 20px;

      &.is-active {
        color: #0056d2;
      }
    }

    :deep(.el-tabs__active-bar) {
      background: #0056d2;
      height: 3px;
    }

    .tab-label {
      display: flex;
      align-items: center;
      gap: 6px;
    }
  }

  .question-list {
    min-height: 400px;
  }

  .questions {
    display: flex;
    flex-direction: column;
    gap: 16px;
  }

  .question-card {
    background: #ffffff;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    transition: all 0.3s ease;
    cursor: pointer;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
    }

    .question-title {
      font-size: 18px;
      font-weight: 600;
      color: #1a1a1a;
      margin: 0 0 12px 0;
      line-height: 1.4;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }

    .question-content {
      font-size: 14px;
      color: #666;
      margin: 0 0 12px 0;
      line-height: 1.6;
      overflow: hidden;
      text-overflow: ellipsis;
      display: -webkit-box;
      -webkit-line-clamp: 3;
      -webkit-box-orient: vertical;
    }

    .question-tags {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      margin-bottom: 12px;

      .campus-tag {
        display: inline-block;
        padding: 4px 12px;
        background: #f0f7ff;
        color: #0056d2;
        border-radius: 4px;
        font-size: 12px;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
          background: #0056d2;
          color: #ffffff;
        }
      }
    }

    .question-meta {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: 12px;
      border-top: 1px solid #f0f0f0;

      .meta-left {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 13px;
        color: #666;

        .username {
          font-weight: 500;
        }

        .time {
          color: #999;
        }
      }

      .meta-right {
        display: flex;
        align-items: center;
        gap: 12px;
        font-size: 13px;
        color: #999;

        .reward-badge {
          display: flex;
          align-items: center;
          gap: 4px;
          color: #ff6b6b;
          font-weight: 600;

          .el-icon {
            font-size: 14px;
          }
        }

        .stat-item {
          display: flex;
          align-items: center;
          gap: 4px;

          .el-icon {
            font-size: 14px;
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
// 右侧边栏
// ==========================================
.right-sidebar {
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
    gap: 12px;

    .hot-tag-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 8px 12px;
      background: #f5f7fa;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s ease;

      &:hover {
        background: #e6f0ff;
        transform: translateX(4px);
      }

      .tag-name {
        font-size: 14px;
        color: #303133;
        font-weight: 500;
      }

      .tag-count {
        font-size: 12px;
        color: #909399;
      }
    }
  }

  .user-rank-list {
    display: flex;
    flex-direction: column;
    gap: 12px;

    .user-rank-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 8px;
      border-radius: 8px;
      transition: all 0.3s ease;
      cursor: pointer;

      &:hover {
        background: #f5f7fa;
      }

      .rank-badge {
        width: 24px;
        height: 24px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 50%;
        font-size: 12px;
        font-weight: 700;
        color: #ffffff;
        background: #909399;

        &.rank-1 {
          background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
          color: #d97706;
        }

        &.rank-2 {
          background: linear-gradient(135deg, #c0c0c0 0%, #e5e5e5 100%);
          color: #6b7280;
        }

        &.rank-3 {
          background: linear-gradient(135deg, #cd7f32 0%, #e59866 100%);
          color: #ffffff;
        }
      }

      .user-info {
        flex: 1;
        min-width: 0;

        .username {
          font-size: 14px;
          color: #303133;
          font-weight: 500;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }

        .user-points {
          font-size: 12px;
          color: #909399;
        }
      }
    }
  }
}

// ==========================================
// 响应式设计
// ==========================================
@media (max-width: 1024px) {
  .main-content {
    .content-container {
      grid-template-columns: 1fr;
    }
  }

  .right-sidebar {
    display: none;
  }
}

@media (max-width: 768px) {
  .hero-section {
    padding: 60px 16px;

    .hero-title {
      font-size: 36px;
    }

    .hero-subtitle {
      font-size: 18px;
    }

    .hero-actions {
      flex-direction: column;

      :deep(.el-button) {
        width: 100%;
      }
    }
  }

  .stats-section {
    .stats-grid {
      grid-template-columns: repeat(2, 1fr);
    }
  }
}
</style>
