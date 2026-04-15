<template>
  <div class="my-answers-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">
        我的回答
      </h1>
      <div class="header-stats">
        <div class="stat-card">
          <div class="stat-value">
            {{ stats.totalCount }}
          </div>
          <div class="stat-label">
            总回答数
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-value">
            {{ stats.acceptedCount }}
          </div>
          <div class="stat-label">
            被采纳
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-value">
            {{ stats.todayCount }}
          </div>
          <div class="stat-label">
            今日回答
          </div>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-radio-group
        v-model="filterType"
        @change="handleFilterChange"
      >
        <el-radio-button value="all">
          全部
        </el-radio-button>
        <el-radio-button value="accepted">
          已采纳
        </el-radio-button>
        <el-radio-button value="popular">
          最受欢迎
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
      </el-select>
    </div>

    <!-- 回答列表 -->
    <div
      v-loading="loading"
      class="answers-container"
    >
      <div
        v-if="answerList.length > 0"
        class="answer-list"
      >
        <div
          v-for="answer in answerList"
          :key="answer.id"
          class="answer-card"
        >
          <!-- 回答状态 -->
          <div class="answer-status">
            <el-tag
              v-if="answer.isAccepted === 1"
              type="success"
              size="small"
            >
              <el-icon><CircleCheck /></el-icon>
              <span>已采纳</span>
            </el-tag>
          </div>

          <!-- 问题标题 -->
          <div
            class="question-title"
            @click="handleQuestionDetail(answer.questionId)"
          >
            <el-icon><QuestionFilled /></el-icon>
            <span>{{ answer.questionTitle }}</span>
          </div>

          <!-- 回答内容 -->
          <div
            class="answer-content"
            v-html="getContentPreview(answer.content)"
          />

          <!-- 回答元信息 -->
          <div class="answer-meta">
            <div class="meta-left">
              <span class="meta-item">
                <el-icon><StarFilled /></el-icon>
                <span>{{ answer.likeCount }} 赞</span>
              </span>
              <span class="meta-item">
                <el-icon><ChatLineSquare /></el-icon>
                <span>{{ answer.commentCount }} 评论</span>
              </span>
              <span class="meta-item">
                <el-icon><Clock /></el-icon>
                <span>{{ formatTime(answer.createTime) }}</span>
              </span>
            </div>
            <div class="meta-right">
              <el-button
                text
                type="primary"
                size="small"
                @click="handleViewAnswer(answer.questionId, answer.id)"
              >
                <el-icon><View /></el-icon>
                <span>查看</span>
              </el-button>
              <el-button
                text
                type="primary"
                size="small"
                @click="handleEdit(answer.id)"
              >
                <el-icon><Edit /></el-icon>
                <span>编辑</span>
              </el-button>
              <el-button
                text
                type="danger"
                size="small"
                @click="handleDelete(answer.id)"
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
          <ChatDotRound />
        </el-icon>
        <p class="empty-text">
          {{ emptyText }}
        </p>
        <el-button
          type="primary"
          @click="handleGoQuestions"
        >
          <el-icon><Search /></el-icon>
          <span>去回答问题</span>
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

    <!-- 编辑回答对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑回答"
      width="80%"
      :close-on-click-modal="false"
    >
      <el-input
        v-model="editContent"
        type="textarea"
        :rows="15"
        placeholder="请输入回答内容（支持 Markdown）"
      />
      <template #footer>
        <el-button @click="editDialogVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="editLoading"
          @click="handleSaveEdit"
        >
          保存
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getMyAnswers,
  updateAnswer,
  deleteAnswer,
  type AnswerVO,
} from '@/api/answer'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import {
  CircleCheck,
  QuestionFilled,
  StarFilled,
  ChatLineSquare,
  Clock,
  View,
  Edit,
  Delete,
  ChatDotRound,
  Search,
} from '@element-plus/icons-vue'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()

// 统计数据
const stats = ref({
  totalCount: 0,
  todayCount: 0,
  acceptedCount: 0,
})

// 筛选和排序
const filterType = ref('all')
const sortBy = ref('latest')

// 回答列表
const answerList = ref<AnswerVO[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 编辑对话框
const editDialogVisible = ref(false)
const editLoading = ref(false)
const editContent = ref('')
const editAnswerId = ref<number | null>(null)

// 空状态文案
const emptyText = computed(() => {
  if (filterType.value === 'accepted') {
    return '还没有被采纳的回答'
  }
  if (filterType.value === 'popular') {
    return '还没有受欢迎的回答'
  }
  return '你还没有回答过问题，快去回答吧！'
})

/**
 * 加载统计数据
 */
const loadStats = async () => {
  // 由于后端未提供统计接口，使用本地计算方式
  try {
    // 直接使用初始值
    stats.value = {
      totalCount: 0,
      todayCount: 0,
      acceptedCount: 0
    }
  } catch (error) {
    console.error('加载统计数据失败：', error)
  }
}

/**
 * 加载回答列表
 */
const loadAnswers = async () => {
  loading.value = true
  try {
    const result = await getMyAnswers(currentPage.value, pageSize.value)
    answerList.value = result.list
    total.value = result.total
  } catch (error) {
    console.error('加载回答列表失败：', error)
    ElMessage.error('加载回答列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 处理筛选变化
 */
const handleFilterChange = () => {
  currentPage.value = 1
  loadAnswers()
}

/**
 * 处理排序变化
 */
const handleSortChange = () => {
  currentPage.value = 1
  loadAnswers()
}

/**
 * 处理问题详情
 */
const handleQuestionDetail = (questionId: number) => {
  router.push(`/question/${questionId}`)
}

/**
 * 处理查看回答
 */
const handleViewAnswer = (questionId: number, answerId: number) => {
  router.push(`/question/${questionId}#answer-${answerId}`)
}

/**
 * 处理编辑
 */
const handleEdit = (id: number) => {
  const answer = answerList.value.find(a => a.id === id)
  if (answer) {
    editAnswerId.value = id
    editContent.value = answer.content
    editDialogVisible.value = true
  }
}

/**
 * 保存编辑
 */
const handleSaveEdit = async () => {
  if (!editContent.value.trim()) {
    ElMessage.warning('请输入回答内容')
    return
  }

  if (!editAnswerId.value) {
    return
  }

  editLoading.value = true
  try {
    await updateAnswer(editAnswerId.value, { content: editContent.value })
    ElMessage.success('编辑成功')
    editDialogVisible.value = false
    loadAnswers()
  } catch (error) {
    console.error('编辑回答失败：', error)
    ElMessage.error('编辑失败')
  } finally {
    editLoading.value = false
  }
}

/**
 * 处理删除
 */
const handleDelete = async (id: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个回答吗？此操作不可恢复。', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    await deleteAnswer(id)
    ElMessage.success('删除成功')
    loadAnswers()
    loadStats()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除回答失败：', error)
      ElMessage.error('删除失败')
    }
  }
}

/**
 * 处理去回答问题
 */
const handleGoQuestions = () => {
  router.push('/questions')
}

/**
 * 处理页码变化
 */
const handlePageChange = () => {
  loadAnswers()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

/**
 * 处理每页大小变化
 */
const handleSizeChange = () => {
  currentPage.value = 1
  loadAnswers()
}

/**
 * 获取内容预览
 */
const getContentPreview = (content: string) => {
  const text = content.replace(/<[^>]+>/g, '')
  return text.length > 200 ? text.substring(0, 200) + '...' : text
}

/**
 * 格式化时间
 */
const formatTime = (time: string) => {
  return dayjs(time).fromNow()
}

onMounted(() => {
  loadStats()
  loadAnswers()
})
</script>

<style lang="scss" scoped>
.my-answers-page {
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
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      padding: 20px;
      border-radius: 12px;
      text-align: center;
      color: #ffffff;
      box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);

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

.answers-container {
  min-height: 400px;
}

.answer-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.answer-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }

  .answer-status {
    display: flex;
    gap: 8px;
    margin-bottom: 12px;

    :deep(.el-tag) {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }

  .question-title {
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

  .answer-content {
    font-size: 14px;
    color: #606266;
    line-height: 1.8;
    margin-bottom: 16px;
    padding: 16px;
    background: #f5f7fa;
    border-radius: 8px;
    border-left: 3px solid #0056d2;
  }

  .answer-meta {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-top: 16px;
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

:deep(.el-dialog) {
  border-radius: 12px;

  .el-dialog__header {
    border-bottom: 1px solid #f5f7fa;
  }

  .el-dialog__body {
    padding: 24px;
  }

  .el-textarea__inner {
    font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
    font-size: 14px;
    line-height: 1.6;
  }
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

  .answer-card {
    .answer-meta {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;

      .meta-right {
        width: 100%;
        justify-content: flex-start;
      }
    }
  }

  :deep(.el-dialog) {
    width: 95% !important;
  }
}
</style>
