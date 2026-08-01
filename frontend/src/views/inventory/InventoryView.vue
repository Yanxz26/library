<template>
  <div class="page-container">
    <div class="search-form">
      <el-form :model="queryForm" inline>
        <el-form-item label="图书名称">
          <el-input v-model="queryForm.bookName" placeholder="输入图书名称" clearable />
        </el-form-item>
        <el-form-item label="是否有差异">
          <el-select v-model="queryForm.hasDiff" placeholder="全部" clearable style="width: 140px">
            <el-option label="有差异" :value="true" />
            <el-option label="无差异" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-container">
      <PageHeader title="盘点记录">
        <template #actions>
          <el-button type="primary" :icon="Plus" @click="inventoryDialogVisible = true">执行盘点</el-button>
          <el-button :icon="Download" :loading="exporting" @click="handleExport">导出</el-button>
        </template>
      </PageHeader>

      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="bookId" label="图书ID" width="80" />
        <el-table-column prop="bookName" label="图书名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="author" label="作者" width="120" show-overflow-tooltip />
        <el-table-column prop="systemNum" label="系统库存" width="100" />
        <el-table-column prop="actualNum" label="实际库存" width="100" />
        <el-table-column label="差异" width="80">
          <template #default="{ row }">
            <span :style="{ color: getDiffColor(row.diffNum) }">
              {{ row.diffNum > 0 ? '+' : '' }}{{ row.diffNum || 0 }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="盘点时间" width="170">
          <template #default="{ row }">{{ row.createTime || '-' }}</template>
        </el-table-column>
        <el-table-column prop="inventoryUser" label="盘点人" width="100" />
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

    <el-dialog v-model="inventoryDialogVisible" title="执行盘点" width="500px" :close-on-click-modal="false">
      <el-form ref="inventoryFormRef" :model="inventoryForm" :rules="inventoryRules" label-width="100px">
        <el-form-item label="图书选择" prop="bookId">
          <el-select v-model="inventoryForm.bookId" style="width: 100%" placeholder="请选择图书" filterable remote :remote-method="searchBooks">
            <el-option v-for="book in bookOptions" :key="book.id" :label="`${book.bookName} - ${book.author}`" :value="book.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="实际库存" prop="actualNum">
          <el-input-number v-model="inventoryForm.actualNum" :min="0" style="width: 100%" placeholder="请输入实际库存数量" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="inventoryForm.remark" type="textarea" :rows="2" placeholder="盘点备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="inventoryDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="inventoryLoading" @click="handleInventory">确认盘点</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onBeforeMount } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Download } from '@element-plus/icons-vue'
import { getInventoryPage, doInventory, exportInventory } from '@/api/inventory'
import { getBookPage } from '@/api/book'
import { useExport } from '@/composables/useExport'
import PageHeader from '@/components/PageHeader.vue'

const { exporting, handleExport: doExport } = useExport(exportInventory, { filePrefix: '盘点记录' })

function getDiffColor(diffNum) {
  if (diffNum === 0) return 'var(--color-success)'
  if (diffNum > 0) return 'var(--color-info)'
  return 'var(--color-danger)'
}

const queryForm = reactive({ current: 1, size: 10, bookName: '', hasDiff: null })
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
    const res = await getInventoryPage(params)
    if (res?.data) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (err) {
    console.error('[InventoryView] fetchData error:', err)
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
  queryForm.hasDiff = null
  fetchData()
}

const inventoryDialogVisible = ref(false)
const inventoryLoading = ref(false)
const inventoryFormRef = ref(null)
const inventoryForm = reactive({ bookId: null, actualNum: 0, remark: '' })
const inventoryRules = {
  bookId: [{ required: true, message: '请选择图书', trigger: 'blur' }],
  actualNum: [{ required: true, message: '请输入实际库存', trigger: 'blur' }],
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
    console.error('[InventoryView] searchBooks error:', err)
  }
}

async function handleInventory() {
  const valid = await inventoryFormRef.value.validate().catch(() => false)
  if (!valid) return
  inventoryLoading.value = true
  try {
    await doInventory(inventoryForm)
    ElMessage.success('盘点完成')
    inventoryDialogVisible.value = false
    inventoryForm.bookId = null
    inventoryForm.actualNum = 0
    inventoryForm.remark = ''
    bookOptions.value = []
    fetchData()
  } catch (err) {
    console.error('[InventoryView] inventory error:', err)
  } finally {
    inventoryLoading.value = false
  }
}

async function handleExport() {
  await doExport()
}

onBeforeMount(() => { fetchData() })
</script>