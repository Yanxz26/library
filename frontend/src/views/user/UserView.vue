<template>
  <div class="page-container">
    <div class="search-form">
      <el-form :model="queryForm" inline>
        <el-form-item label="账号">
          <el-input v-model="queryForm.userAccount" placeholder="请输入账号" clearable />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="queryForm.userName" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="用户类型">
          <el-select v-model="queryForm.userType" placeholder="全部" clearable style="width: 140px">
            <el-option v-for="[k,v] in Object.entries(DICTS.userType)" :key="k" :label="v.label" :value="Number(k)" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="[k,v] in Object.entries(DICTS.userStatus)" :key="k" :label="v.label" :value="Number(k)" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-container">
      <PageHeader title="用户列表">
        <template #actions>
          <el-button type="primary" :icon="Plus" @click="handleAdd">新增用户</el-button>
          <el-upload
            :action="uploadUrl"
            :headers="uploadHeaders"
            :show-file-list="false"
            :on-success="handleImportSuccess"
            accept=".xlsx,.xls"
            style="display: inline-block"
          >
            <el-button :icon="Upload">批量导入</el-button>
          </el-upload>
          <el-button :icon="Download" :loading="exporting" @click="handleExport">导出</el-button>
        </template>
      </PageHeader>

      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="userAccount" label="账号" width="120" />
        <el-table-column prop="userName" label="姓名" width="100" />
        <el-table-column label="用户类型" width="100">
          <template #default="{ row }">
            <el-tag :type="dict.tag('userType', row.userType)" size="small">
              <span class="status-dot" :class="'dot-' + dict.tag('userType', row.userType)"></span>
              {{ dict.label('userType', row.userType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="borrowNum" label="已借数量" width="90" />
        <el-table-column prop="maxBorrow" label="最大可借" width="90" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="(val) => handleStatusChange(row, val)"
              active-color="var(--color-success)"
              inactive-color="var(--color-danger)"
            />
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ row.createTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link @click="handleResetPwd(row)">重置密码</el-button>
            <el-button type="info" link @click="handleSetPwd(row)">设置密码</el-button>
            <el-popconfirm title="确定删除该用户吗？" @confirm="handleDelete(row.id)">
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="form" :rules="computedRules" label-width="100px">
        <el-form-item label="账号" prop="userAccount">
          <el-input v-model="form.userAccount" :disabled="isEdit" placeholder="学号/工号" />
        </el-form-item>
        <el-form-item label="姓名" prop="userName">
          <el-input v-model="form.userName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="用户类型" prop="userType">
          <el-select v-model="form.userType" placeholder="请选择" style="width: 100%">
            <el-option v-for="[k,v] in Object.entries(DICTS.userType)" :key="k" :label="v.label" :value="Number(k)" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 设置密码 -->
    <el-dialog v-model="setPwdDialogVisible" title="设置密码" width="450px" :close-on-click-modal="false">
      <el-form ref="setPwdFormRef" :model="setPwdForm" :rules="setPwdRules" label-width="100px">
        <el-form-item label="目标用户">
          <span style="font-weight: 600">{{ setPwdTarget?.userName }}（{{ setPwdTarget?.userAccount }}）</span>
        </el-form-item>
        <el-form-item label="新密码" prop="password">
          <el-input v-model="setPwdForm.password" type="password" show-password placeholder="请输入新密码（至少6位）" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="setPwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="setPwdDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="setPwdLoading" @click="handleSetPwdSubmit">确认设置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onBeforeMount, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Upload, Download } from '@element-plus/icons-vue'
import { getUserPage, addUser, updateUser, deleteUser, updateUserStatus, exportUsers } from '@/api/user'
import { resetPassword, setPassword } from '@/api/auth'
import { getToken } from '@/utils/auth'
import { useDict } from '@/composables/useDict'
import { useExport } from '@/composables/useExport'
import PageHeader from '@/components/PageHeader.vue'

const { label, tag, DICTS } = useDict()
const dict = { label, tag }
const { exporting, handleExport: doExport } = useExport(exportUsers, { filePrefix: '用户数据' })

const uploadUrl = '/api/sys/user/batch-import'
const uploadHeaders = computed(() => ({ Authorization: `Bearer ${getToken()}` }))

const queryForm = reactive({
  current: 1, size: 10,
  userAccount: '', userName: '', userType: null, status: null,
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
    const res = await getUserPage(params)
    if (res?.data) {
      tableData.value = res.data.records || []
      total.value = res.data.total || 0
    }
  } catch (err) {
    console.error('[UserView] fetchData error:', err)
  } finally {
    loading.value = false
  }
}

function handleSearch() { queryForm.current = 1; fetchData() }
function handleReset() {
  queryForm.current = 1
  queryForm.userAccount = ''; queryForm.userName = ''
  queryForm.userType = null; queryForm.status = null
  fetchData()
}

async function handleStatusChange(row, val) {
  try {
    await updateUserStatus(row.id, val ? 1 : 0)
    ElMessage.success(val ? '已启用' : '已禁用')
    fetchData()
  } catch (err) { console.error('[UserView] status error:', err) }
}

// ======== 新增/编辑 ========
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const editId = ref(null)

const form = reactive({ userAccount: '', userName: '', password: '', userType: null, phone: '', email: '' })
const baseRules = {
  userAccount: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  userName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码长度不少于6位', trigger: 'blur' }],
  userType: [{ required: true, message: '请选择用户类型', trigger: 'change' }],
}

const computedRules = computed(() => {
  if (isEdit.value) {
    const { password, ...rest } = baseRules
    return rest
  }
  return baseRules
})

const dialogTitle = computed(() => isEdit.value ? '编辑用户' : '新增用户')

function handleAdd() {
  isEdit.value = false; editId.value = null
  form.userAccount = ''; form.userName = ''; form.password = ''
  form.userType = null; form.phone = ''; form.email = ''
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true; editId.value = row.id
  form.userAccount = row.userAccount; form.userName = row.userName
  form.userType = row.userType; form.phone = row.phone || ''; form.email = row.email || ''
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    const data = { ...form }
    if (isEdit.value) {
      data.id = editId.value; delete data.password
      await updateUser(data)
      ElMessage.success('修改成功')
    } else {
      await addUser(data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch (err) { console.error('[UserView] submit error:', err) }
  finally { submitLoading.value = false }
}

async function handleDelete(id) {
  try { await deleteUser(id); ElMessage.success('删除成功'); fetchData() }
  catch (err) { console.error('[UserView] delete error:', err) }
}

async function handleResetPwd(row) {
  try { await resetPassword(row.id); ElMessage.success('密码已重置为默认密码（账号后6位）') }
  catch (err) { console.error('[UserView] resetPwd error:', err) }
}

// ======== 设置密码 ========
const setPwdDialogVisible = ref(false)
const setPwdLoading = ref(false)
const setPwdTarget = ref(null)
const setPwdFormRef = ref(null)
const setPwdForm = reactive({ password: '', confirmPassword: '' })

const validateSetPwdConfirm = (rule, value, callback) => {
  callback(value !== setPwdForm.password ? new Error('两次输入的密码不一致') : undefined)
}

const setPwdRules = {
  password: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请再次输入新密码', trigger: 'blur' }, { validator: validateSetPwdConfirm, trigger: 'blur' }],
}

function handleSetPwd(row) { setPwdTarget.value = row; setPwdForm.password = ''; setPwdForm.confirmPassword = ''; setPwdDialogVisible.value = true }

async function handleSetPwdSubmit() {
  const valid = await setPwdFormRef.value.validate().catch(() => false)
  if (!valid) return
  setPwdLoading.value = true
  try {
    await setPassword(setPwdTarget.value.id, setPwdForm.password)
    ElMessage.success(`已为 ${setPwdTarget.value.userName} 设置新密码`)
    setPwdDialogVisible.value = false
  } catch (err) { console.error('[UserView] setPwd error:', err) }
  finally { setPwdLoading.value = false }
}

function handleImportSuccess(res) {
  if (res.code === 200) { ElMessage.success('导入成功'); fetchData() }
  else { ElMessage.error(res.message || '导入失败') }
}

async function handleExport() {
  const params = {}
  Object.keys(queryForm).forEach(k => {
    if (k !== 'current' && k !== 'size' && queryForm[k] !== '' && queryForm[k] !== null) params[k] = queryForm[k]
  })
  await doExport(params)
}

onBeforeMount(() => { fetchData() })
</script>
