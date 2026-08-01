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
        <el-form-item label="缴费状态">
          <el-select v-model="queryForm.payStatus" placeholder="全部" clearable style="width: 140px">
            <el-option label="未缴费" :value="0" />
            <el-option label="已缴费" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-container">
      <PageHeader title="逾期记录">
        <template #actions>
          <el-button :icon="Download" :loading="exporting" @click="handleExport">导出</el-button>
        </template>
      </PageHeader>

      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="userName" label="用户姓名" width="120" />
        <el-table-column prop="bookId" label="图书ID" width="80" />
        <el-table-column prop="bookName" label="图书名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="author" label="作者" width="120" show-overflow-tooltip />
        <el-table-column prop="overdueDays" label="逾期天数" width="100" />
        <el-table-column label="罚款金额" width="110">
          <template #default="{ row }">¥{{ row.fineMoney || row.fineAmount || 0 }}</template>
        </el-table-column>
        <el-table-column label="缴费状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.payStatus)" size="small">
              {{ getStatusText(row.payStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="缴费时间" width="170">
          <template #default="{ row }">{{ row.payTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <template v-if="row.payStatus !== 1">
              <el-popconfirm title="确认缴纳罚款？" @confirm="handlePay(row)">
                <template #reference>
                  <el-button type="primary" link>缴纳罚款</el-button>
                </template>
              </el-popconfirm>
              <el-popconfirm title="确认减免罚款？" @confirm="handleWaive(row)">
                <template #reference>
                  <el-button type="warning" link>减免</el-button>
                </template>
              </el-popconfirm>
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

    <el-dialog v-model="waiveDialogVisible" title="减免罚款" width="450px">
      <el-form label-width="100px">
        <el-form-item label="减免原因">
          <el-input v-model="waiveReason" type="textarea" :rows="3" placeholder="请输入减免原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="waiveDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="waiveLoading" @click="handleWaiveConfirm">确认减免</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onBeforeMount } from 'vue'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { getOverduePage, payFine, waiveFine, exportOverdue } from '@/api/overdue'
import { useExport } from '@/composables/useExport'
import PageHeader from '@/components/PageHeader.vue'

const { exporting, handleExport: doExport } = useExport(exportOverdue, { filePrefix: '逾期记录' })

const statusMap = {
  0: { text: '未缴费', type: 'danger' },
  1: { text: '已缴费', type: 'success' },
}

function getStatusText(status) {
  return statusMap[status]?.text || '未知'
}

function getStatusType(status) {
  return statusMap[status]?.type || 'info'
}

const queryForm = reactive({ current: 1, size: 10, userId: null, bookName: '', payStatus: null })
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

async function fetchData() {
  loading.value = true
  try {
    const params = { ...queryForm }
    Object.keys(params).forEach(k => {
      if (params[k] === '' || params[k] === null) delete params[k]
    })
    const res = await getOverduePage(params)
    if (res?.data) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (err) {
    console.error('[OverdueView] fetchData error:', err)
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
  queryForm.userId = null
  queryForm.bookName = ''
  queryForm.payStatus = null
  fetchData()
}

async function handlePay(row) {
  try {
    await payFine(row.id)
    ElMessage.success('缴纳成功')
    fetchData()
  } catch (err) {
    console.error('[OverdueView] pay error:', err)
  }
}

const waiveDialogVisible = ref(false)
const waiveLoading = ref(false)
const waiveReason = ref('')
const waiveRow = ref(null)

function handleWaive(row) {
  waiveRow.value = row
  waiveReason.value = ''
  waiveDialogVisible.value = true
}

async function handleWaiveConfirm() {
  waiveLoading.value = true
  try {
    await waiveFine(waiveRow.value.id, waiveReason.value || '管理员减免')
    ElMessage.success('减免成功')
    waiveDialogVisible.value = false
    fetchData()
  } catch (err) {
    console.error('[OverdueView] waive error:', err)
  } finally {
    waiveLoading.value = false
  }
}

async function handleExport() {
  await doExport()
}

onBeforeMount(() => { fetchData() })
</script>