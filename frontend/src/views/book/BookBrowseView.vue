<template>
  <div class="book-browse-container">
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索图书名称、作者、ISBN..."
        prefix-icon="Search"
        clearable
        @keyup.enter="handleSearch"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <div class="filter-bar">
      <el-select v-model="filterCategory" placeholder="全部分类" clearable style="width: 160px">
        <el-option v-for="cat in categoryOptions" :key="cat.id" :label="cat.label" :value="cat.id" />
      </el-select>
      <el-select v-model="filterStatus" placeholder="图书状态" clearable style="width: 120px">
        <el-option label="可借阅" :value="1" />
        <el-option label="已下架" :value="0" />
      </el-select>
      <el-select v-model="filterStock" placeholder="库存筛选" clearable style="width: 120px">
        <el-option label="有库存" :value="1" />
        <el-option label="无库存(可预约)" :value="0" />
      </el-select>
    </div>

    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="6" animated />
    </div>

    <div v-else class="book-grid">
      <div v-for="book in tableData" :key="book.id" class="book-card">
        <div class="book-cover">
          <img
            v-if="book.cover"
            :src="book.cover"
            :alt="book.bookName"
            class="cover-image"
            @error="(e) => handleCoverError(e, book)"
          />
          <div v-else class="cover-placeholder">
            <el-icon :size="48" color="#8B9DF5"><Reading /></el-icon>
          </div>
        </div>
        <div class="book-info">
          <h3 class="book-title">{{ book.bookName }}</h3>
          <p class="book-author">{{ book.author }}</p>
          <p class="book-publisher">{{ book.publisher }}</p>
          <div class="book-meta">
            <span class="meta-item">¥{{ book.price }}</span>
            <span :class="['meta-item', book.remainNum === 0 ? 'text-danger' : 'text-success']">
              {{ book.remainNum }}/{{ book.totalNum }}
            </span>
            <span class="meta-item">{{ book.location }}</span>
          </div>
        </div>
        <div class="book-actions">
          <el-button size="small" @click="handleDetail(book)">详情</el-button>
          <el-button
            v-if="book.status === 1 && book.remainNum > 0"
            size="small" type="primary" @click="handleBorrow(book)"
          >借阅</el-button>
          <el-button
            v-else-if="book.remainNum === 0"
            size="small" type="info" @click="handleReserve(book)"
          >预约</el-button>
          <el-button v-else size="small" disabled>已下架</el-button>
        </div>
      </div>
    </div>

    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="queryForm.current"
        v-model:page-size="queryForm.size"
        :total="total"
        :page-sizes="[12, 24, 36]"
        layout="total, sizes, prev, pager, next, jumper"
        @change="fetchData"
      />
    </div>

    <el-dialog v-model="detailVisible" title="图书详情" width="500px">
      <div v-if="detailData" class="detail-content">
        <div class="detail-header">
          <div class="detail-icon">
            <img
              v-if="detailData.cover"
              :src="detailData.cover"
              :alt="detailData.bookName"
              class="detail-cover-image"
              @error="(e) => handleDetailCoverError(e)"
            />
            <el-icon v-else :size="64" color="#5B6AF0"><Reading /></el-icon>
          </div>
          <div class="detail-title-area">
            <h2>{{ detailData.bookName }}</h2>
            <p>{{ detailData.author }} · {{ detailData.publisher }}</p>
          </div>
        </div>
        <div class="detail-info">
          <div class="info-row">
            <span class="info-label">ISBN</span>
            <span>{{ detailData.isbn }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">价格</span>
            <span class="price">¥{{ detailData.price }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">库存</span>
            <span :class="detailData.remainNum === 0 ? 'text-danger' : 'text-success'">
              剩余 {{ detailData.remainNum }} / {{ detailData.totalNum }}
            </span>
          </div>
          <div class="info-row">
            <span class="info-label">馆藏位置</span>
            <span>{{ detailData.location }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">状态</span>
            <el-tag :type="detailData.status === 1 ? 'success' : 'warning'">
              {{ detailData.status === 1 ? '上架中' : '已下架' }}
            </el-tag>
          </div>
          <div class="info-row full-width">
            <span class="info-label">简介</span>
            <p>{{ detailData.bookDesc || '暂无简介' }}</p>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="detailData?.status === 1 && detailData?.remainNum > 0"
          type="primary" @click="handleBorrowFromDetail"
        >立即借阅</el-button>
        <el-button
          v-else-if="detailData?.status === 1 && detailData?.remainNum === 0"
          type="success" @click="handleReserveFromDetail"
        >预约</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reserveDialogVisible" title="图书预约" width="400px">
      <p>确认预约图书「{{ reserveBookData?.bookName }}」？</p>
      <p style="color: #9CA3AF; font-size: 13px; margin-top: 8px">预约后请在有效期内到馆借阅</p>
      <template #footer>
        <el-button @click="reserveDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="reserveLoading" @click="confirmReserve">确认预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onBeforeMount, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Reading, Search } from '@element-plus/icons-vue'
import { getBookPage } from '@/api/book'
import { getCategoryTree } from '@/api/category'
import { borrowBook } from '@/api/borrow'
import { reserveBook } from '@/api/reserve'

const route = useRoute()

const searchKeyword = ref('')
const filterCategory = ref(null)
const filterStatus = ref(1)
const filterStock = ref(null)

const categoryTree = ref([])
const categoryOptions = computed(() => {
  const options = [{ id: null, label: '全部分类' }]
  const flatten = (list) => {
    list.forEach(cat => {
      options.push({ id: cat.id, label: cat.categoryName })
      if (cat.children) flatten(cat.children)
    })
  }
  flatten(categoryTree.value)
  return options
})

async function loadCategoryTree() {
  try { const res = await getCategoryTree(); if (res?.data) categoryTree.value = res.data }
  catch (err) { console.error('[BookBrowseView] category error:', err) }
}

const queryForm = reactive({ current: 1, size: 12 })
const tableData = ref([])
const total = ref(0)
const loading = ref(true)

async function fetchData() {
  loading.value = true
  try {
    const params = { ...queryForm }
    if (searchKeyword.value) {
      params.bookName = searchKeyword.value
    }
    if (filterCategory.value) {
      params.categoryId = filterCategory.value
    }
    if (filterStatus.value !== null) {
      params.status = filterStatus.value
    }
    if (filterStock.value !== null) {
      params.remainNum = filterStock.value
    }
    const res = await getBookPage(params)
    if (res?.data) { tableData.value = res.data.records || []; total.value = res.data.total || 0 }
  } catch (err) { console.error('[BookBrowseView] fetchData error:', err) }
  finally { loading.value = false }
}

function handleSearch() { queryForm.current = 1; fetchData() }
function handleReset() {
  searchKeyword.value = ''; filterCategory.value = null; filterStatus.value = 1; filterStock.value = null
  queryForm.current = 1; fetchData()
}

const detailVisible = ref(false)
const detailData = ref(null)

function handleDetail(book) { detailData.value = book; detailVisible.value = true }

function handleCoverError(e, book) {
  e.target.style.display = 'none'
  e.target.nextElementSibling?.classList.remove('hidden')
}

function handleDetailCoverError(e) {
  e.target.style.display = 'none'
}

async function handleBorrow(book) {
  try {
    await borrowBook(book.id)
    ElMessage.success(`成功借阅《${book.bookName}》`)
    fetchData()
  } catch {
    ElMessage.error('借阅失败')
  }
}

async function handleBorrowFromDetail() {
  if (!detailData.value) return
  await handleBorrow(detailData.value)
  detailVisible.value = false
}

const reserveDialogVisible = ref(false)
const reserveBookData = ref(null)
const reserveLoading = ref(false)

function handleReserve(book) { reserveBookData.value = book; reserveDialogVisible.value = true }
function handleReserveFromDetail() {
  if (!detailData.value) return
  reserveBookData.value = detailData.value
  detailVisible.value = false
  reserveDialogVisible.value = true
}

async function confirmReserve() {
  if (!reserveBookData.value) return
  reserveLoading.value = true
  try {
    await reserveBook(reserveBookData.value.id)
    ElMessage.success(`成功预约《${reserveBookData.value.bookName}》`)
    reserveDialogVisible.value = false
    fetchData()
  } catch {
    ElMessage.error('预约失败')
  } finally {
    reserveLoading.value = false
  }
}

onBeforeMount(() => {
  if (route.query.reserve === '1') {
    filterStock.value = 0
    filterStatus.value = 1
    ElMessage.info('已筛选无库存图书，可进行预约')
  }
  loadCategoryTree()
  fetchData()
})
</script>

<style scoped lang="scss">
.book-browse-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;

  .el-input {
    flex: 1;
    max-width: 500px;
  }
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.book-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.book-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  border: 1px solid #E5E7EB;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;

  &:hover {
    box-shadow: 0 8px 24px rgba(91, 106, 240, 0.12);
    transform: translateY(-2px);
  }

  .book-cover {
    width: 80px;
    height: 100px;
    background: linear-gradient(135deg, #F3F4F6, #E5E7EB);
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 16px;
    overflow: hidden;

    .cover-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .cover-placeholder {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 100%;
      height: 100%;

      &.hidden {
        display: none;
      }
    }
  }

  .book-info {
    flex: 1;

    .book-title {
      font-size: 16px;
      font-weight: 600;
      color: #1F2937;
      margin-bottom: 6px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .book-author {
      font-size: 14px;
      color: #4B5563;
      margin-bottom: 4px;
    }

    .book-publisher {
      font-size: 13px;
      color: #9CA3AF;
      margin-bottom: 12px;
    }

    .book-meta {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;

      .meta-item {
        font-size: 12px;
        padding: 4px 10px;
        background: #F3F4F6;
        border-radius: 4px;
        color: #6B7280;

        &.text-danger { color: #EF4444; background: #FEF2F2; }
        &.text-success { color: #22C55E; background: #F0FDF4; }
      }
    }
  }

  .book-actions {
    display: flex;
    gap: 8px;
    margin-top: 16px;
    justify-content: flex-end;
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

.detail-content {
  .detail-header {
    display: flex;
    gap: 20px;
    padding-bottom: 20px;
    border-bottom: 1px solid #E5E7EB;
    margin-bottom: 20px;

    .detail-icon {
      width: 80px;
      height: 100px;
      background: linear-gradient(135deg, #EEF2FF, #E0E7FF);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      overflow: hidden;

      .detail-cover-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }

    .detail-title-area {
      flex: 1;

      h2 {
        font-size: 20px;
        font-weight: 600;
        color: #1F2937;
        margin-bottom: 8px;
      }

      p {
        font-size: 14px;
        color: #6B7280;
      }
    }
  }

  .detail-info {
    .info-row {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      padding: 10px 0;
      border-bottom: 1px solid #F3F4F6;

      &.full-width {
        flex-direction: column;

        p {
          margin-top: 8px;
          color: #4B5563;
          line-height: 1.6;
        }
      }

      .info-label {
        font-size: 14px;
        color: #9CA3AF;
        width: 80px;
      }

      .price {
        font-size: 18px;
        font-weight: 600;
        color: #5B6AF0;
      }
    }
  }
}

.text-danger { color: #EF4444; }
.text-success { color: #22C55E; }
</style>