<template>
  <div class="question-card-skeleton">
    <!-- 标题骨架 -->
    <el-skeleton
      :rows="2"
      animated
    />

    <!-- 内容骨架 -->
    <el-skeleton
      :rows="3"
      animated
      style="margin-top: 12px;"
    />

    <!-- 标签骨架 -->
    <div class="tags-skeleton">
      <el-skeleton-item
        variant="button"
        style="width: 80px; height: 24px;"
      />
      <el-skeleton-item
        variant="button"
        style="width: 80px; height: 24px;"
      />
      <el-skeleton-item
        variant="button"
        style="width: 80px; height: 24px;"
      />
    </div>

    <!-- 统计信息骨架 -->
    <div class="stats-skeleton">
      <el-skeleton-item
        variant="text"
        style="width: 60px;"
      />
      <el-skeleton-item
        variant="text"
        style="width: 60px;"
      />
      <el-skeleton-item
        variant="text"
        style="width: 60px;"
      />
      <el-skeleton-item
        variant="text"
        style="width: 60px;"
      />
    </div>

    <!-- 底部信息骨架 -->
    <div class="footer-skeleton">
      <div class="user-skeleton">
        <el-skeleton-item
          variant="circle"
          style="width: 32px; height: 32px;"
        />
        <el-skeleton-item
          variant="text"
          style="width: 80px; margin-left: 8px;"
        />
      </div>
      <el-skeleton-item
        variant="text"
        style="width: 100px;"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
// 问题卡片骨架屏组件
</script>

<style scoped lang="scss">
.question-card-skeleton {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.tags-skeleton {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.stats-skeleton {
  display: flex;
  gap: 16px;
  margin-top: 12px;
  padding-top: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.footer-skeleton {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
}

.user-skeleton {
  display: flex;
  align-items: center;
}
</style>