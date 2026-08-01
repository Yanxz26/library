<template>
  <div class="page-container">
    <div class="table-container">
      <PageHeader title="系统配置">
        <template #actions>
          <el-button v-if="isAdmin" type="primary" :icon="Plus" @click="handleAdd">新增配置</el-button>
        </template>
      </PageHeader>

      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="configKey" label="配置键" min-width="180" show-overflow-tooltip />
        <el-table-column prop="configValue" label="配置值" min-width="200" show-overflow-tooltip />
        <el-table-column prop="configDesc" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ row.createTime || '-' }}</template>
        </el-table-column>
        <el-table-column v-if="isAdmin" label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除该配置吗？" @confirm="handleDelete(row.id)">
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="配置键" prop="configKey">
          <el-input v-model="form.configKey" :disabled="isEdit" placeholder="请输入配置键" />
        </el-form-item>
        <el-form-item label="配置值" prop="configValue">
          <el-input v-model="form.configValue" type="textarea" :rows="3" placeholder="请输入配置值" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.configDesc" type="textarea" :rows="2" placeholder="请输入配置描述" />
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
import { getConfigPage, addConfig, updateConfig, deleteConfig } from '@/api/config'
import { useRole } from '@/composables/useRole'
import PageHeader from '@/components/PageHeader.vue'

const { isAdmin } = useRole()

const queryForm = reactive({ current: 1, size: 10 })
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

async function fetchData() {
  loading.value = true
  try {
    const res = await getConfigPage(queryForm)
    if (res?.data) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (err) {
    console.error('[ConfigView] fetchData error:', err)
  } finally {
    loading.value = false
  }
}

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const editId = ref(null)

const form = reactive({ configKey: '', configValue: '', configDesc: '' })
const formRules = {
  configKey: [{ required: true, message: '请输入配置键', trigger: 'blur' }],
  configValue: [{ required: true, message: '请输入配置值', trigger: 'blur' }],
}

const dialogTitle = computed(() => isEdit.value ? '编辑配置' : '新增配置')

function handleAdd() {
  isEdit.value = false
  editId.value = null
  form.configKey = ''
  form.configValue = ''
  form.configDesc = ''
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  editId.value = row.id
  form.configKey = row.configKey
  form.configValue = row.configValue
  form.configDesc = row.configDesc || ''
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const data = { ...form }
    if (isEdit.value) {
      data.id = editId.value
      await updateConfig(data)
      ElMessage.success('修改成功')
    } else {
      await addConfig(data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (err) {
    console.error('[ConfigView] submit error:', err)
  } finally {
    submitLoading.value = false
  }
}

async function handleDelete(id) {
  try {
    await deleteConfig(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch (err) {
    console.error('[ConfigView] delete error:', err)
  }
}

onBeforeMount(() => { fetchData() })
</script>
