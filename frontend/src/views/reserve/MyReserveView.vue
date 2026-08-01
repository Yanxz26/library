<template>
  <div class="my-reserve-container">
    <div class="status-tabs">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="待生效" name="pending">
          <template #label>
            <span>待生效</span>
            <el-badge v-if="stats.pendingCount > 0" :value="stats.pendingCount" />
          </template>
        </el-tab-pane>
        <el-tab-pane label="已完成" name="completed">
          <template #label>
            <span>已完成</span>
            <el-badge v-if="stats.completedCount > 0" :value="stats.completedCount" />
          </template>
        </el-tab-pane>
        <el-tab-pane label="已失效" name="expired">
          <template #label>
            <span>已失效</span>
            <el-badge v-if="stats.expiredCount > 0" :value="stats.expiredCount" type="danger" />
          </template>
        </el-tab-pane>
      </el-tabs>
    </div>

    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="3" animated />
    </div>

    <div v-else-if="reserveList.length > 0" class="reserve-list">
      <div v-for="item in reserveList" :key="item.id" class="reserve-card">
        <div class="card-left">
          <div class="book-icon">
            <el-icon :size="32" :color="getIconColor(item.reserveStatus || item.status)"><Clock /></el-icon>
          </div>
          <div class="book-info">
            <h3 class="book-name">{{ item.bookName }}</h3>
            <p class="book-author">{{ item.author }}</p>
            <div class="reserve-meta">
              <span class="meta-item">
                <el-icon :size="14"><Calendar /></el-icon>
                预约时间：{{ formatDate(item.reserveTime || item.createTime) }}
              </span>
              <span class="meta-item">
                <el-icon :size="14"><Timer /></el-icon>
                失效时间：{{ formatDate(item.expireTime) }}
              </span>
            </div>
            <div v-if="item.reserveStatus === 1 && isExpiringSoon(item.expireTime)" class="expiring-warning">
              <el-icon :size="14" color="#F59E0B"><Warning /></el-icon>
              <span>预约即将失效，请尽快到馆借阅</span>
            </div>
          </div>
        </div>
        <div class="card-right">
          <div class="status-badge">
            <el-tag :type="getStatusType(item.reserveStatus || item.status)" size="small">
              {{ getStatusText(item.reserveStatus || item.status) }}
            </el-tag>
          </div>
          <div class="actions">
            <template v-if="(item.reserveStatus || item.status) === 1">
              <el-button size="small" type="primary" @click="handleGoBorrow(item)">去借阅</el-button>
              <el-popconfirm title="确定取消该预约吗？" @confirm="handleCancel(item)">
                <template #reference>
                  <el-button size="small" type="danger">取消预约</el-button>
                </template>
              </el-popconfirm>
            </template>
            <template v-else-if="(item.reserveStatus || item.status) === 2">
              <el-button size="small" type="success">已借阅</el-button>
            </template>
            <template v-else>
              <span class="text-muted">已失效</span>
            </template>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <div class="empty-icon">
        <el-icon :size="64" color="#CBD5E1"><Clock /></el-icon>
      </div>
      <p>{{ emptyText }}</p>
      <el-button type="primary" @click="$router.push('/book-browse')">去预约</el-button>
    </div>

    <div v-if="reserveList.length > 0" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="queryForm.current"
        v-model:page-size="queryForm.size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @change="fetchData"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onBeforeMount, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Clock, Calendar, Timer, Warning } from '@element-plus/icons-vue'
import { getMyReserves, cancelReserve } from '@/api/reserve'

const activeTab = ref('pending')
const queryForm = reactive({ current: 1, size: 10 })
const reserveList = ref([])
const total = ref(0)
const loading = ref(true)

const stats = reactive({
  pendingCount: 0,
  completedCount: 0,
  expiredCount: 0,
})

const statusMap = {
  1: { text: '待生效', type: 'warning', color: '#F59E0B' },
  2: { text: '已完成', type: 'success', color: '#22C55E' },
  3: { text: '已失效', type: 'info', color: '#6B7280' },
}

function getStatusText(status) {
  return statusMap[status]?.text || '未知'
}

function getStatusType(status) {
  return statusMap[status]?.type || 'info'
}

function getIconColor(status) {
  return statusMap[status]?.color || '#6B7280'
}

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return dateStr.split(' ')[0]
}

function isExpiringSoon(expireTime) {
  if (!expireTime) return false
  const expire = new Date(expireTime)
  const now = new Date()
  const diffDays = Math.ceil((expire - now) / (1000 * 60 * 60 * 24))
  return diffDays >= 0 && diffDays <= 3
}

const emptyText = computed(() => {
  const texts = {
    pending: '暂无待生效的预约',
    completed: '暂无已完成的预约',
    expired: '暂无已失效的预约',
  }
  return texts[activeTab.value]
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getMyReserves(queryForm)
    if (res?.data) {
      const allRecords = res.data.records || res.data.list || []
      total.value = res.data.total || 0
      updateStats(allRecords)
      filterByStatus(allRecords)
    }
  } catch (err) {
    console.error('[MyReserveView] fetchData error:', err)
  } finally {
    loading.value = false
  }
}

function updateStats(data) {
  stats.pendingCount = data.filter(item => (item.reserveStatus || item.status) === 1).length
  stats.completedCount = data.filter(item => (item.reserveStatus || item.status) === 2).length
  stats.expiredCount = data.filter(item => (item.reserveStatus || item.status) === 3).length
}

function filterByStatus(data) {
  if (activeTab.value === 'pending') {
    reserveList.value = data.filter(item => (item.reserveStatus || item.status) === 1)
  } else if (activeTab.value === 'completed') {
    reserveList.value = data.filter(item => (item.reserveStatus || item.status) === 2)
  } else if (activeTab.value === 'expired') {
    reserveList.value = data.filter(item => (item.reserveStatus || item.status) === 3)
  }
}

function handleTabChange() {
  queryForm.current = 1
  fetchData()
}

function handleGoBorrow(item) {
  ElMessage.info(`请前往图书馆借阅《${item.bookName}》`)
  $router.push('/book-browse')
}

async function handleCancel(item) {
  try {
    await cancelReserve(item.id)
    ElMessage.success('预约已取消')
    fetchData()
  } catch {
    ElMessage.error('取消失败')
  }
}

onBeforeMount(() => { fetchData() })
</script>

<style scoped lang="scss">
.my-reserve-container {
  padding: 24px;
  max-width: 1000px;
  margin: 0 auto;
}

.status-tabs {
  margin-bottom: 24px;

  .el-tabs__content {
    display: none;
  }
}

.reserve-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.reserve-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #E5E7EB;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  transition: all 0.2s ease;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  }

  .card-left {
    display: flex;
    gap: 16px;

    .book-icon {
      width: 56px;
      height: 72px;
      background: linear-gradient(135deg, #FFFBEB, #FEF3C7);
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }

    .book-info {
      .book-name {
        font-size: 16px;
        font-weight: 600;
        color: #1F2937;
        margin-bottom: 4px;
      }

      .book-author {
        font-size: 13px;
        color: #9CA3AF;
        margin-bottom: 8px;
      }

      .reserve-meta {
        display: flex;
        flex-wrap: wrap;
        gap: 16px;
        font-size: 13px;
        color: #6B7280;
        margin-bottom: 8px;

        .meta-item {
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }

      .expiring-warning {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 13px;
        color: #F59E0B;
        padding: 6px 12px;
        background: #FFFBEB;
        border-radius: 4px;
        width: fit-content;
      }
    }
  }

  .card-right {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 12px;

    .actions {
      display: flex;
      gap: 8px;
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 80px 0;
  color: #9CA3AF;

  .empty-icon {
    width: 100px;
    height: 100px;
    background: #F9FAFB;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  p {
    margin-top: 16px;
    font-size: 16px;
  }

  .el-button {
    margin-top: 20px;
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

.text-muted {
  color: #9CA3AF;
  font-size: 13px;
}
</style>