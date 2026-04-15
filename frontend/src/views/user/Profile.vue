<template>
  <div class="profile-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">
        个人中心
      </h1>
      <p class="page-subtitle">
        管理你的个人信息和账户设置
      </p>
    </div>

    <div class="profile-container">
      <!-- 左侧：个人信息展示 -->
      <div class="profile-main">
        <!-- 基本信息卡片 -->
        <div class="info-card">
          <div class="card-header">
            <h2 class="card-title">
              基本信息
            </h2>
            <el-button
              v-if="!editing"
              type="primary"
              text
              @click="handleEditToggle"
            >
              <el-icon><Edit /></el-icon>
              <span>编辑</span>
            </el-button>
            <div
              v-else
              class="edit-actions"
            >
              <el-button
                type="primary"
                size="small"
                :loading="saving"
                @click="handleSave"
              >
                <el-icon><Check /></el-icon>
                <span>保存</span>
              </el-button>
              <el-button
                size="small"
                @click="handleCancel"
              >
                <el-icon><Close /></el-icon>
                <span>取消</span>
              </el-button>
            </div>
          </div>

          <div class="card-content">
            <!-- 头像 -->
            <div class="avatar-section">
              <image-uploader
                v-if="editing"
                v-model="avatarUrls"
                :max-count="1"
                :max-size="2 * 1024 * 1024"
                accept="image/jpeg,image/jpg,image/png"
                hint="支持 jpg、png 格式，大小不超过 2MB"
                @upload-success="handleAvatarUploadSuccess"
              />
              <el-avatar
                v-else
                :size="120"
                :src="userInfo.avatar || `https://ui-avatars.com/api/?name=${userInfo.username}&background=0056D2&color=fff&size=128`"
              >
                <el-icon :size="60">
                  <User />
                </el-icon>
              </el-avatar>
            </div>

            <!-- 信息表单 -->
            <el-form
              ref="profileFormRef"
              :model="profileForm"
              :rules="profileRules"
              label-width="100px"
              class="profile-form"
            >
              <!-- 用户名（不可编辑） -->
              <el-form-item label="用户名">
                <el-input
                  v-model="userInfo.username"
                  disabled
                  :prefix-icon="User"
                />
              </el-form-item>

              <!-- 邮箱 -->
              <el-form-item
                label="邮箱"
                prop="email"
              >
                <el-input
                  v-model="profileForm.email"
                  :disabled="!editing"
                  :prefix-icon="Message"
                  placeholder="请输入邮箱"
                />
              </el-form-item>

              <!-- 真实姓名 -->
              <el-form-item label="真实姓名">
                <el-input
                  v-model="profileForm.realName"
                  :disabled="!editing"
                  placeholder="请输入真实姓名"
                />
              </el-form-item>

              <!-- 学号 -->
              <el-form-item label="学号">
                <el-input
                  v-model="profileForm.studentId"
                  :disabled="!editing"
                  placeholder="请输入学号"
                />
              </el-form-item>

              <!-- 学校 -->
              <el-form-item label="学校">
                <el-input
                  v-model="profileForm.school"
                  :disabled="!editing"
                  placeholder="请输入学校"
                />
              </el-form-item>

              <!-- 专业 -->
              <el-form-item label="专业">
                <el-input
                  v-model="profileForm.major"
                  :disabled="!editing"
                  placeholder="请输入专业"
                />
              </el-form-item>

              <!-- 年级 -->
              <el-form-item label="年级">
                <el-select
                  v-model="profileForm.grade"
                  :disabled="!editing"
                  placeholder="请选择年级"
                  style="width: 100%"
                >
                  <el-option
                    label="大一"
                    value="大一"
                  />
                  <el-option
                    label="大二"
                    value="大二"
                  />
                  <el-option
                    label="大三"
                    value="大三"
                  />
                  <el-option
                    label="大四"
                    value="大四"
                  />
                  <el-option
                    label="研一"
                    value="研一"
                  />
                  <el-option
                    label="研二"
                    value="研二"
                  />
                  <el-option
                    label="研三"
                    value="研三"
                  />
                  <el-option
                    label="博士"
                    value="博士"
                  />
                </el-select>
              </el-form-item>

              <!-- 个人简介 -->
              <el-form-item label="个人简介">
                <el-input
                  v-model="profileForm.bio"
                  type="textarea"
                  :rows="4"
                  :disabled="!editing"
                  placeholder="介绍一下自己吧..."
                  maxlength="200"
                  show-word-limit
                />
              </el-form-item>
            </el-form>
          </div>
        </div>

        <!-- 密码修改卡片 -->
        <div class="info-card">
          <div class="card-header">
            <h2 class="card-title">
              安全设置
            </h2>
          </div>
          <div class="card-content">
            <el-form
              ref="passwordFormRef"
              :model="passwordForm"
              :rules="passwordRules"
              label-width="100px"
              class="password-form"
            >
              <el-form-item
                label="当前密码"
                prop="oldPassword"
              >
                <el-input
                  v-model="passwordForm.oldPassword"
                  type="password"
                  show-password
                  :prefix-icon="Lock"
                  placeholder="请输入当前密码"
                />
              </el-form-item>

              <el-form-item
                label="新密码"
                prop="newPassword"
              >
                <el-input
                  v-model="passwordForm.newPassword"
                  type="password"
                  show-password
                  :prefix-icon="Lock"
                  placeholder="请输入新密码（6-32个字符）"
                />
              </el-form-item>

              <el-form-item
                label="确认密码"
                prop="confirmPassword"
              >
                <el-input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  show-password
                  :prefix-icon="Lock"
                  placeholder="请再次输入新密码"
                />
              </el-form-item>

              <el-form-item>
                <el-button
                  type="primary"
                  :loading="changingPassword"
                  @click="handleChangePassword"
                >
                  <el-icon><Key /></el-icon>
                  <span>修改密码</span>
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </div>

      <!-- 右侧：统计和快捷操作 -->
      <div class="profile-sidebar">
        <!-- 积分卡片 -->
        <div class="sidebar-card points-card">
          <div class="points-header">
            <el-icon
              class="points-icon"
              :size="32"
            >
              <Coin />
            </el-icon>
            <div class="points-info">
              <span class="points-label">我的积分</span>
              <span class="points-number">{{ userInfo.points }}</span>
            </div>
          </div>
          <el-button
            type="primary"
            plain
            block
            @click="handleGoToPoints"
          >
            查看积分明细
          </el-button>
        </div>

        <!-- 统计卡片 -->
        <div class="sidebar-card">
          <div class="card-header">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据统计</span>
          </div>
          <div class="card-content">
            <div class="stats-list">
              <div
                class="stats-item"
                @click="handleGoToQuestions"
              >
                <div class="stats-info">
                  <span class="stats-label">我的提问</span>
                  <span class="stats-value">{{ userInfo.questionCount || 0 }}</span>
                </div>
                <el-icon class="stats-icon">
                  <ArrowRight />
                </el-icon>
              </div>
              <div
                class="stats-item"
                @click="handleGoToAnswers"
              >
                <div class="stats-info">
                  <span class="stats-label">我的回答</span>
                  <span class="stats-value">{{ userInfo.answerCount || 0 }}</span>
                </div>
                <el-icon class="stats-icon">
                  <ArrowRight />
                </el-icon>
              </div>
              <div
                class="stats-item"
                @click="handleGoToCollections"
              >
                <div class="stats-info">
                  <span class="stats-label">我的收藏</span>
                  <span class="stats-value">{{ userInfo.collectionCount || 0 }}</span>
                </div>
                <el-icon class="stats-icon">
                  <ArrowRight />
                </el-icon>
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
                plain
                block
                @click="handleGoToQuestions"
              >
                <el-icon><QuestionFilled /></el-icon>
                <span>我的提问</span>
              </el-button>
              <el-button
                plain
                block
                @click="handleGoToAnswers"
              >
                <el-icon><ChatLineSquare /></el-icon>
                <span>我的回答</span>
              </el-button>
              <el-button
                plain
                block
                @click="handleGoToCollections"
              >
                <el-icon><Star /></el-icon>
                <span>我的收藏</span>
              </el-button>
              <el-button
                plain
                block
                @click="handleGoToNotices"
              >
                <el-icon><Bell /></el-icon>
                <span>消息通知</span>
              </el-button>
            </div>
          </div>
        </div>

        <!-- 账户信息 -->
        <div class="sidebar-card">
          <div class="card-header">
            <el-icon><InfoFilled /></el-icon>
            <span>账户信息</span>
          </div>
          <div class="card-content">
            <div class="account-info">
              <div class="info-item">
                <span class="info-label">注册时间</span>
                <span class="info-value">{{ formatDate(userInfo.createTime) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">最后登录</span>
                <span class="info-value">{{ formatDate(userInfo.updateTime) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">账户状态</span>
                <el-tag
                  :type="userInfo.status === 1 ? 'success' : 'danger'"
                  size="small"
                >
                  {{ userInfo.status === 1 ? '正常' : '已禁用' }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { getCurrentUser, updateUser, updatePassword, type UserUpdateDTO } from '@/api/user'
import ImageUploader from '@/components/ImageUploader.vue'
import type { FileUploadDTO } from '@/api/file'
import dayjs from 'dayjs'
import {
  User,
  Message,
  Lock,
  Edit,
  Check,
  Close,
  Upload,
  Key,
  Coin,
  DataAnalysis,
  ArrowRight,
  Guide,
  QuestionFilled,
  ChatLineSquare,
  Star,
  Bell,
  InfoFilled,
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 表单引用
const profileFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()

// 用户信息
const userInfo = ref({
  id: 0,
  username: '',
  email: '',
  avatar: '',
  bio: '',
  school: '',
  major: '',
  grade: '',
  studentId: '',
  realName: '',
  points: 0,
  questionCount: 0,
  answerCount: 0,
  collectionCount: 0,
  status: 1,
  createTime: '',
  updateTime: '',
})

// 编辑状态
const editing = ref(false)
const saving = ref(false)

// 个人信息表单
const profileForm = reactive<UserUpdateDTO>({
  email: '',
  avatar: '',
  bio: '',
  school: '',
  major: '',
  grade: '',
})

// 密码修改表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const changingPassword = ref(false)

// 头像 URL 列表（用于 ImageUploader 组件）
const avatarUrls = ref<string[]>([])

// 表单验证规则
const profileRules: FormRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
}

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入当前密码', trigger: 'blur' },
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度为6-32个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule: any, value: any, callback: any) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

/**
 * 加载用户信息
 */
const loadUserInfo = async () => {
  try {
    const data = await getCurrentUser()
    userInfo.value = data

    // 同步到表单
    profileForm.email = data.email || ''
    profileForm.avatar = data.avatar || ''
    profileForm.bio = data.bio || ''
    profileForm.school = data.school || ''
    profileForm.major = data.major || ''
    profileForm.grade = data.grade || ''

    // 同步头像 URL 到 avatarUrls
    if (data.avatar) {
      avatarUrls.value = [data.avatar]
    } else {
      avatarUrls.value = []
    }

    // 更新 Store
    userStore.updateUserInfo({
      id: data.id,
      username: data.username,
      avatar: data.avatar,
      points: data.points,
      major: data.major,
      grade: data.grade,
      studentId: data.studentId,
    })
  } catch (error) {
    console.error('加载用户信息失败：', error)
    ElMessage.error('加载用户信息失败')
  }
}

/**
 * 切换编辑状态
 */
const handleEditToggle = () => {
  editing.value = true
}

/**
 * 保存个人信息
 */
const handleSave = async () => {
  if (!profileFormRef.value) return

  try {
    await profileFormRef.value.validate()
  } catch (error) {
    return
  }

  saving.value = true
  try {
    await updateUser(profileForm)
    ElMessage.success('保存成功')
    editing.value = false
    loadUserInfo()
  } catch (error: any) {
    console.error('保存失败：', error)
    ElMessage.error(error.message || '保存失败')
  } finally {
    saving.value = false
  }
}

/**
 * 取消编辑
 */
const handleCancel = () => {
  editing.value = false
  // 恢复原始数据
  profileForm.email = userInfo.value.email || ''
  profileForm.bio = userInfo.value.bio || ''
  profileForm.school = userInfo.value.school || ''
  profileForm.major = userInfo.value.major || ''
  profileForm.grade = userInfo.value.grade || ''
}

/**
 * 头像上传成功回调
 */
const handleAvatarUploadSuccess = (data: FileUploadDTO) => {
  profileForm.avatar = data.url
  userInfo.value.avatar = data.url
  // 更新 Store 中的用户信息
  userStore.updateUserInfo({
    avatar: data.url
  })
  ElMessage.success('头像上传成功')
}

/**
 * 修改密码
 */
const handleChangePassword = async () => {
  if (!passwordFormRef.value) return

  try {
    await passwordFormRef.value.validate()
  } catch (error) {
    return
  }

  try {
    await ElMessageBox.confirm('确定要修改密码吗？', '修改密码确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch (error) {
    return
  }

  changingPassword.value = true
  try {
    await updatePassword(passwordForm.oldPassword, passwordForm.newPassword)
    ElMessage.success('密码修改成功，请重新登录')

    // 清空表单
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''

    // 退出登录
    setTimeout(() => {
      userStore.logout()
      router.push('/login')
    }, 1500)
  } catch (error: any) {
    console.error('修改密码失败：', error)
    ElMessage.error(error.message || '修改密码失败')
  } finally {
    changingPassword.value = false
  }
}

/**
 * 跳转到积分页面
 */
const handleGoToPoints = () => {
  router.push('/user/points')
}

/**
 * 跳转到我的提问
 */
const handleGoToQuestions = () => {
  router.push('/user/questions')
}

/**
 * 跳转到我的回答
 */
const handleGoToAnswers = () => {
  router.push('/user/answers')
}

/**
 * 跳转到我的收藏
 */
const handleGoToCollections = () => {
  router.push('/user/collections')
}

/**
 * 跳转到消息通知
 */
const handleGoToNotices = () => {
  router.push('/user/notices')
}

/**
 * 格式化日期
 */
const formatDate = (date: string) => {
  return date ? dayjs(date).format('YYYY-MM-DD HH:mm') : '-'
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style lang="scss" scoped>
.profile-page {
  width: 100%;
  min-height: calc(100vh - 64px);
}

// ==========================================
// 页面头部
// ==========================================
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
    margin-bottom: 8px;
  }

  .page-subtitle {
    font-size: 14px;
    color: #909399;
  }
}

// ==========================================
// 主容器
// ==========================================
.profile-container {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
  align-items: start;
}

// ==========================================
// 主内容区
// ==========================================
.profile-main {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.info-card {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20px 24px;
    border-bottom: 1px solid #e4e7ed;

    .card-title {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }

    .edit-actions {
      display: flex;
      gap: 8px;
    }

    :deep(.el-button) {
      display: flex;
      align-items: center;
      gap: 6px;
    }
  }

  .card-content {
    padding: 24px;
  }
}

// ==========================================
// 头像区域
// ==========================================
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  margin-bottom: 32px;
  padding-bottom: 32px;
  border-bottom: 1px solid #e4e7ed;
  width: 100%;

  :deep(.image-uploader) {
    width: 100%;
    max-width: 200px;
  }

  :deep(.image-list) {
    grid-template-columns: 1fr;
  }

  :deep(.upload-button) {
    padding-bottom: 100%;
  }

  .upload-btn {
    :deep(.el-button) {
      display: flex;
      align-items: center;
      gap: 6px;
    }
  }
}

// ==========================================
// 表单
// ==========================================
.profile-form,
.password-form {
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }

  :deep(.el-form-item__label) {
    font-weight: 500;
    color: #606266;
  }

  :deep(.el-input.is-disabled .el-input__wrapper) {
    background-color: #f5f7fa;
  }
}

// ==========================================
// 右侧边栏
// ==========================================
.profile-sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: sticky;
  top: 88px;
}

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

// 积分卡片
.points-card {
  background: linear-gradient(135deg, #ff6b6b 0%, #ff8e53 100%);
  color: #ffffff;

  .points-header {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 24px;

    .points-icon {
      color: #ffffff;
    }

    .points-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 8px;

      .points-label {
        font-size: 14px;
        opacity: 0.9;
      }

      .points-number {
        font-size: 32px;
        font-weight: 700;
      }
    }
  }

  :deep(.el-button) {
    margin: 0 24px 24px;
    background: rgba(255, 255, 255, 0.2);
    border-color: rgba(255, 255, 255, 0.3);
    color: #ffffff;

    &:hover {
      background: rgba(255, 255, 255, 0.3);
      border-color: rgba(255, 255, 255, 0.5);
    }
  }
}

// 统计列表
.stats-list {
  display: flex;
  flex-direction: column;
  gap: 12px;

  .stats-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px;
    background: #f5f7fa;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.3s ease;

    &:hover {
      background: #e6f0ff;
      transform: translateX(4px);
    }

    .stats-info {
      display: flex;
      align-items: center;
      gap: 12px;

      .stats-label {
        font-size: 14px;
        color: #606266;
      }

      .stats-value {
        font-size: 20px;
        font-weight: 700;
        color: #0056d2;
      }
    }

    .stats-icon {
      font-size: 16px;
      color: #909399;
    }
  }
}

// 快捷操作
.quick-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;

  :deep(.el-button) {
    justify-content: flex-start;
    gap: 8px;
  }
}

// 账户信息
.account-info {
  display: flex;
  flex-direction: column;
  gap: 12px;

  .info-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 0;
    border-bottom: 1px dashed #e4e7ed;

    &:last-child {
      border-bottom: none;
    }

    .info-label {
      font-size: 13px;
      color: #909399;
    }

    .info-value {
      font-size: 13px;
      color: #303133;
      font-weight: 500;
    }
  }
}

// ==========================================
// 响应式设计
// ==========================================
@media (max-width: 1024px) {
  .profile-container {
    grid-template-columns: 1fr;
  }

  .profile-sidebar {
    position: static;
  }
}

@media (max-width: 768px) {
  .page-header {
    padding: 20px;

    .page-title {
      font-size: 24px;
    }
  }

  .info-card {
    .card-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 12px;

      .edit-actions {
        width: 100%;

        :deep(.el-button) {
          flex: 1;
        }
      }
    }

    .card-content {
      padding: 20px;
    }
  }

  .profile-form {
    :deep(.el-form-item__label) {
      text-align: left;
    }
  }

  .quick-actions {
    :deep(.el-button) {
      justify-content: center;
    }
  }
}
</style>
