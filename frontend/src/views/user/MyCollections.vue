<template>
  <div class="my-collections-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">
        我的收藏
      </h1>
      <div class="header-stats">
        <div class="stat-card">
          <div class="stat-value">
            {{ stats.totalCount }}
          </div>
          <div class="stat-label">
            收藏总数
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-value">
            {{ stats.questionCount }}
          </div>
          <div class="stat-label">
            问题
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-value">
            {{ stats.answerCount }}
          </div>
          <div class="stat-label">
            回答
          </div>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-radio-group
        v-model="collectionType"
        @change="handleTypeChange"
      >
        <el-radio-button value="all">
          全部
        </el-radio-button>
        <el-radio-button value="question">
          问题
        </el-radio-button>
        <el-radio-button value="answer">
          回答
        </el-radio-button>
      </el-radio-group>
      <el-select
        v-model="sortBy"
        placeholder="排序"
        style="width: 140px"
        @change="handleSortChange"
      >
        <el-option
          label="最近收藏"
          value="latest"
        />
        <el-option
          label="最热门"
          value="hot"
        />
      </el-select>
    </div>

    <!-- 收藏列表 -->
    <div
      v-loading="loading"
      class="collections-container"
    >
      <div
        v-if="collectionList.length > 0"
        class="collection-list"
      >
        <!-- 问题收藏卡片 -->
        <div
          v-for="item in collectionList"
          :key="`${item.targetType}-${item.targetId}`"
          class="collection-card"
          @click="handleViewDetail(item)"
        >
          <!-- 类型标识 -->
          <div class="collection-type">
            <el-tag
              v-if="item.targetType === 1"
              type="primary"
              size="small"
            >
              <el-icon><QuestionFilled /></el-icon>
              <span>问题</span>
            </el-tag>
            <el-tag
              v-else-if="item.targetType === 2"
              type="success"
              size="small"
            >
              <el-icon><ChatLineRound /></el-icon>
              <span>回答</span>
            </el-tag>
          </div>

          <!-- 问题收藏内容 -->
          <template v-if="item.targetType === 1 && item.question">
            <div class="collection-status">
              <el-tag
                v-if="item.question.isSolved"
                type="success"
                size="small"
              >
                <el-icon><CircleCheck /></el-icon>
                <span>已解决</span>
              </el-tag>
              <el-tag
                v-if="item.question.rewardPoints > 0"
                type="danger"
                size="small"
              >
                <el-icon><Coin /></el-icon>
                <span>{{ item.question.rewardPoints }}</span>
              </el-tag>
            </div>

            <h3 class="collection-title">
              {{ item.question.title }}
            </h3>

            <div
              class="collection-content"
              v-html="getContentPreview(item.question.content)"
            />

            <div
              v-if="item.question.tags && item.question.tags.length > 0"
              class="collection-tags"
            >
              <span
                v-for="tag in item.question.tags"
                :key="tag.id"
                class="campus-tag"
              >
                {{ tag.name }}
              </span>
            </div>

            <div class="collection-meta">
              <span class="meta-item">
                <el-icon><User /></el-icon>
                <span>{{ item.question.username }}</span>
              </span>
              <span class="meta-item">
                <el-icon><View /></el-icon>
                <span>{{ formatNumber(item.question.viewCount) }}</span>
              </span>
              <span class="meta-item">
                <el-icon><ChatLineSquare /></el-icon>
                <span>{{ item.question.answerCount }} 回答</span>
              </span>
              <span class="meta-item">
                <el-icon><Clock /></el-icon>
                <span>{{ formatTime(item.createTime) }}</span>
              </span>
            </div>
          </template>

          <!-- 回答收藏内容 -->
          <template v-else-if="item.targetType === 2 && item.answer">
            <div class="collection-status">
              <el-tag
                v-if="item.answer.isAccepted === 1"
                type="success"
                size="small"
              >
                <el-icon><CircleCheck /></el-icon>
                <span>已采纳</span>
              </el-tag>
            </div>

            <div
              class="question-link"
              @click.stop="handleViewQuestion(item.answer.questionId)"
            >
              <el-icon><QuestionFilled /></el-icon>
              <span>{{ item.answer.questionTitle }}</span>
            </div>

            <div
              class="collection-content answer-content"
              v-html="getContentPreview(item.answer.content)"
            />

            <div class="collection-meta">
              <span class="meta-item">
                <el-icon><User /></el-icon>
                <span>{{ item.answer.username }}</span>
              </span>
              <span class="meta-item">
                <el-icon><StarFilled /></el-icon>
                <span>{{ item.answer.likeCount }} 赞</span>
              </span>
              <span class="meta-item">
                <el-icon><ChatLineSquare /></el-icon>
                <span>{{ item.answer.commentCount }} 评论</span>
              </span>
              <span class="meta-item">
                <el-icon><Clock /></el-icon>
                <span>{{ formatTime(item.createTime) }}</span>
              </span>
            </div>
          </template>

          <!-- 取消收藏按钮 -->
          <div class="collection-actions">
            <el-button
              text
              type="danger"
              size="small"
              @click.stop="handleRemoveCollection(item)"
            >
              <el-icon><Delete /></el-icon>
              <span>取消收藏</span>
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
          <Star />
        </el-icon>
        <p class="empty-text">
          {{ emptyText }}
        </p>
        <el-button
          type="primary"
          @click="handleGoQuestions"
        >
          <el-icon><Search /></el-icon>
          <span>去探索内容</span>
        </el-button>
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
import { getMyCollections, removeCollection } from '@/api/interaction'
import type { QuestionVO } from '@/api/question'
import type { AnswerVO } from '@/api/answer'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import {
  QuestionFilled,
  ChatLineRound,
  CircleCheck,
  Coin,
  User,
  View,
  ChatLineSquare,
  Clock,
  StarFilled,
  Delete,
  Star,
  Search,
} from '@element-plus/icons-vue'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()

// 收藏类型
interface CollectionItem {
  id: number
  targetType: 'question' | 'answer'
  targetId: number
  createTime: string
  question?: QuestionVO
  answer?: AnswerVO
}

// 统计数据
const stats = ref({
  totalCount: 0,
  questionCount: 0,
  answerCount: 0,
})

// 筛选和排序
const collectionType = ref('all')
const sortBy = ref('latest')

// 收藏列表
const collectionList = ref<CollectionItem[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 空状态文案
const emptyText = computed(() => {
  if (collectionType.value === 'question') {
    return '暂无收藏的问题'
  }
  if (collectionType.value === 'answer') {
    return '暂无收藏的回答'
  }
  return '你还没有收藏任何内容'
})

/**
 * 加载收藏列表
 */
const loadCollections = async () => {
  loading.value = true
  try {
    const result = await getMyCollections(currentPage.value, pageSize.value)
    collectionList.value = result.list
    total.value = result.total

    // 计算统计数据
    stats.value.totalCount = result.total
    stats.value.questionCount = result.list.filter(
      (item: CollectionItem) => item.targetType === 1
    ).length
    stats.value.answerCount = result.list.filter(
      (item: CollectionItem) => item.targetType === 2
    ).length
  } catch (error) {
    console.error('加载收藏列表失败：', error)
    ElMessage.error('加载收藏列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 处理类型变化
 */
const handleTypeChange = () => {
  currentPage.value = 1
  loadCollections()
}

/**
 * 处理排序变化
 */
const handleSortChange = () => {
  currentPage.value = 1
  loadCollections()
}

/**
 * 处理查看详情
 */
const handleViewDetail = (item: CollectionItem) => {
  if (item.targetType === 1) {
    router.push(`/question/${item.targetId}`)
  } else if (item.targetType === 2 && item.answer) {
    router.push(`/question/${item.answer.questionId}#answer-${item.targetId}`)
  }
}

/**
 * 处理查看问题
 */
const handleViewQuestion = (questionId: number) => {
  router.push(`/question/${questionId}`)
}

/**
 * 处理取消收藏
 */
const handleRemoveCollection = async (item: CollectionItem) => {
  try {
    await ElMessageBox.confirm('确定要取消收藏吗？', '取消收藏', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    await removeCollection(item.targetType, item.targetId)
    ElMessage.success('已取消收藏')
    loadCollections()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('取消收藏失败：', error)
      ElMessage.error('取消收藏失败')
    }
  }
}

/**
 * 处理去探索
 */
const handleGoQuestions = () => {
  router.push('/questions')
}

/**
 * 处理页码变化
 */
const handlePageChange = () => {
  loadCollections()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

/**
 * 处理每页大小变化
 */
const handleSizeChange = () => {
  currentPage.value = 1
  loadCollections()
}

/**
 * 获取内容预览
 */
const getContentPreview = (content: string) => {
  const text = content.replace(/<[^>]+>/g, '')
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
const formatNumber = (num: number) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

onMounted(() => {
  loadCollections()
})
</script>

<style lang="scss" scoped>
.my-collections-page {
  width: 100%;
  min-height: calc(100vh - 64px);
}

.page-header {
  padding: 24px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 24px;

  .page-title {
    font-size: 28px;
    font-weight: 700;
    color: #303133;
    margin-bottom: 20px;
  }

  .header-stats {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;

    .stat-card {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
      padding: 20px;
      border-radius: 12px;
      text-align: center;
      color: #ffffff;
      box-shadow: 0 4px 12px rgba(240, 147, 251, 0.3);

      .stat-value {
        font-size: 32px;
        font-weight: 700;
        margin-bottom: 8px;
      }

      .stat-label {
        font-size: 14px;
        opacity: 0.9;
      }
    }
  }
}

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 20px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 24px;
}

.collections-container {
  min-height: 400px;
}

.collection-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.collection-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }

  .collection-type {
    position: absolute;
    top: 16px;
    right: 16px;

    :deep(.el-tag) {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }

  .collection-status {
    display: flex;
    gap: 8px;
    margin-bottom: 12px;

    :deep(.el-tag) {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }

  .collection-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 12px;
    line-height: 1.6;
    transition: color 0.3s ease;
    padding-right: 80px;

    &:hover {
      color: #0056d2;
    }
  }

  .question-link {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: #0056d2;
    margin-bottom: 16px;
    cursor: pointer;
    transition: color 0.3s ease;

    &:hover {
      color: #003d99;
    }

    .el-icon {
      font-size: 18px;
    }
  }

  .collection-content {
    font-size: 14px;
    color: #606266;
    line-height: 1.8;
    margin-bottom: 16px;

    &.answer-content {
      padding: 16px;
      background: #f5f7fa;
      border-radius: 8px;
      border-left: 3px solid #67c23a;
    }
  }

  .collection-tags {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
    margin-bottom: 16px;

    .campus-tag {
      padding: 4px 12px;
      background: #f0f2f5;
      border-radius: 4px;
      font-size: 12px;
      color: #606266;
      transition: all 0.3s ease;

      &:hover {
        background: #0056d2;
        color: #ffffff;
      }
    }
  }

  .collection-meta {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;
    padding-top: 16px;
    border-top: 1px solid #f5f7fa;

    .meta-item {
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

  .collection-actions {
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid #f5f7fa;
    display: flex;
    justify-content: flex-end;

    :deep(.el-button) {
      display: flex;
      align-items: center;
      gap: 4px;
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
    margin-bottom: 24px;
  }

  :deep(.el-button) {
    display: flex;
    align-items: center;
    gap: 6px;
  }
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}

@media (max-width: 768px) {
  .page-header {
    .header-stats {
      grid-template-columns: 1fr;

      .stat-card {
        .stat-value {
          font-size: 24px;
        }
      }
    }
  }

  .filter-bar {
    flex-direction: column;
    align-items: stretch;

    :deep(.el-radio-group) {
      width: 100%;
    }

    :deep(.el-select) {
      width: 100%;
    }
  }

  .collection-card {
    .collection-type {
      position: static;
      margin-bottom: 12px;
    }

    .collection-title {
      padding-right: 0;
    }

    .collection-meta {
      flex-direction: column;
      gap: 8px;
    }
  }
}
</style>
