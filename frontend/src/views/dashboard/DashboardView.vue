<template>
  <div class="dashboard-container">
    <!-- 欢迎横幅 -->
    <div class="welcome-banner">
      <div>
        <div class="welcome-greeting">{{ greeting }}，{{ authStore.userInfo?.userName || '用户' }}！</div>
        <div class="welcome-subtitle">
          <span>{{ todayText }}</span>
        </div>
      </div>
      <div class="welcome-decoration">
        <el-icon :size="72" color="rgba(255,255,255,0.15)"><Reading /></el-icon>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div v-if="!loading && !loadError" class="stat-cards">
      <StatCard
        icon="Document"
        :icon-bg="'linear-gradient(135deg, #5B6AF0, #7B8AF5)'"
        label="图书总数"
        :value="bookStats.totalBooks || 0"
        :clickable="true"
        @click="$router.push('/book')"
      />
      <StatCard
        icon="User"
        :icon-bg="'linear-gradient(135deg, #22C55E, #4ADE80)'"
        label="注册用户"
        :value="userStats.totalUsers || 0"
        :clickable="true"
        @click="$router.push('/user')"
      />
      <StatCard
        icon="Clock"
        :icon-bg="'linear-gradient(135deg, #F59E0B, #FBBF24)'"
        label="借阅中"
        :value="bookStats.borrowingCount || 0"
        :clickable="true"
        @click="$router.push('/borrow')"
      />
      <StatCard
        icon="WarningFilled"
        :icon-bg="'linear-gradient(135deg, #EF4444, #F87171)'"
        label="逾期未还"
        :value="bookStats.overdueCount || 0"
        :clickable="true"
        @click="$router.push('/overdue')"
      />
    </div>

    <!-- 骨架屏 -->
    <div v-if="loading" class="stat-cards">
      <SkeletonCard v-for="i in 4" :key="'sk'+i" />
    </div>

    <!-- 错误状态 -->
    <ErrorResult v-if="loadError" @retry="loadData" />

    <!-- 快捷操作 -->
    <div v-if="!loading && !loadError" class="quick-actions">
      <button v-if="authStore.hasRole(['admin', 'library'])" class="quick-action-btn" @click="$router.push('/book')">
        <el-icon><Plus /></el-icon> 新增图书
      </button>
      <button class="quick-action-btn" @click="$router.push('/borrow')">
        <el-icon><Document /></el-icon> 借阅登记
      </button>
      <button class="quick-action-btn" @click="$router.push('/borrow')">
        <el-icon><CircleCheck /></el-icon> 归还登记
      </button>
      <button v-if="authStore.hasRole(['admin', 'library'])" class="quick-action-btn" @click="$router.push('/inventory')">
        <el-icon><List /></el-icon> 库存盘点
      </button>
    </div>

    <!-- 图表区域 -->
    <div v-if="!loading && !loadError" class="chart-row">
      <div class="chart-card">
        <div class="chart-title">借阅趋势（近7天）</div>
        <div class="chart-body">
          <ErrorResult v-if="chartErrors.borrow" title="借阅趋势加载失败" @retry="loadBorrowTrend" :retrying="retrying.borrow" />
          <SkeletonCard v-else-if="retrying.borrow" />
          <v-chart v-else :option="borrowTrendOption" autoresize />
        </div>
      </div>
      <div class="chart-card">
        <div class="chart-title">热门图书 TOP10</div>
        <div class="chart-body">
          <ErrorResult v-if="chartErrors.hot" title="热门图书加载失败" @retry="loadHotBooks" :retrying="retrying.hot" />
          <SkeletonCard v-else-if="retrying.hot" />
          <v-chart v-else :option="hotBooksOption" autoresize />
        </div>
      </div>
    </div>

    <div v-if="!loading && !loadError" class="chart-row">
      <div class="chart-card">
        <div class="chart-title">用户类型分布</div>
        <div class="chart-body">
          <v-chart :option="userTypeOption" autoresize />
        </div>
      </div>
      <div class="chart-card">
        <div class="chart-title">图书分类统计</div>
        <div class="chart-body">
          <ErrorResult v-if="chartErrors.category" title="分类统计加载失败" @retry="loadCategoryStats" :retrying="retrying.category" />
          <v-chart v-else :option="categoryStatsOption" autoresize />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onBeforeMount } from 'vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
} from 'echarts/components'
import { useAuthStore } from '@/stores/auth'
import { getBookStatistics, getUserStatistics, getBorrowStatistics, getHotBooksTop10 } from '@/api/statistics'
import StatCard from '@/components/StatCard.vue'
import SkeletonCard from '@/components/SkeletonCard.vue'
import ErrorResult from '@/components/ErrorResult.vue'

use([CanvasRenderer, BarChart, LineChart, PieChart, TitleComponent, TooltipComponent, LegendComponent, GridComponent])

const authStore = useAuthStore()

// ========== 欢迎语 ==========
const hours = new Date().getHours()
const greeting = computed(() => {
  if (hours < 6) return '夜深了'
  if (hours < 9) return '早上好'
  if (hours < 12) return '上午好'
  if (hours < 14) return '中午好'
  if (hours < 18) return '下午好'
  return '晚上好'
})

const todayText = computed(() => {
  const now = new Date()
  const weekDays = ['日', '一', '二', '三', '四', '五', '六']
  const y = now.getFullYear()
  const m = now.getMonth() + 1
  const d = now.getDate()
  const w = weekDays[now.getDay()]
  return `今天是 ${y}年${m}月${d}日 星期${w}，系统运行正常。`
})

// ========== 数据状态 ==========
const loading = ref(true)
const loadError = ref(false)

const bookStats = reactive({
  totalBooks: 0,
  borrowingCount: 0,
  overdueCount: 0,
  categoryStats: [],
})
const userStats = reactive({
  totalUsers: 0,
  studentCount: 0,
  teacherCount: 0,
  adminCount: 0,
})
const borrowTrendData = ref([])
const hotBooksData = ref([])
const categoryStatsData = ref([])

const retrying = reactive({ borrow: false, hot: false, category: false })
const chartErrors = reactive({ borrow: false, hot: false, category: false })

// ========== 图表配置 ==========
const borrowTrendOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category',
    data: borrowTrendData.value.map(d => d.date || d.name || ''),
    axisLine: { lineStyle: { color: '#E3E6F0' } },
  },
  yAxis: {
    type: 'value', minInterval: 1,
    splitLine: { lineStyle: { color: '#F1F3F9' } },
  },
  series: [{
    name: '借阅量',
    type: 'line',
    data: borrowTrendData.value.map(d => d.count || d.value || 0),
    smooth: true,
    areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(91,106,240,0.25)' }, { offset: 1, color: 'rgba(91,106,240,0.02)' }] } },
    itemStyle: { color: '#5B6AF0' },
    lineStyle: { width: 2 },
  }],
}))

const hotBooksOption = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: '3%', right: '8%', bottom: '3%', containLabel: true },
  xAxis: { type: 'value', minInterval: 1, splitLine: { lineStyle: { color: '#F1F3F9' } } },
  yAxis: {
    type: 'category',
    data: hotBooksData.value.map(d => d.bookName || d.name || '').reverse(),
    axisLabel: { width: 120, overflow: 'truncate' },
  },
  series: [{
    name: '借阅次数',
    type: 'bar',
    data: hotBooksData.value.map(d => d.borrowCount || d.count || d.value || 0).reverse(),
    itemStyle: {
      borderRadius: [0, 4, 4, 0],
      color: { type: 'linear', x: 0, y: 0, x2: 1, y2: 0, colorStops: [{ offset: 0, color: '#5B6AF0' }, { offset: 1, color: '#22C55E' }] },
    },
  }],
}))

const userTypeOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: '0%', textStyle: { color: '#717894' } },
  series: [{
    name: '用户类型',
    type: 'pie',
    radius: ['50%', '75%'],
    avoidLabelOverlap: false,
    itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 3 },
    label: { show: true, formatter: '{b}\n{c}人', color: '#48506B' },
    data: [
      { value: userStats.studentCount || 0, name: '学生', itemStyle: { color: '#22C55E' } },
      { value: userStats.teacherCount || 0, name: '教师', itemStyle: { color: '#F59E0B' } },
      { value: userStats.adminCount || 0, name: '管理员', itemStyle: { color: '#5B6AF0' } },
    ].filter(d => d.value > 0),
  }],
}))

const categoryStatsOption = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: '3%', right: '4%', bottom: '8%', containLabel: true },
  xAxis: {
    type: 'category',
    data: categoryStatsData.value.map(d => d.categoryName || d.name || ''),
    axisLabel: { rotate: 30, fontSize: 11, color: '#717894' },
  },
  yAxis: { type: 'value', minInterval: 1, splitLine: { lineStyle: { color: '#F1F3F9' } } },
  series: [{
    name: '图书数量',
    type: 'bar',
    data: categoryStatsData.value.map(d => d.bookCount || d.count || d.value || 0),
    itemStyle: { color: '#22C55E', borderRadius: [4, 4, 0, 0] },
  }],
}))

// ========== 独立加载函数（用于错误重试） ==========
async function loadBorrowTrend() {
  retrying.borrow = true
  chartErrors.borrow = false
  try {
    const res = await getBorrowStatistics({ statType: 'day', startDate: getDateBefore(7), endDate: getToday() })
    borrowTrendData.value = Array.isArray(res?.data) ? res.data : res?.data?.list || res?.data?.records || []
  } catch { chartErrors.borrow = true } finally { retrying.borrow = false }
}

async function loadHotBooks() {
  retrying.hot = true
  chartErrors.hot = false
  try {
    const res = await getHotBooksTop10()
    hotBooksData.value = Array.isArray(res?.data) ? res.data : res?.data?.list || res?.data?.records || []
  } catch { chartErrors.hot = true } finally { retrying.hot = false }
}

async function loadCategoryStats() {
  retrying.category = true
  chartErrors.category = false
  try {
    const res = await getBookStatistics()
    if (res?.data?.categoryStats) {
      categoryStatsData.value = res.data.categoryStats
    }
  } catch { chartErrors.category = true } finally { retrying.category = false }
}

// ========== 主加载函数（并行） ==========
async function loadData() {
  loading.value = true
  loadError.value = false
  Object.keys(chartErrors).forEach(k => chartErrors[k] = false)

  const [bookR, userR, borrowR, hotR] = await Promise.allSettled([
    getBookStatistics(),
    getUserStatistics(),
    getBorrowStatistics({ statType: 'day', startDate: getDateBefore(7), endDate: getToday() }),
    getHotBooksTop10(),
  ])

  // 图书统计
  if (bookR.status === 'fulfilled' && bookR.value?.data) {
    Object.assign(bookStats, bookR.value.data)
    if (bookR.value.data.categoryStats) {
      categoryStatsData.value = bookR.value.data.categoryStats
    }
  } else {
    chartErrors.category = true
  }

  // 用户统计
  if (userR.status === 'fulfilled' && userR.value?.data) {
    Object.assign(userStats, userR.value.data)
  }

  // 借阅趋势
  if (borrowR.status === 'fulfilled' && borrowR.value?.data) {
    const d = borrowR.value.data
    borrowTrendData.value = Array.isArray(d) ? d : d?.list || d?.records || []
  } else {
    chartErrors.borrow = true
  }

  // 热门图书
  if (hotR.status === 'fulfilled' && hotR.value?.data) {
    const d = hotR.value.data
    hotBooksData.value = Array.isArray(d) ? d : d?.list || d?.records || []
  } else {
    chartErrors.hot = true
  }

  // 如果全部失败
  if ([bookR, userR, borrowR, hotR].every(r => r.status === 'rejected')) {
    loadError.value = true
  }

  loading.value = false
}

function getDateBefore(days) {
  const d = new Date()
  d.setDate(d.getDate() - days)
  return d.toISOString().slice(0, 10)
}

function getToday() {
  return new Date().toISOString().slice(0, 10)
}

onBeforeMount(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.dashboard-container {
  // 使用全局样式中的 .stat-cards, .chart-row, .chart-card,
  // .welcome-banner, .quick-actions, .quick-action-btn
}
</style>
