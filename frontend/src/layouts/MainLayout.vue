<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="sidebarCollapse ? '64px' : '220px'" class="layout-aside">
      <!-- Logo 区域 -->
      <div class="logo-area" @click="goHome">
        <div class="logo-icon">
          <el-icon :size="22"><Reading /></el-icon>
        </div>
        <transition name="fade">
          <span v-show="!sidebarCollapse" class="logo-text">图书管理系统</span>
        </transition>
      </div>

      <!-- 导航菜单 -->
      <el-scrollbar>
        <el-menu
          :default-active="activeMenu"
          :collapse="sidebarCollapse"
          :collapse-transition="false"
          background-color="transparent"
          text-color="var(--sidebar-text)"
          active-text-color="var(--sidebar-text-active)"
          router
        >
          <template v-for="item in menuItems" :key="item.path">
            <el-menu-item v-if="!item.children" :index="item.path">
              <el-icon><component :is="item.meta.icon" /></el-icon>
              <template #title>{{ item.meta.title }}</template>
            </el-menu-item>
            <el-sub-menu v-else :index="item.path">
              <template #title>
                <el-icon><component :is="item.meta.icon" /></el-icon>
                <span>{{ item.meta.title }}</span>
              </template>
              <el-menu-item v-for="child in item.children" :key="child.path" :index="child.path">
                {{ child.meta.title }}
              </el-menu-item>
            </el-sub-menu>
          </template>
        </el-menu>
      </el-scrollbar>

      <!-- 底部用户信息 -->
      <div class="sidebar-footer" @click="handleCommand('profile')">
        <el-avatar :size="34" class="sidebar-avatar">
          {{ (authStore.userInfo?.userName || 'U')[0] }}
        </el-avatar>
        <transition name="fade">
          <div v-show="!sidebarCollapse" class="sidebar-user-text">
            <span class="sidebar-user-name">{{ authStore.userInfo?.userName || '未登录' }}</span>
            <span class="sidebar-user-role">{{ roleLabel }}</span>
          </div>
        </transition>
      </div>
    </el-aside>

    <!-- 右侧主体 -->
    <el-container class="main-container">
      <!-- 顶部导航 -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-button
            class="collapse-btn"
            :icon="sidebarCollapse ? Expand : Fold"
            text
            @click="toggleSidebar"
          />
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: homePath }">
              <el-icon><HomeFilled /></el-icon>
              <span>首页</span>
            </el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click" @command="handleCommand" popper-class="user-dropdown">
            <div class="user-trigger">
              <el-avatar :size="34" class="header-avatar">
                {{ (authStore.userInfo?.userName || 'U')[0] }}
              </el-avatar>
              <span class="user-name">{{ authStore.userInfo?.userName || '未登录' }}</span>
              <el-icon class="dropdown-arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <div class="dropdown-user-info">
                  <el-avatar :size="40" class="dropdown-avatar">
                    {{ (authStore.userInfo?.userName || 'U')[0] }}
                  </el-avatar>
                  <div>
                    <div class="dropdown-user-name">{{ authStore.userInfo?.userName }}</div>
                    <div class="dropdown-user-role">{{ roleLabel }}</div>
                  </div>
                </div>
                <el-dropdown-item command="profile" divided>
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="changePassword">
                  <el-icon><Lock /></el-icon>修改密码
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区域 -->
      <el-main class="layout-main">
        <router-view v-slot="{ Component, route }">
          <transition name="fade-slide">
            <component :is="Component" :key="route.path" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>

  <!-- 修改密码对话框 -->
  <el-dialog v-model="passwordDialogVisible" title="修改密码" width="440px" :close-on-click-modal="false">
    <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
      <el-form-item label="原密码" prop="oldPassword">
        <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码（至少6位）" />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="passwordDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">确认修改</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { storeToRefs } from 'pinia'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { useAppStore } from '@/stores/app'
import { changePassword } from '@/api/auth'
import { Reading, HomeFilled, ArrowDown, User, Lock, SwitchButton, Fold, Expand } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const appStore = useAppStore()
const { sidebarCollapse } = storeToRefs(appStore)

// 当前激活菜单
const activeMenu = computed(() => route.path)

// 当前页面标题
const currentTitle = computed(() => route.meta?.title || '')

// 角色标签
const roleMap = { admin: '超级管理员', library: '图书管理员', user: '学生', teacher: '教师' }
const roleLabel = computed(() => roleMap[authStore.userInfo?.roleCode] || '')

// 首页路径
const homePath = computed(() => {
  const role = authStore.userInfo?.roleCode
  if (role === 'user') return '/user-dashboard'
  if (role === 'teacher') return '/teacher-dashboard'
  return '/dashboard'
})

// 返回首页
function goHome() {
  router.push(homePath.value)
}

// 菜单项 - 根据权限过滤
const menuItems = computed(() => {
  const allRoutes = router.options.routes.find(r => r.path === '/')?.children || []
  return allRoutes
    .filter(r => !r.meta?.hidden)
    .filter(r => {
      if (!r.meta?.roles) return true
      return r.meta.roles.includes(authStore.userInfo?.roleCode)
    })
    .map(r => ({
      path: `/${r.path}`,
      meta: r.meta,
      children: r.children,
    }))
})

function toggleSidebar() {
  appStore.toggleSidebar()
}

// 下拉菜单命令
function handleCommand(command) {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'changePassword':
      passwordDialogVisible.value = true
      break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }).then(async () => {
        await authStore.logout()
        router.push('/login')
        ElMessage.success('已退出登录')
      }).catch(() => { /* 取消 */ })
      break
  }
}

// ========== 修改密码 ==========
const passwordDialogVisible = ref(false)
const passwordLoading = ref(false)
const passwordFormRef = ref(null)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

async function handleChangePassword() {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return

  passwordLoading.value = true
  try {
    await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    })
    ElMessage.success('密码修改成功，请重新登录')
    passwordDialogVisible.value = false
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    setTimeout(async () => {
      await authStore.logout()
      router.push('/login')
    }, 1000)
  } finally {
    passwordLoading.value = false
  }
}
</script>

<style scoped lang="scss">
.layout-container {
  height: 100%;
}

// ========== 侧边栏 ==========
.layout-aside {
  background: var(--sidebar-bg);
  display: flex;
  flex-direction: column;
  transition: width var(--transition-base);
  overflow: hidden;
  position: relative;

  .logo-area {
    height: 64px;
    display: flex;
    align-items: center;
    padding: 0 20px;
    gap: 12px;
    cursor: pointer;
    border-bottom: 1px solid rgba(255, 255, 255, 0.06);
    flex-shrink: 0;

    .logo-icon {
      width: 36px;
      height: 36px;
      border-radius: 10px;
      background: var(--sidebar-logo-gradient);
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      flex-shrink: 0;
    }

    .logo-text {
      font-size: 17px;
      font-weight: 700;
      color: #fff;
      white-space: nowrap;
      letter-spacing: 0.5px;
    }
  }

  // 自定义 el-menu 覆盖
  :deep(.el-menu) {
    border-right: none;
    background: transparent;

    .el-menu-item,
    .el-sub-menu__title {
      height: 48px;
      line-height: 48px;
      margin: 2px 8px;
      border-radius: 8px;
      font-size: 14px;

      &:hover {
        background: var(--sidebar-bg-hover) !important;
      }
    }

    .el-menu-item.is-active {
      background: var(--sidebar-bg-active) !important;
      border-left: 3px solid var(--sidebar-active-border);
      border-radius: 0 8px 8px 0;
      margin-left: 0;
      padding-left: calc(20px - 3px) !important;
      color: var(--sidebar-text-active) !important;
    }

    // 折叠模式不显示左边框
    .el-menu--collapse .el-menu-item.is-active {
      border-left: none;
      margin-left: 8px;
      border-radius: 8px;
    }

    .el-sub-menu {
      .el-menu {
        background: rgba(0, 0, 0, 0.15);

        .el-menu-item {
          padding-left: 56px !important;
          height: 40px;
          line-height: 40px;
          font-size: 13px;

          &.is-active {
            background: var(--sidebar-bg-active) !important;
          }
        }
      }
    }
  }

  :deep(.el-scrollbar) {
    flex: 1;
    overflow: hidden;
  }
}

// 侧边栏底部
.sidebar-footer {
  padding: 14px 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  flex-shrink: 0;
  transition: background var(--transition-fast);

  &:hover {
    background: var(--sidebar-bg-hover);
  }

  .sidebar-avatar {
    background: var(--sidebar-logo-gradient);
    color: #fff;
    font-weight: 600;
    font-size: 14px;
    flex-shrink: 0;
  }

  .sidebar-user-text {
    min-width: 0;
    overflow: hidden;

    .sidebar-user-name {
      display: block;
      font-size: 13px;
      color: #fff;
      font-weight: 500;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .sidebar-user-role {
      font-size: 11px;
      color: var(--sidebar-text);
      margin-top: 1px;
    }
  }
}

// ========== 主区域 ==========
.main-container {
  flex-direction: column;
  min-width: 0;
}

// ========== 顶栏 ==========
.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--bg-card);
  border-bottom: 1px solid var(--border-light);
  box-shadow: var(--shadow-xs);
  padding: 0 24px;
  height: 60px;
  flex-shrink: 0;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .collapse-btn {
      font-size: 18px;
      color: var(--text-secondary);
      transition: color var(--transition-fast);

      &:hover {
        color: var(--color-primary);
      }
    }

    :deep(.el-breadcrumb) {
      .el-breadcrumb__item {
        .el-breadcrumb__inner {
          display: inline-flex;
          align-items: center;
          gap: 4px;
          font-size: 13px;
          color: var(--text-secondary);
          font-weight: 400;

          &.is-link:hover {
            color: var(--color-primary);
          }
        }

        &:last-child .el-breadcrumb__inner {
          color: var(--text-primary);
          font-weight: 500;
        }
      }
    }
  }

  .header-right {
    .user-trigger {
      display: flex;
      align-items: center;
      gap: 10px;
      cursor: pointer;
      padding: 4px 12px 4px 4px;
      border-radius: 20px;
      transition: background var(--transition-fast);

      &:hover {
        background: var(--bg-page);
      }

      .header-avatar {
        background: var(--sidebar-logo-gradient);
        color: #fff;
        font-weight: 600;
        font-size: 14px;
      }

      .user-name {
        font-size: 14px;
        color: var(--text-primary);
        font-weight: 500;
      }

      .dropdown-arrow {
        font-size: 12px;
        color: var(--text-secondary);
      }
    }
  }
}

// ========== 下拉菜单 ==========
.dropdown-user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-bottom: 1px solid var(--border-light);
  margin-bottom: 4px;

  .dropdown-avatar {
    background: var(--sidebar-logo-gradient);
    color: #fff;
    font-weight: 600;
  }

  .dropdown-user-name {
    font-size: 14px;
    font-weight: 600;
    color: var(--text-primary);
  }

  .dropdown-user-role {
    font-size: 12px;
    color: var(--text-secondary);
    margin-top: 2px;
  }
}

// ========== 主内容区 ==========
.layout-main {
  background: var(--bg-page);
  min-height: calc(100vh - 60px);
  padding: 24px;
  overflow-y: auto;
}

// ========== 过渡动画 ==========
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
