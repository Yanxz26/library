<template>
  <div class="page-container">
    <div class="search-form">
      <el-form :model="queryForm" inline>
        <el-form-item label="用户ID">
          <el-input v-model="queryForm.userId" placeholder="输入用户ID" clearable />
        </el-form-item>
        <el-form-item label="图书名称">
          <el-input v-model="queryForm.bookName" placeholder="输入图书名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.reserveStatus" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="待生效" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已失效" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-container">
      <PageHeader title="预约记录">
        <template #actions>
          <el-button type="primary" :icon="Plus" @click="reserveDialogVisible = true">新增预约</el-button>
        </template>
      </PageHeader>

      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="userName" label="用户姓名" width="120" />
        <el-table-column prop="bookId" label="图书ID" width="80" />
        <el-table-column prop="bookName" label="图书名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="author" label="作者" width="120" show-overflow-tooltip />
        <el-table-column prop="publisher" label="出版社" width="150" show-overflow-tooltip />
        <el-table-column label="预约时间" width="170">
          <template #default="{ row }">{{ row.createTime || row.reserveTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="失效时间" width="170">
          <template #default="{ row }">{{ row.expireTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.reserveStatus ?? row.status)" size="small">
              {{ getStatusText(row.reserveStatus ?? row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <template v-if="(row.reserveStatus ?? row.status) === 1">
              <el-popconfirm title="确定取消该预约吗？" @confirm="handleCancel(row)">
                <template #reference>
                  <el-button type="danger" link>取消预约</el-button>
                </template>
              </el-popconfirm>
              <el-button type="primary" link @click="handleComplete(row)">标记完成</el-button>
            </template>
            <span v-else style="color: var(--text-placeholder)">-</span>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px; display: flex; justify-content: flex-end">
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

    <el-dialog v-model="reserveDialogVisible" title="新增预约" width="500px">
      <el-form ref="reserveFormRef" :model="reserveForm" :rules="reserveRules" label-width="100px">
        <el-form-item label="用户ID" prop="userId">
          <el-input-number v-model="reserveForm.userId" :min="1" style="width: 100%" placeholder="请输入用户ID" />
        </el-form-item>
        <el-form-item label="图书选择" prop="bookId">
          <el-select v-model="reserveForm.bookId" style="width: 100%" placeholder="请选择图书" filterable remote :remote-method="searchBooks">
            <el-option v-for="book in bookOptions" :key="book.id" :label="`${book.bookName} - ${book.author}`" :value="book.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reserveDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="reserveLoading" @click="handleReserve">确认预约</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onBeforeMount } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getReservePage, adminReserveBook, cancelReserve, completeReserve } from '@/api/reserve'
import { getBookPage } from '@/api/book'
import PageHeader from '@/components/PageHeader.vue'

const queryForm = reactive({ current: 1, size: 10, userId: null, bookName: '', reserveStatus: null })
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

const statusMap = {
  1: { text: '待生效', type: 'warning' },
  2: { text: '已完成', type: 'success' },
  3: { text: '已失效', type: 'info' },
}

function getStatusText(status) {
  return statusMap[status]?.text || '未知'
}

function getStatusType(status) {
  return statusMap[status]?.type || 'info'
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getReservePage(queryForm)
    if (res?.data) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (err) {
    console.error('[ReserveView] fetchData error:', err)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryForm.current = 1
  fetchData()
}

function handleReset() {
  queryForm.userId = null
  queryForm.bookName = ''
  queryForm.reserveStatus = null
  queryForm.current = 1
  fetchData()
}

const reserveDialogVisible = ref(false)
const reserveLoading = ref(false)
const reserveFormRef = ref(null)
const reserveForm = reactive({ userId: null, bookId: null })
const reserveRules = {
  userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
  bookId: [{ required: true, message: '请选择图书', trigger: 'blur' }],
}

const bookOptions = ref([])

async function searchBooks(query) {
  if (!query) {
    bookOptions.value = []
    return
  }
  try {
    const res = await getBookPage({ bookName: query, current: 1, size: 20 })
    if (res?.data) {
      bookOptions.value = res.data.records || []
    }
  } catch (err) {
    console.error('[ReserveView] searchBooks error:', err)
  }
}

async function handleReserve() {
  const valid = await reserveFormRef.value.validate().catch(() => false)
  if (!valid) return
  reserveLoading.value = true
  try {
    await adminReserveBook(reserveForm.bookId, reserveForm.userId)
    ElMessage.success('预约成功')
    reserveDialogVisible.value = false
    reserveForm.userId = null
    reserveForm.bookId = null
    bookOptions.value = []
    fetchData()
  } catch (err) {
    console.error('[ReserveView] reserve error:', err)
  } finally {
    reserveLoading.value = false
  }
}

async function handleCancel(row) {
  try {
    await cancelReserve(row.id)
    ElMessage.success('预约已取消')
    fetchData()
  } catch (err) {
    console.error('[ReserveView] cancel error:', err)
  }
}

async function handleComplete(row) {
  try {
    await completeReserve(row.id)
    ElMessage.success('已标记为完成')
    fetchData()
  } catch (err) {
    console.error('[ReserveView] complete error:', err)
  }
}

onBeforeMount(() => { fetchData() })
</script>

<style scoped lang="scss">
.page-container {
  padding: 20px;
}

.search-form {
  margin-bottom: 20px;
  padding: 16px;
  background: #fff;
  border-radius: 8px;
}

.table-container {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}
</style>