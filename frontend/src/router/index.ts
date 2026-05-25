import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('@/views/Register.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          name: 'Dashboard',
          component: () => import('@/views/Dashboard.vue')
        },
        {
          path: 'projects',
          name: 'ProjectList',
          component: () => import('@/views/ProjectList.vue')
        },
        {
          path: 'projects/:id',
          name: 'ProjectDetail',
          component: () => import('@/views/ProjectDetail.vue')
        },
        {
          path: 'workspaces',
          name: 'WorkspaceList',
          component: () => import('@/views/WorkspaceList.vue')
        },
        {
          path: 'my-tasks',
          name: 'MyTasks',
          component: () => import('@/views/MyTasks.vue')
        },
        {
          path: 'documents',
          name: 'DocumentCenter',
          component: () => import('@/views/DocumentCenter.vue')
        },
        {
          path: 'reports',
          name: 'Report',
          component: () => import('@/views/Report.vue')
        },
        {
          path: 'settings',
          name: 'Settings',
          component: () => import('@/views/Settings.vue')
        },
        {
          path: 'admin/users',
          name: 'UserManagement',
          component: () => import('@/views/UserManagement.vue')
        }
      ]
    }
  ]
})

router.beforeEach((to, _from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth !== false && !authStore.accessToken) {
    next({ path: '/login', query: { redirect: to.fullPath } })
  } else if ((to.path === '/login' || to.path === '/register') && authStore.accessToken) {
    next({ path: '/' })
  } else if ((to.path.startsWith('/admin/') || to.path === '/workspaces') && authStore.user?.username !== 'admin') {
    next({ path: '/' })
  } else {
    next()
  }
})

export default router
