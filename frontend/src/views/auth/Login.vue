<template>
  <div class="login-page">
    <div class="login-container">
      <!-- 左侧装饰区 -->
      <div class="login-left">
        <div class="decoration-content">
          <div class="logo-section">
            <el-icon
              :size="64"
              class="logo-icon"
            >
              <ChatDotRound />
            </el-icon>
            <h1 class="brand-name">
              CampusZhihu
            </h1>
            <p class="brand-slogan">
              连接校园智慧 · 分享知识力量
            </p>
          </div>
          <div class="feature-list">
            <div class="feature-item">
              <el-icon class="feature-icon">
                <QuestionFilled />
              </el-icon>
              <div class="feature-text">
                <h3>学术交流</h3>
                <p>专业问题，专业解答</p>
              </div>
            </div>
            <div class="feature-item">
              <el-icon class="feature-icon">
                <Coin />
              </el-icon>
              <div class="feature-text">
                <h3>积分悬赏</h3>
                <p>激励优质内容创作</p>
              </div>
            </div>
            <div class="feature-item">
              <el-icon class="feature-icon">
                <User />
              </el-icon>
              <div class="feature-text">
                <h3>校园社区</h3>
                <p>专属学生知识平台</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="login-right">
        <div class="login-form-wrapper">
          <div class="form-header">
            <h2 class="form-title">
              欢迎回来
            </h2>
            <p class="form-subtitle">
              登录您的 CampusZhihu 账号
            </p>
          </div>

          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="login-form"
            @submit.prevent="handleLogin"
          >
            <!-- 用户名 -->
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                size="large"
                clearable
                :prefix-icon="User"
              />
            </el-form-item>

            <!-- 密码 -->
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                show-password
                :prefix-icon="Lock"
                @keyup.enter="handleLogin"
              />
            </el-form-item>

            <!-- 记住我和忘记密码 -->
            <div class="form-options">
              <el-checkbox v-model="rememberMe">
                记住我
              </el-checkbox>
              <el-link
                type="primary"
                :underline="false"
                @click="handleForgotPassword"
              >
                忘记密码？
              </el-link>
            </div>

            <!-- 登录按钮 -->
            <el-button
              type="primary"
              size="large"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '登录' }}
            </el-button>
          </el-form>

          <!-- 分隔线 -->
          <div class="divider">
            <span>或</span>
          </div>

          <!-- 其他登录方式 -->
          <div class="other-login">
            <el-button
              class="oauth-btn"
              @click="handleOAuthLogin('github')"
            >
              <svg
                class="oauth-icon"
                viewBox="0 0 1024 1024"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  d="M512 42.666667A464.64 464.64 0 0 0 42.666667 502.186667 460.373333 460.373333 0 0 0 363.52 938.666667c23.466667 4.266667 32-9.813333 32-22.186667v-78.08c-130.56 27.733333-158.293333-61.44-158.293333-61.44a122.026667 122.026667 0 0 0-52.053334-67.413333c-42.666667-28.16 3.413333-27.733333 3.413334-27.733334a98.56 98.56 0 0 1 71.68 47.36 101.12 101.12 0 0 0 136.533333 37.973334 99.413333 99.413333 0 0 1 29.866667-61.44c-104.106667-11.52-213.333333-50.773333-213.333334-226.986667a177.066667 177.066667 0 0 1 47.36-124.16 161.28 161.28 0 0 1 4.693334-121.173333s39.68-12.373333 128 46.933333a455.68 455.68 0 0 1 234.666666 0c89.6-59.306667 128-46.933333 128-46.933333a161.28 161.28 0 0 1 4.693334 121.173333A177.066667 177.066667 0 0 1 810.666667 477.866667c0 176.64-110.08 215.466667-213.333334 226.986666a106.666667 106.666667 0 0 1 32 85.333334v125.866666c0 14.933333 8.533333 26.88 32 22.186667A460.8 460.8 0 0 0 981.333333 502.186667 464.64 464.64 0 0 0 512 42.666667"
                  fill="currentColor"
                />
              </svg>
              <span>GitHub 登录</span>
            </el-button>
            <el-button
              class="oauth-btn"
              @click="handleOAuthLogin('wechat')"
            >
              <svg
                class="oauth-icon"
                viewBox="0 0 1024 1024"
                xmlns="http://www.w3.org/2000/svg"
              >
                <path
                  d="M664.250054 368.541681c10.015098 0 19.892049 0.732687 29.67281 1.795902-26.647917-122.810047-159.358451-214.077703-310.826188-214.077703-169.353083 0-308.085774 114.232694-308.085774 259.274068 0 83.708494 46.165436 152.460344 123.281791 205.78483l-30.80868 91.730191 107.688651-53.455469c38.558178 7.53665 69.459978 15.308661 107.924012 15.308661 9.66308 0 19.230993-0.470721 28.752858-1.225921-6.025227-20.36584-9.521864-41.723264-9.521864-63.862493C402.328693 476.632491 517.908058 368.541681 664.250054 368.541681zM498.62897 285.87389c23.200398 0 38.557154 15.120372 38.557154 38.061874 0 22.846334-15.356756 38.156018-38.557154 38.156018-23.107277 0-46.260603-15.309684-46.260603-38.156018C452.368366 300.994262 475.522716 285.87389 498.62897 285.87389zM283.016307 362.090758c-23.107277 0-46.402843-15.309684-46.402843-38.156018 0-22.941502 23.295566-38.061874 46.402843-38.061874 23.081695 0 38.46301 15.120372 38.46301 38.061874C321.479317 346.782098 306.098002 362.090758 283.016307 362.090758zM945.448458 606.151333c0-121.888048-123.258255-221.236753-261.683954-221.236753-146.57838 0-262.015505 99.348706-262.015505 221.236753 0 122.06508 115.437126 221.200938 262.015505 221.200938 30.66644 0 61.617359-7.609305 92.423993-15.262612l84.513836 45.786813-23.178909-76.17082C899.379213 735.776599 945.448458 674.90216 945.448458 606.151333zM598.803483 567.994292c-15.332197 0-30.807656-15.096836-30.807656-30.501688 0-15.190981 15.47546-30.477129 30.807656-30.477129 23.295566 0 38.558178 15.286148 38.558178 30.477129C637.361661 552.897456 622.099049 567.994292 598.803483 567.994292zM768.25071 567.994292c-15.213493 0-30.594809-15.096836-30.594809-30.501688 0-15.190981 15.381315-30.477129 30.594809-30.477129 23.107277 0 38.558178 15.286148 38.558178 30.477129C806.808888 552.897456 791.357987 567.994292 768.25071 567.994292z"
                  fill="currentColor"
                />
              </svg>
              <span>微信登录</span>
            </el-button>
          </div>

          <!-- 注册提示 -->
          <div class="register-tip">
            <span>还没有账号？</span>
            <el-link
              type="primary"
              :underline="false"
              @click="handleRegister"
            >
              立即注册
            </el-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { login, type LoginDTO } from '@/api/user'
import {
  ChatDotRound,
  QuestionFilled,
  Coin,
  User,
  Lock,
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 表单引用
const loginFormRef = ref<FormInstance>()

// 表单数据
const loginForm = reactive<LoginDTO>({
  username: '',
  password: '',
})

// 记住我
const rememberMe = ref(false)

// 加载状态
const loading = ref(false)

// 表单验证规则
const loginRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为 3-20 个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度为 6-32 个字符', trigger: 'blur' },
  ],
}

/**
 * 处理登录
 */
const handleLogin = async () => {
  if (!loginFormRef.value) return

  try {
    await loginFormRef.value.validate()
  } catch (error) {
    return
  }

  loading.value = true
  try {
    const result = await login(loginForm)

    // 保存用户信息
    userStore.login({
      token: result.token,
      userInfo: {
        id: result.userInfo.id,
        username: result.userInfo.username,
        avatar: result.userInfo.avatar,
        points: result.userInfo.points,
        major: result.userInfo.major,
        grade: result.userInfo.grade,
        studentId: result.userInfo.studentId,
      },
    })

    ElMessage.success('登录成功')

    // 跳转到重定向页面或首页
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch (error: any) {
    console.error('登录失败：', error)
    ElMessage.error(error.message || '登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}

/**
 * 处理注册
 */
const handleRegister = () => {
  router.push('/register')
}

/**
 * 处理忘记密码
 */
const handleForgotPassword = () => {
  ElMessage.info('请联系管理员重置密码')
}

/**
 * 处理第三方登录
 */
const handleOAuthLogin = (provider: string) => {
  ElMessage.info(`${provider} 登录功能开发中...`)
}
</script>

<style lang="scss" scoped>
.login-page {
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

.login-container {
  width: 100%;
  max-width: 1000px;
  background: #ffffff;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  overflow: hidden;
  display: grid;
  grid-template-columns: 1fr 1fr;
  position: relative;
  z-index: 1;
}

// ==========================================
// 左侧装饰区
// ==========================================
.login-left {
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
      font-size: 36px;
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
        font-size: 32px;
        flex-shrink: 0;
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
.login-right {
  padding: 60px 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-form-wrapper {
  width: 100%;
  max-width: 360px;

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

  .login-form {
    .form-options {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 24px;
    }

    .login-btn {
      width: 100%;
      height: 48px;
      font-size: 16px;
      font-weight: 600;
      border-radius: 8px;
      background: linear-gradient(135deg, #0056d2 0%, #0070f3 100%);
      border: none;

      &:hover {
        background: linear-gradient(135deg, #0045a8 0%, #0056d2 100%);
        box-shadow: 0 4px 12px rgba(0, 86, 210, 0.3);
      }
    }
  }

  .divider {
    position: relative;
    text-align: center;
    margin: 24px 0;

    &::before {
      content: '';
      position: absolute;
      top: 50%;
      left: 0;
      right: 0;
      height: 1px;
      background: #e4e7ed;
    }

    span {
      position: relative;
      display: inline-block;
      padding: 0 16px;
      background: #ffffff;
      color: #909399;
      font-size: 14px;
    }
  }

  .other-login {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px;
    margin-bottom: 24px;

    .oauth-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      height: 44px;
      border-radius: 8px;
      border: 1px solid #e4e7ed;
      transition: all 0.3s ease;

      &:hover {
        border-color: #0056d2;
        color: #0056d2;
        background: #f5f7fa;
      }

      .oauth-icon {
        width: 20px;
        height: 20px;
      }

      span {
        font-size: 14px;
        font-weight: 500;
      }
    }
  }

  .register-tip {
    text-align: center;
    font-size: 14px;
    color: #606266;

    .el-link {
      margin-left: 8px;
      font-weight: 500;
    }
  }
}

// ==========================================
// 响应式设计
// ==========================================
@media (max-width: 768px) {
  .login-container {
    grid-template-columns: 1fr;
  }

  .login-left {
    display: none;
  }

  .login-right {
    padding: 40px 24px;
  }
}

// Element Plus 组件样式覆盖
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
}

:deep(.el-checkbox__label) {
  font-size: 14px;
  color: #606266;
}
</style>
