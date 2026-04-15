<template>
  <div class="tags-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          话题广场
        </h1>
        <p class="page-subtitle">
          探索感兴趣的话题，关注你喜欢的领域
        </p>
      </div>
      <div class="header-stats">
        <div class="stat-item">
          <span class="stat-number">{{ totalTags }}</span>
          <span class="stat-label">话题总数</span>
        </div>
        <div class="stat-item">
          <span class="stat-number">{{ totalQuestions }}</span>
          <span class="stat-label">问题总数</span>
        </div>
      </div>
    </div>

    <!-- 搜索和筛选 -->
    <div class="filter-section">
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索话题..."
          size="large"
          clearable
          @keyup.enter="handleSearch"
          @clear="handleSearchClear"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button
              :icon="Search"
              @click="handleSearch"
            />
          </template>
        </el-input>
      </div>
      <div class="sort-box">
        <el-radio-group
          v-model="sortBy"
          @change="handleSortChange"
        >
          <el-radio-button value="hot">
            最热
          </el-radio-button>
          <el-radio-button value="new">
            最新
          </el-radio-button>
          <el-radio-button value="name">
            名称
          </el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <!-- 热门话题 -->
    <div
      v-if="!searchKeyword && sortBy === 'hot'"
      class="hot-tags-section"
    >
      <h2 class="section-title">
        <el-icon><TrendCharts /></el-icon>
        <span>热门话题</span>
      </h2>
      <div
        v-loading="loadingHot"
        class="hot-tags-grid"
      >
        <template v-if="hotTags.length > 0">
          <div
            v-for="tag in hotTags"
            :key="tag.id"
            class="hot-tag-card"
            @click="handleTagClick(tag.id)"
          >
            <div class="tag-icon">
              <el-icon>
                <component :is="getIconComponent(tag.icon)" />
              </el-icon>
            </div>
            <h3 class="tag-name">
              {{ tag.name }}
            </h3>
            <p class="tag-description">
              {{ tag.description || '暂无描述' }}
            </p>
            <div class="tag-stats">
              <span class="stat">
                <el-icon><QuestionFilled /></el-icon>
                <span>{{ formatNumber(tag.questionCount) }} 问题</span>
              </span>
              <span class="stat">
                <el-icon><User /></el-icon>
                <span>{{ formatNumber(tag.followCount) }} 关注</span>
              </span>
            </div>
            <el-button
              :type="tag.isFollowed ? 'default' : 'primary'"
              size="small"
              class="follow-btn"
              @click.stop="handleFollowToggle(tag)"
            >
              <el-icon><Star /></el-icon>
              <span>{{ tag.isFollowed ? '已关注' : '关注' }}</span>
            </el-button>
          </div>
        </template>
        <template v-else>
          <div class="empty-state">
            <el-icon
              class="empty-icon"
              :size="48"
            >
              <TrendCharts />
            </el-icon>
            <p class="empty-text">
              暂无热门话题
            </p>
          </div>
        </template>
      </div>
    </div>

    <!-- 所有话题列表 -->
    <div class="tags-list-section">
      <h2 class="section-title">
        <el-icon><PriceTag /></el-icon>
        <span>{{ searchKeyword ? '搜索结果' : '所有话题' }}</span>
      </h2>

      <!-- 加载中 -->
      <div
        v-if="loading"
        class="loading-container"
      >
        <el-skeleton
          :rows="8"
          animated
        />
      </div>

      <!-- 标签列表 -->
      <div
        v-else-if="tagList.length > 0"
        class="tags-grid"
      >
        <div
          v-for="tag in tagList"
          :key="tag.id"
          class="tag-card"
          @click="handleTagClick(tag.id)"
        >
          <div class="tag-header">
            <div class="tag-info">
              <h3 class="tag-name">
                {{ tag.name }}
              </h3>
              <p class="tag-description">
                {{ tag.description || '暂无描述' }}
              </p>
            </div>
            <el-button
              :type="tag.isFollowed ? 'default' : 'primary'"
              size="small"
              text
              @click.stop="handleFollowToggle(tag)"
            >
              <el-icon><Star /></el-icon>
              <span>{{ tag.isFollowed ? '已关注' : '关注' }}</span>
            </el-button>
          </div>
          <div class="tag-stats">
            <div class="stat-item">
              <el-icon><QuestionFilled /></el-icon>
              <span>{{ formatNumber(tag.questionCount) }} 问题</span>
            </div>
            <div class="stat-item">
              <el-icon><User /></el-icon>
              <span>{{ formatNumber(tag.followCount) }} 关注</span>
            </div>
            <div class="stat-item">
              <el-icon><Clock /></el-icon>
              <span>{{ formatTime(tag.createTime) }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div
        v-else
        class="empty-container"
      >
        <el-icon
          class="empty-icon"
          :size="80"
        >
          <Document />
        </el-icon>
        <p class="empty-text">
          {{ emptyText }}
        </p>
        <el-button
          v-if="searchKeyword"
          type="primary"
          @click="handleSearchClear"
        >
          查看所有话题
        </el-button>
      </div>

      <!-- 分页 -->
      <div
        v-if="total > 0"
        class="pagination"
      >
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[12, 24, 48, 96]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>

    <!-- 右侧边栏 -->
    <div class="tags-sidebar">
      <!-- 我关注的话题 -->
      <div
        v-if="userStore.isLoggedIn"
        class="sidebar-card"
      >
        <div class="card-header">
          <el-icon><Star /></el-icon>
          <span>我的关注</span>
        </div>
        <div class="card-content">
          <div
            v-loading="loadingFollowed"
            class="followed-tags"
          >
            <div
              v-for="tag in followedTags"
              :key="tag.id"
              class="followed-tag-item"
              @click="handleTagClick(tag.id)"
            >
              <span class="tag-name">{{ tag.name }}</span>
              <span class="tag-count">{{ tag.questionCount }}</span>
            </div>
            <div
              v-if="followedTags.length === 0"
              class="empty-text"
            >
              暂未关注话题
            </div>
          </div>
        </div>
      </div>

      <!-- 话题分类 -->
      <div class="sidebar-card">
        <div class="card-header">
          <el-icon><Menu /></el-icon>
          <span>话题分类</span>
        </div>
        <div class="card-content">
          <div class="category-list">
            <div
              v-for="category in categories"
              :key="category.name"
              class="category-item"
              :class="{ active: selectedCategory === category.name }"
              @click="handleCategoryClick(category.name)"
            >
              <el-icon>
                <component :is="category.icon" />
              </el-icon>
              <span>{{ category.label }}</span>
              <span class="category-count">{{ category.count }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 推荐话题 -->
      <div class="sidebar-card">
        <div class="card-header">
          <el-icon><MagicStick /></el-icon>
          <span>推荐话题</span>
        </div>
        <div class="card-content">
          <div
            v-loading="loadingRecommended"
            class="recommended-tags"
          >
            <template v-if="recommendedTags.length > 0">
              <span
                v-for="tag in recommendedTags"
                :key="tag.id"
                class="campus-tag"
                @click="handleTagClick(tag.id)"
              >
                {{ tag.name }}
              </span>
            </template>
            <template v-else>
              <div class="empty-text">
                暂无推荐话题
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { getTagList, getHotTags, getAllTags, searchTags, followTag, unfollowTag, getMyFollowedTags, type TagVO } from '@/api/tag'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import {
  Search,
  TrendCharts,
  PriceTag,
  QuestionFilled,
  User,
  Star,
  Clock,
  Document,
  Menu,
  MagicStick,
  Monitor,
  Reading,
  Coffee,
  Briefcase,
  MoreFilled,
  Coin,
  Tools,
  Cpu,
  Cellphone,
  Sunny,
  DataAnalysis,
  Lock,
  CircleCheck,
  Lightning,
  Promotion,
  Compass,
} from '@element-plus/icons-vue'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const router = useRouter()
const userStore = useUserStore()

// 图标映射表：将字符串图标名映射到实际组件
const iconMap: Record<string, any> = {
  PriceTag,
  Monitor,
  Reading,
  Coffee,
  Briefcase,
  MoreFilled,
  Coin,
  Tools,
  Cpu,
  Cellphone,
  Sunny,
  DataAnalysis,
  Lock,
  CircleCheck,
  Lightning,
  Promotion,
  Compass,
}

// 获取图标组件的函数
const getIconComponent = (iconName?: string) => {
  return iconName && iconMap[iconName] ? iconMap[iconName] : PriceTag
}

// 搜索关键词
const searchKeyword = ref('')

// 排序方式
const sortBy = ref<'hot' | 'new' | 'name'>('hot')

// 分类选择
const selectedCategory = ref('')

// 热门标签
const hotTags = ref<TagVO[]>([])
const loadingHot = ref(false)

// 标签列表
const tagList = ref<TagVO[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(24)
const total = ref(0)

// 我关注的话题
const followedTags = ref<TagVO[]>([])
const loadingFollowed = ref(false)

// 推荐话题
const recommendedTags = ref<TagVO[]>([])
const loadingRecommended = ref(false)

// 统计数据
const totalTags = ref(0)
const totalQuestions = ref(0)

// 分类列表 - 使用数据库中实际存在的分类
const categories = ref([
  { name: 'programming', label: '编程语言', icon: Monitor, count: 0 },
  { name: 'frontend', label: '前端技术', icon: Monitor, count: 0 },
  { name: 'backend', label: '后端技术', icon: Coffee, count: 0 },
  { name: 'database', label: '数据库', icon: Coin, count: 0 },
  { name: 'devops', label: 'DevOps', icon: Tools, count: 0 },
  { name: 'ai', label: '人工智能', icon: MagicStick, count: 0 },
  { name: 'algorithm', label: '算法', icon: Cpu, count: 0 },
  { name: 'mobile', label: '移动开发', icon: Cellphone, count: 0 },
  { name: 'cloud', label: '云计算', icon: Sunny, count: 0 },
  { name: 'data', label: '大数据', icon: DataAnalysis, count: 0 },
  { name: 'security', label: '安全', icon: Lock, count: 0 },
  { name: 'testing', label: '测试', icon: CircleCheck, count: 0 },
  { name: 'optimization', label: '性能优化', icon: Lightning, count: 0 },
  { name: 'architecture', label: '架构', icon: Promotion, count: 0 },
  { name: 'methodology', label: '方法论', icon: Compass, count: 0 },
])

// 空状态文案
const emptyText = computed(() => {
  if (searchKeyword.value) {
    return `未找到与"${searchKeyword.value}"相关的话题`
  }
  return '暂无话题'
})

/**
 * 加载热门标签
 */
const loadHotTags = async () => {
  loadingHot.value = true
  try {
    const result = await getHotTags(12)
    // 确保返回的是数组
    if (Array.isArray(result)) {
      hotTags.value = result
    } else {
      hotTags.value = []
      console.error('加载热门标签失败：返回数据不是数组')
    }
  } catch (error) {
    console.error('加载热门标签失败：', error)
    hotTags.value = []
  } finally {
    loadingHot.value = false
  }
}

/**
 * 加载标签列表
 */
const loadTags = async () => {
  loading.value = true
  try {
    let result
    if (searchKeyword.value) {
      result = await searchTags(searchKeyword.value, currentPage.value, pageSize.value, sortBy.value)
    } else {
      result = await getTagList(currentPage.value, pageSize.value, selectedCategory.value, sortBy.value)
    }
    // 确保 result 和 result.list 存在且为数组
    tagList.value = Array.isArray(result?.list) ? result.list : []
    total.value = result?.total || 0
    totalTags.value = result?.total || 0
    // 更新问题总数
    totalQuestions.value = result?.totalQuestions || 0
    
    // 如果没有搜索关键词，更新分类统计
    if (!searchKeyword.value) {
      // 获取所有标签以统计分类数量
      const allTags = await getAllTags()
      if (Array.isArray(allTags)) {
        // 统计每个分类的标签数量
        const categoryCountMap: Record<string, number> = {}
        allTags.forEach(tag => {
          const category = tag.category || 'other'
          categoryCountMap[category] = (categoryCountMap[category] || 0) + 1
        })
        
        // 更新分类数组的count属性
        categories.value.forEach(category => {
          category.count = categoryCountMap[category.name] || 0
        })
      }
    }
  } catch (error) {
    console.error('加载标签列表失败：', error)
    tagList.value = []
    total.value = 0
    totalTags.value = 0
    totalQuestions.value = 0
    ElMessage.error('加载标签列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 加载我关注的话题
 */
const loadFollowedTags = async () => {
  if (!userStore.isLoggedIn) return

  loadingFollowed.value = true
  try {
    followedTags.value = await getMyFollowedTags()
  } catch (error) {
    console.error('加载关注话题失败：', error)
  } finally {
    loadingFollowed.value = false
  }
}

/**
 * 加载推荐话题
 */
const loadRecommendedTags = async () => {
  loadingRecommended.value = true
  try {
    const allTags = await getAllTags()
    // 确保返回的是数组
    if (Array.isArray(allTags)) {
      // 随机推荐 10 个
      recommendedTags.value = allTags.sort(() => Math.random() - 0.5).slice(0, 10)
    } else {
      recommendedTags.value = []
      console.error('加载推荐话题失败：返回数据不是数组')
    }
  } catch (error) {
    console.error('加载推荐话题失败：', error)
    recommendedTags.value = []
  } finally {
    loadingRecommended.value = false
  }
}

/**
 * 处理搜索
 */
const handleSearch = () => {
  currentPage.value = 1
  loadTags()
}

/**
 * 处理清空搜索
 */
const handleSearchClear = () => {
  searchKeyword.value = ''
  currentPage.value = 1
  loadTags()
}

/**
 * 处理排序变化
 */
const handleSortChange = () => {
  currentPage.value = 1
  loadTags()
}

/**
 * 处理分类点击
 */
const handleCategoryClick = (category: string) => {
  if (selectedCategory.value === category) {
    selectedCategory.value = ''
  } else {
    selectedCategory.value = category
  }
  currentPage.value = 1
  loadTags()
}

/**
 * 处理标签点击
 */
const handleTagClick = (tagId: number) => {
  router.push(`/tag/${tagId}`)
}

/**
 * 处理关注切换
 */
const handleFollowToggle = async (tag: TagVO) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    if (tag.isFollowed) {
      await unfollowTag(tag.id)
      tag.isFollowed = false
      tag.followCount--
      ElMessage.success('已取消关注')
    } else {
      await followTag(tag.id)
      tag.isFollowed = true
      tag.followCount++
      ElMessage.success('关注成功')
    }

    // 重新加载关注列表
    loadFollowedTags()
  } catch (error) {
    console.error('关注操作失败：', error)
    ElMessage.error('操作失败')
  }
}

/**
 * 处理页码变化
 */
const handlePageChange = (page: number) => {
  currentPage.value = page
  loadTags()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

/**
 * 处理每页大小变化
 */
const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  loadTags()
}

/**
 * 格式化时间
 */
const formatTime = (time: string) => {
  return dayjs(time).fromNow()
}

/**
 * 格式化数字
 */
const formatNumber = (num: number) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

onMounted(() => {
  loadHotTags()
  loadTags()
  loadFollowedTags()
  loadRecommendedTags()
})
</script>

<style lang="scss" scoped>
.tags-page {
  width: 100%;
  min-height: calc(100vh - 64px);
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
  align-items: start;
  position: relative;
  z-index: 1;
}

// ==========================================
// 页面头部
// ==========================================
.page-header {
  grid-column: 1 / -1;
  background: linear-gradient(135deg, #0056d2 0%, #0070f3 100%);
  border-radius: 12px;
  padding: 40px;
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 4px 12px rgba(0, 86, 210, 0.3);

  .header-content {
    .page-title {
      font-size: 32px;
      font-weight: 700;
      margin-bottom: 8px;
    }

    .page-subtitle {
      font-size: 16px;
      opacity: 0.9;
    }
  }

  .header-stats {
    display: flex;
    gap: 40px;

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;

      .stat-number {
        font-size: 32px;
        font-weight: 700;
      }

      .stat-label {
        font-size: 14px;
        opacity: 0.8;
      }
    }
  }
}

// ==========================================
// 筛选区域
// ==========================================
.filter-section {
  grid-column: 1;
  display: flex;
  gap: 16px;
  align-items: center;
  padding: 20px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .search-box {
    flex: 1;
    max-width: 500px;
  }

  .sort-box {
    flex-shrink: 0;
  }
}

// ==========================================
// 热门话题区域
// ==========================================
.hot-tags-section {
  grid-column: 1;

  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 20px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 20px;
    padding: 0 4px;

    .el-icon {
      color: #0056d2;
    }
  }

  .hot-tags-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 16px;

    .hot-tag-card {
      background: #ffffff;
      border-radius: 12px;
      padding: 24px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      cursor: pointer;
      transition: all 0.3s ease;
      display: flex;
      flex-direction: column;
      align-items: center;
      text-align: center;

      &:hover {
        transform: translateY(-4px);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
      }

      .tag-icon {
        width: 64px;
        height: 64px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: linear-gradient(135deg, #0056d2 0%, #0070f3 100%);
        border-radius: 50%;
        margin-bottom: 16px;

        .el-icon {
          font-size: 32px;
          color: #ffffff;
        }
      }

      .tag-name {
        font-size: 18px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 8px;
      }

      .tag-description {
        font-size: 13px;
        color: #909399;
        margin-bottom: 16px;
        line-height: 1.5;
        display: -webkit-box;
        -webkit-box-orient: vertical;
        -webkit-line-clamp: 2;
        overflow: hidden;
        min-height: 40px;
      }

      .tag-stats {
        display: flex;
        gap: 16px;
        margin-bottom: 16px;

        .stat {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 13px;
          color: #606266;

          .el-icon {
            font-size: 16px;
          }
        }
      }

      .follow-btn {
        width: 100%;

        :deep(.el-button) {
          display: flex;
          align-items: center;
          gap: 6px;
        }
      }
    }
  }
}

// ==========================================
// 标签列表区域
// ==========================================
.tags-list-section {
  grid-column: 1;

  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 20px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 20px;
    padding: 0 4px;

    .el-icon {
      color: #0056d2;
    }
  }

  .loading-container {
    background: #ffffff;
    border-radius: 12px;
    padding: 24px;
  }

  .tags-grid {
    display: grid;
    grid-template-columns: 1fr;
    gap: 12px;

    .tag-card {
      background: #ffffff;
      border-radius: 12px;
      padding: 20px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
      cursor: pointer;
      transition: all 0.3s ease;

      &:hover {
        transform: translateX(4px);
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
      }

      .tag-header {
        display: flex;
        align-items: flex-start;
        justify-content: space-between;
        margin-bottom: 12px;

        .tag-info {
          flex: 1;

          .tag-name {
            font-size: 18px;
            font-weight: 600;
            color: #303133;
            margin-bottom: 8px;
          }

          .tag-description {
            font-size: 14px;
            color: #606266;
            line-height: 1.6;
            display: -webkit-box;
            -webkit-box-orient: vertical;
            -webkit-line-clamp: 2;
            overflow: hidden;
          }
        }

        :deep(.el-button) {
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }

      .tag-stats {
        display: flex;
        gap: 20px;

        .stat-item {
          display: flex;
          align-items: center;
          gap: 6px;
          font-size: 13px;
          color: #909399;

          .el-icon {
            font-size: 16px;
          }
        }
      }
    }
  }

  .pagination {
    margin-top: 24px;
    display: flex;
    justify-content: center;
  }
}

// ==========================================
// 右侧边栏
// ==========================================
.tags-sidebar {
  display: flex;
  flex-direction: column;
  gap: 16px;
  position: sticky;
  top: 88px;

  .sidebar-card {
    background: #ffffff;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    overflow: hidden;

    .card-header {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 16px 20px;
      border-bottom: 1px solid #e4e7ed;
      font-size: 15px;
      font-weight: 600;
      color: #303133;

      .el-icon {
        color: #0056d2;
      }
    }

    .card-content {
      padding: 16px 20px;
    }
  }

  .followed-tags {
    display: flex;
    flex-direction: column;
    gap: 8px;

    .followed-tag-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 10px 12px;
      background: #f5f7fa;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s ease;

      &:hover {
        background: #e6f0ff;
        transform: translateX(4px);
      }

      .tag-name {
        font-size: 14px;
        color: #303133;
        font-weight: 500;
      }

      .tag-count {
        font-size: 12px;
        color: #909399;
      }
    }

    .empty-text {
      text-align: center;
      font-size: 13px;
      color: #909399;
      padding: 20px 0;
    }
  }

  .category-list {
    display: flex;
    flex-direction: column;
    gap: 8px;

    .category-item {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 10px 12px;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s ease;
      color: #606266;

      &:hover {
        background: #f5f7fa;
      }

      &.active {
        background: #e6f0ff;
        color: #0056d2;

        .category-count {
          color: #0056d2;
        }
      }

      .el-icon {
        font-size: 18px;
      }

      span {
        flex: 1;
        font-size: 14px;
      }

      .category-count {
        font-size: 12px;
        color: #909399;
      }
    }
  }

  .recommended-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }
}

// ==========================================
// 空状态
// ==========================================
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .empty-icon {
    color: #dcdfe6;
    margin-bottom: 16px;
  }

  .empty-text {
    font-size: 14px;
    color: #909399;
    margin-bottom: 20px;
  }
}

// 热门话题空状态
.hot-tags-grid .empty-state {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);

  .empty-icon {
    color: #dcdfe6;
    margin-bottom: 16px;
  }

  .empty-text {
    font-size: 14px;
    color: #909399;
  }
}

// ==========================================
// 响应式设计
// ==========================================
@media (max-width: 1024px) {
  .tags-page {
    grid-template-columns: 1fr;
  }

  .page-header {
    grid-column: 1;
  }

  .tags-sidebar {
    display: none;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
    padding: 24px;

    .header-stats {
      width: 100%;
      justify-content: space-around;
    }
  }

  .filter-section {
    flex-direction: column;

    .search-box {
      width: 100%;
      max-width: none;
    }

    .sort-box {
      width: 100%;

      :deep(.el-radio-group) {
        width: 100%;
        display: flex;

        .el-radio-button {
          flex: 1;
        }
      }
    }
  }

  .hot-tags-grid {
    grid-template-columns: 1fr !important;
  }
}
</style>
