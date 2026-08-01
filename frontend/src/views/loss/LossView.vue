<template>
  <div class="page-container">
    <div class="search-form">
      <el-form :model="queryForm" inline>
        <el-form-item label="图书名称">
          <el-input v-model="queryForm.bookName" placeholder="输入图书名称" clearable />
        </el-form-item>
        <el-form-item label="损耗类型">
          <el-select v-model="queryForm.lossType" placeholder="全部" clearable style="width: 120px">
            <el-option label="丢失" :value="1" />
            <el-option label="损坏" :value="2" />
            <el-option label="其他" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-container">
      <PageHeader title="损耗记录">
        <template #actions>
          <el-button type="primary" :icon="Plus" @click="lossDialogVisible = true">登记损耗</el-button>
          <el-button :icon="Download" :loading="exporting" @click="handleExport">导出</el-button>
        </template>
      </PageHeader>

      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="bookId" label="图书ID" width="80" />
        <el-table-column prop="bookName" label="图书名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="author" label="作者" width="120" show-overflow-tooltip />
        <el-table-column prop="lossNum" label="损耗数量" width="100" />
        <el-table-column label="损耗类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getLossTypeTag(row.lossType)" size="small">{{ getLossTypeText(row.lossType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lossReason" label="损耗原因" min-width="160" show-overflow-tooltip />
        <el-table-column label="登记时间" width="170">
          <template #default="{ row }">{{ row.createTime || '-' }}</template>
        </el-table-column>
        <el-table-column prop="recordUser" label="登记人" width="100" />
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

    <el-dialog v-model="lossDialogVisible" title="登记损耗" width="500px" :close-on-click-modal="false">
      <el-form ref="lossFormRef" :model="lossForm" :rules="lossRules" label-width="100px">
        <el-form-item label="图书选择" prop="bookId">
          <el-select v-model="lossForm.bookId" style="width: 100%" placeholder="请选择图书" filterable remote :remote-method="searchBooks">
            <el-option v-for="book in bookOptions" :key="book.id" :label="`${book.bookName} - ${book.author}`" :value="book.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="损耗数量" prop="lossNum">
          <el-input-number v-model="lossForm.lossNum" :min="1" :max="999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="损耗类型" prop="lossType">
          <el-select v-model="lossForm.lossType" placeholder="请选择" style="width: 100%">
            <el-option label="丢失" :value="1" />
            <el-option label="损坏" :value="2" />
            <el-option label="其他" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="损耗原因">
          <el-input v-model="lossForm.lossReason" type="textarea" :rows="2" placeholder="请输入损耗原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="lossDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="lossLoading" @click="handleLoss">确认登记</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onBeforeMount } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Download } from '@element-plus/icons-vue'
import { getLossPage, registerLoss, exportLoss } from '@/api/loss'
import { getBookPage } from '@/api/book'
import { useExport } from '@/composables/useExport'
import PageHeader from '@/components/PageHeader.vue'

const { exporting, handleExport: doExport } = useExport(exportLoss, { filePrefix: '损耗记录' })

const lossTypeMap = {
  1: { text: '丢失', type: 'danger' },
  2: { text: '损坏', type: 'warning' },
  3: { text: '其他', type: 'info' },
}

function getLossTypeText(type) {
  return lossTypeMap[type]?.text || '未知'
}

function getLossTypeTag(type) {
  return lossTypeMap[type]?.type || 'info'
}

const queryForm = reactive({ current: 1, size: 10, bookName: '', lossType: null })
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
    const res = await getLossPage(params)
    if (res?.data) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (err) {
    console.error('[LossView] fetchData error:', err)
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
  queryForm.lossType = null
  fetchData()
}

const lossDialogVisible = ref(false)
const lossLoading = ref(false)
const lossFormRef = ref(null)
const lossForm = reactive({ bookId: null, lossNum: 1, lossType: 1, lossReason: '' })
const lossRules = {
  bookId: [{ required: true, message: '请选择图书', trigger: 'blur' }],
  lossNum: [{ required: true, message: '请输入损耗数量', trigger: 'blur' }],
  lossType: [{ required: true, message: '请选择损耗类型', trigger: 'change' }],
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
    console.error('[LossView] searchBooks error:', err)
  }
}

async function handleLoss() {
  const valid = await lossFormRef.value.validate().catch(() => false)
  if (!valid) return
  lossLoading.value = true
  try {
    await registerLoss(lossForm)
    ElMessage.success('损耗登记成功')
    lossDialogVisible.value = false
    lossForm.bookId = null
    lossForm.lossNum = 1
    lossForm.lossType = 1
    lossForm.lossReason = ''
    bookOptions.value = []
    fetchData()
  } catch (err) {
    console.error('[LossView] register error:', err)
  } finally {
    lossLoading.value = false
  }
}

async function handleExport() {
  await doExport()
}

onBeforeMount(() => { fetchData() })
</script>