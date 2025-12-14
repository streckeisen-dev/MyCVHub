import { PropsWithChildren, ReactNode, use } from 'react'
import {
  AuthorizationContext,
  AuthorizedUser
} from '@/context/AuthorizationContext.tsx'
import { AuthLevel } from '@/types/AuthLevel.ts'
import { UnauthorizedPage } from '@/pages/UnauthorizedPage.tsx'

const DEFAULT_AUTH_REQUIRED = true
const DEFAULT_MIN_AUTH_LEVEL = AuthLevel.VERIFIED

export type SecurityCheckProps = PropsWithChildren & {
  requiresAuth: boolean | undefined
  minAuthLevel: AuthLevel | undefined
}

function canUserAccessRoute(
  authorizedUser: AuthorizedUser | undefined,
  requiresAuth: boolean,
  minAuthLevel?: AuthLevel
): boolean {
  if (!requiresAuth) {
    return true
  }

  if (!authorizedUser) {
    return false
  }

  const requiredAuthLevel = minAuthLevel ?? DEFAULT_MIN_AUTH_LEVEL

  switch (requiredAuthLevel) {
    case AuthLevel.INCOMPLETE:
      return true
    case AuthLevel.UNVERIFIED:
      return authorizedUser.authLevel !== AuthLevel.INCOMPLETE
    case AuthLevel.VERIFIED:
      return !(
        authorizedUser.authLevel === AuthLevel.INCOMPLETE ||
        authorizedUser.authLevel === AuthLevel.UNVERIFIED
      )
  }
}

export function SecurityCheck(props: SecurityCheckProps): ReactNode {
  const { requiresAuth, minAuthLevel, children } = props
  const { user } = use(AuthorizationContext)
  
  const doesRequireAuth = requiresAuth ?? DEFAULT_AUTH_REQUIRED
  const canAccess = canUserAccessRoute(user, doesRequireAuth, minAuthLevel)
  
  return <>
    {canAccess && children}
    {!canAccess && <UnauthorizedPage />}
  </>
}