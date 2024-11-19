import {
  createRouter,
  createWebHistory,
  type NavigationGuardNext,
  type RouteLocationNormalized,
  type RouteLocationNormalizedLoaded
} from 'vue-router'
import LoginStateService from '@/services/LoginStateService'
import { AccountStatus } from '@/dto/AccountStatusDto'
import RouteSecurityService from '@/services/RouteSecurityService'

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
      props: (route) => ({ redirect: route.query.redirect })
    },
    {
      path: '/ui/login/oauth-success',
      name: 'login-oauth-success',
      component: () => import('@/views/account/OAuthLoginSuccess.vue')
    },
    {
      path: '/ui/login/oauth-failure',
      name: 'login-oauth-failure',
      component: () => import('@/views/account/OAuthLoginFailure.vue')
    },
    {
      path: '/ui/login/oauth-signup',
      name: 'oauth-signup',
      component: () => import('@/views/account/OAuthSignupView.vue')
    },
    {
      path: '/ui/signup',
      name: 'signup',
      component: () => import('@/views/account/SignUpView.vue')
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
      path: '/ui/account/verification',
      name: 'account-verification',
      component: () => import('@/views/account/AccountVerificationView.vue'),
      props: (route) => ({ accountId: route.query.id, token: route.query.token })
    },
    {
      path: '/ui/account/verification/pending',
      name: 'account-verification-pending',
      component: () => import('@/views/account/AccountVerificationPendingView.vue'),
      meta: {
        authRequired: true,
        requiredAccountStatus: AccountStatus.UNVERIFIED
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

router.beforeEach(
  async (
    to: RouteLocationNormalized,
    from: RouteLocationNormalizedLoaded,
    next: NavigationGuardNext
  ) => {
    const authRequired = to.matched.some((record) => record.meta.authRequired)
    const requiredAccountStatus = to.matched.find((record) => record.meta.requiredAccountStatus)
      ?.meta.requiredAccountStatus as AccountStatus | undefined
    const isLoggedIn = LoginStateService.isLoggedIn()
    const accountStatus = LoginStateService.getAccountStatus()

    if (authRequired && !isLoggedIn) {
      next({ name: 'login', query: { redirect: to.fullPath } })
      return
    }

    if (authRequired) {
      await RouteSecurityService.enforceRouteAccessPermissions(
        requiredAccountStatus,
        accountStatus,
        next
      )
      return
    }
    next()
  }
)

export default router
