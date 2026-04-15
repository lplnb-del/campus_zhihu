<template>
  <div class="ask-question-page">
    <div class="ask-container">
      <!-- 页面标题 -->
      <div class="page-header">
        <h1 class="page-title">
          {{ isEdit ? '编辑问题' : '提出问题' }}
        </h1>
        <p class="page-subtitle">
          详细描述你的问题，获得更好的回答
        </p>
      </div>

      <!-- 提问表单 -->
      <el-form
        ref="questionFormRef"
        :model="questionForm"
        :rules="questionRules"
        label-position="top"
        class="question-form"
      >
        <!-- 问题标题 -->
        <el-form-item
          label="问题标题"
          prop="title"
        >
          <el-input
            v-model="questionForm.title"
            placeholder="请用一句话简洁地描述你的问题（至少10个字）"
            size="large"
            maxlength="100"
            show-word-limit
            clearable
          >
            <template #prefix>
              <el-icon><Edit /></el-icon>
            </template>
          </el-input>
          <template #extra>
            <div class="form-tip">
              <el-icon><QuestionFilled /></el-icon>
              <span>清晰的标题可以让问题更容易被搜索到</span>
            </div>
          </template>
        </el-form-item>

        <!-- 问题内容 -->
        <el-form-item
          label="问题详情"
          prop="content"
        >
          <el-input
            v-model="questionForm.content"
            type="textarea"
            :rows="12"
            placeholder="详细描述你的问题，包括：&#10;1. 问题的具体情况&#10;2. 你尝试过的方法&#10;3. 期望得到什么样的答案&#10;&#10;支持 Markdown 格式"
            maxlength="10000"
            show-word-limit
          />
          <template #extra>
            <div class="form-tip">
              <el-icon><Document /></el-icon>
              <span>详细的问题描述能获得更专业的回答</span>
            </div>
          </template>
        </el-form-item>

        <!-- 标签选择 -->
        <el-form-item
          label="选择话题标签"
          prop="tagIds"
        >
          <el-select
            v-model="questionForm.tagIds"
            multiple
            filterable
            placeholder="选择或搜索话题标签（最多5个）"
            size="large"
            style="width: 100%"
            :max-collapse-tags="3"
            collapse-tags
            collapse-tags-tooltip
          >
            <el-option
              v-for="tag in tags"
              :key="tag.id"
              :label="tag.name"
              :value="tag.id"
              :disabled="questionForm.tagIds.length >= 5 && !questionForm.tagIds.includes(tag.id)"
            >
              <div class="tag-option">
                <span class="tag-name">{{ tag.name }}</span>
                <span class="tag-count">{{ tag.questionCount }} 问题</span>
              </div>
            </el-option>
          </el-select>
          <template #extra>
            <div class="form-tip">
              <el-icon><PriceTag /></el-icon>
              <span>选择合适的标签，让问题更容易被相关领域的人看到</span>
            </div>
          </template>
        </el-form-item>

        <!-- 悬赏积分 -->
        <el-form-item
          label="悬赏积分（可选）"
          prop="rewardPoints"
        >
          <div class="reward-section">
            <el-input-number
              v-model="questionForm.rewardPoints"
              :min="0"
              :max="maxRewardPoints"
              :step="10"
              size="large"
              controls-position="right"
            />
            <div class="reward-info">
              <span class="current-points">当前积分：{{ userStore.points }}</span>
              <span class="reward-tip">
                <el-icon><Coin /></el-icon>
                <span>悬赏积分可以激励更多人回答你的问题</span>
              </span>
            </div>
          </div>
          <template #extra>
            <div class="form-tip warning">
              <el-icon><Warning /></el-icon>
              <span>悬赏积分在问题发布后会立即扣除，采纳答案后将转给回答者</span>
            </div>
          </template>
        </el-form-item>

        <!-- 草稿保存 -->
        <el-form-item>
          <el-checkbox v-model="questionForm.isDraft">
            保存为草稿（不公开发布）
          </el-checkbox>
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item>
          <div class="form-actions">
            <el-button
              type="primary"
              size="large"
              class="submit-btn"
              :loading="submitting"
              @click="handleSubmit"
            >
              <el-icon><Check /></el-icon>
              <span>{{ questionForm.isDraft ? '保存草稿' : (isEdit ? '更新问题' : '发布问题') }}</span>
            </el-button>
            <el-button
              size="large"
              @click="handleCancel"
            >
              <el-icon><Close /></el-icon>
              <span>取消</span>
            </el-button>
            <el-button
              v-if="!isEdit"
              size="large"
              @click="handlePreview"
            >
              <el-icon><View /></el-icon>
              <span>预览</span>
            </el-button>
          </div>
        </el-form-item>
      </el-form>

      <!-- 右侧提示卡片 -->
      <div class="tips-sidebar">
        <!-- 提问指南 -->
        <div class="tip-card">
          <div class="card-header">
            <el-icon><Guide /></el-icon>
            <span>提问指南</span>
          </div>
          <div class="card-content">
            <ul class="tip-list">
              <li>
                <el-icon class="tip-icon">
                  <Check />
                </el-icon>
                <span>使用清晰、具体的标题</span>
              </li>
              <li>
                <el-icon class="tip-icon">
                  <Check />
                </el-icon>
                <span>详细描述问题的背景和细节</span>
              </li>
              <li>
                <el-icon class="tip-icon">
                  <Check />
                </el-icon>
                <span>说明你已经尝试过的方法</span>
              </li>
              <li>
                <el-icon class="tip-icon">
                  <Check />
                </el-icon>
                <span>选择合适的话题标签</span>
              </li>
              <li>
                <el-icon class="tip-icon">
                  <Check />
                </el-icon>
                <span>考虑设置悬赏以获得更多关注</span>
              </li>
            </ul>
          </div>
        </div>

        <!-- 社区规范 -->
        <div class="tip-card">
          <div class="card-header">
            <el-icon><Warning /></el-icon>
            <span>社区规范</span>
          </div>
          <div class="card-content">
            <ul class="tip-list warning">
              <li>
                <el-icon class="tip-icon">
                  <Close />
                </el-icon>
                <span>不要发布重复的问题</span>
              </li>
              <li>
                <el-icon class="tip-icon">
                  <Close />
                </el-icon>
                <span>不要包含广告或垃圾信息</span>
              </li>
              <li>
                <el-icon class="tip-icon">
                  <Close />
                </el-icon>
                <span>不要使用不当或攻击性语言</span>
              </li>
              <li>
                <el-icon class="tip-icon">
                  <Close />
                </el-icon>
                <span>不要抄袭他人内容</span>
              </li>
            </ul>
          </div>
        </div>

        <!-- Markdown 语法提示 -->
        <div class="tip-card">
          <div class="card-header">
            <el-icon><Document /></el-icon>
            <span>Markdown 语法</span>
          </div>
          <div class="card-content">
            <div class="markdown-examples">
              <div class="example-item">
                <code>**粗体**</code>
                <span class="arrow">→</span>
                <strong>粗体</strong>
              </div>
              <div class="example-item">
                <code>*斜体*</code>
                <span class="arrow">→</span>
                <em>斜体</em>
              </div>
              <div class="example-item">
                <code>`代码`</code>
                <span class="arrow">→</span>
                <code>代码</code>
              </div>
              <div class="example-item">
                <code>- 列表</code>
                <span class="arrow">→</span>
                <span>• 列表</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 预览对话框 -->
    <el-dialog
      v-model="previewVisible"
      title="问题预览"
      width="800px"
      :close-on-click-modal="false"
    >
      <div class="preview-content">
        <h2 class="preview-title">
          {{ questionForm.title || '（标题未填写）' }}
        </h2>
        <div class="preview-tags">
          <span
            v-for="tagId in questionForm.tagIds"
            :key="tagId"
            class="campus-tag"
          >
            {{ getTagName(tagId) }}
          </span>
        </div>
        <div
          class="preview-body"
          v-html="previewContent"
        />
        <div
          v-if="questionForm.rewardPoints > 0"
          class="preview-reward"
        >
          <el-icon><Coin /></el-icon>
          <span>悬赏 {{ questionForm.rewardPoints }} 积分</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="previewVisible = false">
          关闭
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
import type { FormInstance, FormRules } from 'element-plus'
import { publishQuestion, updateQuestion, getQuestionById, type QuestionPublishDTO, type QuestionUpdateDTO } from '@/api/question'
import { getAllTags, type TagVO } from '@/api/tag'
import {
  Edit,
  QuestionFilled,
  Document,
  PriceTag,
  Coin,
  Warning,
  Check,
  Close,
  View,
  Guide,
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 表单引用
const questionFormRef = ref<FormInstance>()

// 是否为编辑模式
const isEdit = computed(() => !!route.query.id)
const editId = computed(() => route.query.id ? Number(route.query.id) : undefined)

// 表单数据
const questionForm = reactive<QuestionPublishDTO & { isDraft: boolean }>({
  title: '',
  content: '',
  tagIds: [],
  rewardPoints: 0,
  isDraft: false,
})

// 标签列表
const tags = ref<TagVO[]>([])

// 最大悬赏积分
const maxRewardPoints = computed(() => {
  return Math.max(0, userStore.points)
})

// 提交状态
const submitting = ref(false)

// 预览
const previewVisible = ref(false)
const previewContent = computed(() => {
  // 简单的 Markdown 转 HTML（实际项目应使用 markdown-it）
  return questionForm.content
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    .replace(/\*(.+?)\*/g, '<em>$1</em>')
    .replace(/`(.+?)`/g, '<code>$1</code>')
    .replace(/\n/g, '<br>')
})

// 表单验证规则
const questionRules: FormRules = {
  title: [
    { required: true, message: '请输入问题标题', trigger: 'blur' },
    { min: 10, message: '标题至少需要10个字符', trigger: 'blur' },
    { max: 100, message: '标题不能超过100个字符', trigger: 'blur' },
  ],
  content: [
    { required: true, message: '请输入问题详情', trigger: 'blur' },
    { min: 20, message: '问题详情至少需要20个字符', trigger: 'blur' },
  ],
  tagIds: [
    {
      type: 'array',
      required: true,
      message: '请至少选择一个标签',
      trigger: 'change',
    },
    {
      type: 'array',
      min: 1,
      max: 5,
      message: '请选择1-5个标签',
      trigger: 'change',
    },
  ],
}

/**
 * 加载标签列表
 */
const loadTags = async () => {
  try {
    tags.value = await getAllTags()
  } catch (error) {
    console.error('加载标签列表失败：', error)
    ElMessage.error('加载标签列表失败')
  }
}

/**
 * 加载问题详情（编辑模式）
 */
const loadQuestion = async () => {
  if (!editId.value) return

  try {
    const question = await getQuestionById(editId.value)
    questionForm.title = question.title
    questionForm.content = question.content
    questionForm.tagIds = question.tags.map(tag => tag.id)
    questionForm.rewardPoints = question.rewardPoints
    questionForm.isDraft = question.isDraft
  } catch (error) {
    console.error('加载问题详情失败：', error)
    ElMessage.error('加载问题详情失败')
    router.push('/questions')
  }
}

/**
 * 获取标签名称
 */
const getTagName = (tagId: number) => {
  const tag = tags.value.find(t => t.id === tagId)
  return tag ? tag.name : ''
}

/**
 * 处理提交
 */
const handleSubmit = async () => {
  if (!questionFormRef.value) return

  try {
    await questionFormRef.value.validate()
  } catch (error) {
    return
  }

  // 检查积分是否足够
  if (questionForm.rewardPoints > userStore.points) {
    ElMessage.warning('积分不足，无法设置悬赏')
    return
  }

  submitting.value = true
  try {
    if (isEdit.value && editId.value) {
      // 更新问题
      await updateQuestion(editId.value, {
        title: questionForm.title,
        content: questionForm.content,
        tagIds: questionForm.tagIds,
        rewardPoints: questionForm.rewardPoints,
        isDraft: questionForm.isDraft,
      })
      ElMessage.success('更新成功')
      router.push(`/question/${editId.value}`)
    } else {
      // 发布问题
      const result = await publishQuestion({
        title: questionForm.title,
        content: questionForm.content,
        tagIds: questionForm.tagIds,
        rewardPoints: questionForm.rewardPoints,
        isDraft: questionForm.isDraft,
      })

      // 扣除悬赏积分
      if (questionForm.rewardPoints > 0) {
        userStore.subtractPoints(questionForm.rewardPoints)
      }

      ElMessage.success(questionForm.isDraft ? '草稿保存成功' : '发布成功')

      if (questionForm.isDraft) {
        router.push('/user/questions')
      } else {
        router.push(`/question/${result.id}`)
      }
    }
  } catch (error: any) {
    console.error('提交失败：', error)
    ElMessage.error(error.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

/**
 * 处理取消
 */
const handleCancel = async () => {
  // 检查是否有未保存的内容
  if (questionForm.title || questionForm.content || questionForm.tagIds.length > 0) {
    try {
      await ElMessageBox.confirm('确定要取消吗？未保存的内容将丢失。', '取消确认', {
        confirmButtonText: '确定',
        cancelButtonText: '继续编辑',
        type: 'warning',
      })
      router.back()
    } catch (error) {
      // 用户点击了"继续编辑"
    }
  } else {
    router.back()
  }
}

/**
 * 处理预览
 */
const handlePreview = async () => {
  if (!questionForm.title) {
    ElMessage.warning('请先填写标题')
    return
  }
  if (!questionForm.content) {
    ElMessage.warning('请先填写问题详情')
    return
  }
  previewVisible.value = true
}

onMounted(() => {
  loadTags()
  if (isEdit.value) {
    loadQuestion()
  }
})
</script>

<style lang="scss" scoped>
.ask-question-page {
  width: 100%;
  min-height: calc(100vh - 64px);
}

.ask-container {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
  align-items: start;
}

// ==========================================
// 页面头部
// ==========================================
.page-header {
  grid-column: 1 / -1;
  padding: 24px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .page-title {
    font-size: 28px;
    font-weight: 700;
    color: #303133;
    margin-bottom: 8px;
  }

  .page-subtitle {
    font-size: 14px;
    color: #909399;
  }
}

// ==========================================
// 提问表单
// ==========================================
.question-form {
  background: #ffffff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .form-tip {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-top: 8px;
    font-size: 13px;
    color: #909399;

    .el-icon {
      font-size: 14px;
    }

    &.warning {
      color: #e6a23c;

      .el-icon {
        color: #e6a23c;
      }
    }
  }

  .tag-option {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;

    .tag-name {
      font-size: 14px;
      color: #303133;
    }

    .tag-count {
      font-size: 12px;
      color: #909399;
    }
  }

  .reward-section {
    display: flex;
    flex-direction: column;
    gap: 12px;

    .reward-info {
      display: flex;
      flex-direction: column;
      gap: 8px;
      padding: 12px;
      background: #f5f7fa;
      border-radius: 8px;

      .current-points {
        font-size: 14px;
        font-weight: 600;
        color: #0056d2;
      }

      .reward-tip {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 13px;
        color: #606266;

        .el-icon {
          color: #ff6b6b;
        }
      }
    }
  }

  .form-actions {
    display: flex;
    align-items: center;
    gap: 12px;

    .submit-btn {
      display: flex;
      align-items: center;
      gap: 8px;
      background: linear-gradient(135deg, #0056d2 0%, #0070f3 100%);
      border: none;

      &:hover {
        background: linear-gradient(135deg, #0045a8 0%, #0056d2 100%);
        box-shadow: 0 4px 12px rgba(0, 86, 210, 0.3);
      }
    }

    :deep(.el-button) {
      display: flex;
      align-items: center;
      gap: 6px;
    }
  }
}

// ==========================================
// 右侧提示卡片
// ==========================================
.tips-sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: sticky;
  top: 88px;

  .tip-card {
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

    .tip-list {
      list-style: none;
      padding: 0;
      margin: 0;
      display: flex;
      flex-direction: column;
      gap: 12px;

      li {
        display: flex;
        align-items: flex-start;
        gap: 8px;
        font-size: 13px;
        color: #606266;
        line-height: 1.6;

        .tip-icon {
          flex-shrink: 0;
          margin-top: 2px;
          font-size: 14px;
          color: #67c23a;
        }
      }

      &.warning {
        li {
          .tip-icon {
            color: #f56c6c;
          }
        }
      }
    }

    .markdown-examples {
      display: flex;
      flex-direction: column;
      gap: 12px;

      .example-item {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 13px;

        code {
          padding: 2px 6px;
          background: #f5f7fa;
          border-radius: 4px;
          font-family: 'Consolas', 'Monaco', monospace;
          color: #0056d2;
        }

        .arrow {
          color: #909399;
        }

        strong,
        em {
          color: #303133;
        }
      }
    }
  }
}

// ==========================================
// 预览对话框
// ==========================================
.preview-content {
  .preview-title {
    font-size: 24px;
    font-weight: 700;
    color: #303133;
    margin-bottom: 16px;
    line-height: 1.4;
  }

  .preview-tags {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
    margin-bottom: 20px;
  }

  .preview-body {
    font-size: 15px;
    color: #303133;
    line-height: 1.8;
    margin-bottom: 20px;
    word-break: break-word;

    :deep(strong) {
      font-weight: 600;
    }

    :deep(em) {
      font-style: italic;
    }

    :deep(code) {
      padding: 2px 6px;
      background: #f5f7fa;
      border-radius: 4px;
      font-family: 'Consolas', 'Monaco', monospace;
      color: #0056d2;
    }
  }

  .preview-reward {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    background: linear-gradient(135deg, #ff6b6b 0%, #ff8e53 100%);
    color: #ffffff;
    border-radius: 20px;
    font-size: 14px;
    font-weight: 600;
  }
}

// ==========================================
// 响应式设计
// ==========================================
@media (max-width: 1024px) {
  .ask-container {
    grid-template-columns: 1fr;
  }

  .tips-sidebar {
    display: none;
  }
}

@media (max-width: 768px) {
  .page-header {
    padding: 20px;

    .page-title {
      font-size: 24px;
    }
  }

  .question-form {
    padding: 20px;

    .form-actions {
      flex-direction: column;

      :deep(.el-button) {
        width: 100%;
        justify-content: center;
      }
    }
  }
}

// ==========================================
// Element Plus 组件样式覆盖
// ==========================================
:deep(.el-form-item__label) {
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner) {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;

  &:hover,
  &.is-focus {
    box-shadow: 0 2px 12px rgba(0, 86, 210, 0.2);
  }
}

:deep(.el-select) {
  .el-input__wrapper {
    border-radius: 8px;
  }
}
</style>
