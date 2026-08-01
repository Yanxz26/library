<template>
  <div class="dashboard-container">
    <div class="welcome-banner">
      <div>
        <div class="welcome-greeting">{{ greeting }}，{{ authStore.userInfo?.userName || '老师' }}！</div>
        <div class="welcome-subtitle">
          <span>{{ todayText }}</span>
          <span class="role-tag">教师</span>
        </div>
      </div>
      <div class="welcome-decoration">
        <el-icon :size="72" color="rgba(255,255,255,0.15)"><Reading /></el-icon>
      </div>
    </div>

    <div v-if="!loading" class="stat-cards">
      <StatCard
        icon="User"
        :icon-bg="'linear-gradient(135deg, #5B6AF0, #7B8AF5)'"
        label="已借阅"
        :value="myStats.borrowingCount || 0"
        :sub-text="'/ ' + (myStats.maxBorrow || 15) + ' 本'"
        :clickable="true"
        @click="$router.push('/my-borrow')"
      />
      <StatCard
        icon="Clock"
        :icon-bg="'linear-gradient(135deg, #F59E0B, #FBBF24)'"
        label="即将到期"
        :value="myStats.expiringCount || 0"
        :sub-text="'近7天内'"
        :clickable="true"
        @click="$router.push('/my-borrow')"
      />
      <StatCard
        icon="WarningFilled"
        :icon-bg="'linear-gradient(135deg, #EF4444, #F87171)'"
        label="逾期未还"
        :value="myStats.overdueCount || 0"
        :sub-text="'请尽快归还'"
        :clickable="true"
        @click="$router.push('/my-borrow')"
      />
      <StatCard
        icon="Document"
        :icon-bg="'linear-gradient(135deg, #22C55E, #4ADE80)'"
        label="当前预约"
        :value="myStats.reserveCount || 0"
        :sub-text="'待借阅'"
        :clickable="true"
        @click="$router.push('/my-reserve')"
      />
    </div>

    <div v-if="loading" class="stat-cards">
      <SkeletonCard v-for="i in 4" :key="'sk'+i" />
    </div>

    <div class="quick-actions">
      <button class="quick-action-btn" @click="$router.push('/book-browse')">
        <el-icon><Document /></el-icon> 借阅图书
      </button>
      <button class="quick-action-btn" @click="$router.push('/my-borrow')">
        <el-icon><CircleCheck /></el-icon> 归还图书
      </button>
      <button class="quick-action-btn" @click="$router.push('/book-browse?reserve=1')">
        <el-icon><Clock /></el-icon> 预约图书
      </button>
      <button class="quick-action-btn" @click="$router.push('/profile')">
        <el-icon><UserFilled /></el-icon> 个人中心
      </button>
    </div>

    <div class="content-row">
      <div class="content-card">
        <div class="card-header">
          <div class="card-title">我的借阅</div>
          <el-button type="text" @click="$router.push('/my-borrow')">查看全部</el-button>
        </div>
        <div v-if="myBorrows.length > 0" class="borrow-list">
          <div v-for="item in myBorrows" :key="item.id" class="borrow-item">
            <div class="borrow-info">
              <div class="borrow-book-name">{{ item.bookName }}</div>
              <div class="borrow-meta">
                <span>借阅时间：{{ formatDate(item.borrowTime) }}</span>
                <span>到期时间：{{ formatDate(item.expireTime) }}</span>
              </div>
            </div>
            <div class="borrow-status">
              <el-tag :type="getBorrowStatusType(item.borrowStatus)">
                {{ getBorrowStatusText(item.borrowStatus) }}
              </el-tag>
              <el-button v-if="item.borrowStatus === 1" size="small" type="primary" @click="handleReturn(item.id)">
                归还
              </el-button>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">
          <el-icon :size="48" color="#E5E7EB"><Document /></el-icon>
          <p>暂无借阅记录</p>
          <el-button type="primary" @click="$router.push('/book-browse')">去借阅</el-button>
        </div>
      </div>

      <div class="content-card">
        <div class="card-header">
          <div class="card-title">教师推荐图书</div>
          <el-button type="text" @click="$router.push('/book-browse')">查看更多</el-button>
        </div>
        <div v-if="hotBooks.length > 0" class="book-list">
          <div v-for="book in hotBooks" :key="book.id" class="book-item">
            <div class="book-cover">
              <el-icon :size="32" color="#CBD5E1"><Reading /></el-icon>
            </div>
            <div class="book-info">
              <div class="book-name">{{ book.bookName }}</div>
              <div class="book-author">{{ book.author }}</div>
              <div class="book-meta">
                <span>库存：{{ book.remainNum }}/{{ book.totalNum }}</span>
                <span>借阅：{{ book.borrowCount || 0 }}次</span>
              </div>
            </div>
            <el-button size="small" @click="handleBorrow(book.id)">借阅</el-button>
          </div>
        </div>
        <div v-else class="empty-state">
          <el-icon :size="48" color="#E5E7EB"><BookOpen /></el-icon>
          <p>暂无推荐图书</p>
        </div>
      </div>
    </div>

    <div class="teacher-tips">
      <div class="tips-header">
        <el-icon><Reading /></el-icon>
        <span>教师借阅须知</span>
      </div>
      <ul class="tips-list">
        <li>教师最大可借阅数量为15本，借阅期限为60天</li>
        <li>可通过预约功能预订暂时无库存的图书</li>
        <li>逾期图书将影响后续借阅权限，请按时归还</li>
        <li>如需延长借阅期限，请联系图书馆管理员</li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onBeforeMount } from 'vue'
import { ElMessage } from 'element-plus'
import { Reading, Document, Clock, WarningFilled, UserFilled, CircleCheck } from '@element-plus/icons-vue'
import StatCard from '@/components/StatCard.vue'
import SkeletonCard from '@/components/SkeletonCard.vue'
import { useAuthStore } from '@/stores/auth'
import { getMyBorrows, borrowBook, returnBook } from '@/api/borrow'
import { getHotBooks } from '@/api/book'
import { getMyStatistics } from '@/api/statistics'

const authStore = useAuthStore()

const loading = ref(true)
const myBorrows = ref([])
const hotBooks = ref([])
const myStats = reactive({
  borrowingCount: 0,
  overdueCount: 0,
  expiringCount: 0,
  reserveCount: 0,
  maxBorrow: 15,
})

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '早上好'
  if (hour < 14) return '中午好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const todayText = computed(() => {
  const now = new Date()
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日 ${weekDays[now.getDay()]}`
})

function formatDate(dateStr) {
  if (!dateStr) return '-'
  return dateStr.split(' ')[0]
}

function getBorrowStatusType(status) {
  const types = { 1: 'warning', 2: 'success', 3: 'danger' }
  return types[status] || 'info'
}

function getBorrowStatusText(status) {
  const texts = { 1: '借阅中', 2: '已归还', 3: '已逾期' }
  return texts[status] || '未知'
}

async function handleBorrow(bookId) {
  try {
    await borrowBook(bookId)
    ElMessage.success('借阅成功')
    loadData()
  } catch {
    ElMessage.error('借阅失败')
  }
}

async function handleReturn(borrowId) {
  try {
    await returnBook(borrowId)
    ElMessage.success('归还成功')
    loadData()
  } catch {
    ElMessage.error('归还失败')
  }
}

async function loadData() {
  loading.value = true

  const [borrowR, hotR, statsR] = await Promise.allSettled([
    getMyBorrows({ page: 1, size: 5 }),
    getHotBooks(),
    getMyStatistics(),
  ])

  if (borrowR.status === 'fulfilled' && borrowR.value?.data) {
    const d = borrowR.value.data
    myBorrows.value = Array.isArray(d) ? d : d?.list || d?.records || []
  }

  if (hotR.status === 'fulfilled' && hotR.value?.data) {
    const d = hotR.value.data
    hotBooks.value = Array.isArray(d) ? d : d?.list || d?.records || []
  }

  if (statsR.status === 'fulfilled' && statsR.value?.data) {
    const d = statsR.value.data
    myStats.borrowingCount = d.borrowingCount || d.currentBorrowing || 0
    myStats.overdueCount = d.overdueCount || d.currentOverdue || 0
    myStats.expiringCount = d.expiringCount || 0
    myStats.reserveCount = d.reserveCount || 0
  }

  loading.value = false
}

onBeforeMount(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.dashboard-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.welcome-banner {
  background: linear-gradient(135deg, #1B2838 0%, #2A475E 40%, #3D6D8F 100%);
  border-radius: 16px;
  padding: 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  position: relative;
  overflow: hidden;

  .welcome-greeting {
    font-size: 28px;
    font-weight: 700;
    color: #fff;
    margin-bottom: 8px;
  }

  .welcome-subtitle {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.6);
    display: flex;
    align-items: center;
    gap: 12px;

    .role-tag {
      background: rgba(255, 255, 255, 0.2);
      padding: 4px 12px;
      border-radius: 12px;
      font-size: 12px;
    }
  }

  .welcome-decoration {
    position: absolute;
    right: 40px;
    bottom: 0;
  }
}

.stat-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;

  @media (max-width: 1000px) {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (max-width: 600px) {
    grid-template-columns: 1fr;
  }
}

.quick-actions {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;

  .quick-action-btn {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 12px 24px;
    background: #fff;
    border: 1px solid #E5E7EB;
    border-radius: 8px;
    font-size: 14px;
    color: #374151;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background: #F3F4F6;
      border-color: #3D6D8F;
      color: #3D6D8F;
    }
  }
}

.content-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;

  @media (max-width: 1000px) {
    grid-template-columns: 1fr;
  }
}

.content-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid #E5E7EB;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .card-title {
      font-size: 18px;
      font-weight: 600;
      color: #1F2937;
    }
  }
}

.borrow-list {
  .borrow-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    border-bottom: 1px solid #F3F4F6;

    &:last-child {
      border-bottom: none;
    }

    .borrow-info {
      flex: 1;

      .borrow-book-name {
        font-size: 15px;
        font-weight: 600;
        color: #1F2937;
        margin-bottom: 8px;
      }

      .borrow-meta {
        display: flex;
        gap: 16px;
        font-size: 13px;
        color: #9CA3AF;
      }
    }

    .borrow-status {
      display: flex;
      align-items: center;
      gap: 12px;
    }
  }
}

.book-list {
  .book-item {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 16px;
    border-bottom: 1px solid #F3F4F6;

    &:last-child {
      border-bottom: none;
    }

    .book-cover {
      width: 48px;
      height: 64px;
      background: #F3F4F6;
      border-radius: 6px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .book-info {
      flex: 1;

      .book-name {
        font-size: 15px;
        font-weight: 600;
        color: #1F2937;
        margin-bottom: 4px;
      }

      .book-author {
        font-size: 13px;
        color: #6B7280;
        margin-bottom: 4px;
      }

      .book-meta {
        display: flex;
        gap: 16px;
        font-size: 12px;
        color: #9CA3AF;
      }
    }
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 0;
  color: #9CA3AF;

  p {
    margin-top: 12px;
    font-size: 14px;
  }

  .el-button {
    margin-top: 16px;
  }
}

.teacher-tips {
  background: linear-gradient(135deg, #FFFBEB 0%, #FEF3C7 100%);
  border-radius: 12px;
  padding: 24px;
  margin-top: 24px;
  border: 1px solid #FDE68A;

  .tips-header {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: #92400E;
    margin-bottom: 16px;
  }

  .tips-list {
    list-style: none;
    padding: 0;
    margin: 0;

    li {
      padding: 8px 0;
      font-size: 14px;
      color: #B45309;
      position: relative;
      padding-left: 20px;

      &::before {
        content: '•';
        position: absolute;
        left: 0;
        color: #F59E0B;
      }
    }
  }
}
</style>