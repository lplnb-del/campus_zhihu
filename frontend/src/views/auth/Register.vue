<template>
  <div class="register-page">
    <div class="register-container">
      <!-- 左侧装饰区 -->
      <div class="register-left">
        <div class="decoration-content">
          <div class="logo-section">
            <el-icon
              :size="64"
              class="logo-icon"
            >
              <ChatDotRound />
            </el-icon>
            <h1 class="brand-name">
              加入 CampusZhihu
            </h1>
            <p class="brand-slogan">
              开启你的知识分享之旅
            </p>
          </div>
          <div class="feature-list">
            <div class="feature-item">
              <el-icon class="feature-icon">
                <Check />
              </el-icon>
              <div class="feature-text">
                <h3>完全免费</h3>
                <p>注册即可享受所有功能</p>
              </div>
            </div>
            <div class="feature-item">
              <el-icon class="feature-icon">
                <Check />
              </el-icon>
              <div class="feature-text">
                <h3>积分奖励</h3>
                <p>新用户赠送 100 积分</p>
              </div>
            </div>
            <div class="feature-item">
              <el-icon class="feature-icon">
                <Check />
              </el-icon>
              <div class="feature-text">
                <h3>学术交流</h3>
                <p>与校园精英共同成长</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧注册表单 -->
      <div class="register-right">
        <div class="register-form-wrapper">
          <div class="form-header">
            <h2 class="form-title">
              创建账号
            </h2>
            <p class="form-subtitle">
              填写信息，快速注册
            </p>
          </div>

          <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            class="register-form"
            label-position="top"
          >
            <!-- 用户名 -->
            <el-form-item
              label="用户名"
              prop="username"
            >
              <el-input
                v-model="registerForm.username"
                placeholder="3-20个字符，字母、数字、下划线"
                size="large"
                clearable
                :prefix-icon="User"
                @blur="checkUsernameAvailable"
              >
                <template #suffix>
                  <el-icon
                    v-if="usernameChecking"
                    class="is-loading"
                  >
                    <Loading />
                  </el-icon>
                  <el-icon
                    v-else-if="usernameAvailable === true"
                    class="success-icon"
                  >
                    <CircleCheck />
                  </el-icon>
                  <el-icon
                    v-else-if="usernameAvailable === false"
                    class="error-icon"
                  >
                    <CircleClose />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>

            <!-- 邮箱 -->
            <el-form-item
              label="邮箱"
              prop="email"
            >
              <el-input
                v-model="registerForm.email"
                placeholder="请输入邮箱地址"
                size="large"
                clearable
                :prefix-icon="Message"
              />
            </el-form-item>

            <!-- 密码 -->
            <el-form-item
              label="密码"
              prop="password"
            >
              <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="6-32个字符，建议包含字母和数字"
                size="large"
                show-password
                :prefix-icon="Lock"
              />
              <div class="password-strength">
                <span class="strength-label">密码强度：</span>
                <div class="strength-bar">
                  <div
                    class="strength-fill"
                    :class="passwordStrengthClass"
                    :style="{ width: passwordStrengthPercent }"
                  />
                </div>
                <span
                  class="strength-text"
                  :class="passwordStrengthClass"
                >
                  {{ passwordStrengthText }}
                </span>
              </div>
            </el-form-item>

            <!-- 确认密码 -->
            <el-form-item
              label="确认密码"
              prop="confirmPassword"
            >
              <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="请再次输入密码"
                size="large"
                show-password
                :prefix-icon="Lock"
                @keyup.enter="handleRegister"
              />
            </el-form-item>

            <!-- 学号（可选） -->
            <el-form-item
              label="学号（可选）"
              prop="studentId"
            >
              <el-input
                v-model="registerForm.studentId"
                placeholder="请输入学号"
                size="large"
                clearable
                :prefix-icon="Postcard"
                @blur="checkStudentIdAvailable"
              >
                <template #suffix>
                  <el-icon
                    v-if="studentIdChecking"
                    class="is-loading"
                  >
                    <Loading />
                  </el-icon>
                  <el-icon
                    v-else-if="studentIdAvailable === true"
                    class="success-icon"
                  >
                    <CircleCheck />
                  </el-icon>
                  <el-icon
                    v-else-if="studentIdAvailable === false"
                    class="error-icon"
                  >
                    <CircleClose />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>

            <!-- 真实姓名（可选） -->
            <el-form-item
              label="真实姓名（可选）"
              prop="realName"
            >
              <el-input
                v-model="registerForm.realName"
                placeholder="请输入真实姓名"
                size="large"
                clearable
                :prefix-icon="User"
              />
            </el-form-item>

            <!-- 用户协议 -->
            <el-form-item prop="agree">
              <el-checkbox v-model="registerForm.agree">
                <span class="agree-text">
                  我已阅读并同意
                  <el-link
                    type="primary"
                    :underline="false"
                    @click="showAgreement"
                  >
                    《用户协议》
                  </el-link>
                  和
                  <el-link
                    type="primary"
                    :underline="false"
                    @click="showPrivacy"
                  >
                    《隐私政策》
                  </el-link>
                </span>
              </el-checkbox>
            </el-form-item>

            <!-- 注册按钮 -->
            <el-button
              type="primary"
              size="large"
              class="register-btn"
              :loading="loading"
              :disabled="!registerForm.agree"
              @click="handleRegister"
            >
              {{ loading ? '注册中...' : '立即注册' }}
            </el-button>
          </el-form>

          <!-- 登录提示 -->
          <div class="login-tip">
            <span>已有账号？</span>
            <el-link
              type="primary"
              :underline="false"
              @click="handleLogin"
            >
              立即登录
            </el-link>
          </div>
        </div>
      </div>
    </div>

    <!-- 用户协议对话框 -->
    <el-dialog
      v-model="agreementVisible"
      title="用户协议"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="agreement-content">
        <h3>欢迎使用 CampusZhihu</h3>
        <p>在使用我们的服务之前，请仔细阅读以下用户协议：</p>
        <ol>
          <li>用户应遵守国家法律法规，不得发布违法违规内容。</li>
          <li>用户应尊重他人知识产权，不得抄袭他人内容。</li>
          <li>用户应文明交流，不得发布攻击性言论。</li>
          <li>用户应保护账号安全，不得将账号出借他人。</li>
          <li>平台有权对违规内容进行删除或限制。</li>
        </ol>
        <p>如有疑问，请联系客服。</p>
      </div>
      <template #footer>
        <el-button
          type="primary"
          @click="agreementVisible = false"
        >
          我已了解
        </el-button>
      </template>
    </el-dialog>

    <!-- 隐私政策对话框 -->
    <el-dialog
      v-model="privacyVisible"
      title="隐私政策"
      width="600px"
      :close-on-click-modal="false"
    >
      <div class="agreement-content">
        <h3>隐私保护承诺</h3>
        <p>我们非常重视用户的隐私保护：</p>
        <ol>
          <li>我们只收集必要的用户信息。</li>
          <li>用户信息仅用于提供服务，不会泄露给第三方。</li>
          <li>用户可以随时修改或删除个人信息。</li>
          <li>我们采用加密技术保护用户数据安全。</li>
          <li>未经用户同意，不会发送营销信息。</li>
        </ol>
        <p>我们承诺保护您的隐私安全。</p>
      </div>
      <template #footer>
        <el-button
          type="primary"
          @click="privacyVisible = false"
        >
          我已了解
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { register, checkUsername, checkStudentId, type RegisterDTO } from '@/api/user'
import {
  ChatDotRound,
  Check,
  User,
  Message,
  Lock,
  Postcard,
  Loading,
  CircleCheck,
  CircleClose,
} from '@element-plus/icons-vue'

const router = useRouter()

// 表单引用
const registerFormRef = ref<FormInstance>()

// 表单数据
const registerForm = reactive<RegisterDTO & { confirmPassword: string; agree: boolean }>({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  studentId: '',
  realName: '',
  agree: false,
})

// 加载状态
const loading = ref(false)

// 用户名检查
const usernameChecking = ref(false)
const usernameAvailable = ref<boolean | null>(null)

// 学号检查
const studentIdChecking = ref(false)
const studentIdAvailable = ref<boolean | null>(null)

// 对话框
const agreementVisible = ref(false)
const privacyVisible = ref(false)

// 密码强度
const passwordStrength = computed(() => {
  const pwd = registerForm.password
  if (!pwd) return 0

  let strength = 0

  // 长度检查
  if (pwd.length >= 6) strength += 1
  if (pwd.length >= 10) strength += 1

  // 包含小写字母
  if (/[a-z]/.test(pwd)) strength += 1

  // 包含大写字母
  if (/[A-Z]/.test(pwd)) strength += 1

  // 包含数字
  if (/\d/.test(pwd)) strength += 1

  // 包含特殊字符
  if (/[!@#$%^&*(),.?":{}|<>]/.test(pwd)) strength += 1

  return Math.min(strength, 4)
})

const passwordStrengthPercent = computed(() => {
  return `${(passwordStrength.value / 4) * 100}%`
})

const passwordStrengthClass = computed(() => {
  const strength = passwordStrength.value
  if (strength === 0) return ''
  if (strength <= 1) return 'weak'
  if (strength <= 2) return 'medium'
  if (strength <= 3) return 'strong'
  return 'very-strong'
})

const passwordStrengthText = computed(() => {
  const strength = passwordStrength.value
  if (strength === 0) return ''
  if (strength <= 1) return '弱'
  if (strength <= 2) return '中'
  if (strength <= 3) return '强'
  return '很强'
})

// 自定义验证器
const validateUsername = (rule: any, value: any, callback: any) => {
  if (!value) {
    callback(new Error('请输入用户名'))
  } else if (!/^[a-zA-Z0-9_]{3,20}$/.test(value)) {
    callback(new Error('用户名只能包含字母、数字、下划线，3-20个字符'))
  } else {
    callback()
  }
}

const validatePassword = (rule: any, value: any, callback: any) => {
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (value.length < 6 || value.length > 32) {
    callback(new Error('密码长度为6-32个字符'))
  } else {
    // 如果确认密码已输入，重新验证确认密码
    if (registerForm.confirmPassword) {
      registerFormRef.value?.validateField('confirmPassword')
    }
    callback()
  }
}

const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (!value) {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const validateAgree = (rule: any, value: any, callback: any) => {
  if (!value) {
    callback(new Error('请阅读并同意用户协议'))
  } else {
    callback()
  }
}

// 表单验证规则
const registerRules: FormRules = {
  username: [
    { required: true, validator: validateUsername, trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  password: [
    { required: true, validator: validatePassword, trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' },
  ],
  agree: [
    { validator: validateAgree, trigger: 'change' },
  ],
}

// 监听用户名变化，重置检查状态
watch(() => registerForm.username, () => {
  usernameAvailable.value = null
})

// 监听学号变化，重置检查状态
watch(() => registerForm.studentId, () => {
  studentIdAvailable.value = null
})

/**
 * 检查用户名是否可用
 */
const checkUsernameAvailable = async () => {
  if (!registerForm.username || !/^[a-zA-Z0-9_]{3,20}$/.test(registerForm.username)) {
    return
  }

  usernameChecking.value = true
  try {
    const exists = await checkUsername(registerForm.username)
    usernameAvailable.value = !exists
    if (exists) {
      ElMessage.warning('用户名已被使用')
    }
  } catch (error) {
    console.error('检查用户名失败：', error)
  } finally {
    usernameChecking.value = false
  }
}

/**
 * 检查学号是否可用
 */
const checkStudentIdAvailable = async () => {
  if (!registerForm.studentId) {
    studentIdAvailable.value = null
    return
  }

  studentIdChecking.value = true
  try {
    const exists = await checkStudentId(registerForm.studentId)
    studentIdAvailable.value = !exists
    if (exists) {
      ElMessage.warning('学号已被注册')
    }
  } catch (error) {
    console.error('检查学号失败：', error)
  } finally {
    studentIdChecking.value = false
  }
}

/**
 * 处理注册
 */
const handleRegister = async () => {
  if (!registerFormRef.value) return

  try {
    await registerFormRef.value.validate()
  } catch (error) {
    return
  }

  // 检查用户名是否可用
  if (usernameAvailable.value === false) {
    ElMessage.warning('用户名已被使用，请更换')
    return
  }

  // 检查学号是否可用
  if (registerForm.studentId && studentIdAvailable.value === false) {
    ElMessage.warning('学号已被注册，请检查')
    return
  }

  loading.value = true
  try {
    await register({
      username: registerForm.username,
      password: registerForm.password,
      email: registerForm.email,
      studentId: registerForm.studentId || undefined,
      realName: registerForm.realName || undefined,
    })

    ElMessage.success('注册成功！请登录')
    router.push('/login')
  } catch (error: any) {
    console.error('注册失败：', error)
    ElMessage.error(error.message || '注册失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

/**
 * 处理登录
 */
const handleLogin = () => {
  router.push('/login')
}

/**
 * 显示用户协议
 */
const showAgreement = () => {
  agreementVisible.value = true
}

/**
 * 显示隐私政策
 */
const showPrivacy = () => {
  privacyVisible.value = true
}
</script>

<style lang="scss" scoped>
.register-page {
  width: 100%;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  position: relative;
  overflow: hidden;

  // 背景装饰
  &::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -50%;
    width: 100%;
    height: 100%;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
    animation: float 20s ease-in-out infinite;
  }

  &::after {
    content: '';
    position: absolute;
    bottom: -50%;
    left: -50%;
    width: 100%;
    height: 100%;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.05) 0%, transparent 70%);
    animation: float 25s ease-in-out infinite reverse;
  }
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg);
  }
  50% {
    transform: translate(50px, 50px) rotate(180deg);
  }
}

.register-container {
  width: 100%;
  max-width: 1100px;
  background: #ffffff;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  display: grid;
  grid-template-columns: 400px 1fr;
  position: relative;
  z-index: 1;
  max-height: 90vh;
}

// ==========================================
// 左侧装饰区
// ==========================================
.register-left {
  background: linear-gradient(135deg, #0056d2 0%, #0070f3 100%);
  padding: 60px 40px;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
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

  .decoration-content {
    position: relative;
    z-index: 1;
  }

  .logo-section {
    text-align: center;
    margin-bottom: 60px;

    .logo-icon {
      color: #ffffff;
      margin-bottom: 16px;
    }

    .brand-name {
      font-size: 32px;
      font-weight: 800;
      margin-bottom: 12px;
      letter-spacing: -1px;
    }

    .brand-slogan {
      font-size: 16px;
      opacity: 0.9;
    }
  }

  .feature-list {
    display: flex;
    flex-direction: column;
    gap: 32px;

    .feature-item {
      display: flex;
      align-items: flex-start;
      gap: 16px;

      .feature-icon {
        font-size: 28px;
        flex-shrink: 0;
        color: #67c23a;
      }

      .feature-text {
        h3 {
          font-size: 18px;
          font-weight: 600;
          margin-bottom: 4px;
        }

        p {
          font-size: 14px;
          opacity: 0.8;
        }
      }
    }
  }
}

// ==========================================
// 右侧表单区
// ==========================================
.register-right {
  padding: 40px;
  overflow-y: auto;
  max-height: 90vh;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: #f5f7fa;
  }

  &::-webkit-scrollbar-thumb {
    background: #dcdfe6;
    border-radius: 3px;

    &:hover {
      background: #c0c4cc;
    }
  }
}

.register-form-wrapper {
  width: 100%;
  max-width: 480px;
  margin: 0 auto;

  .form-header {
    margin-bottom: 32px;

    .form-title {
      font-size: 28px;
      font-weight: 700;
      color: #303133;
      margin-bottom: 8px;
    }

    .form-subtitle {
      font-size: 14px;
      color: #909399;
    }
  }

  .register-form {
    .password-strength {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-top: 8px;
      font-size: 12px;

      .strength-label {
        color: #909399;
        flex-shrink: 0;
      }

      .strength-bar {
        flex: 1;
        height: 4px;
        background: #e4e7ed;
        border-radius: 2px;
        overflow: hidden;

        .strength-fill {
          height: 100%;
          transition: all 0.3s ease;
          border-radius: 2px;

          &.weak {
            width: 25%;
            background: #f56c6c;
          }

          &.medium {
            width: 50%;
            background: #e6a23c;
          }

          &.strong {
            width: 75%;
            background: #409eff;
          }

          &.very-strong {
            width: 100%;
            background: #67c23a;
          }
        }
      }

      .strength-text {
        flex-shrink: 0;
        font-weight: 500;

        &.weak {
          color: #f56c6c;
        }

        &.medium {
          color: #e6a23c;
        }

        &.strong {
          color: #409eff;
        }

        &.very-strong {
          color: #67c23a;
        }
      }
    }

    .agree-text {
      font-size: 13px;
      color: #606266;
    }

    .register-btn {
      width: 100%;
      height: 48px;
      font-size: 16px;
      font-weight: 600;
      border-radius: 8px;
      background: linear-gradient(135deg, #0056d2 0%, #0070f3 100%);
      border: none;
      margin-top: 8px;

      &:hover:not(:disabled) {
        background: linear-gradient(135deg, #0045a8 0%, #0056d2 100%);
        box-shadow: 0 4px 12px rgba(0, 86, 210, 0.3);
      }

      &:disabled {
        opacity: 0.6;
        cursor: not-allowed;
      }
    }
  }

  .login-tip {
    text-align: center;
    margin-top: 24px;
    font-size: 14px;
    color: #606266;

    .el-link {
      margin-left: 8px;
      font-weight: 500;
    }
  }
}

// ==========================================
// 对话框内容
// ==========================================
.agreement-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;

  h3 {
    font-size: 16px;
    color: #303133;
    margin-bottom: 12px;
  }

  p {
    margin-bottom: 12px;
  }

  ol {
    padding-left: 20px;
    margin-bottom: 12px;

    li {
      margin-bottom: 8px;
    }
  }
}

// ==========================================
// 响应式设计
// ==========================================
@media (max-width: 968px) {
  .register-container {
    grid-template-columns: 1fr;
  }

  .register-left {
    display: none;
  }
}

@media (max-width: 576px) {
  .register-right {
    padding: 30px 20px;
  }

  .register-form-wrapper {
    .form-header {
      .form-title {
        font-size: 24px;
      }
    }
  }
}

// ==========================================
// Element Plus 组件样式覆盖
// ==========================================
:deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;

  &:hover,
  &.is-focus {
    box-shadow: 0 2px 12px rgba(0, 86, 210, 0.2);
  }
}

:deep(.el-form-item) {
  margin-bottom: 20px;

  &:last-of-type {
    margin-bottom: 0;
  }
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
}

:deep(.el-checkbox__label) {
  font-size: 13px;
  color: #606266;
}

// 图标样式
.success-icon {
  color: #67c23a;
  font-size: 16px;
}

.error-icon {
  color: #f56c6c;
  font-size: 16px;
}

:deep(.is-loading) {
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}
</style>
