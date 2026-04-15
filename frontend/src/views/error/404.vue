<template>
  <div class="error-404-page">
    <div class="error-container">
      <!-- 404 插图 -->
      <div class="error-illustration">
        <div class="number-404">
          <span class="number">4</span>
          <div class="zero-container">
            <el-icon class="zero-icon">
              <QuestionFilled />
            </el-icon>
          </div>
          <span class="number">4</span>
        </div>
        <div class="floating-icons">
          <el-icon class="float-icon icon-1">
            <Document />
          </el-icon>
          <el-icon class="float-icon icon-2">
            <Star />
          </el-icon>
          <el-icon class="float-icon icon-3">
            <ChatDotRound />
          </el-icon>
        </div>
      </div>

      <!-- 错误信息 -->
      <div class="error-content">
        <h1 class="error-title">
          页面走丢了
        </h1>
        <p class="error-subtitle">
          抱歉，您访问的页面不存在或已被删除
        </p>
        <p class="error-description">
          可能的原因：链接已失效、输入错误的地址或页面已被移除
        </p>

        <!-- 操作按钮 -->
        <div class="error-actions">
          <el-button
            type="primary"
            size="large"
            class="action-btn"
            @click="handleGoHome"
          >
            <el-icon><HomeFilled /></el-icon>
            <span>返回首页</span>
          </el-button>
          <el-button
            size="large"
            class="action-btn"
            @click="handleGoBack"
          >
            <el-icon><Back /></el-icon>
            <span>返回上一页</span>
          </el-button>
        </div>

        <!-- 快捷链接 -->
        <div class="quick-links">
          <p class="links-title">
            或者尝试访问：
          </p>
          <div class="links-grid">
            <router-link
              to="/questions"
              class="link-item"
            >
              <el-icon><QuestionFilled /></el-icon>
              <span>问题广场</span>
            </router-link>
            <router-link
              to="/tags"
              class="link-item"
            >
              <el-icon><PriceTag /></el-icon>
              <span>话题标签</span>
            </router-link>
            <router-link
              to="/ask"
              class="link-item"
            >
              <el-icon><EditPen /></el-icon>
              <span>我要提问</span>
            </router-link>
            <router-link
              to="/user/profile"
              class="link-item"
            >
              <el-icon><User /></el-icon>
              <span>个人中心</span>
            </router-link>
          </div>
        </div>
      </div>

      <!-- 背景装饰 -->
      <div class="background-decoration">
        <div class="decoration-circle circle-1" />
        <div class="decoration-circle circle-2" />
        <div class="decoration-circle circle-3" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import {
  QuestionFilled,
  Document,
  Star,
  ChatDotRound,
  HomeFilled,
  Back,
  PriceTag,
  EditPen,
  User,
} from '@element-plus/icons-vue'

const router = useRouter()

/**
 * 返回首页
 */
const handleGoHome = () => {
  router.push('/')
}

/**
 * 返回上一页
 */
const handleGoBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}
</script>

<style lang="scss" scoped>
.error-404-page {
  width: 100%;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
  padding: 20px;

  // 全局背景动画
  &::before,
  &::after {
    content: '';
    position: absolute;
    width: 400px;
    height: 400px;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  }

  &::before {
    top: -200px;
    right: -200px;
    animation: float 15s ease-in-out infinite;
  }

  &::after {
    bottom: -200px;
    left: -200px;
    animation: float 20s ease-in-out infinite reverse;
  }
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  50% {
    transform: translate(30px, 30px) scale(1.1);
  }
}

.error-container {
  max-width: 800px;
  width: 100%;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  padding: 60px 40px;
  text-align: center;
  position: relative;
  z-index: 1;
}

// ==========================================
// 404 插图
// ==========================================
.error-illustration {
  position: relative;
  margin-bottom: 40px;

  .number-404 {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 20px;
    margin-bottom: 20px;

    .number {
      font-size: 120px;
      font-weight: 800;
      color: #0056d2;
      line-height: 1;
      text-shadow: 0 4px 12px rgba(0, 86, 210, 0.3);
      animation: bounce 2s ease-in-out infinite;
    }

    .zero-container {
      width: 120px;
      height: 120px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #0056d2 0%, #0070f3 100%);
      border-radius: 50%;
      box-shadow: 0 8px 24px rgba(0, 86, 210, 0.4);
      animation: rotate 10s linear infinite;

      .zero-icon {
        font-size: 60px;
        color: #ffffff;
      }
    }
  }

  .floating-icons {
    position: relative;
    height: 60px;

    .float-icon {
      position: absolute;
      font-size: 32px;
      color: #0056d2;
      opacity: 0.6;

      &.icon-1 {
        left: 20%;
        top: 0;
        animation: floatUp 3s ease-in-out infinite;
      }

      &.icon-2 {
        left: 50%;
        top: 10px;
        animation: floatUp 3s ease-in-out infinite 1s;
      }

      &.icon-3 {
        right: 20%;
        top: 5px;
        animation: floatUp 3s ease-in-out infinite 2s;
      }
    }
  }
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-20px);
  }
}

@keyframes rotate {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

@keyframes floatUp {
  0%, 100% {
    transform: translateY(0);
    opacity: 0.6;
  }
  50% {
    transform: translateY(-30px);
    opacity: 0.3;
  }
}

// ==========================================
// 错误内容
// ==========================================
.error-content {
  .error-title {
    font-size: 36px;
    font-weight: 700;
    color: #303133;
    margin-bottom: 16px;
  }

  .error-subtitle {
    font-size: 18px;
    color: #606266;
    margin-bottom: 12px;
  }

  .error-description {
    font-size: 14px;
    color: #909399;
    margin-bottom: 40px;
    line-height: 1.6;
  }

  .error-actions {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 16px;
    margin-bottom: 40px;

    .action-btn {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 12px 32px;
      font-size: 16px;
      font-weight: 500;
      border-radius: 12px;
      transition: all 0.3s ease;

      &:hover {
        transform: translateY(-2px);
      }
    }

    :deep(.el-button--primary) {
      background: linear-gradient(135deg, #0056d2 0%, #0070f3 100%);
      border: none;

      &:hover {
        background: linear-gradient(135deg, #0045a8 0%, #0056d2 100%);
        box-shadow: 0 4px 16px rgba(0, 86, 210, 0.3);
      }
    }
  }

  .quick-links {
    .links-title {
      font-size: 14px;
      color: #909399;
      margin-bottom: 16px;
    }

    .links-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 12px;

      .link-item {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 8px;
        padding: 16px 12px;
        background: #f5f7fa;
        border-radius: 12px;
        color: #606266;
        text-decoration: none;
        transition: all 0.3s ease;

        .el-icon {
          font-size: 24px;
          color: #0056d2;
        }

        span {
          font-size: 13px;
          font-weight: 500;
        }

        &:hover {
          background: #e6f0ff;
          transform: translateY(-4px);
          box-shadow: 0 4px 12px rgba(0, 86, 210, 0.15);
        }
      }
    }
  }
}

// ==========================================
// 背景装饰
// ==========================================
.background-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
  border-radius: 24px;

  .decoration-circle {
    position: absolute;
    border-radius: 50%;
    opacity: 0.1;

    &.circle-1 {
      width: 200px;
      height: 200px;
      top: -50px;
      right: -50px;
      background: radial-gradient(circle, #0056d2 0%, transparent 70%);
      animation: pulse 4s ease-in-out infinite;
    }

    &.circle-2 {
      width: 150px;
      height: 150px;
      bottom: -30px;
      left: -30px;
      background: radial-gradient(circle, #0070f3 0%, transparent 70%);
      animation: pulse 5s ease-in-out infinite 1s;
    }

    &.circle-3 {
      width: 100px;
      height: 100px;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      background: radial-gradient(circle, #667eea 0%, transparent 70%);
      animation: pulse 6s ease-in-out infinite 2s;
    }
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.1;
  }
  50% {
    transform: scale(1.2);
    opacity: 0.2;
  }
}

// ==========================================
// 响应式设计
// ==========================================
@media (max-width: 768px) {
  .error-container {
    padding: 40px 24px;
  }

  .error-illustration {
    .number-404 {
      gap: 10px;

      .number {
        font-size: 80px;
      }

      .zero-container {
        width: 80px;
        height: 80px;

        .zero-icon {
          font-size: 40px;
        }
      }
    }
  }

  .error-content {
    .error-title {
      font-size: 28px;
    }

    .error-subtitle {
      font-size: 16px;
    }

    .error-actions {
      flex-direction: column;
      gap: 12px;

      .action-btn {
        width: 100%;
        justify-content: center;
      }
    }

    .quick-links {
      .links-grid {
        grid-template-columns: repeat(2, 1fr);
      }
    }
  }
}

@media (max-width: 480px) {
  .error-illustration {
    .number-404 {
      .number {
        font-size: 60px;
      }

      .zero-container {
        width: 60px;
        height: 60px;

        .zero-icon {
          font-size: 30px;
        }
      }
    }

    .floating-icons {
      display: none;
    }
  }

  .error-content {
    .error-title {
      font-size: 24px;
    }

    .quick-links {
      .links-grid {
        grid-template-columns: 1fr;
      }
    }
  }
}
</style>
