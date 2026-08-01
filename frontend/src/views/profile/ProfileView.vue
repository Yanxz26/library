<template>
  <div class="page-container">
    <div class="table-container" style="max-width: 700px; margin: 0 auto;">
      <PageHeader title="个人中心" />

      <el-descriptions v-if="userInfo" :column="2" border style="margin-bottom: 24px">
        <el-descriptions-item label="账号">{{ userInfo.userAccount || '-' }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ userInfo.userName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="用户类型">
          <el-tag :type="dict.tag('userType', userInfo.userType)" size="small">
            {{ dict.label('userType', userInfo.userType) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="角色">{{ userInfo.roleName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ userInfo.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ userInfo.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="已借数量">{{ userInfo.borrowNum || 0 }}</el-descriptions-item>
        <el-descriptions-item label="最大可借">{{ userInfo.maxBorrow || 0 }}</el-descriptions-item>
        <el-descriptions-item label="注册时间" :span="2">{{ userInfo.createTime || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider />

      <h3 style="margin-bottom: 20px; font-size: 16px; color: var(--text-primary)">修改个人信息</h3>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px" style="max-width: 500px">
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitLoading" @click="handleSubmit">保存修改</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onBeforeMount } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { getUserInfo as fetchUserInfoApi } from '@/api/auth'
import { updateProfile } from '@/api/user'
import { useDict } from '@/composables/useDict'
import PageHeader from '@/components/PageHeader.vue'

const authStore = useAuthStore()
const { label, tag } = useDict()
const dict = { label, tag }

const userInfo = ref(null)
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({ phone: '', email: '' })
const formRules = {
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }],
}

async function loadUserInfo() {
  try {
    const res = await fetchUserInfoApi()
    if (res?.data) {
      userInfo.value = res.data
      form.phone = res.data.phone || ''
      form.email = res.data.email || ''
    }
  } catch (err) {
    console.error('[ProfileView] loadUserInfo error:', err)
  }
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    await updateProfile({ phone: form.phone, email: form.email })
    ElMessage.success('修改成功')
    await loadUserInfo()
  } catch (err) {
    console.error('[ProfileView] submit error:', err)
  } finally {
    submitLoading.value = false
  }
}

onBeforeMount(() => { loadUserInfo() })
</script>
