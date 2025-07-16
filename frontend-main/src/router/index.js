import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import MainView from '../views/MainView.vue'
import Home from '../components/Home.vue' // 重新导入 Home 组件
import Navigation from '../components/Navigation.vue'
import MapQuery from '../components/MapQuery.vue'
import UserManagement from '../components/UserManagement.vue'
import { useAuthStore } from '../stores/auth.js'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'login',
      component: LoginView,
      meta: { requiresGuest: true } // 只允许未登录用户访问
    },
    {
      path: '/main',
      name: 'main',
      component: MainView,
      meta: { requiresAuth: true }, // 需要登录才能访问
      // 默认重定向到首页
      redirect: '/main/home',
      children: [
        {
          path: 'home', // 添加首页路由
          name: 'home',
          component: Home,
          meta: { requiresAuth: true }
        },
        {
          path: 'navigation',
          name: 'navigation',
          component: Navigation,
          meta: { requiresAuth: true }
        },
        // {
        //   path: 'map-query',
        //   name: 'map-query',
        //   component: MapQuery,
        //   meta: { requiresAuth: true }
        // },
        {
          path: 'user-management',
          name: 'user-management',
          component: UserManagement,
          meta: { requiresAuth: true }
        }
      ]
    }
  ]
})

/**
 * 路由守卫
 * 检查用户认证状态并进行相应的重定向
 */
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  // 检查路由是否需要认证
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!authStore.isAuthenticated) {
      // 用户未登录，重定向到登录页
      next({
        path: '/',
        query: { redirect: to.fullPath } // 保存重定向路径
      })
    } else {
      next()
    }
  } 
  // 检查路由是否只允许未登录用户访问
  else if (to.matched.some(record => record.meta.requiresGuest)) {
    if (authStore.isAuthenticated) {
      // 用户已登录，重定向到主页
      next('/main')
    } else {
      next()
    }
  } 
  else {
    next()
  }
})

export default router