import { createRouter, createWebHistory } from 'vue-router'

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
      component: () => import('@/views/account/LoginView.vue'),
      props: route => ({ redirect: route.query.redirect })
    },
    {
      path: '/ui/signup',
      name: 'signup',
      component: () => import('@/views/account/SignUp.vue')
    },
    {
      path: '/ui/logout',
      name: 'logout',
      component: () => import('@/views/account/LogoutView.vue')
    },
    {
      path: '/ui/account',
      name: 'account',
      component: () => import('@/views/account/AccountView.vue'),
      meta: {
        authRequired: true
      }
    },
    {
      path: '/ui/account/edit',
      name: 'edit-account',
      component: () => import('@/views/account/EditAccountView.vue'),
      meta: {
        authRequired: true
      }
    },
    {
      path: '/ui/account/change-password',
      name: 'change-password',
      component: () => import('@/views/account/ChangePasswordView.vue'),
      meta: {
        authRequired: true
      }
    },
    {
      path: '/ui/account/profile/create',
      name: 'create-profile',
      component: () => import('@/views/profile/CreateProfileView.vue'),
      meta: {
        authRequired: true
      }
    },
    {
      path: '/ui/account/profile/edit',
      name: 'edit-profile',
      component: () => import('@/views/profile/EditProfileView.vue'),
      meta: {
        authRequired: true
      }
    },
    {
      path: '/ui/profile/:alias',
      name: 'public-profile',
      component: () => import('@/views/profile/ProfileView.vue'),
      props: true,
      meta: {
        hideNavigation: true
      }
    },
    {
      path: '/ui/privacy',
      name: 'privacy-policy',
      component: () => import('@/views/PrivacyPolicy.vue')
    },
    {
      path: '/:pathMatch(.*)*',
      component: () => import('@/views/NotFound.vue')
    }
  ]
})

export default router
