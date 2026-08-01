<template>
  <div class="login-page">
    <!-- 左侧品牌区 -->
    <div class="login-brand">
      <div class="brand-content">
        <div class="brand-logo">
          <div class="logo-icon">
            <el-icon :size="40"><Reading /></el-icon>
          </div>
        </div>
        <h1 class="brand-title">校园图书管理系统</h1>
        <p class="brand-subtitle">Library Management System</p>
        <div class="brand-features">
          <div class="feature-item">
            <el-icon><Collection /></el-icon>
            <span>海量图书资源管理</span>
          </div>
          <div class="feature-item">
            <el-icon><Clock /></el-icon>
            <span>智能借阅归还流程</span>
          </div>
          <div class="feature-item">
            <el-icon><DataAnalysis /></el-icon>
            <span>多维度数据统计</span>
          </div>
          <div class="feature-item">
            <el-icon><Setting /></el-icon>
            <span>灵活的系统配置</span>
          </div>
        </div>
      </div>
      <div class="brand-decoration">
        <div class="deco-circle deco-1"></div>
        <div class="deco-circle deco-2"></div>
        <div class="deco-circle deco-3"></div>
      </div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="login-form-side">
      <div class="login-card">
        <div class="login-card-header">
          <h2>欢迎回来</h2>
          <p>请输入您的账号信息登录系统</p>
        </div>

        <el-form v-if="!showForgotPassword" ref="loginFormRef" :model="loginForm" :rules="loginRules" size="large">
          <el-form-item prop="userAccount">
            <el-input
              v-model="loginForm.userAccount"
              placeholder="请输入账号（学号/工号）"
              :prefix-icon="User"
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              show-password
              placeholder="请输入密码"
              :prefix-icon="Lock"
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          <el-form-item class="login-form-actions">
            <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin" round>
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
            <el-button type="text" class="forgot-password-btn" @click="showForgotPassword = true">忘记密码？</el-button>
          </el-form-item>
        </el-form>

        <el-form v-else ref="forgotFormRef" :model="forgotForm" :rules="forgotRules" size="large">
          <div v-if="forgotStep === 1" class="forgot-step">
            <div class="step-header">
              <el-icon :size="32" color="#5B6AF0"><HelpFilled /></el-icon>
              <h3>找回密码</h3>
              <p>请输入您的账号和用户名进行身份验证</p>
            </div>
            <el-form-item prop="userAccount">
              <el-input
                v-model="forgotForm.userAccount"
                placeholder="请输入账号（学号/工号）"
                :prefix-icon="User"
                @keyup.enter="handleForgotVerify"
              />
            </el-form-item>
            <el-form-item prop="userName">
              <el-input
                v-model="forgotForm.userName"
                placeholder="请输入用户名"
                :prefix-icon="UserFilled"
                @keyup.enter="handleForgotVerify"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" class="login-btn" :loading="forgotLoading" @click="handleForgotVerify" round>
                {{ forgotLoading ? '验证中...' : '下一步' }}
              </el-button>
              <el-button type="text" class="forgot-back-btn" @click="resetForgotForm">返回登录</el-button>
            </el-form-item>
          </div>

          <div v-else class="forgot-step">
            <div class="step-header">
              <el-icon :size="32" color="#22C55E"><CircleCheck /></el-icon>
              <h3>设置新密码</h3>
              <p>身份验证成功，请设置新密码</p>
            </div>
            <el-form-item prop="newPassword">
              <el-input
                v-model="forgotForm.newPassword"
                type="password"
                show-password
                placeholder="请输入新密码（至少6位）"
                :prefix-icon="Lock"
                @keyup.enter="handleForgotReset"
              />
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input
                v-model="forgotForm.confirmPassword"
                type="password"
                show-password
                placeholder="请确认新密码"
                :prefix-icon="Lock"
                @keyup.enter="handleForgotReset"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" class="login-btn" :loading="forgotLoading" @click="handleForgotReset" round>
                {{ forgotLoading ? '提交中...' : '确认修改' }}
              </el-button>
              <el-button type="text" class="forgot-back-btn" @click="forgotStep = 1">返回上一步</el-button>
            </el-form-item>
          </div>
        </el-form>
      </div>
      <p class="login-footer">© 2026 校园图书管理系统. All rights reserved.</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, UserFilled, HelpFilled, CircleCheck } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { forgotPasswordVerify, forgotPasswordReset } from '@/api/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loginFormRef = ref(null)
const forgotFormRef = ref(null)
const loading = ref(false)
const forgotLoading = ref(false)
const showForgotPassword = ref(false)
const forgotStep = ref(1)

const loginForm = reactive({
  userAccount: '',
  password: '',
})

const loginRules = {
  userAccount: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const forgotForm = reactive({
  userAccount: '',
  userName: '',
  newPassword: '',
  confirmPassword: '',
})

const forgotRules = {
  userAccount: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== forgotForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

async function handleLogin() {
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await authStore.login(loginForm)
    if (res.code === 200) {
      ElMessage.success('登录成功')
      let redirect = route.query.redirect
      if (!redirect) {
        const roleCode = authStore.userInfo.roleCode
        if (roleCode === 'user') {
          redirect = '/user-dashboard'
        } else if (roleCode === 'teacher') {
          redirect = '/teacher-dashboard'
        } else {
          redirect = '/dashboard'
        }
      }
      router.push(redirect)
    }
  } catch {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

async function handleForgotVerify() {
  const valid = await forgotFormRef.value.validate().catch(() => false)
  if (!valid) return

  forgotLoading.value = true
  try {
    const res = await forgotPasswordVerify({
      userAccount: forgotForm.userAccount,
      userName: forgotForm.userName,
    })
    if (res.code === 200) {
      ElMessage.success('身份验证成功')
      forgotStep.value = 2
    }
  } catch {
    // 错误已在拦截器中处理
  } finally {
    forgotLoading.value = false
  }
}

async function handleForgotReset() {
  const valid = await forgotFormRef.value.validate().catch(() => false)
  if (!valid) return

  forgotLoading.value = true
  try {
    const res = await forgotPasswordReset({
      userAccount: forgotForm.userAccount,
      userName: forgotForm.userName,
      newPassword: forgotForm.newPassword,
    })
    if (res.code === 200) {
      ElMessage.success(res.msg || '密码修改成功')
      resetForgotForm()
    }
  } catch {
    // 错误已在拦截器中处理
  } finally {
    forgotLoading.value = false
  }
}

function resetForgotForm() {
  forgotStep.value = 1
  showForgotPassword.value = false
  forgotForm.userAccount = ''
  forgotForm.userName = ''
  forgotForm.newPassword = ''
  forgotForm.confirmPassword = ''
}
</script>

<style scoped lang="scss">
.login-page {
  height: 100vh;
  display: flex;
  overflow: hidden;
}

// ========== 左侧品牌区 ==========
.login-brand {
  flex: 1;
  background: linear-gradient(135deg, #1B1B2F 0%, #2D2D55 40%, #4A3D8F 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;

  // 装饰圆形
  .brand-decoration {
    position: absolute;
    inset: 0;
    pointer-events: none;
    .deco-circle {
      position: absolute;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.03);
      &.deco-1 {
        width: 500px;
        height: 500px;
        top: -150px;
        right: -150px;
      }
      &.deco-2 {
        width: 300px;
        height: 300px;
        bottom: -80px;
        left: -80px;
      }
      &.deco-3 {
        width: 200px;
        height: 200px;
        top: 50%;
        left: 60%;
      }
    }
  }

  .brand-content {
    position: relative;
    z-index: 1;
    text-align: center;
    color: #fff;
    max-width: 420px;
    padding: 40px;
  }

  .brand-logo {
    margin-bottom: 24px;
    .logo-icon {
      width: 80px;
      height: 80px;
      border-radius: 20px;
      background: linear-gradient(135deg, #5B6AF0, #8B5CF6);
      display: inline-flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      box-shadow: 0 12px 40px rgba(91, 106, 240, 0.4);
    }
  }

  .brand-title {
    font-size: 28px;
    font-weight: 700;
    margin-bottom: 8px;
    letter-spacing: 1px;
  }

  .brand-subtitle {
    font-size: 14px;
    opacity: 0.6;
    letter-spacing: 4px;
    text-transform: uppercase;
    margin-bottom: 48px;
  }

  .brand-features {
    display: flex;
    flex-direction: column;
    gap: 16px;
    text-align: left;
    padding: 0 20px;

    .feature-item {
      display: flex;
      align-items: center;
      gap: 14px;
      font-size: 15px;
      opacity: 0.85;
      padding: 12px 16px;
      border-radius: 10px;
      background: rgba(255, 255, 255, 0.06);
      transition: background var(--transition-fast);

      &:hover {
        background: rgba(255, 255, 255, 0.1);
      }

      .el-icon {
        font-size: 20px;
        color: #8B9DF5;
      }
    }
  }
}

// ========== 右侧登录表单 ==========
.login-form-side {
  width: 480px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: var(--bg-card);
  padding: 60px;
}

.login-card {
  width: 100%;
  max-width: 380px;

  &-header {
    margin-bottom: 36px;

    h2 {
      font-size: 26px;
      font-weight: 700;
      color: var(--text-primary);
      margin-bottom: 8px;
    }

    p {
      font-size: 14px;
      color: var(--text-secondary);
    }
  }
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  letter-spacing: 4px;
}

.login-footer {
  margin-top: 40px;
  font-size: 12px;
  color: var(--text-placeholder);
}

// ========== 响应式 ==========
@media (max-width: 900px) {
  .login-brand {
    display: none;
  }
  .login-form-side {
    width: 100%;
    padding: 40px 24px;
  }
}
</style>
