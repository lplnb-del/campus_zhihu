<template>
  <div
    class="loading-skeleton"
    :class="`loading-skeleton--${variant}`"
  >
    <template v-if="variant === 'card'">
      <!-- 卡片式骨架屏 -->
      <div
        v-for="i in count"
        :key="i"
        class="skeleton-card"
      >
        <el-skeleton
          :rows="rows"
          animated
        />
      </div>
    </template>

    <template v-else-if="variant === 'list'">
      <!-- 列表式骨架屏 -->
      <div
        v-for="i in count"
        :key="i"
        class="skeleton-list-item"
      >
        <el-skeleton-item
          variant="circle"
          style="width: 40px; height: 40px;"
        />
        <div class="skeleton-list-content">
          <el-skeleton
            :rows="2"
            animated
          />
        </div>
      </div>
    </template>

    <template v-else-if="variant === 'image'">
      <!-- 图片式骨架屏 -->
      <div
        v-for="i in count"
        :key="i"
        class="skeleton-image"
      >
        <el-skeleton-item
          variant="image"
          style="width: 100%; height: 100%;"
        />
      </div>
    </template>

    <template v-else>
      <!-- 默认骨架屏 -->
      <el-skeleton
        :rows="rows"
        :count="count"
        animated
      />
    </template>
  </div>
</template>

<script setup lang="ts">
interface Props {
  variant?: 'default' | 'card' | 'list' | 'image';
  rows?: number;
  count?: number;
}

withDefaults(defineProps<Props>(), {
  variant: 'default',
  rows: 3,
  count: 1
});
</script>

<style scoped lang="scss">
.loading-skeleton {
  width: 100%;
}

.skeleton-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.skeleton-list-item {
  display: flex;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;

  &:last-child {
    border-bottom: none;
  }
}

.skeleton-list-content {
  flex: 1;
  min-width: 0;
}

.skeleton-image {
  width: 100%;
  padding-bottom: 100%; // 1:1 比例
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;

  :deep(.el-skeleton__image) {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
  }
}
</style>