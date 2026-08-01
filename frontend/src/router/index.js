import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

// 布局
const Layout = () => import('@/layouts/MainLayout.vue')

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/LoginView.vue'),
    meta: { title: '登录', noAuth: true },
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '首页', icon: 'HomeFilled', roles: ['admin', 'library'] },
      },
      {
        path: 'user-dashboard',
        name: 'UserDashboard',
        component: () => import('@/views/dashboard/UserDashboardView.vue'),
        meta: { title: '首页', icon: 'HomeFilled', roles: ['user'] },
      },
      {
        path: 'teacher-dashboard',
        name: 'TeacherDashboard',
        component: () => import('@/views/dashboard/TeacherDashboardView.vue'),
        meta: { title: '首页', icon: 'HomeFilled', roles: ['teacher'] },
      },
      {
        path: 'user',
        name: 'UserManage',
        component: () => import('@/views/user/UserView.vue'),
        meta: { title: '用户管理', icon: 'User', roles: ['admin', 'library'] },
      },
      {
        path: 'book',
        name: 'BookManage',
        component: () => import('@/views/book/BookView.vue'),
        meta: { title: '图书管理', icon: 'Reading', roles: ['admin', 'library'] },
      },
      {
        path: 'book-browse',
        name: 'BookBrowse',
        component: () => import('@/views/book/BookBrowseView.vue'),
        meta: { title: '图书借阅', icon: 'Reading', roles: ['user', 'teacher'] },
      },
      {
        path: 'category',
        name: 'CategoryManage',
        component: () => import('@/views/category/CategoryView.vue'),
        meta: { title: '分类管理', icon: 'Grid', roles: ['admin', 'library'] },
      },
      {
        path: 'borrow',
        name: 'BorrowManage',
        component: () => import('@/views/borrow/BorrowView.vue'),
        meta: { title: '借阅管理', icon: 'Document', roles: ['admin', 'library'] },
      },
      {
        path: 'my-borrow',
        name: 'MyBorrow',
        component: () => import('@/views/borrow/MyBorrowView.vue'),
        meta: { title: '我的借阅', icon: 'Document', roles: ['user', 'teacher'] },
      },
      {
        path: 'reserve',
        name: 'ReserveManage',
        component: () => import('@/views/reserve/ReserveView.vue'),
        meta: { title: '预约管理', icon: 'Clock', roles: ['admin', 'library'] },
      },
      {
        path: 'my-reserve',
        name: 'MyReserve',
        component: () => import('@/views/reserve/MyReserveView.vue'),
        meta: { title: '我的预约', icon: 'Clock', roles: ['user', 'teacher'] },
      },
      {
        path: 'overdue',
        name: 'OverdueManage',
        component: () => import('@/views/overdue/OverdueView.vue'),
        meta: { title: '逾期管理', icon: 'Warning', roles: ['admin', 'library'] },
      },
      {
        path: 'inventory',
        name: 'InventoryManage',
        component: () => import('@/views/inventory/InventoryView.vue'),
        meta: { title: '盘点管理', icon: 'List', roles: ['admin', 'library'] },
      },
      {
        path: 'loss',
        name: 'LossManage',
        component: () => import('@/views/loss/LossView.vue'),
        meta: { title: '损耗管理', icon: 'Delete', roles: ['admin', 'library'] },
      },
      {
        path: 'log',
        name: 'LogManage',
        component: () => import('@/views/log/LogView.vue'),
        meta: { title: '系统日志', icon: 'Tickets', roles: ['admin', 'library'] },
      },
      {
        path: 'config',
        name: 'ConfigManage',
        component: () => import('@/views/config/ConfigView.vue'),
        meta: { title: '系统配置', icon: 'Setting', roles: ['admin', 'library'] },
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '数据统计', icon: 'DataAnalysis', roles: ['admin', 'library'] },
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/ProfileView.vue'),
        meta: { title: '个人中心', icon: 'UserFilled', hidden: true },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

// 路由守卫 - 权限验证
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 图书管理系统` : '图书管理系统'

  // 不需要认证的页面直接放行
  if (to.meta.noAuth) {
    return next()
  }

  // 检查是否已登录
  const token = getToken()
  if (!token) {
    return next({ name: 'Login', query: { redirect: to.fullPath } })
  }

  // 根据用户角色动态重定向首页
  if (to.path === '/' || to.path === '/dashboard') {
    const user = localStorage.getItem('library_user')
    if (user) {
      const userInfo = JSON.parse(user)
      if (userInfo.roleCode === 'user') {
        return next('/user-dashboard')
      } else if (userInfo.roleCode === 'teacher') {
        return next('/teacher-dashboard')
      }
    }
  }

  next()
})

export default router
