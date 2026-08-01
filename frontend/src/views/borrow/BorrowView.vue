<template>
  <div class="page-container">
    <div class="search-form">
      <el-form :model="queryForm" inline>
        <el-form-item label="图书名称">
          <el-input v-model="queryForm.bookName" placeholder="请输入图书名称" clearable />
        </el-form-item>
        <el-form-item label="借阅状态">
          <el-select v-model="queryForm.borrowStatus" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="[k,v] in Object.entries(DICTS.borrowStatus)" :key="k" :label="v.label" :value="Number(k)" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-container">
      <PageHeader :title="isStaff ? '借阅记录' : '我的借阅'">
        <template #actions>
          <el-button v-if="isStaff" type="primary" :icon="Plus" @click="borrowDialogVisible = true">图书借阅</el-button>
          <el-button v-if="isStaff" :icon="Download" :loading="exporting" @click="handleExport">导出</el-button>
        </template>
      </PageHeader>

      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column v-if="isStaff" prop="userId" label="用户ID" width="80" />
        <el-table-column prop="bookId" label="图书ID" width="80" />
        <el-table-column label="借阅时间" width="170">
          <template #default="{ row }">{{ row.borrowTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="到期时间" width="170">
          <template #default="{ row }">
            <span :style="{ color: isOverdue(row) ? 'var(--color-danger)' : '' }">{{ row.expireTime || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="归还时间" width="170">
          <template #default="{ row }">{{ row.returnTime || '未归还' }}</template>
        </el-table-column>
        <el-table-column prop="renewCount" label="续借次数" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="dict.tag('borrowStatus', row.borrowStatus)" size="small">
              {{ dict.label('borrowStatus', row.borrowStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <template v-if="row.borrowStatus === 1 || row.borrowStatus === 3">
              <el-button type="primary" link @click="handleReturn(row)">归还</el-button>
              <el-button type="success" link @click="handleRenew(row)">续借</el-button>
              <el-button v-if="isStaff" type="warning" link @click="handleAdminReturn(row)">管理员归还</el-button>
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

    <el-dialog v-model="borrowDialogVisible" title="图书借阅" width="500px">
      <el-form ref="borrowFormRef" :model="borrowForm" :rules="borrowRules" label-width="100px">
        <el-form-item label="图书ID" prop="bookId">
          <el-input-number v-model="borrowForm.bookId" :min="1" style="width: 100%" placeholder="请输入图书ID" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="borrowDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="borrowLoading" @click="handleBorrow">确认借阅</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onBeforeMount } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Download } from '@element-plus/icons-vue'
import { getBorrowPage, getMyBorrows, borrowBook, returnBook, renewBook, adminReturn, exportBorrows } from '@/api/borrow'
import { useRole } from '@/composables/useRole'
import { useDict } from '@/composables/useDict'
import { useExport } from '@/composables/useExport'
import PageHeader from '@/components/PageHeader.vue'

const { isStaff } = useRole()
const { label, tag, DICTS } = useDict()
const dict = { label, tag }
const { exporting, handleExport: doExport } = useExport(exportBorrows, { filePrefix: '借阅记录' })

const queryForm = reactive({
  current: 1,
  size: 10,
  bookName: '',
  borrowStatus: null,
})

const tableData = ref([])
const total = ref(0)
const loading = ref(false)

async function fetchData() {
  loading.value = true
  try {
    const params = {}
    Object.keys(queryForm).forEach(k => {
      if (queryForm[k] !== '' && queryForm[k] !== null) params[k] = queryForm[k]
    })
    const api = isStaff.value ? getBorrowPage : getMyBorrows
    const res = await api(params)
    if (res?.data) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (err) {
    console.error('[BorrowView] fetchData error:', err)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  queryForm.current = 1
  fetchData()
}

function handleReset() {
  queryForm.current = 1
  queryForm.bookName = ''
  queryForm.borrowStatus = null
  fetchData()
}

function isOverdue(row) {
  if (row.borrowStatus === 2) return false
  if (row.borrowStatus === 3) return true
  return row.expireTime && new Date(row.expireTime) < new Date()
}

const borrowDialogVisible = ref(false)
const borrowLoading = ref(false)
const borrowFormRef = ref(null)
const borrowForm = reactive({ bookId: null })
const borrowRules = {
  bookId: [{ required: true, message: '请输入图书ID', trigger: 'blur' }],
}

async function handleBorrow() {
  const valid = await borrowFormRef.value.validate().catch(() => false)
  if (!valid) return
  borrowLoading.value = true
  try {
    await borrowBook(borrowForm.bookId)
    ElMessage.success('借阅成功')
    borrowDialogVisible.value = false
    borrowForm.bookId = null
    fetchData()
  } catch (err) {
    console.error('[BorrowView] borrow error:', err)
  } finally {
    borrowLoading.value = false
  }
}

async function handleReturn(row) {
  try {
    await returnBook(row.id)
    ElMessage.success('归还成功')
    fetchData()
  } catch (err) {
    console.error('[BorrowView] return error:', err)
  }
}

async function handleRenew(row) {
  try {
    await renewBook(row.id)
    ElMessage.success('续借成功')
    fetchData()
  } catch (err) {
    console.error('[BorrowView] renew error:', err)
  }
}

async function handleAdminReturn(row) {
  try {
    await adminReturn(row.id)
    ElMessage.success('归还登记成功')
    fetchData()
  } catch (err) {
    console.error('[BorrowView] adminReturn error:', err)
  }
}

async function handleExport() {
  const params = {}
  Object.keys(queryForm).forEach(k => {
    if (k !== 'current' && k !== 'size' && queryForm[k] !== '' && queryForm[k] !== null) {
      params[k] = queryForm[k]
    }
  })
  await doExport(params)
}

onBeforeMount(() => { fetchData() })
</script>
