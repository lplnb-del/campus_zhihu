<template>
  <div class="campus-layout">
    <!-- 顶部导航栏 -->
    <header class="campus-header glass-header">
      <div class="header-container">
        <!-- Logo 和站点名称 -->
        <div class="header-left">
          <router-link
            to="/"
            class="logo-link"
          >
            <el-icon
              :size="28"
              class="logo-icon"
            >
              <ChatDotRound />
            </el-icon>
            <span class="site-name">CampusZhihu</span>
          </router-link>
        </div>

        <!-- 导航菜单 -->
        <nav class="header-nav">
          <router-link
            to="/home"
            class="nav-item"
            active-class="active"
          >
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </router-link>
          <router-link
            to="/questions"
            class="nav-item"
            active-class="active"
          >
            <el-icon><QuestionFilled /></el-icon>
            <span>问题</span>
          </router-link>
          <router-link
            to="/tags"
            class="nav-item"
            active-class="active"
          >
            <el-icon><PriceTag /></el-icon>
            <span>话题</span>
          </router-link>
        </nav>

        <!-- 右侧操作区 -->
        <div class="header-right">
          <!-- 搜索框 -->
          <div class="search-box">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索问题..."
              :prefix-icon="Search"
              clearable
              @keyup.enter="handleSearch"
            />
          </div>

          <!-- 提问按钮 -->
          <el-button
            type="primary"
            class="campus-btn campus-btn-primary ask-btn"
            @click="handleAskQuestion"
          >
            <el-icon><EditPen /></el-icon>
            <span>提问</span>
          </el-button>

          <!-- 用户菜单 -->
          <div
            v-if="userStore.isLoggedIn"
            class="user-menu"
          >
            <!-- 通知 -->
            <el-badge
              :value="unreadCount"
              :max="99"
              class="notice-badge"
            >
              <el-button
                circle
                @click="handleNotice"
              >
                <el-icon><Bell /></el-icon>
              </el-button>
            </el-badge>

            <!-- 用户下拉菜单 -->
            <el-dropdown
              trigger="click"
              @command="handleUserCommand"
            >
              <div class="user-avatar-box">
                <el-avatar
                  :size="36"
                  :src="userStore.avatar || `https://ui-avatars.com/api/?name=${userStore.username}&background=0056D2&color=fff&size=72`"
                  class="user-avatar"
                >
                  <el-icon><User /></el-icon>
                </el-avatar>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">
                    <el-icon><User /></el-icon>
                    <span>个人中心</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="questions">
                    <el-icon><QuestionFilled /></el-icon>
                    <span>我的提问</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="answers">
                    <el-icon><ChatLineSquare /></el-icon>
                    <span>我的回答</span>
                  </el-dropdown-item>
                  <el-dropdown-item command="collections">
                    <el-icon><Star /></el-icon>
                    <span>我的收藏</span>
                  </el-dropdown-item>
                  <el-dropdown-item
                    command="points"
                    divided
                  >
                    <el-icon><Coin /></el-icon>
                    <span>我的积分 ({{ userStore.points }})</span>
                  </el-dropdown-item>
                  <el-dropdown-item
                    command="logout"
                    divided
                  >
                    <el-icon><SwitchButton /></el-icon>
                    <span>退出登录</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>

          <!-- 未登录状态 -->
          <div
            v-else
            class="auth-buttons"
          >
            <el-button @click="handleLogin">
              登录
            </el-button>
            <el-button
              type="primary"
              @click="handleRegister"
            >
              注册
            </el-button>
          </div>
        </div>
      </div>
    </header>

    <!-- 主体内容区 -->
    <main class="campus-main">
      <div class="main-container">
        <router-view v-slot="{ Component }">
          <transition
            name="fade-slide"
            mode="out-in"
          >
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </main>

    <!-- 底部 -->
    <footer class="campus-footer">
      <div class="footer-container">
        <div class="footer-content">
          <div class="footer-section">
            <h4>关于我们</h4>
            <p>CampusZhihu 是一个专为高校学生打造的知识问答平台</p>
          </div>
          <div class="footer-section">
            <h4>友情链接</h4>
            <a href="#">知乎</a>
            <a href="#">Stack Overflow</a>
          </div>
          <div class="footer-section">
            <h4>联系我们</h4>
            <p>Email: contact@campuszhihu.com</p>
          </div>
        </div>
        <div class="footer-copyright">
          <p>&copy; 2025 CampusZhihu. All rights reserved.</p>
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import {
  Search,
  ChatDotRound,
  HomeFilled,
  QuestionFilled,
  PriceTag,
  EditPen,
  Bell,
  User,
  ChatLineSquare,
  Star,
  Coin,
  SwitchButton,
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 搜索关键词
const searchKeyword = ref('')

// 未读通知数
const unreadCount = ref(0)

/**
 * 处理搜索
 */
const handleSearch = () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  router.push({
    path: '/questions',
    query: { keyword: searchKeyword.value },
  })
}

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
 * 处理通知
 */
const handleNotice = () => {
  router.push('/user/notices')
}

/**
 * 处理用户菜单命令
 */
const handleUserCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/user/profile')
      break
    case 'questions':
      router.push('/user/questions')
      break
    case 'answers':
      router.push('/user/answers')
      break
    case 'collections':
      router.push('/user/collections')
      break
    case 'points':
      router.push('/user/points')
      break
    case 'logout':
      handleLogout()
      break
  }
}

/**
 * 处理登录
 */
const handleLogin = () => {
  router.push('/login')
}

/**
 * 处理注册
 */
const handleRegister = () => {
  router.push('/register')
}

/**
 * 处理登出
 */
const handleLogout = () => {
  userStore.logout()
  ElMessage.success('退出登录成功')
  router.push('/login')
}

/**
 * 加载未读通知数
 */
const loadUnreadCount = async () => {
  // TODO: 调用 API 获取未读通知数
  // const count = await getUnreadNoticeCount()
  // unreadCount.value = count
  unreadCount.value = 0
}

onMounted(() => {
  if (userStore.isLoggedIn) {
    loadUnreadCount()
  }
})
</script>

<style lang="scss" scoped>
.campus-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background-color: #f5f7fa;
}

// ==========================================
// 顶部导航栏
// ==========================================
.campus-header {
  height: 64px;
  padding: 0 24px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 1000;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);

  .header-container {
    max-width: 1440px;
    height: 100%;
    margin: 0 auto;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 24px;
  }

  .header-left {
    display: flex;
    align-items: center;

    .logo-link {
      display: flex;
      align-items: center;
      gap: 8px;
      text-decoration: none;
      transition: all 0.3s ease;

      .logo-icon {
        color: #0056d2;
        transition: transform 0.3s ease;
      }

      .site-name {
        font-size: 20px;
        font-weight: 700;
        background: linear-gradient(135deg, #0056d2 0%, #0070f3 100%);
        -webkit-background-clip: text;
        -webkit-text-fill-color: transparent;
        background-clip: text;
      }

      &:hover {
        .logo-icon {
          transform: scale(1.1) rotate(5deg);
        }
      }
    }
  }

  .header-nav {
    display: flex;
    align-items: center;
    gap: 8px;

    .nav-item {
      display: flex;
      align-items: center;
      gap: 6px;
      padding: 8px 16px;
      color: #606266;
      text-decoration: none;
      border-radius: 8px;
      font-size: 14px;
      font-weight: 500;
      transition: all 0.3s ease;

      &:hover {
        background: #f5f7fa;
        color: #0056d2;
      }

      &.active {
        background: #e6f0ff;
        color: #0056d2;
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;

    .search-box {
      width: 240px;

      :deep(.el-input__wrapper) {
        border-radius: 20px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
        transition: all 0.3s ease;

        &:hover,
        &.is-focus {
          box-shadow: 0 2px 12px rgba(0, 86, 210, 0.2);
        }
      }
    }

    .ask-btn {
      display: flex;
      align-items: center;
      gap: 6px;
      border-radius: 8px;
      padding: 8px 20px;
      font-weight: 600;
      white-space: nowrap;
    }

    .user-menu {
      display: flex;
      align-items: center;
      gap: 12px;

      .notice-badge {
        :deep(.el-badge__content) {
          background: #ff6b6b;
          border: none;
        }
      }

      .user-avatar-box {
        cursor: pointer;
        transition: transform 0.3s ease;

        &:hover {
          transform: scale(1.05);
        }

        .user-avatar {
          border: 2px solid #e6f0ff;
          transition: border-color 0.3s ease;

          &:hover {
            border-color: #0056d2;
          }
        }
      }
    }

    .auth-buttons {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }
}

// ==========================================
// 主体内容区
// ==========================================
.campus-main {
  flex: 1;
  padding: 24px 0;

  .main-container {
    max-width: 1440px;
    margin: 0 auto;
    padding: 0 24px;
  }
}

// ==========================================
// 底部
// ==========================================
.campus-footer {
  background: #ffffff;
  border-top: 1px solid #e4e7ed;
  padding: 40px 0 20px;
  margin-top: 60px;

  .footer-container {
    max-width: 1440px;
    margin: 0 auto;
    padding: 0 24px;
  }

  .footer-content {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 40px;
    margin-bottom: 30px;

    .footer-section {
      h4 {
        font-size: 16px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 16px;
      }

      p {
        font-size: 14px;
        color: #606266;
        line-height: 1.6;
      }

      a {
        display: block;
        font-size: 14px;
        color: #606266;
        text-decoration: none;
        margin-bottom: 8px;
        transition: color 0.3s ease;

        &:hover {
          color: #0056d2;
        }
      }
    }
  }

  .footer-copyright {
    text-align: center;
    padding-top: 20px;
    border-top: 1px solid #e4e7ed;

    p {
      font-size: 13px;
      color: #909399;
    }
  }
}

// ==========================================
// 响应式设计
// ==========================================
@media (max-width: 768px) {
  .campus-header {
    padding: 0 16px;

    .header-nav {
      display: none;
    }

    .header-right {
      .search-box {
        display: none;
      }
    }
  }

  .campus-main {
    .main-container {
      padding: 0 16px;
    }
  }

  .campus-footer {
    .footer-content {
      grid-template-columns: 1fr;
      gap: 24px;
    }
  }
}

// 下拉菜单样式
:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;

  .el-icon {
    font-size: 16px;
  }
}
</style>
