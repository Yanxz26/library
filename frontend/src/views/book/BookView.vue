<template>
  <div class="page-container">
    <div class="search-form">
      <el-form :model="queryForm" inline>
        <el-form-item label="图书名称">
          <el-input v-model="queryForm.bookName" placeholder="请输入图书名称" clearable />
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="queryForm.author" placeholder="请输入作者" clearable />
        </el-form-item>
        <el-form-item label="ISBN">
          <el-input v-model="queryForm.isbn" placeholder="请输入ISBN" clearable />
        </el-form-item>
        <el-form-item label="分类">
          <el-tree-select
            v-model="queryForm.categoryId"
            :data="categoryTree"
            :props="{ label: 'categoryName', value: 'id', children: 'children' }"
            placeholder="全部分类"
            clearable check-strictly
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-container">
      <PageHeader title="图书列表">
        <template #actions>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增图书</el-button>
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleImportSuccess"
            :on-error="handleImportError"
            accept=".xlsx,.xls"
            name="file"
            style="display: inline-block"
          >
            <el-button :icon="Upload">批量导入</el-button>
          </el-upload>
          <el-button :icon="Download" :loading="exporting" @click="handleExport">导出</el-button>
        </template>
      </PageHeader>

      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="bookNo" label="图书编号" width="130" />
        <el-table-column prop="isbn" label="ISBN" width="140" />
        <el-table-column prop="bookName" label="图书名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column prop="publisher" label="出版社" width="140" show-overflow-tooltip />
        <el-table-column label="价格" width="80">
          <template #default="{ row }">¥{{ row.price }}</template>
        </el-table-column>
        <el-table-column label="库存" width="100">
          <template #default="{ row }">
            <span :style="{ color: row.remainNum === 0 ? 'var(--color-danger)' : 'var(--color-success)' }">
              {{ row.remainNum }} / {{ row.totalNum }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="馆藏位置" width="100" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="dict.tag('bookStatus', row.status)" size="small">
              {{ dict.label('bookStatus', row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
            <el-button type="success" link @click="handleEdit(row)">编辑</el-button>
            <el-button
              v-if="row.status === 1" type="warning" link @click="handleOffShelf(row)"
            >下架</el-button>
            <el-button v-else type="success" link @click="handleOnShelf(row)">上架</el-button>
            <el-button type="info" link @click="handleAddStock(row)">增补</el-button>
            <el-popconfirm title="确定删除该图书吗？" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
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

    <!-- 新增/编辑 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="650px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="图书编号" prop="bookNo">
              <el-input v-model="form.bookNo" placeholder="自动生成可留空" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="ISBN" prop="isbn">
              <el-input v-model="form.isbn" placeholder="请输入ISBN" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="图书名称" prop="bookName">
              <el-input v-model="form.bookName" placeholder="请输入图书名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="作者" prop="author">
              <el-input v-model="form.author" placeholder="请输入作者" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="出版社" prop="publisher">
              <el-input v-model="form.publisher" placeholder="请输入出版社" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="出版时间">
              <el-date-picker
                v-model="form.publishTime" type="date" placeholder="请选择出版时间"
                value-format="YYYY-MM-DD" style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="分类" prop="categoryId">
              <el-tree-select
                v-model="form.categoryId" :data="categoryTree"
                :props="{ label: 'categoryName', value: 'id', children: 'children' }"
                placeholder="请选择分类" check-strictly style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="价格" prop="price">
              <el-input-number v-model="form.price" :precision="2" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="总数量" prop="totalNum">
              <div class="stock-input-wrapper">
                <el-input-number v-model="form.totalNum" :min="0" style="flex: 1" />
                <el-button
                  v-if="isEdit && editId"
                  type="success"
                  link
                  size="small"
                  @click="handleAddStockFromEdit"
                  style="margin-left: 8px"
                >+增补库存</el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="馆藏位置">
              <el-input v-model="form.location" placeholder="请输入馆藏位置" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="封面图片">
          <el-upload
            class="cover-upload"
            :action="coverUploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleCoverUploadSuccess"
            :on-error="handleCoverUploadError"
            :before-upload="handleCoverBeforeUpload"
            :disabled="coverUploading"
            accept="image/*"
          >
            <div v-if="form.cover" class="cover-preview">
              <img :src="form.cover" alt="封面预览" @error="handleCoverImageError" />
              <div class="cover-overlay">
                <el-icon :size="24" color="#fff"><Upload /></el-icon>
                <span>更换封面</span>
              </div>
              <div class="cover-delete" @click.stop="handleRemoveCover">
                <el-icon :size="16" color="#fff"><Close /></el-icon>
              </div>
            </div>
            <div v-else-if="coverUploading" class="cover-uploading">
              <el-icon :size="24" color="#409EFF"><Loading /></el-icon>
              <span>上传中...</span>
            </div>
            <el-button v-else :icon="Upload" type="primary">上传封面</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="图书简介">
          <el-input v-model="form.bookDesc" type="textarea" :rows="3" placeholder="请输入图书简介" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 库存增补 -->
    <el-dialog v-model="stockDialogVisible" title="库存增补" width="400px">
      <el-form label-width="100px">
        <el-form-item label="当前库存">
          <span>{{ currentStockRow?.totalNum || 0 }}</span>
        </el-form-item>
        <el-form-item label="增补数量">
          <el-input-number v-model="addStockNum" :min="1" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="stockLoading" @click="handleStockConfirm">确定增补</el-button>
      </template>
    </el-dialog>

    <!-- 详情 -->
    <el-dialog v-model="detailVisible" title="图书详情" width="650px" class="detail-dialog">
      <el-descriptions :column="2" border v-if="detailData">
        <el-descriptions-item label="图书编号">{{ detailData.bookNo }}</el-descriptions-item>
        <el-descriptions-item label="ISBN">{{ detailData.isbn }}</el-descriptions-item>
        <el-descriptions-item label="图书名称">{{ detailData.bookName }}</el-descriptions-item>
        <el-descriptions-item label="作者">{{ detailData.author }}</el-descriptions-item>
        <el-descriptions-item label="出版社">{{ detailData.publisher }}</el-descriptions-item>
        <el-descriptions-item label="出版时间">{{ detailData.publishTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ detailData.categoryName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="价格">¥{{ detailData.price }}</el-descriptions-item>
        <el-descriptions-item label="总数量">{{ detailData.totalNum }}</el-descriptions-item>
        <el-descriptions-item label="剩余库存">{{ detailData.remainNum }}</el-descriptions-item>
        <el-descriptions-item label="馆藏位置">{{ detailData.location || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="dict.tag('bookStatus', detailData.status)" size="small">
            {{ dict.label('bookStatus', detailData.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="图书简介" :span="2">{{ detailData.bookDesc || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onBeforeMount, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Upload, Download, Close, Loading } from '@element-plus/icons-vue'
import { getBookPage, addBook, updateBook, deleteBook, offShelf, onShelf, addStock, exportBooks } from '@/api/book'
import { getCategoryTree } from '@/api/category'
import { getToken } from '@/utils/auth'
import { useDict } from '@/composables/useDict'
import { useExport } from '@/composables/useExport'
import PageHeader from '@/components/PageHeader.vue'

const { label, tag } = useDict()
const dict = { label, tag }
const { exporting, handleExport: doExport } = useExport(exportBooks, { filePrefix: '图书数据' })

const categoryTree = ref([])
const uploadUrl = '/api/sys/book/batch-import'
const coverUploadUrl = '/api/sys/book/upload-cover'
const uploadHeaders = computed(() => ({ Authorization: `Bearer ${getToken()}` }))

async function loadCategoryTree() {
  try { const res = await getCategoryTree(); if (res?.data) categoryTree.value = res.data }
  catch (err) { console.error('[BookView] category error:', err) }
}

const queryForm = reactive({
  current: 1, size: 10,
  bookName: '', author: '', isbn: '', categoryId: null, status: null,
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
    const res = await getBookPage(params)
    if (res?.data) { tableData.value = res.data.records || []; total.value = res.data.total || 0 }
  } catch (err) { console.error('[BookView] fetchData error:', err) }
  finally { loading.value = false }
}

function handleSearch() { queryForm.current = 1; fetchData() }
function handleReset() {
  queryForm.current = 1
  queryForm.bookName = ''; queryForm.author = ''; queryForm.isbn = ''
  queryForm.categoryId = null; queryForm.status = null
  fetchData()
}

// ======== 新增/编辑 ========
const dialogVisible = ref(false); const isEdit = ref(false)
const submitLoading = ref(false); const formRef = ref(null); const editId = ref(null)

const defaultForm = { bookNo: '', isbn: '', bookName: '', author: '', publisher: '', publishTime: '', categoryId: null, price: 0, totalNum: 1, location: '', cover: '', bookDesc: '' }
const form = reactive({ ...defaultForm })
const coverUploading = ref(false)
const formRules = {
  bookName: [{ required: true, message: '请输入图书名称', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者', trigger: 'blur' }],
  isbn: [{ required: true, message: '请输入ISBN', trigger: 'blur' }],
  publisher: [{ required: true, message: '请输入出版社', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  totalNum: [{ required: true, message: '请输入数量', trigger: 'blur' }],
}

const dialogTitle = computed(() => isEdit.value ? '编辑图书' : '新增图书')

function handleAdd() {
  isEdit.value = false; editId.value = null
  Object.assign(form, defaultForm)
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true; editId.value = row.id
  Object.assign(form, {
    bookNo: row.bookNo || '', isbn: row.isbn || '', bookName: row.bookName || '',
    author: row.author || '', publisher: row.publisher || '', publishTime: row.publishTime || '',
    categoryId: row.categoryId || null, price: row.price || 0, totalNum: row.totalNum || 0,
    location: row.location || '', cover: row.cover || '', bookDesc: row.bookDesc || '',
  })
  dialogVisible.value = true
}

function handleCoverBeforeUpload() {
  coverUploading.value = true
}

function handleCoverUploadSuccess(res) {
  coverUploading.value = false
  if (res?.code === 200 && res.data) {
    form.cover = res.data
    ElMessage.success('封面上传成功')
  } else {
    ElMessage.error(res?.message || '封面上传失败')
  }
}

function handleCoverUploadError(err) {
  coverUploading.value = false
  ElMessage.error('封面上传失败：' + (err?.message || '网络错误'))
}

function handleCoverImageError(e) {
  e.target.src = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="100" height="120" viewBox="0 0 100 120"%3E%3Crect fill="%23E5E7EB" width="100" height="120"/%3E%3Ctext fill="%239CA3AF" font-family="sans-serif" font-size="12" x="50" y="60" text-anchor="middle"%3E图片加载失败%3C/text%3E%3C/svg%3E'
}

function handleRemoveCover() {
  form.cover = ''
  ElMessage.success('已移除封面')
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const data = { ...form }
    if (isEdit.value) { data.id = editId.value; await updateBook(data); ElMessage.success('修改成功') }
    else { await addBook(data); ElMessage.success('新增成功') }
    dialogVisible.value = false
    fetchData()
  } catch (err) { console.error('[BookView] submit error:', err) }
  finally { submitLoading.value = false }
}

// ======== 详情（使用行数据，无需额外 API 调用） ========
const detailVisible = ref(false); const detailData = ref(null)

function handleDetail(row) {
  detailData.value = row
  detailVisible.value = true
}

// ======== 上下架 ========
async function handleOffShelf(row) {
  try { await offShelf(row.id); ElMessage.success('已下架'); fetchData() }
  catch (err) { console.error('[BookView] offShelf error:', err) }
}
async function handleOnShelf(row) {
  try { await onShelf(row.id); ElMessage.success('已上架'); fetchData() }
  catch (err) { console.error('[BookView] onShelf error:', err) }
}

// ======== 删除 ========
async function handleDelete(id) {
  try { await deleteBook(id); ElMessage.success('删除成功'); fetchData() }
  catch (err) { console.error('[BookView] delete error:', err) }
}

// ======== 库存增补 ========
const stockDialogVisible = ref(false); const stockLoading = ref(false)
const currentStockRow = ref(null); const addStockNum = ref(1)

function handleAddStock(row) { currentStockRow.value = row; addStockNum.value = 1; stockDialogVisible.value = true }

function handleAddStockFromEdit() {
  const row = tableData.value.find(item => item.id === editId.value)
  if (row) {
    handleAddStock(row)
  }
}

async function handleStockConfirm() {
  stockLoading.value = true
  try {
    await addStock(currentStockRow.value.id, addStockNum.value)
    ElMessage.success('增补成功'); stockDialogVisible.value = false; fetchData()
  } catch (err) { console.error('[BookView] stock error:', err) }
  finally { stockLoading.value = false }
}

function handleImportSuccess(res) {
  if (res.code === 200) {
    const data = res.data || {}
    let msg = `导入成功，成功${data.successCount || 0}条`
    if (data.errorCount && data.errorCount > 0) {
      msg += `，失败${data.errorCount}条`
    }
    ElMessage.success(msg)
    if (data.errors && data.errors.length > 0) {
      ElMessageBox.alert(data.errors.join('\n'), '导入失败详情', {
        confirmButtonText: '确定',
        type: 'warning'
      })
    }
    fetchData()
  } else {
    ElMessage.error(res.message || '导入失败')
  }
}

function handleImportError(err) {
  console.error('[BookView] batch import error:', err)
  ElMessage.error('导入失败，请检查网络连接或联系管理员')
}

async function handleExport() {
  const params = {}
  Object.keys(queryForm).forEach(k => {
    if (k !== 'current' && k !== 'size' && queryForm[k] !== '' && queryForm[k] !== null) params[k] = queryForm[k]
  })
  await doExport(params)
}

onBeforeMount(() => { loadCategoryTree(); fetchData() })
</script>

<style scoped lang="scss">
.stock-input-wrapper {
  display: flex;
  align-items: center;
}

.cover-upload {
  .cover-preview {
    width: 120px;
    height: 150px;
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid #E5E7EB;
    position: relative;
    cursor: pointer;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    .cover-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity 0.2s;

      span {
        color: #fff;
        font-size: 12px;
        margin-top: 4px;
      }
    }

    .cover-delete {
      position: absolute;
      top: 4px;
      right: 4px;
      width: 24px;
      height: 24px;
      border-radius: 50%;
      background: rgba(239, 68, 68, 0.9);
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      opacity: 0;
      transition: opacity 0.2s;
    }

    &:hover {
      .cover-overlay,
      .cover-delete {
        opacity: 1;
      }
    }
  }

  .cover-uploading {
    width: 120px;
    height: 150px;
    border-radius: 8px;
    border: 2px dashed #409EFF;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    background: #f0f5ff;

    span {
      color: #409EFF;
      font-size: 12px;
      margin-top: 8px;
    }
  }
}
</style>
