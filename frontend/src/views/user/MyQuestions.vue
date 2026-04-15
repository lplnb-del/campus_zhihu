<template>
  <div class="my-questions-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">
        我的提问
      </h1>
      <el-button
        type="primary"
        @click="handleAskQuestion"
      >
        <el-icon><EditPen /></el-icon>
        <span>发布新问题</span>
      </el-button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-radio-group
        v-model="filterStatus"
        @change="handleFilterChange"
      >
        <el-radio-button value="all">
          全部
        </el-radio-button>
        <el-radio-button value="unsolved">
          待解决
        </el-radio-button>
        <el-radio-button value="solved">
          已解决
        </el-radio-button>
        <el-radio-button value="draft">
          草稿
        </el-radio-button>
      </el-radio-group>
      <el-select
        v-model="sortBy"
        placeholder="排序"
        style="width: 120px"
        @change="handleSortChange"
      >
        <el-option
          label="最新"
          value="latest"
        />
        <el-option
          label="最热"
          value="hot"
        />
        <el-option
          label="浏览最多"
          value="views"
        />
      </el-select>
    </div>

    <!-- 问题列表 -->
    <div
      v-loading="loading"
      class="questions-container"
    >
      <div
        v-if="questionList.length > 0"
        class="question-list"
      >
        <div
          v-for="question in questionList"
          :key="question.id"
          class="question-card"
          @click="handleQuestionDetail(question.id)"
        >
          <!-- 状态标识 -->
          <div class="question-status">
            <el-tag
              v-if="question.isSolved"
              type="success"
              size="small"
            >
              <el-icon><CircleCheck /></el-icon>
              <span>已解决</span>
            </el-tag>
            <el-tag
              v-else-if="question.isDraft"
              type="info"
              size="small"
            >
              <el-icon><Document /></el-icon>
              <span>草稿</span>
            </el-tag>
            <el-tag
              v-else
              type="warning"
              size="small"
            >
              <el-icon><Warning /></el-icon>
              <span>待解决</span>
            </el-tag>
            <el-tag
              v-if="question.rewardPoints > 0"
              type="danger"
              size="small"
              class="reward-tag"
            >
              <el-icon><Coin /></el-icon>
              <span>{{ question.rewardPoints }}</span>
            </el-tag>
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
            >
              {{ tag.name }}
            </span>
          </div>

          <!-- 问题元信息 -->
          <div class="question-meta">
            <div class="meta-left">
              <span class="meta-item">
                <el-icon><View /></el-icon>
                <span>{{ formatNumber(question.viewCount) }} 浏览</span>
              </span>
              <span class="meta-item">
                <el-icon><ChatLineSquare /></el-icon>
                <span>{{ question.answerCount }} 回答</span>
              </span>
              <span class="meta-item">
                <el-icon><Star /></el-icon>
                <span>{{ question.collectionCount }} 收藏</span>
              </span>
              <span class="meta-item">
                <el-icon><Clock /></el-icon>
                <span>{{ formatTime(question.createTime) }}</span>
              </span>
            </div>
            <div class="meta-right">
              <el-button
                text
                type="primary"
                size="small"
                @click.stop="handleEdit(question.id)"
              >
                <el-icon><Edit /></el-icon>
                <span>编辑</span>
              </el-button>
              <el-button
                text
                type="danger"
                size="small"
                @click.stop="handleDelete(question.id)"
              >
                <el-icon><Delete /></el-icon>
                <span>删除</span>
              </el-button>
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
          @click="handleAskQuestion"
        >
          <el-icon><EditPen /></el-icon>
          <span>发布第一个问题</span>
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
import { getMyQuestions, deleteQuestion, type QuestionVO } from '@/api/question'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import {
  EditPen,
  CircleCheck,
  Document,
  Warning,
  Coin,
  View,
  ChatLineSquare,
  Star,
  Clock,
  Edit,
  Delete,
} from '@element-plus/icons-vue'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()

// 筛选和排序
const filterStatus = ref('all')
const sortBy = ref('latest')

// 问题列表
const questionList = ref<QuestionVO[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 空状态文案
const emptyText = computed(() => {
  if (filterStatus.value === 'draft') {
    return '暂无草稿'
  }
  if (filterStatus.value === 'solved') {
    return '暂无已解决的问题'
  }
  if (filterStatus.value === 'unsolved') {
    return '暂无待解决的问题'
  }
  return '你还没有提问过，快去提问吧！'
})

/**
 * 加载问题列表
 */
const loadQuestions = async () => {
  loading.value = true
  try {
    const result = await getMyQuestions(currentPage.value, pageSize.value)
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
 * 处理筛选变化
 */
const handleFilterChange = () => {
  currentPage.value = 1
  loadQuestions()
}

/**
 * 处理排序变化
 */
const handleSortChange = () => {
  currentPage.value = 1
  loadQuestions()
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
  router.push('/ask')
}

/**
 * 处理编辑
 */
const handleEdit = (id: number) => {
  router.push(`/ask?id=${id}`)
}

/**
 * 处理删除
 */
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个问题吗？此操作不可恢复。', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    await deleteQuestion(id)
    ElMessage.success('删除成功')
    loadQuestions()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除问题失败：', error)
      ElMessage.error('删除失败')
    }
  }
}

/**
 * 处理页码变化
 */
const handlePageChange = () => {
  loadQuestions()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

/**
 * 处理每页大小变化
 */
const handleSizeChange = () => {
  currentPage.value = 1
  loadQuestions()
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
  loadQuestions()
})
</script>

<style lang="scss" scoped>
.my-questions-page {
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

  :deep(.el-button) {
    display: flex;
    align-items: center;
    gap: 6px;
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

.questions-container {
  min-height: 400px;
}

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
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }

  .question-status {
    display: flex;
    gap: 8px;
    margin-bottom: 12px;

    .reward-tag {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }

  .question-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 12px;
    line-height: 1.6;
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
    padding-top: 12px;
    border-top: 1px solid #f5f7fa;

    .meta-left {
      display: flex;
      gap: 16px;
      flex-wrap: wrap;

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

    .meta-right {
      display: flex;
      gap: 8px;

      :deep(.el-button) {
        display: flex;
        align-items: center;
        gap: 4px;
      }
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
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;

    :deep(.el-button) {
      width: 100%;
      justify-content: center;
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

  .question-card {
    .question-meta {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;

      .meta-right {
        width: 100%;
        justify-content: flex-start;
      }
    }
  }
}
</style>
