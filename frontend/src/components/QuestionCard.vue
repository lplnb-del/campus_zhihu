<template>
  <div
    class="question-card"
    @click="handleClick"
  >
    <!-- 问题标题 -->
    <h3
      class="question-title"
      :title="question.title"
    >
      {{ question.title }}
    </h3>

    <!-- 问题内容摘要 -->
    <p
      v-if="showContent"
      class="question-content"
    >
      {{ question.content }}
    </p>

    <!-- 标签列表 -->
    <div
      v-if="question.tags && question.tags.length"
      class="tags-container"
    >
      <el-tag
        v-for="tag in question.tags.slice(0, 3)"
        :key="tag.id"
        :color="tag.color"
        effect="plain"
        size="small"
        class="tag-item"
        @click.stop="handleTagClick(tag)"
      >
        {{ tag.name }}
      </el-tag>
    </div>

    <!-- 统计信息 -->
    <div class="stats-container">
      <div
        v-if="question.answerCount !== undefined"
        class="stat-item"
      >
        <el-icon><ChatDotRound /></el-icon>
        <span>{{ question.answerCount }} 回答</span>
      </div>
      <div
        v-if="question.viewCount !== undefined"
        class="stat-item"
      >
        <el-icon><View /></el-icon>
        <span>{{ question.viewCount }} 浏览</span>
      </div>
      <div
        v-if="question.likeCount !== undefined"
        class="stat-item"
      >
        <el-icon><Star /></el-icon>
        <span>{{ question.likeCount }} 点赞</span>
      </div>
      <div
        v-if="question.collectionCount !== undefined"
        class="stat-item"
      >
        <el-icon><Collection /></el-icon>
        <span>{{ question.collectionCount }} 收藏</span>
      </div>
    </div>

    <!-- 底部信息栏 -->
    <div class="footer-container">
      <div class="user-info">
        <el-avatar
          :size="32"
          :src="question.userAvatar || `https://ui-avatars.com/api/?name=${question.username}&background=0056D2&color=fff&size=64`"
        >
          {{ question.username?.charAt(0)?.toUpperCase() }}
        </el-avatar>
        <span class="username">{{ question.username }}</span>
      </div>

      <div class="meta-info">
        <!-- 悬赏积分 -->
        <span
          v-if="question.rewardPoints && question.rewardPoints > 0"
          class="reward-points"
        >
          <el-icon><Coin /></el-icon>
          {{ question.rewardPoints }} 积分悬赏
        </span>

        <!-- 已解决标识 -->
        <el-tag
          v-if="question.isSolved"
          type="success"
          size="small"
          effect="plain"
        >
          已解决
        </el-tag>

        <!-- 草稿标识 -->
        <el-tag
          v-if="question.isDraft"
          type="info"
          size="small"
          effect="plain"
        >
          草稿
        </el-tag>

        <!-- 时间 -->
        <span class="time">{{ formatTime(question.createTime) }}</span>
      </div>
    </div>

    <!-- 操作按钮（可选） -->
    <div
      v-if="showActions"
      class="actions-container"
    >
      <el-button
        type="primary"
        size="small"
        :icon="question.isLiked ? StarFilled : Star"
        @click.stop="handleLike"
      >
        {{ question.likeCount || 0 }}
      </el-button>
      <el-button
        size="small"
        :icon="question.isCollected ? CollectionTag : Collection"
        @click.stop="handleCollect"
      >
        {{ question.collectionCount || 0 }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import {
  ChatDotRound,
  View,
  Star,
  StarFilled,
  Collection,
  CollectionTag,
  Coin
} from '@element-plus/icons-vue';
import type { QuestionVO } from '@/api/question';

interface Props {
  question: QuestionVO;
  showContent?: boolean;
  showActions?: boolean;
}

interface Emits {
  (e: 'click', question: QuestionVO): void;
  (e: 'like', question: QuestionVO): void;
  (e: 'collect', question: QuestionVO): void;
  (e: 'tag-click', tag: any): void;
}

const props = withDefaults(defineProps<Props>(), {
  showContent: false,
  showActions: false
});

const emit = defineEmits<Emits>();
const router = useRouter();

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
 * 点击卡片
 */
const handleClick = () => {
  emit('click', props.question);
  router.push(`/question/${props.question.id}`);
};

/**
 * 点击标签
 */
const handleTagClick = (tag: any) => {
  emit('tag-click', tag);
  router.push(`/tags/${tag.id}`);
};

/**
 * 点赞
 */
const handleLike = () => {
  emit('like', props.question);
};

/**
 * 收藏
 */
const handleCollect = () => {
  emit('collect', props.question);
};
</script>

<style scoped lang="scss">
.question-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  cursor: pointer;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  }
}

.question-title {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 12px 0;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.question-content {
  font-size: 14px;
  color: #666;
  margin: 0 0 12px 0;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.tag-item {
  cursor: pointer;
  transition: transform 0.2s ease;

  &:hover {
    transform: scale(1.05);
  }
}

.stats-container {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-bottom: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #999;

  .el-icon {
    font-size: 14px;
  }
}

.footer-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.username {
  font-size: 14px;
  color: #666;
  font-weight: 500;
}

.meta-info {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
}

.reward-points {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #ff6b6b;
  font-weight: 600;

  .el-icon {
    font-size: 14px;
  }
}

.time {
  color: #999;
}

.actions-container {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}
</style>