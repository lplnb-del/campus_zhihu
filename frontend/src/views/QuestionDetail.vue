<template>
  <div class="question-detail-page">
    <!-- 加载中 -->
    <div
      v-if="loading"
      class="loading-container"
    >
      <el-skeleton
        :rows="10"
        animated
      />
    </div>

    <!-- 问题详情 -->
    <div
      v-else-if="question"
      class="detail-container"
    >
      <!-- 左侧主内容 -->
      <div class="detail-main">
        <!-- 问题卡片 -->
        <div class="question-card">
          <!-- 问题标题 -->
          <h1 class="question-title">
            {{ question.title }}
          </h1>

          <!-- 问题元信息 -->
          <div class="question-meta">
            <div class="meta-left">
              <el-avatar
                :size="48"
                :src="question.userAvatar || `https://ui-avatars.com/api/?name=${question.username}&background=0056D2&color=fff&size=96`"
              >
                <el-icon><User /></el-icon>
              </el-avatar>
              <div class="user-info">
                <span class="username">{{ question.username }}</span>
                <div class="meta-info">
                  <span class="time">{{ formatTime(question.createTime) }}</span>
                  <span class="separator">·</span>
                  <span class="views">
                    <el-icon><View /></el-icon>
                    <span>{{ formatNumber(question.viewCount) }} 浏览</span>
                  </span>
                </div>
              </div>
            </div>
            <div class="meta-right">
              <!-- 悬赏标识 -->
              <div
                v-if="question.rewardPoints > 0"
                class="reward-badge"
              >
                <el-icon><Coin /></el-icon>
                <span>悬赏 {{ question.rewardPoints }} 积分</span>
              </div>
              <!-- 已解决标识 -->
              <div
                v-if="question.isSolved"
                class="solved-badge"
              >
                <el-icon><CircleCheck /></el-icon>
                <span>已解决</span>
              </div>
            </div>
          </div>

          <!-- 问题标签 -->
          <div
            v-if="question.tags && question.tags.length > 0"
            class="question-tags"
          >
            <span
              v-for="tag in question.tags"
              :key="tag.id"
              class="campus-tag"
              @click="handleTagClick(tag.id)"
            >
              {{ tag.name }}
            </span>
          </div>

          <!-- 问题内容 -->
          <div
            class="question-content"
            v-html="question.content"
          />

          <!-- 问题操作栏 -->
          <div class="question-actions">
            <div class="actions-left">
              <!-- 点赞 -->
              <el-button
                :type="question.isLiked ? 'primary' : 'default'"
                :plain="!question.isLiked"
                @click="handleLike"
              >
                <el-icon><Star /></el-icon>
                <span>{{ question.isLiked ? '已点赞' : '点赞' }}</span>
                <span v-if="question.likeCount > 0">({{ formatNumber(question.likeCount) }})</span>
              </el-button>

              <!-- 收藏 -->
              <el-button
                :type="question.isCollected ? 'warning' : 'default'"
                :plain="!question.isCollected"
                @click="handleCollect"
              >
                <el-icon><Star /></el-icon>
                <span>{{ question.isCollected ? '已收藏' : '收藏' }}</span>
                <span v-if="question.collectionCount > 0">({{ formatNumber(question.collectionCount) }})</span>
              </el-button>

              <!-- 分享 -->
              <el-button @click="handleShare">
                <el-icon><Share /></el-icon>
                <span>分享</span>
              </el-button>
            </div>

            <div class="actions-right">
              <!-- 编辑（仅所有者） -->
              <el-button
                v-if="isOwner"
                text
                @click="handleEdit"
              >
                <el-icon><Edit /></el-icon>
                <span>编辑</span>
              </el-button>

              <!-- 删除（仅所有者） -->
              <el-button
                v-if="isOwner"
                text
                type="danger"
                @click="handleDelete"
              >
                <el-icon><Delete /></el-icon>
                <span>删除</span>
              </el-button>
            </div>
          </div>
        </div>

        <!-- 回答统计 -->
        <div class="answer-header">
          <h2 class="section-title">
            {{ answerTotal }} 个回答
          </h2>
          <el-radio-group
            v-model="answerOrder"
            size="small"
            @change="handleAnswerOrderChange"
          >
            <el-radio-button value="hot">
              最热
            </el-radio-button>
            <el-radio-button value="latest">
              最新
            </el-radio-button>
            <el-radio-button value="accepted">
              最佳
            </el-radio-button>
          </el-radio-group>
        </div>

        <!-- 回答列表 -->
        <div
          v-if="answerList"
          v-loading="loadingAnswers"
          class="answer-list"
        >
          <div
            v-for="answer in answerList"
            :key="answer.id"
            class="answer-card"
          >
            <!-- 回答头部 -->
            <div class="answer-header-info">
              <el-avatar
                :size="48"
                :src="answer.userAvatar || `https://ui-avatars.com/api/?name=${answer.username || 'U'}&background=0056D2&color=fff&size=96`"
              >
                <el-icon><User /></el-icon>
              </el-avatar>
              <div class="user-info">
                <span class="username">{{ answer.username || '匿名用户' }}</span>
                <span class="time">{{ formatTime(answer.createTime) }}</span>
              </div>
              <!-- 已采纳标识 -->
              <div
                v-if="answer.isAccepted === 1"
                class="accepted-badge"
              >
                <el-icon><Select /></el-icon>
                <span>已采纳</span>
              </div>
            </div>

            <!-- 回答内容 -->
            <div
              class="answer-content"
              v-html="answer.content"
            />

            <!-- 回答操作 -->
            <div class="answer-actions">
              <div class="actions-left">
                <!-- 点赞 -->
                <el-button
                  text
                  :type="answer.isLiked ? 'primary' : 'default'"
                  @click="handleAnswerLike(answer)"
                >
                  <el-icon><Star /></el-icon>
                  <span>{{ answer.isLiked ? '已赞' : '赞' }}</span>
                  <span v-if="answer.likeCount > 0">({{ answer.likeCount }})</span>
                </el-button>

                <!-- 评论 -->
                <el-button
                  text
                  @click="handleShowComments(answer.id)"
                >
                  <el-icon><ChatLineSquare /></el-icon>
                  <span>评论</span>
                  <span v-if="answer.commentCount > 0">({{ answer.commentCount }})</span>
                </el-button>
              </div>

              <div class="actions-right">
                <!-- 采纳（仅问题所有者，且未采纳） -->
                <el-button
                  v-if="isOwner && !question.isSolved && answer.isAccepted === 0"
                  type="success"
                  text
                  @click="handleAcceptAnswer(answer.id)"
                >
                  <el-icon><Select /></el-icon>
                  <span>采纳</span>
                </el-button>

                <!-- 取消采纳（仅问题所有者） -->
                <el-button
                  v-if="isOwner && answer.isAccepted === 1"
                  text
                  @click="handleCancelAcceptAnswer(answer.id)"
                >
                  <el-icon><Close /></el-icon>
                  <span>取消采纳</span>
                </el-button>

                <!-- 编辑（仅回答所有者） -->
                <el-button
                  v-if="answer.userId === userStore.userId"
                  text
                  @click="handleEditAnswer(answer)"
                >
                  <el-icon><Edit /></el-icon>
                  <span>编辑</span>
                </el-button>

                <!-- 删除（仅回答所有者） -->
                <el-button
                  v-if="answer.userId === userStore.userId"
                  text
                  type="danger"
                  @click="handleDeleteAnswer(answer.id)"
                >
                  <el-icon><Delete /></el-icon>
                  <span>删除</span>
                </el-button>
              </div>
            </div>

            <!-- 评论区（展开时显示） -->
            <div
              v-if="expandedComments[answer.id]"
              class="comments-section"
            >
              <el-divider />
              <div class="comments-list">
                <!-- 评论列表 -->
                <div v-if="comments[answer.id] && comments[answer.id].length > 0">
                  <div
                    v-for="comment in comments[answer.id]"
                    :key="comment.id"
                    class="comment-item"
                  >
                    <el-avatar
                      :size="32"
                      :src="comment.userAvatar || `https://ui-avatars.com/api/?name=${comment.username}&background=0056D2&color=fff&size=64`"
                    >
                      <el-icon><User /></el-icon>
                    </el-avatar>
                    <div class="comment-content">
                      <div class="comment-header">
                        <span class="username">{{ comment.username }}</span>
                        <span class="time">{{ formatTime(comment.createTime) }}</span>
                      </div>
                      <p class="content">
                        {{ comment.content }}
                      </p>
                    </div>
                  </div>
                </div>

                <!-- 发表评论 -->
                <div class="comment-input-box">
                  <el-input
                    v-model="commentContent[answer.id]"
                    type="textarea"
                    :rows="2"
                    placeholder="写下你的评论..."
                    maxlength="500"
                    show-word-limit
                  />
                  <el-button
                    type="primary"
                    size="small"
                    :loading="publishingComment[answer.id]"
                    @click="handlePublishComment(answer.id)"
                  >
                    发表
                  </el-button>
                </div>
              </div>
            </div>
          </div>

          <!-- 空状态 -->
          <div
            v-if="answerList && answerList.length === 0"
            class="empty-container"
          >
            <el-icon
              class="empty-icon"
              :size="80"
            >
              <Document />
            </el-icon>
            <p class="empty-text">
              暂无回答，快来抢沙发吧！
            </p>
          </div>

          <!-- 分页 -->
          <div
            v-if="answerTotal > 0"
            class="pagination"
          >
            <el-pagination
              v-model:current-page="answerPage"
              v-model:page-size="answerSize"
              :total="answerTotal"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              @size-change="handleAnswerSizeChange"
              @current-change="handleAnswerPageChange"
            />
          </div>
        </div>

        <!-- 回答编辑器 -->
        <div class="answer-editor-card">
          <h3 class="editor-title">
            撰写回答
          </h3>
          <el-input
            v-model="answerContent"
            type="textarea"
            :rows="8"
            placeholder="写下你的回答..."
            maxlength="10000"
            show-word-limit
          />
          <div class="editor-actions">
            <el-button
              type="primary"
              :loading="publishingAnswer"
              :disabled="!answerContent.trim()"
              @click="handlePublishAnswer"
            >
              <el-icon><EditPen /></el-icon>
              <span>发布回答</span>
            </el-button>
          </div>
        </div>
      </div>

      <!-- 右侧边栏 -->
      <div class="detail-sidebar">
        <!-- 提问者信息 -->
        <div class="sidebar-card">
          <div class="card-header">
            <el-icon><User /></el-icon>
            <span>提问者</span>
          </div>
          <div class="card-content">
            <div class="author-info">
              <el-avatar
                :size="64"
                :src="question.userAvatar || `https://ui-avatars.com/api/?name=${question.username}&background=0056D2&color=fff&size=128`"
              >
                <el-icon><User /></el-icon>
              </el-avatar>
              <div class="author-details">
                <h4 class="author-name">
                  {{ question.username }}
                </h4>
                <p class="author-points">
                  积分：{{ question.userPoints || 0 }}
                </p>
              </div>
            </div>
          </div>
        </div>

        <!-- 相关问题 -->
        <div class="sidebar-card">
          <div class="card-header">
            <el-icon><Link /></el-icon>
            <span>相关问题</span>
          </div>
          <div class="card-content">
            <div
              v-loading="loadingRelated"
              class="related-list"
            >
              <router-link
                v-for="item in relatedQuestions"
                :key="item.id"
                :to="`/question/${item.id}`"
                class="related-item"
              >
                <p class="related-title">
                  {{ item.title }}
                </p>
                <span class="related-meta">
                  {{ item.answerCount }} 回答 · {{ formatNumber(item.viewCount) }} 浏览
                </span>
              </router-link>
              <div
                v-if="relatedQuestions.length === 0"
                class="empty-text"
              >
                暂无相关问题
              </div>
            </div>
          </div>
        </div>

        <!-- 问题统计 -->
        <div class="sidebar-card">
          <div class="card-header">
            <el-icon><DataAnalysis /></el-icon>
            <span>问题统计</span>
          </div>
          <div class="card-content">
            <div class="stats-list">
              <div class="stats-item">
                <span class="label">浏览</span>
                <span class="value">{{ formatNumber(question.viewCount) }}</span>
              </div>
              <div class="stats-item">
                <span class="label">回答</span>
                <span class="value">{{ question.answerCount }}</span>
              </div>
              <div class="stats-item">
                <span class="label">收藏</span>
                <span class="value">{{ question.collectionCount }}</span>
              </div>
              <div class="stats-item">
                <span class="label">点赞</span>
                <span class="value">{{ question.likeCount }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 错误状态 -->
    <div
      v-else
      class="error-container"
    >
      <el-icon
        class="error-icon"
        :size="80"
      >
        <WarningFilled />
      </el-icon>
      <p class="error-text">
        问题不存在或已被删除
      </p>
      <el-button
        type="primary"
        @click="handleGoBack"
      >
        返回
      </el-button>
    </div>

    <!-- 编辑回答对话框 -->
    <el-dialog
      v-model="editAnswerDialogVisible"
      title="编辑回答"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form>
        <el-form-item>
          <el-input
            v-model="editingAnswerContent"
            type="textarea"
            :rows="10"
            placeholder="请输入回答内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleCancelEditAnswer">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="updatingAnswer"
          @click="handleSaveEditAnswer"
        >
          {{ updatingAnswer ? '保存中...' : '保存' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getQuestionById, deleteQuestion, getRelatedQuestions, type QuestionVO } from '@/api/question'
import { publishAnswer, getAnswersByQuestion, updateAnswer, deleteAnswer, acceptAnswer, cancelAcceptAnswer, type AnswerVO } from '@/api/answer'
import { toggleLike, toggleCollection } from '@/api/interaction'
import { publishComment, getCommentsByTarget, type CommentVO, TargetType } from '@/api/interaction'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import {
  User,
  View,
  Coin,
  CircleCheck,
  Star,
  Share,
  Edit,
  Delete,
  ChatLineSquare,
  Select,
  Close,
  EditPen,
  Document,
  Link,
  DataAnalysis,
  WarningFilled,
} from '@element-plus/icons-vue'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 问题ID
const questionId = computed(() => Number(route.params.id))

// 问题详情
const question = ref<QuestionVO | null>(null)
const loading = ref(false)

// 是否为问题所有者
const isOwner = computed(() => {
  return question.value && userStore.userId === question.value.userId
})

// 回答列表
const answerList = ref<AnswerVO[]>([])
const loadingAnswers = ref(false)
const answerPage = ref(1)
const answerSize = ref(20)
const answerTotal = ref(0)
const answerOrder = ref<'hot' | 'latest' | 'accepted'>('hot')

// 回答内容
const answerContent = ref('')
const publishingAnswer = ref(false)

// 编辑回答对话框
const editAnswerDialogVisible = ref(false)
const editingAnswerId = ref<number | null>(null)
const editingAnswerContent = ref('')
const updatingAnswer = ref(false)

// 评论相关
const expandedComments = reactive<Record<number, boolean>>({})
const comments = reactive<Record<number, CommentVO[]>>({})
const commentContent = reactive<Record<number, string>>({})
const publishingComment = reactive<Record<number, boolean>>({})

// 相关问题
const relatedQuestions = ref<QuestionVO[]>([])
const loadingRelated = ref(false)

/**
 * 加载问题详情
 */
const loadQuestion = async () => {
  loading.value = true
  try {
    question.value = await getQuestionById(questionId.value)
  } catch (error) {
    console.error('加载问题详情失败：', error)
    ElMessage.error('加载问题详情失败')
  } finally {
    loading.value = false
  }
}

/**
 * 加载回答列表
 */
const loadAnswers = async () => {
  loadingAnswers.value = true
  try {
    const result = await getAnswersByQuestion(questionId.value, answerPage.value, answerSize.value)
    answerList.value = result.list
    answerTotal.value = result.total
  } catch (error) {
    console.error('加载回答列表失败：', error)
    ElMessage.error('加载回答列表失败')
  } finally {
    loadingAnswers.value = false
  }
}

/**
 * 加载相关问题
 */
const loadRelatedQuestions = async () => {
  loadingRelated.value = true
  try {
    relatedQuestions.value = await getRelatedQuestions(questionId.value, 5)
  } catch (error) {
    console.error('加载相关问题失败：', error)
  } finally {
    loadingRelated.value = false
  }
}

/**
 * 处理点赞
 */
const handleLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    const result = await toggleLike(1, questionId.value)
    if (question.value) {
      question.value.isLiked = result
      question.value.likeCount += result ? 1 : -1
    }
    ElMessage.success(result ? '点赞成功' : '取消点赞')
  } catch (error) {
    console.error('点赞失败：', error)
    ElMessage.error('操作失败')
  }
}

/**
 * 处理收藏
 */
const handleCollect = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    const result = await toggleCollection(1, questionId.value)
    if (question.value) {
      question.value.isCollected = result
      question.value.collectionCount += result ? 1 : -1
    }
    ElMessage.success(result ? '收藏成功' : '取消收藏')
  } catch (error) {
    console.error('收藏失败：', error)
    ElMessage.error('操作失败')
  }
}

/**
 * 处理分享
 */
const handleShare = () => {
  const url = window.location.href
  if (navigator.clipboard) {
    navigator.clipboard.writeText(url).then(() => {
      ElMessage.success('链接已复制到剪贴板')
    })
  } else {
    ElMessage.info('链接：' + url)
  }
}

/**
 * 处理编辑问题
 */
const handleEdit = () => {
  router.push(`/ask?id=${questionId.value}`)
}

/**
 * 处理删除问题
 */
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这个问题吗？此操作不可恢复。', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      center: true,
    })

    await deleteQuestion(questionId.value)
    ElMessage.success('删除成功')
    router.push('/questions')
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除问题失败：', error)
      ElMessage.error('删除失败')
    }
  }
}

/**
 * 处理回答排序变化
 */
const handleAnswerOrderChange = () => {
  answerPage.value = 1
  loadAnswers()
}

/**
 * 处理回答页码变化
 */
const handleAnswerPageChange = () => {
  loadAnswers()
  window.scrollTo({ top: 400, behavior: 'smooth' })
}

/**
 * 处理回答每页大小变化
 */
const handleAnswerSizeChange = () => {
  answerPage.value = 1
  loadAnswers()
}

/**
 * 发布回答
 */
const handlePublishAnswer = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  if (!answerContent.value.trim()) {
    ElMessage.warning('请输入回答内容')
    return
  }

  publishingAnswer.value = true
  try {
    await publishAnswer({
      questionId: questionId.value,
      content: answerContent.value,
    })

    ElMessage.success('发布成功')
    answerContent.value = ''
    loadAnswers()
    if (question.value) {
      question.value.answerCount++
    }
  } catch (error) {
    console.error('发布回答失败：', error)
    ElMessage.error('发布失败')
  } finally {
    publishingAnswer.value = false
  }
}

/**
 * 处理回答点赞
 */
const handleAnswerLike = async (answer: AnswerVO) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    const result = await toggleLike(2, answer.id)
    answer.isLiked = result
    answer.likeCount += result ? 1 : -1
    ElMessage.success(result ? '点赞成功' : '取消点赞')
  } catch (error) {
    console.error('点赞失败：', error)
    ElMessage.error('操作失败')
  }
}

/**
 * 处理采纳回答
 */
const handleAcceptAnswer = async (answerId: number) => {
  try {
    await ElMessageBox.confirm('确定要采纳这个回答吗？', '采纳确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info',
      center: true,
    })

    await acceptAnswer(answerId)
    ElMessage.success('采纳成功')
    loadQuestion()
    loadAnswers()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('采纳回答失败：', error)
      ElMessage.error('采纳失败')
    }
  }
}

/**
 * 处理取消采纳回答
 */
const handleCancelAcceptAnswer = async (answerId: number) => {
  try {
    await ElMessageBox.confirm('确定要取消采纳吗？', '取消采纳确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      center: true,
    })

    await cancelAcceptAnswer(answerId, questionId.value)
    ElMessage.success('已取消采纳')
    loadQuestion()
    loadAnswers()
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('取消采纳失败：', error)
      ElMessage.error('操作失败')
    }
  }
}

/**
 * 处理编辑回答
 */
const handleEditAnswer = (answer: AnswerVO) => {
  editingAnswerId.value = answer.id
  editingAnswerContent.value = answer.content
  editAnswerDialogVisible.value = true
}

/**
 * 处理保存编辑的回答
 */
const handleSaveEditAnswer = async () => {
  if (!editingAnswerId.value) {
    return
  }

  if (!editingAnswerContent.value || !editingAnswerContent.value.trim()) {
    ElMessage.warning('请输入回答内容')
    return
  }

  updatingAnswer.value = true
  try {
    await updateAnswer(editingAnswerId.value, { content: editingAnswerContent.value })
    ElMessage.success('回答更新成功')
    editAnswerDialogVisible.value = false
    loadAnswers()
  } catch (error: any) {
    console.error('更新回答失败：', error)
    ElMessage.error(error.message || '更新失败')
  } finally {
    updatingAnswer.value = false
  }
}

/**
 * 处理取消编辑
 */
const handleCancelEditAnswer = () => {
  editingAnswerId.value = null
  editingAnswerContent.value = ''
  editAnswerDialogVisible.value = false
}

/**
 * 处理删除回答
 */
const handleDeleteAnswer = async (answerId: number) => {
  try {
    await ElMessageBox.confirm('确定要删除这个回答吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
      center: true,
    })

    await deleteAnswer(answerId)
    ElMessage.success('删除成功')
    loadAnswers()
    if (question.value) {
      question.value.answerCount--
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除回答失败：', error)
      ElMessage.error('删除失败')
    }
  }
}

/**
 * 显示/隐藏评论
 */
const handleShowComments = async (answerId: number) => {
  if (!expandedComments[answerId]) {
    // 展开评论，加载评论列表
    expandedComments[answerId] = true
    try {
      const result = await getCommentsByTarget('answer', answerId, 1, 50)
      comments[answerId] = result.list
    } catch (error) {
      console.error('加载评论失败：', error)
    }
  } else {
    // 折叠评论
    expandedComments[answerId] = false
  }
}

/**
 * 发布评论
 */
const handlePublishComment = async (answerId: number) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  const content = commentContent[answerId]
  if (!content || !content.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  publishingComment[answerId] = true
  try {
    await publishComment({
      targetType: TargetType.ANSWER,
      targetId: answerId,
      content: content,
    })

    ElMessage.success('评论成功')
    commentContent[answerId] = ''
    // 重新加载评论
    const result = await getCommentsByTarget(TargetType.ANSWER, answerId, 1, 50)
    comments[answerId] = result.list
  } catch (error) {
    console.error('发布评论失败：', error)
    ElMessage.error('评论失败')
  } finally {
    publishingComment[answerId] = false
  }
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

/**
 * 返回上一页
 */
const handleGoBack = () => {
  router.back()
}

onMounted(() => {
  loadQuestion()
  loadAnswers()
  loadRelatedQuestions()
})
</script>

<style lang="scss" scoped>
.question-detail-page {
  width: 100%;
  min-height: calc(100vh - 64px);
}

.loading-container {
  padding: 40px;
  background: #ffffff;
  border-radius: 12px;
}

.detail-container {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
  align-items: start;
}

// ==========================================
// 主内容区
// ==========================================
.detail-main {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.question-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .question-title {
    font-size: 28px;
    font-weight: 700;
    color: #303133;
    line-height: 1.4;
    margin-bottom: 20px;
  }

  .question-meta {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 20px;

    .meta-left {
      display: flex;
      align-items: center;
      gap: 12px;

      .user-info {
        display: flex;
        flex-direction: column;
        gap: 4px;

        .username {
          font-size: 15px;
          font-weight: 600;
          color: #303133;
        }

        .meta-info {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 13px;
          color: #909399;

          .separator {
            opacity: 0.5;
          }

          .views {
            display: flex;
            align-items: center;
            gap: 4px;
          }
        }
      }
    }

    .meta-right {
      display: flex;
      align-items: center;
      gap: 12px;

      .reward-badge {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 8px 16px;
        background: linear-gradient(135deg, #ff6b6b 0%, #ff8e53 100%);
        color: #ffffff;
        border-radius: 20px;
        font-size: 14px;
        font-weight: 600;
        box-shadow: 0 2px 8px rgba(255, 107, 107, 0.3);
      }

      .solved-badge {
        display: flex;
        align-items: center;
        gap: 6px;
        padding: 8px 16px;
        background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
        color: #ffffff;
        border-radius: 20px;
        font-size: 14px;
        font-weight: 600;
      }
    }
  }

  .question-tags {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
    margin-bottom: 24px;
  }

  .question-content {
    font-size: 16px;
    color: #303133;
    line-height: 1.8;
    margin-bottom: 24px;
    word-break: break-word;

    :deep(p) {
      margin-bottom: 16px;
    }

    :deep(img) {
      max-width: 100%;
      border-radius: 8px;
    }

    :deep(code) {
      background: #f5f7fa;
      padding: 2px 6px;
      border-radius: 4px;
      font-family: 'Consolas', 'Monaco', monospace;
    }
  }

  .question-actions {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-top: 20px;
    border-top: 1px solid #e4e7ed;

    .actions-left {
      display: flex;
      align-items: center;
      gap: 12px;

      :deep(.el-button) {
        display: flex;
        align-items: center;
        gap: 6px;
      }
    }

    .actions-right {
      display: flex;
      align-items: center;
      gap: 12px;

      :deep(.el-button) {
        display: flex;
        align-items: center;
        gap: 6px;
      }
    }
  }
}

.answer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .section-title {
    font-size: 20px;
    font-weight: 600;
    color: #303133;
  }
}

.answer-list {
  display: flex;
  flex-direction: column;
  gap: 16px;

  .answer-card {
    background: #ffffff;
    border-radius: 12px;
    padding: 24px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

    .answer-header-info {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 16px;
      position: relative;

      .user-info {
        display: flex;
        flex-direction: column;
        gap: 4px;
        flex: 1;

        .username {
          font-size: 15px;
          font-weight: 600;
          color: #303133;
        }

        .time {
          font-size: 13px;
          color: #909399;
        }
      }

      .accepted-badge {
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
    }

    .answer-content {
      font-size: 15px;
      color: #303133;
      line-height: 1.8;
      margin-bottom: 16px;
      word-break: break-word;

      :deep(p) {
        margin-bottom: 12px;
      }
    }

    .answer-actions {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding-top: 12px;
      border-top: 1px solid #f5f7fa;

      .actions-left,
      .actions-right {
        display: flex;
        align-items: center;
        gap: 8px;

        :deep(.el-button) {
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }
    }

    .comments-section {
      margin-top: 16px;

      .comments-list {
        .comment-item {
          display: flex;
          gap: 12px;
          margin-bottom: 16px;

          .comment-content {
            flex: 1;

            .comment-header {
              display: flex;
              align-items: center;
              gap: 12px;
              margin-bottom: 8px;

              .username {
                font-size: 14px;
                font-weight: 600;
                color: #303133;
              }

              .time {
                font-size: 12px;
                color: #909399;
              }
            }

            .content {
              font-size: 14px;
              color: #606266;
              line-height: 1.6;
              margin: 0;
            }
          }
        }

        .comment-input-box {
          display: flex;
          gap: 12px;
          margin-top: 16px;

          :deep(.el-textarea) {
            flex: 1;
          }

          :deep(.el-button) {
            align-self: flex-end;
          }
        }
      }
    }
  }

  .pagination {
    margin-top: 16px;
    display: flex;
    justify-content: center;
  }
}

.answer-editor-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .editor-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 16px;
  }

  .editor-actions {
    margin-top: 16px;
    display: flex;
    justify-content: flex-end;

    :deep(.el-button) {
      display: flex;
      align-items: center;
      gap: 6px;
    }
  }
}

// ==========================================
// 右侧边栏
// ==========================================
.detail-sidebar {
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

  .author-info {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;

    .author-details {
      text-align: center;

      .author-name {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 4px;
      }

      .author-points {
        font-size: 13px;
        color: #909399;
      }
    }
  }

  .related-list {
    display: flex;
    flex-direction: column;
    gap: 12px;

    .related-item {
      display: flex;
      flex-direction: column;
      gap: 6px;
      padding: 12px;
      background: #f5f7fa;
      border-radius: 8px;
      text-decoration: none;
      transition: all 0.3s ease;

      &:hover {
        background: #e6f0ff;
        transform: translateX(4px);
      }

      .related-title {
        font-size: 14px;
        color: #303133;
        font-weight: 500;
        line-height: 1.5;
        margin: 0;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;
        overflow: hidden;
      }

      .related-meta {
        font-size: 12px;
        color: #909399;
      }
    }

    .empty-text {
      text-align: center;
      font-size: 13px;
      color: #909399;
      padding: 20px 0;
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
        font-size: 18px;
        font-weight: 700;
        color: #0056d2;
      }
    }
  }
}

// ==========================================
// 错误状态
// ==========================================
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .error-icon {
    color: #f56c6c;
    margin-bottom: 16px;
  }

  .error-text {
    font-size: 16px;
    color: #606266;
    margin-bottom: 24px;
  }
}

// ==========================================
// 响应式设计
// ==========================================
@media (max-width: 1024px) {
  .detail-container {
    grid-template-columns: 1fr;
  }

  .detail-sidebar {
    display: none;
  }
}

@media (max-width: 768px) {
  .question-card {
    padding: 20px;

    .question-title {
      font-size: 22px;
    }

    .question-meta {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;

      .meta-right {
        width: 100%;
        justify-content: flex-start;
      }
    }

    .question-actions {
      flex-direction: column;
      gap: 12px;

      .actions-left,
      .actions-right {
        width: 100%;
        justify-content: flex-start;
      }
    }
  }

  .answer-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;

    :deep(.el-radio-group) {
      width: 100%;
    }
  }

  .answer-card {
    padding: 16px;

    .answer-actions {
      flex-direction: column;
      align-items: flex-start;
      gap: 8px;

      .actions-left,
      .actions-right {
        width: 100%;
        justify-content: flex-start;
      }
    }
  }

  .answer-editor-card {
    padding: 16px;

    .editor-actions {
      :deep(.el-button) {
        width: 100%;
        justify-content: center;
      }
    }
  }
}
</style>
