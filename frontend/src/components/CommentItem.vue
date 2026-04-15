<template>
  <div
    class="comment-item"
    :class="{ 'nested': isNested }"
  >
    <!-- 用户头像 -->
    <el-avatar
      :size="isNested ? 32 : 40"
      :src="comment.userAvatar || `https://ui-avatars.com/api/?name=${comment.username}&background=0056D2&color=fff&size=${isNested ? 64 : 80}`"
      class="user-avatar"
    >
      {{ comment.username?.charAt(0)?.toUpperCase() }}
    </el-avatar>

    <!-- 评论内容区 -->
    <div class="comment-content-wrapper">
      <!-- 用户信息和时间 -->
      <div class="comment-header">
        <span class="username">{{ comment.username }}</span>
        <span class="time">{{ formatTime(comment.createTime) }}</span>
      </div>

      <!-- 回复目标（如果是回复评论） -->
      <div
        v-if="comment.replyToUsername"
        class="reply-target"
      >
        <span class="reply-label">回复</span>
        <span class="reply-to">@{{ comment.replyToUsername }}</span>
      </div>

      <!-- 评论内容 -->
      <div class="comment-text">
        {{ comment.content }}
      </div>

      <!-- 操作栏 -->
      <div class="comment-actions">
        <div
          class="action-item"
          :class="{ 'liked': comment.isLiked }"
          @click="handleLike"
        >
          <el-icon>
            <component :is="comment.isLiked ? StarFilled : Star" />
          </el-icon>
          <span>{{ comment.likeCount || 0 }}</span>
        </div>

        <div
          class="action-item"
          @click="handleReply"
        >
          <el-icon><ChatDotRound /></el-icon>
          <span>回复</span>
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

      <!-- 回复输入框（展开时显示） -->
      <div
        v-if="showReplyInput"
        class="reply-input-wrapper"
      >
        <el-input
          v-model="replyContent"
          type="textarea"
          :rows="3"
          :placeholder="`回复 ${comment.username}...`"
          resize="none"
          @keyup.ctrl.enter="submitReply"
        />
        <div class="reply-input-actions">
          <span class="hint">按 Ctrl + Enter 发送</span>
          <el-button
            type="primary"
            size="small"
            :disabled="!replyContent.trim()"
            @click="submitReply"
          >
            发送
          </el-button>
          <el-button
            size="small"
            @click="cancelReply"
          >
            取消
          </el-button>
        </div>
      </div>

      <!-- 子评论（回复列表） -->
      <div
        v-if="comment.replies && comment.replies.length"
        class="replies-container"
      >
        <CommentItem
          v-for="reply in comment.replies"
          :key="reply.id"
          :comment="reply"
          :current-user-id="currentUserId"
          :show-delete="showDelete"
          :is-nested="true"
          @like="$emit('like', $event)"
          @reply="$emit('reply', $event)"
          @delete="$emit('delete', $event)"
        />
      </div>

      <!-- 查看更多回复 -->
      <div
        v-if="hasMoreReplies"
        class="view-more-replies"
        @click="handleViewMore"
      >
        <span>查看{{ replyCount }}条回复</span>
        <el-icon><ArrowDown /></el-icon>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Star,
  StarFilled,
  ChatDotRound,
  Delete,
  ArrowDown
} from '@element-plus/icons-vue';
import type { CommentVO } from '@/api/interaction';

interface Props {
  comment: CommentVO;
  currentUserId?: number;
  showDelete?: boolean;
  isNested?: boolean;
  replyCount?: number;
  hasMoreReplies?: boolean;
}

interface Emits {
  (e: 'like', comment: CommentVO): void;
  (e: 'reply', data: { comment: CommentVO; content: string }): void;
  (e: 'delete', comment: CommentVO): void;
  (e: 'view-more', comment: CommentVO): void;
}

const props = withDefaults(defineProps<Props>(), {
  showDelete: false,
  isNested: false,
  replyCount: 0,
  hasMoreReplies: false
});

const emit = defineEmits<Emits>();

const showReplyInput = ref(false);
const replyContent = ref('');

/**
 * 是否可以删除（评论作者）
 */
const canDelete = computed(() => {
  return props.currentUserId === props.comment.userId;
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
 * 点赞
 */
const handleLike = () => {
  emit('like', props.comment);
};

/**
 * 回复
 */
const handleReply = () => {
  showReplyInput.value = !showReplyInput.value;
  if (showReplyInput.value) {
    // 自动聚焦到输入框
    setTimeout(() => {
      const textarea = document.querySelector('.reply-input-wrapper textarea') as HTMLTextAreaElement;
      textarea?.focus();
    }, 100);
  }
};

/**
 * 提交回复
 */
const submitReply = () => {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容');
    return;
  }

  emit('reply', {
    comment: props.comment,
    content: replyContent.value.trim()
  });

  replyContent.value = '';
  showReplyInput.value = false;
};

/**
 * 取消回复
 */
const cancelReply = () => {
  replyContent.value = '';
  showReplyInput.value = false;
};

/**
 * 删除
 */
const handleDelete = () => {
  ElMessageBox.confirm(
    '确定删除此评论吗？删除后无法恢复。',
    '删除评论',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(() => {
    emit('delete', props.comment);
    ElMessage.success('删除成功');
  }).catch(() => {
    // 用户取消
  });
};

/**
 * 查看更多回复
 */
const handleViewMore = () => {
  emit('view-more', props.comment);
};
</script>

<style scoped lang="scss">
.comment-item {
  display: flex;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }

  &.nested {
    padding: 12px 0;
    margin-top: 12px;
    background: #fafafa;
    padding: 12px;
    border-radius: 8px;
  }
}

.user-avatar {
  flex-shrink: 0;
}

.comment-content-wrapper {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.username {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
}

.time {
  font-size: 12px;
  color: #999;
}

.reply-target {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 8px;
  padding: 4px 8px;
  background: #f0f7ff;
  border-radius: 4px;
  font-size: 13px;
}

.reply-label {
  color: #999;
}

.reply-to {
  color: #0056d2;
  font-weight: 500;
}

.comment-text {
  font-size: 14px;
  line-height: 1.6;
  color: #333;
  margin-bottom: 12px;
  word-break: break-word;
}

.comment-actions {
  display: flex;
  align-items: center;
  gap: 20px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #999;
  cursor: pointer;
  transition: all 0.2s ease;
  user-select: none;

  &:hover {
    color: #0056d2;
  }

  &.liked {
    color: #ff6b6b;

    .el-icon {
      color: #ff6b6b;
    }
  }

  .el-icon {
    font-size: 16px;
  }

  span {
    min-width: 16px;
  }
}

.reply-input-wrapper {
  margin-top: 12px;

  :deep(.el-textarea__inner) {
    border-radius: 8px;
  }
}

.reply-input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.hint {
  font-size: 12px;
  color: #999;
}

.replies-container {
  margin-top: 12px;
}

.view-more-replies {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 12px;
  padding: 8px 12px;
  background: #f5f5f5;
  border-radius: 6px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: #e8e8e8;
    color: #0056d2;
  }

  .el-icon {
    font-size: 14px;
  }
}
</style>