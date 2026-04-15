<template>
  <div class="my-points-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">
        我的积分
      </h1>
      <div class="points-overview">
        <div class="points-card main-card">
          <div class="card-icon">
            <el-icon :size="48">
              <TrophyBase />
            </el-icon>
          </div>
          <div class="card-content">
            <div class="points-value">
              {{ userPoints }}
            </div>
            <div class="points-label">
              当前积分
            </div>
          </div>
        </div>
        <div class="points-stats">
          <div class="stat-item">
            <div class="stat-icon">
              <el-icon><Plus /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">
                +{{ stats.todayEarned }}
              </div>
              <div class="stat-label">
                今日获得
              </div>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon">
              <el-icon><Minus /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">
                -{{ stats.todaySpent }}
              </div>
              <div class="stat-label">
                今日消耗
              </div>
            </div>
          </div>
          <div class="stat-item">
            <div class="stat-icon">
              <el-icon><TrendCharts /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">
                {{ stats.totalEarned }}
              </div>
              <div class="stat-label">
                累计获得
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 积分规则 -->
    <div class="points-rules">
      <div class="rules-header">
        <h2 class="rules-title">
          <el-icon><InfoFilled /></el-icon>
          <span>积分规则</span>
        </h2>
        <el-button
          text
          @click="rulesVisible = !rulesVisible"
        >
          {{ rulesVisible ? '收起' : '展开' }}
          <el-icon>
            <ArrowUp v-if="rulesVisible" />
            <ArrowDown v-else />
          </el-icon>
        </el-button>
      </div>
      <el-collapse-transition>
        <div
          v-show="rulesVisible"
          class="rules-content"
        >
          <div class="rules-grid">
            <div class="rule-card earn-rule">
              <h3 class="rule-title">
                <el-icon><CirclePlus /></el-icon>
                <span>获得积分</span>
              </h3>
              <ul class="rule-list">
                <li class="rule-item">
                  <span class="rule-action">每日登录</span>
                  <span class="rule-points">+5</span>
                </li>
                <li class="rule-item">
                  <span class="rule-action">发布问题</span>
                  <span class="rule-points">+10</span>
                </li>
                <li class="rule-item">
                  <span class="rule-action">发布回答</span>
                  <span class="rule-points">+15</span>
                </li>
                <li class="rule-item">
                  <span class="rule-action">回答被采纳</span>
                  <span class="rule-points">+50</span>
                </li>
                <li class="rule-item">
                  <span class="rule-action">回答被点赞</span>
                  <span class="rule-points">+2/次</span>
                </li>
                <li class="rule-item">
                  <span class="rule-action">问题被收藏</span>
                  <span class="rule-points">+3/次</span>
                </li>
                <li class="rule-item">
                  <span class="rule-action">完善个人资料</span>
                  <span class="rule-points">+20</span>
                </li>
              </ul>
            </div>
            <div class="rule-card spend-rule">
              <h3 class="rule-title">
                <el-icon><Remove /></el-icon>
                <span>消耗积分</span>
              </h3>
              <ul class="rule-list">
                <li class="rule-item">
                  <span class="rule-action">提问悬赏</span>
                  <span class="rule-points">自定义</span>
                </li>
                <li class="rule-item">
                  <span class="rule-action">置顶问题</span>
                  <span class="rule-points">-100/天</span>
                </li>
                <li class="rule-item">
                  <span class="rule-action">违规处罚</span>
                  <span class="rule-points">-50~-200</span>
                </li>
              </ul>
            </div>
          </div>
        </div>
      </el-collapse-transition>
    </div>

    <!-- 积分明细 -->
    <div class="points-records">
      <div class="records-header">
        <h2 class="records-title">
          积分明细
        </h2>
        <el-radio-group
          v-model="recordType"
          @change="handleTypeChange"
        >
          <el-radio-button value="all">
            全部
          </el-radio-button>
          <el-radio-button value="earn">
            获得
          </el-radio-button>
          <el-radio-button value="spend">
            消耗
          </el-radio-button>
        </el-radio-group>
      </div>

      <div
        v-loading="loading"
        class="records-container"
      >
        <div
          v-if="recordList.length > 0"
          class="records-list"
        >
          <div
            v-for="record in recordList"
            :key="record.id"
            class="record-item"
            :class="{ 'earn-item': record.points > 0, 'spend-item': record.points < 0 }"
          >
            <div class="record-icon">
              <el-icon v-if="record.points > 0">
                <CirclePlus />
              </el-icon>
              <el-icon v-else>
                <Remove />
              </el-icon>
            </div>
            <div class="record-content">
              <div class="record-desc">
                {{ record.description }}
              </div>
              <div class="record-time">
                {{ formatTime(record.createTime) }}
              </div>
            </div>
            <div
              class="record-points"
              :class="{ 'earn-points': record.points > 0, 'spend-points': record.points < 0 }"
            >
              {{ record.points > 0 ? '+' : '' }}{{ record.points }}
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
            <Coin />
          </el-icon>
          <p class="empty-text">
            暂无积分记录
          </p>
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
            :page-sizes="[20, 50, 100]"
            layout="total, sizes, prev, pager, next"
            @size-change="handleSizeChange"
            @current-change="handlePageChange"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import {
  TrophyBase,
  Plus,
  Minus,
  TrendCharts,
  InfoFilled,
  ArrowUp,
  ArrowDown,
  CirclePlus,
  Remove,
  Coin,
} from '@element-plus/icons-vue'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const userStore = useUserStore()

// 积分记录类型
interface PointRecord {
  id: number
  userId: number
  points: number
  description: string
  type: string
  createTime: string
}

// 用户积分
const userPoints = ref(0)

// 统计数据
const stats = ref({
  todayEarned: 0,
  todaySpent: 0,
  totalEarned: 0,
})

// 规则展开状态
const rulesVisible = ref(true)

// 记录类型
const recordType = ref('all')

// 积分记录列表
const recordList = ref<PointRecord[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

/**
 * 加载用户积分
 */
const loadUserPoints = async () => {
  try {
    // 从用户 store 获取积分
    const user = userStore.user
    if (user) {
      userPoints.value = user.points || 0
    }
  } catch (error) {
    console.error('加载用户积分失败：', error)
  }
}

/**
 * 加载积分统计
 */
const loadStats = async () => {
  try {
    // 模拟数据 - 实际应该调用 API
    stats.value = {
      todayEarned: 25,
      todaySpent: 0,
      totalEarned: 856,
    }
  } catch (error) {
    console.error('加载积分统计失败：', error)
  }
}

/**
 * 加载积分记录
 */
const loadRecords = async () => {
  loading.value = true
  try {
    // 模拟数据 - 实际应该调用 API
    // const result = await getPointRecords(currentPage.value, pageSize.value, recordType.value)

    // 模拟数据
    const mockData: PointRecord[] = [
      {
        id: 1,
        userId: 1,
        points: 15,
        description: '发布回答',
        type: 'answer',
        createTime: new Date().toISOString(),
      },
      {
        id: 2,
        userId: 1,
        points: 10,
        description: '发布问题',
        type: 'question',
        createTime: new Date(Date.now() - 3600000).toISOString(),
      },
      {
        id: 3,
        userId: 1,
        points: 50,
        description: '回答被采纳',
        type: 'accepted',
        createTime: new Date(Date.now() - 7200000).toISOString(),
      },
      {
        id: 4,
        userId: 1,
        points: 2,
        description: '回答获得点赞',
        type: 'like',
        createTime: new Date(Date.now() - 10800000).toISOString(),
      },
      {
        id: 5,
        userId: 1,
        points: -20,
        description: '提问悬赏',
        type: 'reward',
        createTime: new Date(Date.now() - 86400000).toISOString(),
      },
      {
        id: 6,
        userId: 1,
        points: 5,
        description: '每日登录',
        type: 'login',
        createTime: new Date(Date.now() - 90000000).toISOString(),
      },
      {
        id: 7,
        userId: 1,
        points: 20,
        description: '完善个人资料',
        type: 'profile',
        createTime: new Date(Date.now() - 172800000).toISOString(),
      },
      {
        id: 8,
        userId: 1,
        points: 3,
        description: '问题被收藏',
        type: 'collection',
        createTime: new Date(Date.now() - 259200000).toISOString(),
      },
    ]

    recordList.value = mockData
    total.value = mockData.length

  } catch (error) {
    console.error('加载积分记录失败：', error)
    ElMessage.error('加载积分记录失败')
  } finally {
    loading.value = false
  }
}

/**
 * 处理类型变化
 */
const handleTypeChange = () => {
  currentPage.value = 1
  loadRecords()
}

/**
 * 处理页码变化
 */
const handlePageChange = () => {
  loadRecords()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

/**
 * 处理每页大小变化
 */
const handleSizeChange = () => {
  currentPage.value = 1
  loadRecords()
}

/**
 * 格式化时间
 */
const formatTime = (time: string) => {
  return dayjs(time).fromNow()
}

onMounted(() => {
  loadUserPoints()
  loadStats()
  loadRecords()
})
</script>

<style lang="scss" scoped>
.my-points-page {
  width: 100%;
  min-height: calc(100vh - 64px);
}

.page-header {
  padding: 24px;
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 24px;

  .page-title {
    font-size: 28px;
    font-weight: 700;
    color: #303133;
    margin-bottom: 24px;
  }

  .points-overview {
    display: grid;
    grid-template-columns: 1fr 2fr;
    gap: 24px;

    .points-card {
      background: linear-gradient(135deg, #fad0c4 0%, #ffd1ff 100%);
      padding: 32px;
      border-radius: 16px;
      display: flex;
      align-items: center;
      gap: 20px;
      box-shadow: 0 4px 16px rgba(250, 208, 196, 0.4);

      .card-icon {
        width: 80px;
        height: 80px;
        border-radius: 50%;
        background: rgba(255, 255, 255, 0.3);
        backdrop-filter: blur(10px);
        display: flex;
        align-items: center;
        justify-content: center;
        color: #ffffff;
        font-size: 48px;
      }

      .card-content {
        flex: 1;

        .points-value {
          font-size: 48px;
          font-weight: 700;
          color: #ffffff;
          text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
          margin-bottom: 8px;
        }

        .points-label {
          font-size: 16px;
          color: rgba(255, 255, 255, 0.9);
        }
      }
    }

    .points-stats {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 16px;

      .stat-item {
        background: #f8f9fa;
        padding: 20px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        gap: 16px;
        transition: all 0.3s ease;

        &:hover {
          transform: translateY(-4px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
        }

        .stat-icon {
          width: 48px;
          height: 48px;
          border-radius: 12px;
          background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
          display: flex;
          align-items: center;
          justify-content: center;
          color: #ffffff;
          font-size: 24px;
        }

        .stat-content {
          flex: 1;

          .stat-value {
            font-size: 24px;
            font-weight: 700;
            color: #303133;
            margin-bottom: 4px;
          }

          .stat-label {
            font-size: 13px;
            color: #909399;
          }
        }
      }
    }
  }
}

.points-rules {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 24px;
  overflow: hidden;

  .rules-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20px 24px;
    border-bottom: 1px solid #f5f7fa;

    .rules-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 20px;
      font-weight: 600;
      color: #303133;

      .el-icon {
        font-size: 24px;
        color: #0056d2;
      }
    }

    :deep(.el-button) {
      display: flex;
      align-items: center;
      gap: 4px;
    }
  }

  .rules-content {
    padding: 24px;

    .rules-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 24px;

      .rule-card {
        padding: 20px;
        border-radius: 12px;
        border: 2px solid;

        &.earn-rule {
          border-color: #67c23a;
          background: linear-gradient(135deg, #f0fff4 0%, #e8f8ea 100%);
        }

        &.spend-rule {
          border-color: #f56c6c;
          background: linear-gradient(135deg, #fff5f5 0%, #fee 100%);
        }

        .rule-title {
          display: flex;
          align-items: center;
          gap: 8px;
          font-size: 18px;
          font-weight: 600;
          margin-bottom: 16px;

          .el-icon {
            font-size: 22px;
          }
        }

        &.earn-rule .rule-title {
          color: #67c23a;
        }

        &.spend-rule .rule-title {
          color: #f56c6c;
        }

        .rule-list {
          list-style: none;
          padding: 0;
          margin: 0;

          .rule-item {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 12px 0;
            border-bottom: 1px solid rgba(0, 0, 0, 0.05);

            &:last-child {
              border-bottom: none;
            }

            .rule-action {
              font-size: 14px;
              color: #606266;
            }

            .rule-points {
              font-size: 16px;
              font-weight: 600;
              padding: 4px 12px;
              border-radius: 6px;
            }
          }
        }

        &.earn-rule .rule-points {
          color: #67c23a;
          background: rgba(103, 194, 58, 0.1);
        }

        &.spend-rule .rule-points {
          color: #f56c6c;
          background: rgba(245, 108, 108, 0.1);
        }
      }
    }
  }
}

.points-records {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;

  .records-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 20px 24px;
    border-bottom: 1px solid #f5f7fa;

    .records-title {
      font-size: 20px;
      font-weight: 600;
      color: #303133;
    }
  }

  .records-container {
    min-height: 400px;

    .records-list {
      padding: 16px 24px;

      .record-item {
        display: flex;
        align-items: center;
        gap: 16px;
        padding: 16px;
        border-radius: 8px;
        margin-bottom: 12px;
        transition: all 0.3s ease;

        &:hover {
          background: #f8f9fa;
        }

        &.earn-item {
          border-left: 3px solid #67c23a;
        }

        &.spend-item {
          border-left: 3px solid #f56c6c;
        }

        .record-icon {
          width: 40px;
          height: 40px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 20px;
        }

        &.earn-item .record-icon {
          background: rgba(103, 194, 58, 0.1);
          color: #67c23a;
        }

        &.spend-item .record-icon {
          background: rgba(245, 108, 108, 0.1);
          color: #f56c6c;
        }

        .record-content {
          flex: 1;

          .record-desc {
            font-size: 15px;
            color: #303133;
            margin-bottom: 4px;
          }

          .record-time {
            font-size: 13px;
            color: #909399;
          }
        }

        .record-points {
          font-size: 24px;
          font-weight: 700;

          &.earn-points {
            color: #67c23a;
          }

          &.spend-points {
            color: #f56c6c;
          }
        }
      }
    }

    .empty-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 80px 20px;

      .empty-icon {
        color: #dcdfe6;
        margin-bottom: 16px;
      }

      .empty-text {
        font-size: 14px;
        color: #909399;
      }
    }

    .pagination {
      padding: 16px 24px;
      border-top: 1px solid #f5f7fa;
      display: flex;
      justify-content: center;
    }
  }
}

@media (max-width: 768px) {
  .page-header {
    .points-overview {
      grid-template-columns: 1fr;

      .points-card {
        .card-icon {
          width: 60px;
          height: 60px;
          font-size: 32px;
        }

        .card-content {
          .points-value {
            font-size: 36px;
          }
        }
      }

      .points-stats {
        grid-template-columns: 1fr;
      }
    }
  }

  .points-rules {
    .rules-content {
      .rules-grid {
        grid-template-columns: 1fr;
      }
    }
  }

  .points-records {
    .records-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;

      :deep(.el-radio-group) {
        width: 100%;
      }
    }

    .records-container {
      .records-list {
        padding: 12px;

        .record-item {
          .record-points {
            font-size: 20px;
          }
        }
      }
    }
  }
}
</style>
