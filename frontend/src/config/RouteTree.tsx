import { RouteObject } from 'react-router-dom'
import { DefaultLayout } from '@/layouts/DefaultLayout.tsx'
import { AuthLevel } from '@/types/account/AuthLevel.ts'
import HomePage from '@/pages/HomePage.tsx'
import { PublicProfilePage } from '@/pages/profile/PublicProfilePage.tsx'
import { KeyValueObject } from '@/types/KeyValueObject.ts'
import { NotFoundPage } from '@/pages/NotFoundPage.tsx'
import LoginPage from '@/pages/account/LoginPage.tsx'
import { LogoutPage } from '@/pages/account/LogoutPage.tsx'
import { DashboardPage } from '@/pages/dashboard/DashboardPage.tsx'
import { AccountPage } from '@/pages/account/AccountPage.tsx'
import { EditAccountPage } from '@/pages/account/EditAccountPage.tsx'
import { OAuthSuccessPage } from '@/pages/account/oauth/OAuthSuccessPage.tsx'
import { OAuthSignupPage } from '@/pages/account/oauth/OAuthSignupPage.tsx'
import { PrivacyPolicyPage } from '@/pages/policy/PrivacyPolicyPage.tsx'
import { ChangePasswordPage } from '@/pages/account/ChangePasswordPage.tsx'
import { SignupPage } from '@/pages/account/SignupPage.tsx'
import { AccountVerificationPage } from '@/pages/account/AccountVerificationPage.tsx'
import { FaUser } from 'react-icons/fa6'
import { FaSignOutAlt } from 'react-icons/fa'
import { CreateProfilePage } from '@/pages/profile/CreateProfilePage.tsx'
import { EditProfilePage } from '@/pages/profile/EditProfilePage.tsx'
import { ApplicationsPage } from '@/pages/applications/ApplicationsPage.tsx'
import { AuthorizedUser } from '@/context/AuthorizationContext.tsx'
import { CvDownloadPage } from '@/pages/cv/CvDownloadPage.tsx'
import { SecurityCheck } from '@/components/security/SecurityCheck.tsx'
import { ApplicationDetailsPage } from '@/pages/applications/ApplicationDetailsPage.tsx'
import { OAuthFailurePage } from '@/pages/account/oauth/OAuthFailurePage.tsx'
import { TermsOfServicePage } from '@/pages/policy/TermsOfServicePage.tsx'
import { AboutPage } from '@/AboutPage.tsx'

type MyCvRouteObject = Omit<RouteObject, 'children'> & {
  id: string
  requiresAuth?: boolean
  minAuthLevel?: AuthLevel
  children?: MyCvRouteObject[]
}

function defineRoutes<T extends readonly MyCvRouteObject[]>(routes: T) {
  return routes
}

const ROUTE_DEFINITIONS = defineRoutes([
  {
    id: 'Root',
    element: <DefaultLayout />,
    requiresAuth: false,
    path: '/',
    errorElement: <NotFoundPage />,
    children: [
      {
        id: 'Home',
        index: true,
        element: <HomePage />,
        requiresAuth: false
      },
      {
        id: 'LoginRoot',
        path: 'login',
        children: [
          {
            id: 'Login',
            index: true,
            element: <LoginPage />,
            requiresAuth: false
          },
          {
            id: 'OAuthSuccess',
            path: 'oauth-success',
            element: <OAuthSuccessPage />,
            requiresAuth: true,
            minAuthLevel: AuthLevel.INCOMPLETE
          },
          {
            id: 'OAuthFailure',
            path: 'oauth-failure',
            element: <OAuthFailurePage />,
            requiresAuth: false
          },
          {
            id: 'OAuthSignup',
            path: 'oauth-signup',
            element: <OAuthSignupPage />,
            minAuthLevel: AuthLevel.INCOMPLETE
          }
        ]
      },
      {
        id: 'Signup',
        path: 'register',
        element: <SignupPage />,
        requiresAuth: false
      },
      {
        id: 'Logout',
        path: 'logout',
        element: <LogoutPage />,
        requiresAuth: false
      },
      {
        id: 'Dashboard',
        path: 'dashboard',
        element: <DashboardPage />,
        minAuthLevel: AuthLevel.UNVERIFIED
      },
      {
        id: 'AccountRoot',
        path: 'account',
        children: [
          {
            id: 'Account',
            index: true,
            element: <AccountPage />
          },
          {
            id: 'EditAccount',
            path: 'edit',
            element: <EditAccountPage />
          },
          {
            id: 'ChangePassword',
            path: 'change-password',
            element: <ChangePasswordPage />
          },
          {
            id: 'AccountVerification',
            path: 'verification',
            element: <AccountVerificationPage />,
            minAuthLevel: AuthLevel.UNVERIFIED
          }
        ]
      },
      {
        id: 'ApplicationsRoot',
        path: 'applications',
        children: [
          {
            id: 'ApplicationsOverview',
            index: true,
            element: <ApplicationsPage />
          },
          {
            id: 'ApplicationDetail',
            path: ':id',
            element: <ApplicationDetailsPage />
          }
        ]
      },
      {
        id: 'ProfileRoot',
        path: 'profile',
        children: [
          {
            id: 'CreateProfile',
            path: 'create',
            element: <CreateProfilePage />
          },
          {
            id: 'EditProfile',
            path: 'edit',
            element: <EditProfilePage />
          }
        ]
      },
      {
        id: 'CvDownload',
        path: 'download/cv',
        element: <CvDownloadPage />
      },
      {
        id: 'About',
        path: 'about',
        element: <AboutPage />,
        requiresAuth: false
      },
      {
        id: 'PrivacyPolicy',
        path: 'privacy',
        element: <PrivacyPolicyPage />,
        requiresAuth: false
      },
      {
        id: 'TermsOfService',
        path: 'terms',
        element: <TermsOfServicePage />,
        requiresAuth: false
      },
      {
        id: 'NotFound',
        path: '*',
        element: <NotFoundPage />,
        requiresAuth: false
      }
    ]
  },
  {
    id: 'ProfileRoot',
    path: '/cv',
    children: [
      {
        id: 'PublicProfileIndex',
        index: true,
        element: <NotFoundPage />,
        requiresAuth: false
      },
      {
        id: 'PublicProfile',
        path: ':username',
        element: <PublicProfilePage />,
        requiresAuth: false
      },
      {
        id: 'PublicProfileNotFound',
        path: '*',
        element: <NotFoundPage />,
        requiresAuth: false
      }
    ]
  }
] as const)

function toRouteObject(route: MyCvRouteObject): RouteObject {
  const { id: _id, index, requiresAuth, minAuthLevel, children, element, ...config } = route
  const newElement = element ? (
    <SecurityCheck requiresAuth={requiresAuth} minAuthLevel={minAuthLevel}>
      {element}
    </SecurityCheck>
  ) : (
    element
  )
  if (index) {
    return {
      ...config,
      element: newElement,
      index: true
    }
  }
  return {
    ...config,
    element: newElement,
    children: children?.map(toRouteObject)
  }
}

export const ROUTES = ROUTE_DEFINITIONS.map(toRouteObject)

type NodesToUnion<T> = T extends readonly (infer N)[]
  ? N extends MyCvRouteObject
    ? N['id'] | (N['children'] extends undefined ? never : NodesToUnion<N['children']>)
    : never
  : never

export type RouteId = NodesToUnion<typeof ROUTE_DEFINITIONS>

export const RouteId = new Proxy(
  {},
  {
    get(_, prop: string) {
      // runtime: return the string; compile-time: use the RouteId type
      return prop
    }
  }
) as { [K in RouteId]: K }

const routeMap: KeyValueObject<string> = {}
function findRoutes(routeObject: MyCvRouteObject, prefix = '') {
  const routePath = routeObject.path ?? ''
  if (routeObject.children) {
    routeObject.children.forEach((route) => {
      const newPrefix = prefix === '' ? routeObject.path : mergePath(prefix, routePath)
      findRoutes(route, newPrefix)
    })
    return
  }

  routeMap[routeObject.id] = routeObject.index ? prefix : mergePath(prefix, routePath)
}

function mergePath(prefix: string, path: string) {
  return prefix.endsWith('/') ? prefix + path : prefix + '/' + path
}

ROUTE_DEFINITIONS.map((root) => {
  findRoutes(root, '')
})

export function getRoutePath(routeId: RouteId, hash?: string, ...params: string[]): string {
  const id = routeId as string
  const path = routeMap[id]

  if (params) {
    const parts = path.split('/')
    const finalParts: string[] = []

    let paramIndex = 0
    for (const part of parts) {
      if (part.startsWith(':')) {
        if (paramIndex >= params.length) {
          throw new Error(
            `Path requires ${parts.filter((p) => p.startsWith(':')).length} parameters`
          )
        }
        const param = params[paramIndex]
        finalParts.push(param)
        paramIndex++
      } else {
        finalParts.push(part)
      }
    }
    return finalParts.join('/') + (hash ? `#${hash}` : '')
  }

  return path
}

interface NavItemConfig {
  id: string
  label: string
  href: string | ((user: AuthorizedUser | undefined) => string)
  predicate: (user: AuthorizedUser | undefined) => boolean
  newTab?: boolean
}

const navItems: NavItemConfig[] = [
  {
    id: 'home',
    label: 'app.home',
    href: getRoutePath(RouteId.Home),
    predicate: (user: AuthorizedUser | undefined) => user == null
  },
  {
    id: 'dashboard',
    label: 'dashboard.title',
    href: getRoutePath(RouteId.Dashboard),
    predicate: (user: AuthorizedUser | undefined) => user != null
  },
  {
    id: 'publicProfile',
    label: 'app.publicProfile',
    href: (user) => getRoutePath(RouteId.PublicProfile, undefined, user?.username ?? ''),
    predicate: (user: AuthorizedUser | undefined) => user?.hasProfile ?? false,
    newTab: true
  },
  {
    id: 'cvDownload',
    label: 'app.generateCV',
    href: getRoutePath(RouteId.CvDownload),
    predicate: (user: AuthorizedUser | undefined) => user?.hasProfile ?? false
  },
  {
    id: 'applications',
    label: 'application.title',
    href: getRoutePath(RouteId.ApplicationsOverview),
    predicate: (user: AuthorizedUser | undefined) => user?.hasProfile ?? false
  },
  {
    id: 'about',
    label: 'about.title',
    href: getRoutePath(RouteId.About),
    predicate: () => true
  }
]

export const SITE_CONFIG = {
  name: 'MyCVHub',
  description: 'Create beautiful CVs and keep track of your job applications.',
  navItems: navItems,
  accountMenu: [
    {
      label: 'account.title',
      href: getRoutePath(RouteId.Account),
      icon: <FaUser />
    },
    {
      label: 'account.logout.action',
      href: getRoutePath(RouteId.Logout),
      icon: <FaSignOutAlt />
    }
  ],
  links: {
    github: 'https://github.com/streckeisen-dev/MyCVHub',
    sponsor: 'https://patreon.com/jrgarciadev'
  },
  contact: 'contact@mycvhub.ch'
}
