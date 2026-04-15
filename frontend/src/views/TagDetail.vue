<template>
  <div class="tag-detail-page">
    <!-- 话题头部信息 -->
    <div
      v-loading="tagLoading"
      class="tag-header"
    >
      <div class="tag-info">
        <div class="tag-icon-wrapper">
          <div
            class="tag-icon"
            :style="{ backgroundColor: tag?.color || '#0056d2' }"
          >
            <span v-if="tag?.icon">{{ tag.icon }}</span>
            <el-icon
              v-else
              :size="32"
            >
              <CollectionTag />
            </el-icon>
          </div>
        </div>
        <div class="tag-main">
          <h1 class="tag-name">
            {{ tag?.name }}
          </h1>
          <p class="tag-description">
            {{ tag?.description || '暂无描述' }}
          </p>
          <div class="tag-stats">
            <span class="stat-item">
              <el-icon><Document /></el-icon>
              <span>{{ formatNumber(tag?.questionCount || 0) }} 个问题</span>
            </span>
            <span class="stat-item">
              <el-icon><User /></el-icon>
              <span>{{ formatNumber(tag?.followCount || 0) }} 人关注</span>
            </span>
          </div>
        </div>
        <div class="tag-actions">
          <el-button
            :type="isFollowed ? 'default' : 'primary'"
            :loading="followLoading"
            @click="handleToggleFollow"
          >
            <el-icon><Star v-if="!isFollowed" /><StarFilled v-else /></el-icon>
            <span>{{ isFollowed ? '已关注' : '关注话题' }}</span>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-tabs">
        <el-radio-group
          v-model="activeTab"
          @change="handleTabChange"
        >
          <el-radio-button value="hot">
            热门
          </el-radio-button>
          <el-radio-button value="latest">
            最新
          </el-radio-button>
          <el-radio-button value="unsolved">
            待解决
          </el-radio-button>
          <el-radio-button value="reward">
            有悬赏
          </el-radio-button>
        </el-radio-group>
      </div>
      <div class="filter-actions">
        <el-button
          type="primary"
          @click="handleAskQuestion"
        >
          <el-icon><EditPen /></el-icon>
          <span>提问</span>
        </el-button>
      </div>
    </div>

    <!-- 问题列表 -->
    <div
      v-loading="questionsLoading"
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
          <!-- 问题状态 -->
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
              v-if="question.rewardPoints > 0"
              type="danger"
              size="small"
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

          <!-- 提问者信息 -->
          <div class="question-author">
            <el-avatar
              :size="24"
              :src="question.userAvatar || `https://ui-avatars.com/api/?name=${question.username}&background=0056D2&color=fff&size=48`"
            >
              <el-icon><User /></el-icon>
            </el-avatar>
            <span class="author-name">{{ question.username }}</span>
            <span class="author-points">{{ question.userPoints }} 积分</span>
          </div>

          <!-- 问题元信息 -->
          <div class="question-meta">
            <span class="meta-item">
              <el-icon><View /></el-icon>
              <span>{{ formatNumber(question.viewCount) }}</span>
            </span>
            <span class="meta-item">
              <el-icon><ChatLineSquare /></el-icon>
              <span>{{ question.answerCount }} 回答</span>
            </span>
            <span class="meta-item">
              <el-icon><Star /></el-icon>
              <span>{{ question.collectionCount }}</span>
            </span>
            <span class="meta-item">
              <el-icon><Clock /></el-icon>
              <span>{{ formatTime(question.createTime) }}</span>
            </span>
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
          该话题下暂无问题
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
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getTagById,
  followTag,
  unfollowTag,
  checkFollowed,
  type TagVO,
} from '@/api/tag'
import { getQuestionsByTag, type QuestionVO } from '@/api/question'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import {
  CollectionTag,
  Document,
  User,
  Star,
  StarFilled,
  EditPen,
  CircleCheck,
  Coin,
  View,
  ChatLineSquare,
  Clock,
} from '@element-plus/icons-vue'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const route = useRoute()
const router = useRouter()

// 话题 ID
const tagId = ref<number>(Number(route.params.id))

// 话题信息
const tag = ref<TagVO | null>(null)
const tagLoading = ref(false)

// 关注状态
const isFollowed = ref(false)
const followLoading = ref(false)

// 筛选和排序
const activeTab = ref('hot')

// 问题列表
const questionList = ref<QuestionVO[]>([])
const questionsLoading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

/**
 * 加载话题信息
 */
const loadTagInfo = async () => {
  tagLoading.value = true
  try {
    tag.value = await getTagById(tagId.value)
    // 检查是否已关注
    isFollowed.value = await checkFollowed(tagId.value)
  } catch (error) {
    console.error('加载话题信息失败：', error)
    ElMessage.error('加载话题信息失败')
  } finally {
    tagLoading.value = false
  }
}

/**
 * 加载问题列表
 */
const loadQuestions = async () => {
  questionsLoading.value = true
  try {
    const result = await getQuestionsByTag(
      tagId.value,
      currentPage.value,
      pageSize.value,
      activeTab.value
    )
    questionList.value = result.list
    total.value = result.total
  } catch (error) {
    console.error('加载问题列表失败：', error)
    ElMessage.error('加载问题列表失败')
  } finally {
    questionsLoading.value = false
  }
}

/**
 * 处理关注/取消关注
 */
const handleToggleFollow = async () => {
  followLoading.value = true
  try {
    if (isFollowed.value) {
      await unfollowTag(tagId.value)
      ElMessage.success('已取消关注')
      isFollowed.value = false
      if (tag.value) {
        tag.value.followCount--
      }
    } else {
      await followTag(tagId.value)
      ElMessage.success('关注成功')
      isFollowed.value = true
      if (tag.value) {
        tag.value.followCount++
      }
    }
  } catch (error) {
    console.error('关注操作失败：', error)
    ElMessage.error('操作失败')
  } finally {
    followLoading.value = false
  }
}

/**
 * 处理标签页切换
 */
const handleTabChange = () => {
  currentPage.value = 1
  loadQuestions()
}

/**
 * 处理提问
 */
const handleAskQuestion = () => {
  router.push(`/ask?tagId=${tagId.value}`)
}

/**
 * 处理问题详情
 */
const handleQuestionDetail = (id: number) => {
  router.push(`/question/${id}`)
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
  return text.length > 120 ? text.substring(0, 120) + '...' : text
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
  loadTagInfo()
  loadQuestions()
})

/**
 * 监听路由参数变化
 */
watch(
  () => route.params.id,
  (newId) => {
    if (newId) {
      tagId.value = Number(newId)
      currentPage.value = 1
      loadTagInfo()
      loadQuestions()
    }
  },
  { immediate: false }
)
</script>

<style lang="scss" scoped>
.tag-detail-page {
  width: 100%;
  min-height: calc(100vh - 64px);
}

.tag-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 40px;
  margin-bottom: 24px;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.2);

  .tag-info {
    display: flex;
    gap: 24px;
    align-items: flex-start;

    .tag-icon-wrapper {
      flex-shrink: 0;

      .tag-icon {
        width: 80px;
        height: 80px;
        border-radius: 16px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 36px;
        color: #ffffff;
        background: rgba(255, 255, 255, 0.2);
        backdrop-filter: blur(10px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }
    }

    .tag-main {
      flex: 1;

      .tag-name {
        font-size: 32px;
        font-weight: 700;
        color: #ffffff;
        margin-bottom: 12px;
        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      }

      .tag-description {
        font-size: 16px;
        color: rgba(255, 255, 255, 0.9);
        line-height: 1.6;
        margin-bottom: 16px;
      }

      .tag-stats {
        display: flex;
        gap: 24px;

        .stat-item {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 14px;
          color: rgba(255, 255, 255, 0.9);

          .el-icon {
            font-size: 18px;
          }
        }
      }
    }

    .tag-actions {
      flex-shrink: 0;

      :deep(.el-button) {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 12px 24px;
        font-size: 16px;
        border-radius: 8px;
      }
    }
  }
}

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 24px;

  .filter-actions {
    :deep(.el-button) {
      display: flex;
      align-items: center;
      gap: 6px;
    }
  }
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

    :deep(.el-tag) {
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

  .question-author {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 16px;
    padding-bottom: 16px;
    border-bottom: 1px solid #f5f7fa;

    .author-name {
      font-size: 14px;
      font-weight: 500;
      color: #303133;
    }

    .author-points {
      font-size: 12px;
      color: #909399;
    }
  }

  .question-meta {
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
  .tag-header {
    padding: 24px;

    .tag-info {
      flex-direction: column;
      gap: 16px;

      .tag-icon-wrapper {
        .tag-icon {
          width: 60px;
          height: 60px;
          font-size: 28px;
        }
      }

      .tag-main {
        .tag-name {
          font-size: 24px;
        }

        .tag-description {
          font-size: 14px;
        }
      }

      .tag-actions {
        width: 100%;

        :deep(.el-button) {
          width: 100%;
          justify-content: center;
        }
      }
    }
  }

  .filter-bar {
    flex-direction: column;
    gap: 12px;

    .filter-tabs {
      width: 100%;

      :deep(.el-radio-group) {
        width: 100%;
        display: grid;
        grid-template-columns: repeat(2, 1fr);
      }
    }

    .filter-actions {
      width: 100%;

      :deep(.el-button) {
        width: 100%;
        justify-content: center;
      }
    }
  }
}
</style>
