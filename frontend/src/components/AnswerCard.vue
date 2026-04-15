<template>
  <div
    class="answer-card"
    :class="{ 'accepted': answer.isAccepted === 1 }"
  >
    <!-- 采纳标识 -->
    <div
      v-if="answer.isAccepted === 1"
      class="accepted-badge"
    >
      <el-icon><Select /></el-icon>
      <span>已采纳</span>
    </div>

    <!-- 用户信息 -->
    <div class="user-header">
      <el-avatar
        :size="40"
        :src="answer.userAvatar || `https://ui-avatars.com/api/?name=${answer.username}&background=0056D2&color=fff&size=80`"
      >
        {{ answer.username?.charAt(0)?.toUpperCase() }}
      </el-avatar>
      <div class="user-info">
        <div class="username">
          {{ answer.username }}
        </div>
        <div class="user-meta">
          <span v-if="answer.userPoints !== undefined">
            <el-icon><Medal /></el-icon>
            {{ answer.userPoints }} 积分
          </span>
          <span>{{ formatTime(answer.createTime) }}</span>
        </div>
      </div>
    </div>

    <!-- 回答内容 -->
    <div
      class="answer-content"
      v-html="renderContent(answer.content)"
    />

    <!-- 图片展示 -->
    <div
      v-if="answer.images && answer.images.length"
      class="images-container"
    >
      <el-image
        v-for="(image, index) in answer.images"
        :key="index"
        :src="image"
        :preview-src-list="answer.images"
        :initial-index="index"
        fit="cover"
        class="answer-image"
        lazy
      >
        <template #placeholder>
          <div class="image-placeholder">
            <el-icon class="is-loading">
              <Loading />
            </el-icon>
          </div>
        </template>
      </el-image>
    </div>

    <!-- 操作栏 -->
    <div class="actions-bar">
      <div
        class="action-item"
        @click="handleLike"
      >
        <el-icon :class="{ 'liked': answer.isLiked }">
          <component :is="answer.isLiked ? StarFilled : Star" />
        </el-icon>
        <span>{{ answer.likeCount || 0 }}</span>
      </div>

      <div
        class="action-item"
        @click="handleComment"
      >
        <el-icon><ChatDotRound /></el-icon>
        <span>{{ answer.commentCount || 0 }} 评论</span>
      </div>

      <div
        v-if="showAccept && canAccept"
        class="action-item"
        @click="handleAccept"
      >
        <el-icon><Select /></el-icon>
        <span>采纳</span>
      </div>

      <div
        v-if="showShare"
        class="action-item"
        @click="handleShare"
      >
        <el-icon><Share /></el-icon>
        <span>分享</span>
      </div>

      <div
        v-if="showDelete && canDelete"
        class="action-item"
        @click="handleDelete"
      >
        <el-icon><Delete /></el-icon>
        <span>删除</span>
      </div>
    </div>

    <!-- 评论区域（可选） -->
    <div
      v-if="showComments"
      class="comments-section"
    >
      <slot name="comments" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Select,
  Star,
  StarFilled,
  ChatDotRound,
  Share,
  Delete,
  Loading,
  Medal
} from '@element-plus/icons-vue';
import type { AnswerVO } from '@/api/answer';
import MarkdownIt from 'markdown-it';

interface Props {
  answer: AnswerVO;
  currentUserId?: number;
  questionUserId?: number;
  showAccept?: boolean;
  showShare?: boolean;
  showDelete?: boolean;
  showComments?: boolean;
}

interface Emits {
  (e: 'like', answer: AnswerVO): void;
  (e: 'comment', answer: AnswerVO): void;
  (e: 'accept', answer: AnswerVO): void;
  (e: 'share', answer: AnswerVO): void;
  (e: 'delete', answer: AnswerVO): void;
}

const props = withDefaults(defineProps<Props>(), {
  showAccept: false,
  showShare: false,
  showDelete: false,
  showComments: false
});

const emit = defineEmits<Emits>();

// Markdown渲染器
const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true
});

/**
 * 是否可以采纳（问题作者且回答未被采纳）
 */
const canAccept = computed(() => {
  return props.currentUserId === props.questionUserId &&
         props.answer.isAccepted === 0 &&
         props.answer.status === 1;
});

/**
 * 是否可以删除（回答作者）
 */
const canDelete = computed(() => {
  return props.currentUserId === props.answer.userId;
});

/**
 * 格式化时间
 */
const formatTime = (time: string) => {
  if (!time) return '';
  const date = new Date(time);
  const now = new Date();
  const diff = now.getTime() - date.getTime();
  const minutes = Math.floor(diff / 60000);
  const hours = Math.floor(diff / 3600000);
  const days = Math.floor(diff / 86400000);

  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days < 7) return `${days}天前`;

  return date.toLocaleDateString('zh-CN');
};

/**
 * 渲染Markdown内容
 */
const renderContent = (content: string) => {
  if (!content) return '';
  return md.render(content);
};

/**
 * 点赞
 */
const handleLike = () => {
  emit('like', props.answer);
};

/**
 * 评论
 */
const handleComment = () => {
  emit('comment', props.answer);
};

/**
 * 采纳
 */
const handleAccept = () => {
  ElMessageBox.confirm(
    '采纳此回答后，问题将被标记为已解决，且悬赏积分将转给回答者。确定采纳吗？',
    '采纳回答',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    emit('accept', props.answer);
    ElMessage.success('采纳成功');
  }).catch(() => {
    // 用户取消
  });
};

/**
 * 分享
 */
const handleShare = () => {
  const url = window.location.href;
  navigator.clipboard.writeText(url).then(() => {
    ElMessage.success('链接已复制到剪贴板');
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制链接');
  });
  emit('share', props.answer);
};

/**
 * 删除
 */
const handleDelete = () => {
  ElMessageBox.confirm(
    '确定删除此回答吗？删除后无法恢复。',
    '删除回答',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    emit('delete', props.answer);
    ElMessage.success('删除成功');
  }).catch(() => {
    // 用户取消
  });
};
</script>

<style scoped lang="scss">
.answer-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  position: relative;

  &.accepted {
    border: 2px solid #67c23a;
    background: linear-gradient(135deg, #f0f9ff 0%, #ffffff 100%);
  }

  &:not(.accepted):hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  }
}

.accepted-badge {
  position: absolute;
  top: 20px;
  right: 20px;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  background: #67c23a;
  color: #fff;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.3);

  .el-icon {
    font-size: 16px;
  }
}

.user-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.user-info {
  flex: 1;
}

.username {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.user-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: #999;

  span {
    display: flex;
    align-items: center;
    gap: 4px;
  }

  .el-icon {
    font-size: 14px;
  }
}

.answer-content {
  font-size: 15px;
  line-height: 1.8;
  color: #333;
  margin-bottom: 16px;
  word-break: break-word;

  :deep(p) {
    margin: 12px 0;
  }

  :deep(h1),
  :deep(h2),
  :deep(h3),
  :deep(h4),
  :deep(h5),
  :deep(h6) {
    margin: 20px 0 12px 0;
    font-weight: 600;
  }

  :deep(code) {
    background: #f5f5f5;
    padding: 2px 6px;
    border-radius: 4px;
    font-family: 'Courier New', monospace;
    font-size: 14px;
  }

  :deep(pre) {
    background: #f5f5f5;
    padding: 16px;
    border-radius: 8px;
    overflow-x: auto;
    margin: 12px 0;
  }

  :deep(blockquote) {
    border-left: 4px solid #0056d2;
    padding-left: 16px;
    margin: 16px 0;
    color: #666;
  }

  :deep(img) {
    max-width: 100%;
    border-radius: 8px;
  }

  :deep(a) {
    color: #0056d2;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}

.images-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.answer-image {
  width: 100%;
  height: 150px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s ease;

  &:hover {
    transform: scale(1.02);
  }
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  color: #999;

  .el-icon {
    font-size: 24px;
  }
}

.actions-bar {
  display: flex;
  align-items: center;
  gap: 24px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;

  &:hover {
    color: #0056d2;
  }

  .el-icon {
    font-size: 18px;

    &.liked {
      color: #ff6b6b;
    }
  }

  span {
    min-width: 20px;
  }
}

.comments-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}
</style>