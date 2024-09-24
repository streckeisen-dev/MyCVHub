import {
  createRouter,
  createWebHistory
} from 'vue-router'
import type {
  NavigationGuardNext,
  RouteLocationNormalized,
  RouteLocationNormalizedLoaded
} from 'vue-router'
import accountApi from '@/api/account-api'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/ui'
    },
    {
      path: '/ui',
      name: 'home',
      component: () => import('@/views/HomeView.vue')
    },
    {
      path: '/ui/login',
      name: 'login',
      component: () => import('@/views/account/Login.vue')
    },
    {
      path: '/ui/signup',
      name: 'signup',
      component: () => import('@/views/account/SignUp.vue')
    },
    {
      path: '/ui/logout',
      name: 'logout',
      component: () => import('@/views/account/Logout.vue')
    },
    {
      path: '/ui/account',
      name: 'account',
      component: () => import('@/views/account/ShowAccount.vue'),
      meta: {
        authRequired: true
      }
    },
    {
      path: '/ui/:pathMatch(.*)*',
      component: () => import('@/views/NotFound.vue')
    }
  ]
})

export default router
