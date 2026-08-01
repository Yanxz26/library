<template>
  <div class="page-container">
    <div class="table-container">
      <PageHeader title="图书分类管理">
        <template #actions>
          <el-button type="primary" :icon="Plus" @click="handleAdd(null)">新增一级分类</el-button>
        </template>
      </PageHeader>

      <el-table
        :data="treeData"
        v-loading="loading"
        border stripe
        row-key="id"
        style="width: 100%"
        default-expand-all
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="categoryName" label="分类名称" min-width="200" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              <span class="status-dot" :class="row.status === 1 ? 'dot-success' : 'dot-danger'"></span>
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ row.createTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="300">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleAdd(row)">添加子分类</el-button>
            <el-button type="success" link @click="handleEdit(row)">编辑</el-button>
            <el-button
              :type="row.status === 1 ? 'warning' : 'success'"
              link
              @click="handleStatusChange(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-popconfirm title="确定删除该分类吗？子分类将一并删除" @confirm="handleDelete(row.id)">
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="上级分类">
          <el-input :model-value="parentName" disabled />
        </el-form-item>
        <el-form-item label="分类名称" prop="categoryName">
          <el-input v-model="form.categoryName" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onBeforeMount, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getCategoryTree, addCategory, updateCategory, deleteCategory, updateCategoryStatus } from '@/api/category'
import PageHeader from '@/components/PageHeader.vue'

const treeData = ref([])
const loading = ref(false)

async function fetchData() {
  loading.value = true
  try {
    const res = await getCategoryTree()
    if (res?.data) treeData.value = res.data
  } catch (err) {
    console.error('[CategoryView] fetchData error:', err)
  } finally {
    loading.value = false
  }
}

function findCategoryName(tree, id) {
  for (const item of tree) {
    if (item.id === id) return item.categoryName
    if (item.children) {
      const found = findCategoryName(item.children, id)
      if (found) return found
    }
  }
  return '无（一级分类）'
}

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const editId = ref(null)
const parentId = ref(0)
const parentName = ref('无（一级分类）')

const form = reactive({ categoryName: '', sort: 0 })
const formRules = {
  categoryName: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  sort: [{ required: true, message: '请输入排序', trigger: 'blur' }],
}

const dialogTitle = computed(() => isEdit.value ? '编辑分类' : '新增分类')

function handleAdd(parent) {
  isEdit.value = false
  editId.value = null
  parentId.value = parent ? parent.id : 0
  parentName.value = parent ? parent.categoryName : '无（一级分类）'
  form.categoryName = ''
  form.sort = 0
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  editId.value = row.id
  parentId.value = row.parentId || 0
  parentName.value = row.parentId ? findCategoryName(treeData.value, row.parentId) : '无（一级分类）'
  form.categoryName = row.categoryName
  form.sort = row.sort || 0
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const data = { categoryName: form.categoryName, sort: form.sort, parentId: parentId.value }
    if (isEdit.value) {
      data.id = editId.value
      await updateCategory(data)
      ElMessage.success('修改成功')
    } else {
      await addCategory(data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (err) {
    console.error('[CategoryView] submit error:', err)
  } finally {
    submitLoading.value = false
  }
}

async function handleStatusChange(row) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await updateCategoryStatus(row.id, newStatus)
    ElMessage.success(newStatus === 1 ? '已启用' : '已禁用')
    fetchData()
  } catch (err) {
    console.error('[CategoryView] status error:', err)
  }
}

async function handleDelete(id) {
  try {
    await deleteCategory(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (err) {
    console.error('[CategoryView] delete error:', err)
  }
}

onBeforeMount(() => { fetchData() })
</script>
