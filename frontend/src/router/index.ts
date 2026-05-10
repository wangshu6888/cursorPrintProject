import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import MainLayout from '@/layouts/MainLayout.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: () => import('@/views/Login.vue'), meta: { public: true } },
    {
      path: '/',
      component: MainLayout,
      redirect: '/dashboard',
      children: [
        { path: 'dashboard', component: () => import('@/views/dashboard/index.vue'), meta: { title: '工作台' } },
        { path: 'order', component: () => import('@/views/order/index.vue'), meta: { title: '订单管理' } },
        { path: 'customer', component: () => import('@/views/customer/index.vue'), meta: { title: '客户管理' } },
        { path: 'knife-mold', component: () => import('@/views/knife-mold/index.vue'), meta: { title: '刀模管理' } },
        { path: 'statistics', component: () => import('@/views/statistics/index.vue'), meta: { title: '数据统计' } },
        { path: 'system/user', component: () => import('@/views/system/user/index.vue'), meta: { title: '用户管理' } },
        { path: 'system/role', component: () => import('@/views/system/role/index.vue'), meta: { title: '角色管理' } },
        { path: 'system/menu', component: () => import('@/views/system/menu/index.vue'), meta: { title: '菜单管理' } },
        { path: 'system/log', component: () => import('@/views/system/log/index.vue'), meta: { title: '操作日志' } },
      ],
    },
  ],
})

function menuPaths(menus: { path?: string; children?: unknown[] }[]): Set<string> {
  const s = new Set<string>()
  const walk = (list: typeof menus) => {
    for (const m of list) {
      if (m.path) s.add(m.path.replace(/^\//, ''))
      if (m.children?.length) walk(m.children as typeof menus)
    }
  }
  walk(menus)
  return s
}

router.beforeEach(async (to, _from, next) => {
  if (to.meta.public) {
    return next()
  }
  const u = useUserStore()
  if (!u.token) {
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }
  if (!u.menus.length && to.path !== '/login') {
    try {
      await u.fetchMe()
    } catch {
      u.clear()
      return next({ path: '/login' })
    }
  }
  const allowed = menuPaths(u.menus)
  const p = to.path.replace(/^\//, '')
  if (to.path === '/' || p === 'dashboard') {
    return next()
  }
  if (!allowed.has(p)) {
    return next({ path: '/dashboard' })
  }
  next()
})

export default router
