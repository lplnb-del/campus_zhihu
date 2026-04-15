<template>
  <div class="comment-item-skeleton">
    <!-- 用户头像骨架 -->
    <el-skeleton-item
      variant="circle"
      style="width: 40px; height: 40px; flex-shrink: 0;"
    />

    <!-- 评论内容骨架 -->
    <div class="comment-content-skeleton">
      <!-- 用户名和时间 -->
      <div class="header-skeleton">
        <el-skeleton-item
          variant="text"
          style="width: 80px;"
        />
        <el-skeleton-item
          variant="text"
          style="width: 80px;"
        />
      </div>

      <!-- 评论内容 -->
      <div class="text-skeleton">
        <el-skeleton
          :rows="2"
          animated
        />
      </div>

      <!-- 操作栏 -->
      <div class="actions-skeleton">
        <el-skeleton-item
          variant="text"
          style="width: 40px;"
        />
        <el-skeleton-item
          variant="text"
          style="width: 40px;"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// 评论列表项骨架屏组件
</script>

<style scoped lang="scss">
.comment-item-skeleton {
  display: flex;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-content-skeleton {
  flex: 1;
  min-width: 0;
}

.header-skeleton {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.text-skeleton {
  margin-bottom: 12px;
}

.actions-skeleton {
  display: flex;
  gap: 20px;
}
</style>