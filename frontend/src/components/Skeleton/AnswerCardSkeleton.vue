<template>
  <div class="answer-card-skeleton">
    <!-- 用户信息骨架 -->
    <div class="user-skeleton">
      <el-skeleton-item
        variant="circle"
        style="width: 40px; height: 40px;"
      />
      <div class="user-info-skeleton">
        <el-skeleton-item
          variant="text"
          style="width: 100px;"
        />
        <el-skeleton-item
          variant="text"
          style="width: 150px; margin-top: 4px;"
        />
      </div>
    </div>

    <!-- 回答内容骨架 -->
    <div class="content-skeleton">
      <el-skeleton
        :rows="4"
        animated
      />
      <el-skeleton
        :rows="3"
        animated
        style="margin-top: 8px;"
      />
    </div>

    <!-- 图片骨架 -->
    <div class="images-skeleton">
      <el-skeleton-item
        variant="image"
        style="width: 100%; height: 150px; border-radius: 8px;"
      />
    </div>

    <!-- 操作栏骨架 -->
    <div class="actions-skeleton">
      <el-skeleton-item
        variant="button"
        style="width: 60px; height: 32px;"
      />
      <el-skeleton-item
        variant="button"
        style="width: 80px; height: 32px;"
      />
      <el-skeleton-item
        variant="button"
        style="width: 60px; height: 32px;"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
// 回答卡片骨架屏组件
</script>

<style scoped lang="scss">
.answer-card-skeleton {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.user-skeleton {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.user-info-skeleton {
  flex: 1;
}

.content-skeleton {
  margin-bottom: 16px;
}

.images-skeleton {
  margin-bottom: 16px;
}

.actions-skeleton {
  display: flex;
  gap: 16px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}
</style>