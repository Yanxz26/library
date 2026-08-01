<template>
  <div class="my-borrow-container">
    <div class="status-tabs">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="借阅中" name="borrowing">
          <template #label>
            <span>借阅中</span>
            <el-badge v-if="stats.borrowingCount > 0" :value="stats.borrowingCount" />
          </template>
        </el-tab-pane>
        <el-tab-pane label="已归还" name="returned">
          <template #label>
            <span>已归还</span>
            <el-badge v-if="stats.returnedCount > 0" :value="stats.returnedCount" />
          </template>
        </el-tab-pane>
        <el-tab-pane label="已逾期" name="overdue">
          <template #label>
            <span>已逾期</span>
            <el-badge v-if="stats.overdueCount > 0" :value="stats.overdueCount" type="danger" />
          </template>
        </el-tab-pane>
      </el-tabs>
    </div>

    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="3" animated />
    </div>

    <div v-else-if="borrowList.length > 0" class="borrow-list">
      <div v-for="item in borrowList" :key="item.id" class="borrow-card">
        <div class="card-left">
          <div class="book-icon">
            <el-icon :size="32" :color="getIconColor(item.borrowStatus)"><Reading /></el-icon>
          </div>
          <div class="book-info">
            <h3 class="book-name">{{ item.bookName || '未知图书' }}</h3>
            <p class="book-author">{{ item.author || '-' }} · {{ item.publisher || '-' }}</p>
            <div class="borrow-meta">
              <span class="meta-item">
                <el-icon :size="14"><Calendar /></el-icon>
                借阅时间：{{ formatDate(item.borrowTime) }}
              </span>
              <span class="meta-item">
                <el-icon :size="14"><Timer /></el-icon>
                到期时间：{{ formatDate(item.expireTime) }}
              </span>
            </div>
            <div v-if="item.returnTime" class="return-info">
              <el-icon :size="14" color="#22C55E"><CircleCheck /></el-icon>
              <span>归还时间：{{ formatDate(item.returnTime) }}</span>
            </div>
            <div v-if="item.borrowStatus === 1 && isExpiringSoon(item.expireTime)" class="expiring-warning">
              <el-icon :size="14" color="#F59E0B"><Warning /></el-icon>
              <span>即将到期，请尽快归还</span>
            </div>
            <div v-if="item.borrowStatus === 1 && item.renewCount > 0" class="renew-info">
              <el-icon :size="14" color="#5B6AF0"><RefreshLeft /></el-icon>
              <span>已续借{{ item.renewCount }}次</span>
            </div>
          </div>
        </div>
        <div class="card-right">
          <div class="status-badge">
            <el-tag :type="getStatusType(item.borrowStatus)" size="small">
              {{ getStatusText(item.borrowStatus) }}
            </el-tag>
          </div>
          <div class="actions">
            <template v-if="item.borrowStatus === 1">
              <el-button size="small" type="success" @click="handleRenew(item)">续借</el-button>
              <el-button size="small" type="primary" @click="handleReturn(item)">归还</el-button>
            </template>
            <template v-else-if="item.borrowStatus === 3">
              <el-button size="small" type="danger" @click="handleReturn(item)">立即归还</el-button>
            </template>
            <template v-else>
              <span class="text-muted">已完成</span>
            </template>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <div class="empty-icon">
        <el-icon :size="64" color="#CBD5E1"><Document /></el-icon>
      </div>
      <p>{{ emptyText }}</p>
      <el-button type="primary" @click="$router.push('/book-browse')">去借阅</el-button>
    </div>

    <div v-if="borrowList.length > 0" class="pagination-wrapper">
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
import { Reading, Document, Calendar, Timer, CircleCheck, Warning, RefreshLeft } from '@element-plus/icons-vue'
import { getMyBorrows, returnBook, renewBook } from '@/api/borrow'

const activeTab = ref('borrowing')
const queryForm = reactive({ current: 1, size: 10 })
const borrowList = ref([])
const total = ref(0)
const loading = ref(true)

const stats = reactive({
  borrowingCount: 0,
  returnedCount: 0,
  overdueCount: 0,
})

const statusMap = {
  1: { text: '借阅中', type: 'warning', color: '#F59E0B' },
  2: { text: '已归还', type: 'success', color: '#22C55E' },
  3: { text: '已逾期', type: 'danger', color: '#EF4444' },
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
    borrowing: '暂无借阅中的图书',
    returned: '暂无已归还的图书',
    overdue: '暂无逾期图书',
  }
  return texts[activeTab.value]
})

async function fetchData() {
  loading.value = true
  try {
    const params = { ...queryForm }
    if (activeTab.value === 'borrowing') {
      params.borrowStatus = 1
    } else if (activeTab.value === 'returned') {
      params.borrowStatus = 2
    } else if (activeTab.value === 'overdue') {
      params.borrowStatus = 3
    }
    const res = await getMyBorrows(params)
    if (res?.data) {
      const allRecords = res.data.records || res.data.list || []
      total.value = res.data.total || 0
      updateStats(allRecords)
      filterByStatus(allRecords)
    }
  } catch (err) {
    console.error('[MyBorrowView] fetchData error:', err)
  } finally {
    loading.value = false
  }
}

function updateStats(data) {
  stats.borrowingCount = data.filter(item => item.borrowStatus === 1).length
  stats.returnedCount = data.filter(item => item.borrowStatus === 2).length
  stats.overdueCount = data.filter(item => item.borrowStatus === 3).length
}

function filterByStatus(data) {
  if (activeTab.value === 'borrowing') {
    borrowList.value = data.filter(item => item.borrowStatus === 1)
  } else if (activeTab.value === 'returned') {
    borrowList.value = data.filter(item => item.borrowStatus === 2)
  } else if (activeTab.value === 'overdue') {
    borrowList.value = data.filter(item => item.borrowStatus === 3)
  }
}

function handleTabChange() {
  queryForm.current = 1
  fetchData()
}

async function handleReturn(item) {
  try {
    await returnBook(item.id)
    ElMessage.success(`成功归还《${item.bookName || '图书'}》`)
    fetchData()
  } catch {
    ElMessage.error('归还失败')
  }
}

async function handleRenew(item) {
  try {
    await renewBook(item.id)
    ElMessage.success(`成功续借《${item.bookName || '图书'}》`)
    fetchData()
  } catch {
    ElMessage.error('续借失败')
  }
}

onBeforeMount(() => { fetchData() })
</script>

<style scoped lang="scss">
.my-borrow-container {
  padding: 24px;
  max-width: 1000px;
  margin: 0 auto;
}

.loading-state {
  padding: 24px;
}

.status-tabs {
  margin-bottom: 24px;

  .el-tabs__content {
    display: none;
  }
}

.borrow-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.borrow-card {
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
      background: linear-gradient(135deg, #EEF2FF, #E0E7FF);
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

      .borrow-meta {
        display: flex;
        flex-wrap: wrap;
        gap: 16px;
        font-size: 13px;
        color: #6B7280;
        margin-bottom: 6px;

        .meta-item {
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }

      .return-info {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 13px;
        color: #22C55E;
        margin-bottom: 6px;
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
        margin-top: 8px;
      }

      .renew-info {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 13px;
        color: #5B6AF0;
        margin-top: 4px;
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