<template>
  <div
    class="user-avatar-wrapper"
    :class="{ 'clickable': clickable }"
    @click="handleClick"
  >
    <el-badge
      :value="badgeValue"
      :hidden="!badgeValue"
      :type="badgeType"
      :is-dot="badgeDot"
    >
      <el-avatar
        :size="size"
        :src="computedSrc"
        :alt="alt"
        :icon="icon"
        :shape="shape"
        :fit="fit"
      >
        <template v-if="!computedSrc && !icon">
          {{ displayName }}
        </template>
      </el-avatar>
    </el-badge>

    <!-- 在线状态指示器 -->
    <div
      v-if="showOnlineStatus"
      class="online-indicator"
      :class="{ 'online': isOnline }"
    />

    <!-- 用户信息提示（hover时显示） -->
    <el-popover
      v-if="showTooltip && userInfo"
      placement="top"
      :width="280"
      trigger="hover"
    >
      <template #reference>
        <div class="avatar-tooltip-trigger" />
      </template>

      <div class="user-info-tooltip">
        <div class="tooltip-header">
          <el-avatar
            :size="48"
            :src="computedTooltipAvatar"
          >
            {{ userInfo.username?.charAt(0)?.toUpperCase() }}
          </el-avatar>
          <div class="tooltip-user-info">
            <div class="tooltip-username">
              {{ userInfo.username }}
            </div>
            <div
              v-if="userInfo.nickname"
              class="tooltip-nickname"
            >
              {{ userInfo.nickname }}
            </div>
          </div>
        </div>

        <div class="tooltip-stats">
          <div class="stat-item">
            <span class="stat-label">积分</span>
            <span class="stat-value">{{ userInfo.points || 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">提问</span>
            <span class="stat-value">{{ userInfo.questionCount || 0 }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">回答</span>
            <span class="stat-value">{{ userInfo.answerCount || 0 }}</span>
          </div>
        </div>

        <div
          v-if="userInfo.bio"
          class="tooltip-bio"
        >
          {{ userInfo.bio }}
        </div>

        <el-button
          type="primary"
          size="small"
          style="width: 100%; margin-top: 12px;"
          @click="handleViewProfile"
        >
          查看主页
        </el-button>
      </div>
    </el-popover>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { User } from '@element-plus/icons-vue';

interface Props {
  src?: string;
  username?: string;
  nickname?: string;
  size?: number | 'large' | 'default' | 'small';
  shape?: 'circle' | 'square';
  fit?: 'fill' | 'contain' | 'cover' | 'none' | 'scale-down';
  clickable?: boolean;
  showOnlineStatus?: boolean;
  isOnline?: boolean;
  showTooltip?: boolean;
  userInfo?: {
    id?: number;
    username?: string;
    nickname?: string;
    avatar?: string;
    points?: number;
    questionCount?: number;
    answerCount?: number;
    bio?: string;
  };
  badgeValue?: string | number;
  badgeType?: 'primary' | 'success' | 'info' | 'warning' | 'danger';
  badgeDot?: boolean;
}

interface Emits {
  (e: 'click', userInfo?: any): void;
}

const props = withDefaults(defineProps<Props>(), {
  size: 'default',
  shape: 'circle',
  fit: 'cover',
  clickable: false,
  showOnlineStatus: false,
  isOnline: false,
  showTooltip: false,
  badgeDot: false
});

const emit = defineEmits<Emits>();
const router = useRouter();

/**
 * 显示名称（头像文字）
 */
const displayName = computed(() => {
  if (props.nickname) {
    return props.nickname.charAt(0).toUpperCase();
  }
  if (props.username) {
    return props.username.charAt(0).toUpperCase();
  }
  return 'U';
});

/**
 * 计算的头像URL（带默认值）
 */
const computedSrc = computed(() => {
  if (props.src) {
    return props.src;
  }
  const name = props.nickname || props.username || 'U';
  return `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=0056D2&color=fff&size=128`;
});

/**
 * Tooltip中的头像URL（带默认值）
 */
const computedTooltipAvatar = computed(() => {
  if (props.userInfo?.avatar) {
    return props.userInfo.avatar;
  }
  const name = props.userInfo?.nickname || props.userInfo?.username || 'U';
  return `https://ui-avatars.com/api/?name=${encodeURIComponent(name)}&background=0056D2&color=fff&size=96`;
});

/**
 * 头像图标
 */
const icon = computed(() => {
  return User;
});

/**
 * 头像替代文本
 */
const alt = computed(() => {
  return props.nickname || props.username || '用户头像';
});

/**
 * 点击头像
 */
const handleClick = () => {
  if (!props.clickable) return;
  emit('click', props.userInfo);

  if (props.userInfo?.id) {
    router.push(`/user/${props.userInfo.id}`);
  }
};

/**
 * 查看用户主页
 */
const handleViewProfile = () => {
  if (props.userInfo?.id) {
    router.push(`/user/${props.userInfo.id}`);
  }
};
</script>

<style scoped lang="scss">
.user-avatar-wrapper {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;

  &.clickable {
    cursor: pointer;

    :deep(.el-avatar) {
      transition: transform 0.2s ease;

      &:hover {
        transform: scale(1.1);
      }
    }
  }
}

.online-indicator {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background-color: #c0c4cc;
  border: 2px solid #fff;

  &.online {
    background-color: #67c23a;
  }
}

.avatar-tooltip-trigger {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.user-info-tooltip {
  padding: 8px 0;
}

.tooltip-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.tooltip-user-info {
  flex: 1;
  min-width: 0;
}

.tooltip-username {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tooltip-nickname {
  font-size: 13px;
  color: #999;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tooltip-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  padding: 12px 0;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-label {
  font-size: 12px;
  color: #999;
}

.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.tooltip-bio {
  font-size: 13px;
  color: #666;
  line-height: 1.5;
  padding: 8px 0;
  border-top: 1px solid #f0f0f0;
  margin-top: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 不同尺寸的在线指示器位置调整 */
.user-avatar-wrapper:deep(.el-avatar--large) + .online-indicator {
  width: 14px;
  height: 14px;
}

.user-avatar-wrapper:deep(.el-avatar--small) + .online-indicator {
  width: 10px;
  height: 10px;
}
</style>