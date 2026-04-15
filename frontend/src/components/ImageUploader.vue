<template>
  <div class="image-uploader">
    <!-- 图片列表 -->
    <div class="image-list">
      <!-- 已上传的图片 -->
      <div
        v-for="(image, index) in imageList"
        :key="image.objectName"
        class="image-item"
      >
        <el-image
          :src="image.url"
          :preview-src-list="previewUrls"
          :initial-index="index"
          fit="cover"
          class="uploaded-image"
        >
          <template #error>
            <div class="image-error">
              <el-icon><Picture /></el-icon>
            </div>
          </template>
        </el-image>

        <!-- 遮罩层 -->
        <div class="image-mask">
          <div class="mask-actions">
            <span
              class="mask-item"
              @click="previewImage(index)"
            >
              <el-icon><ZoomIn /></el-icon>
            </span>
            <span
              class="mask-item delete"
              @click="handleDelete(index)"
            >
              <el-icon><Delete /></el-icon>
            </span>
          </div>
        </div>

        <!-- 上传进度 -->
        <div
          v-if="image.uploading"
          class="upload-progress"
        >
          <el-progress
            :percentage="image.progress"
            :stroke-width="2"
            :show-text="false"
          />
        </div>
      </div>

      <!-- 上传按钮 -->
      <div
        v-if="imageList.length < maxCount"
        class="upload-button"
        :class="{ 'disabled': disabled }"
        @click="handleSelectFiles"
      >
        <el-icon class="upload-icon">
          <Plus />
        </el-icon>
        <div class="upload-text">
          上传图片
        </div>
        <div class="upload-hint">
          {{ imageList.length }}/{{ maxCount }}
        </div>
        <input
          ref="fileInputRef"
          type="file"
          :accept="accept"
          :multiple="multiple"
          :disabled="disabled"
          style="display: none"
          @change="handleFileChange"
        >
      </div>
    </div>

    <!-- 提示信息 -->
    <div
      v-if="hint"
      class="upload-hint-text"
    >
      {{ hint }}
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { ElMessage } from 'element-plus';
import {
  Plus,
  Delete,
  ZoomIn,
  Picture
} from '@element-plus/icons-vue';
import {
  uploadFile,
  isValidFileType,
  isValidFileSize,
  formatFileSize,
  getPreviewUrl,
  revokePreviewUrl
} from '@/api/file';
import type { FileUploadDTO } from '@/api/file';

interface ImageItem {
  file?: File;
  url: string;
  objectName?: string;
  uploading?: boolean;
  progress?: number;
}

interface Props {
  modelValue?: string[];
  maxCount?: number;
  maxSize?: number;
  accept?: string;
  multiple?: boolean;
  disabled?: boolean;
  hint?: string;
}

interface Emits {
  (e: 'update:modelValue', urls: string[]): void;
  (e: 'change', urls: string[]): void;
  (e: 'upload-success', data: FileUploadDTO): void;
  (e: 'upload-error', error: any): void;
  (e: 'remove', index: number): void;
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => [],
  maxCount: 9,
  maxSize: 10 * 1024 * 1024, // 10MB
  accept: 'image/jpeg,image/jpg,image/png,image/gif,image/webp',
  multiple: true,
  disabled: false,
  hint: '支持 jpg、png、gif、webp 格式，单张图片不超过 10MB'
});

const emit = defineEmits<Emits>();

const fileInputRef = ref<HTMLInputElement>();
const imageList = ref<ImageItem[]>([]);

// 预览URL列表
const previewUrls = computed(() => {
  return imageList.value.map(item => item.url);
});

// 初始化图片列表
watch(
  () => props.modelValue,
  (urls) => {
    if (urls && urls.length > 0) {
      imageList.value = urls.map(url => ({
        url,
        objectName: url.split('/').pop()
      }));
    }
  },
  { immediate: true }
);

/**
 * 选择文件
 */
const handleSelectFiles = () => {
  if (props.disabled) return;
  fileInputRef.value?.click();
};

/**
 * 文件变化
 */
const handleFileChange = async (event: Event) => {
  const target = event.target as HTMLInputElement;
  const files = Array.from(target.files || []);

  if (files.length === 0) {
    return;
  }

  // 检查数量限制
  const remainingSlots = props.maxCount - imageList.value.length;
  if (remainingSlots <= 0) {
    ElMessage.warning(`最多只能上传 ${props.maxCount} 张图片`);
    return;
  }

  const filesToUpload = files.slice(0, remainingSlots);

  // 验证文件
  const validFiles = filesToUpload.filter(file => {
    // 验证文件类型
    if (!isValidFileType(file, props.accept.split(','))) {
      ElMessage.error(`文件 ${file.name} 格式不支持`);
      return false;
    }

    // 验证文件大小
    if (!isValidFileSize(file, props.maxSize)) {
      ElMessage.error(`文件 ${file.name} 大小超过限制（最大 ${formatFileSize(props.maxSize)}）`);
      return false;
    }

    return true;
  });

  // 上传文件
  for (const file of validFiles) {
    await uploadSingleFile(file);
  }

  // 清空input
  target.value = '';
};

/**
 * 上传单个文件
 */
const uploadSingleFile = async (file: File) => {
  // 创建预览URL
  const previewUrl = getPreviewUrl(file);

  // 添加到列表
  const imageItem: ImageItem = {
    file,
    url: previewUrl,
    uploading: true,
    progress: 0
  };
  imageList.value.push(imageItem);

  try {
    // 上传文件
    const result = await uploadFile(file, (progress) => {
      imageItem.progress = progress;
    });

    // 更新图片信息
    imageItem.url = result.url;
    imageItem.objectName = result.objectName;
    imageItem.uploading = false;
    imageItem.progress = 100;

    // 释放预览URL
    revokePreviewUrl(previewUrl);

    // 触发事件
    emit('upload-success', result);
    emitChange();

    ElMessage.success('上传成功');
  } catch (error: any) {
    // 移除失败的图片
    const index = imageList.value.indexOf(imageItem);
    if (index > -1) {
      imageList.value.splice(index, 1);
    }

    // 释放预览URL
    revokePreviewUrl(previewUrl);

    // 触发事件
    emit('upload-error', error);
    emitChange();

    ElMessage.error(error.message || '上传失败');
  }
};

/**
 * 删除图片
 */
const handleDelete = (index: number) => {
  const imageItem = imageList.value[index];

  // 如果有预览URL，释放它
  if (imageItem.file) {
    revokePreviewUrl(imageItem.url);
  }

  // 从列表中移除
  imageList.value.splice(index, 1);

  // 触发事件
  emit('remove', index);
  emitChange();

  ElMessage.success('删除成功');
};

/**
 * 预览图片
 */
const previewImage = (index: number) => {
  // Element Plus 的 el-image 组件会自动处理预览
};

/**
 * 触发变化事件
 */
const emitChange = () => {
  const urls = imageList.value
    .filter(item => !item.uploading)
    .map(item => item.url);

  emit('update:modelValue', urls);
  emit('change', urls);
};

/**
 * 清空所有图片
 */
const clear = () => {
  imageList.value.forEach(item => {
    if (item.file) {
      revokePreviewUrl(item.url);
    }
  });
  imageList.value = [];
  emitChange();
};

// 暴露方法
defineExpose({
  clear
});
</script>

<style scoped lang="scss">
.image-uploader {
  width: 100%;
}

.image-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
}

.image-item {
  position: relative;
  width: 100%;
  padding-bottom: 100%; // 1:1 比例
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
  border: 1px solid #e0e0e0;
  transition: all 0.3s ease;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }
}

.uploaded-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}

.image-error {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  color: #999;

  .el-icon {
    font-size: 32px;
  }
}

.image-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;

  .image-item:hover & {
    opacity: 1;
  }
}

.mask-actions {
  display: flex;
  gap: 16px;
}

.mask-item {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  color: #666;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: #fff;
    color: #0056d2;
    transform: scale(1.1);
  }

  &.delete:hover {
    color: #f56c6c;
  }

  .el-icon {
    font-size: 18px;
  }
}

.upload-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  padding: 8px;
  background: rgba(255, 255, 255, 0.9);
}

.upload-button {
  position: relative;
  width: 100%;
  padding-bottom: 100%; // 1:1 比例
  border-radius: 8px;
  border: 2px dashed #d9d9d9;
  background: #fafafa;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  &:hover {
    border-color: #0056d2;
    background: #f0f7ff;

    .upload-icon {
      color: #0056d2;
    }
  }

  &.disabled {
    cursor: not-allowed;
    opacity: 0.6;

    &:hover {
      border-color: #d9d9d9;
      background: #fafafa;

      .upload-icon {
        color: #999;
      }
    }
  }
}

.upload-icon {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -60%);
  font-size: 28px;
  color: #999;
  transition: color 0.3s ease;
}

.upload-text {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, 20%);
  font-size: 14px;
  color: #666;
}

.upload-hint {
  position: absolute;
  bottom: 8px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 12px;
  color: #999;
}

.upload-hint-text {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
  line-height: 1.5;
}
</style>