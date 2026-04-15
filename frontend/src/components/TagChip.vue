<template>
  <el-tag
    :type="tagType"
    :effect="effect"
    :size="size"
    :closable="closable"
    :disable-transitions="disableTransitions"
    :hit="hit"
    :color="computedColor"
    class="tag-chip"
    @click="handleClick"
    @close="handleClose"
  >
    <el-icon
      v-if="tag.icon"
      class="tag-icon"
    >
      <component :is="getIconComponent(tag.icon)" />
    </el-icon>
    <span class="tag-name">{{ tag.name }}</span>
    <span
      v-if="showCount && tag.questionCount !== undefined"
      class="tag-count"
    >
      {{ formatCount(tag.questionCount) }}
    </span>
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import {
  Document,
  ChatDotRound,
  Star,
  TrendCharts,
  Book,
  Edit,
  QuestionFilled,
  Collection,
  Trophy,
  Flag,
  Tools,
  Briefcase,
  School,
  DataAnalysis,
  Monitor,
  Camera,
  Music,
  VideoCamera,
  Headset,
  Basketball,
  Football
} from '@element-plus/icons-vue';

interface Props {
  tag: {
    id: number;
    name: string;
    color?: string;
    icon?: string;
    questionCount?: number;
    followCount?: number;
    description?: string;
  };
  size?: 'large' | 'default' | 'small';
  type?: 'primary' | 'success' | 'info' | 'warning' | 'danger';
  effect?: 'dark' | 'light' | 'plain';
  closable?: boolean;
  disableTransitions?: boolean;
  hit?: boolean;
  showCount?: boolean;
  clickable?: boolean;
}

interface Emits {
  (e: 'click', tag: any): void;
  (e: 'close', tag: any): void;
}

const props = withDefaults(defineProps<Props>(), {
  size: 'default',
  type: 'primary',
  effect: 'plain',
  closable: false,
  disableTransitions: false,
  hit: false,
  showCount: false,
  clickable: true
});

const emit = defineEmits<Emits>();
const router = useRouter();

/**
 * 计算标签类型
 */
const tagType = computed(() => {
  // 如果有自定义颜色，则不使用预设类型
  if (props.tag.color) return '';
  return props.type;
});

/**
 * 计算标签颜色
 */
const computedColor = computed(() => {
  return props.tag.color || '';
});

/**
 * 格式化数量
 */
const formatCount = (count: number) => {
  if (!count) return '';
  if (count >= 10000) return `${(count / 10000).toFixed(1)}万`;
  if (count >= 1000) return `${(count / 1000).toFixed(1)}k`;
  return count.toString();
};

/**
 * 获取图标组件
 */
const getIconComponent = (iconName: string) => {
  const iconMap: Record<string, any> = {
    'document': Document,
    'chat': ChatDotRound,
    'star': Star,
    'trend': TrendCharts,
    'book': Book,
    'edit': Edit,
    'question': QuestionFilled,
    'collection': Collection,
    'trophy': Trophy,
    'flag': Flag,
    'tools': Tools,
    'briefcase': Briefcase,
    'school': School,
    'analysis': DataAnalysis,
    'computer': Monitor,
    'camera': Camera,
    'music': Music,
    'video': VideoCamera,
    'headset': Headset,
    'basketball': Basketball,
    'football': Football
  };

  return iconMap[iconName] || Document;
};

/**
 * 点击标签
 */
const handleClick = () => {
  if (!props.clickable) return;
  emit('click', props.tag);
  router.push(`/tags/${props.tag.id}`);
};

/**
 * 关闭标签
 */
const handleClose = () => {
  emit('close', props.tag);
};
</script>

<style scoped lang="scss">
.tag-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  border-radius: 16px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s ease;
  cursor: pointer;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  :deep(.el-icon__close) {
    transition: all 0.2s ease;

    &:hover {
      color: #f56c6c;
      background-color: #fef0f0;
      border-radius: 50%;
    }
  }
}

.tag-icon {
  font-size: 16px;
}

.tag-name {
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tag-count {
  font-size: 12px;
  opacity: 0.8;
  background: rgba(0, 0, 0, 0.1);
  padding: 2px 6px;
  border-radius: 8px;
  margin-left: 4px;
}

/* 不同尺寸的样式 */
.tag-chip.el-tag--large {
  padding: 8px 16px;
  font-size: 16px;

  .tag-icon {
    font-size: 18px;
  }

  .tag-name {
    max-width: 150px;
  }
}

.tag-chip.el-tag--small {
  padding: 4px 10px;
  font-size: 12px;

  .tag-icon {
    font-size: 14px;
  }

  .tag-name {
    max-width: 100px;
  }
}

/* 不同效果的样式 */
.tag-chip.el-tag--dark {
  .tag-count {
    background: rgba(255, 255, 255, 0.2);
  }
}

.tag-chip.el-tag--light {
  background-color: #ecf5ff;
  border-color: #d9ecff;
  color: #409eff;

  .tag-count {
    background: rgba(64, 158, 255, 0.1);
  }
}

/* 自定义颜色样式 */
.tag-chip:not(.el-tag--primary):not(.el-tag--success):not(.el-tag--info):not(.el-tag--warning):not(.el-tag--danger) {
  border: 1px solid transparent;

  .tag-count {
    background: rgba(0, 0, 0, 0.1);
  }
}
</style>