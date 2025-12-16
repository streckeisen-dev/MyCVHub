import { ReactNode, use, useEffect } from 'react'
import { Spinner } from '@heroui/react'
import { AuthorizationContext } from '@/context/AuthorizationContext.tsx'
import { useNavigate } from 'react-router-dom'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'
import { AuthLevel } from '@/types/AuthLevel.ts'

export function OAuthSuccessPage(): ReactNode {
  const navigate = useNavigate()
  const { user, handleUserUpdate } = use(AuthorizationContext)

  useEffect(() => {
    if (!user) {
      navigate(getRoutePath(RouteId.Login))
      return
    }
    handleUserUpdate()

    if (user.authLevel === AuthLevel.INCOMPLETE) {
      navigate(getRoutePath(RouteId.OAuthSignup))
      return
    }

    navigate(getRoutePath(RouteId.Dashboard))
  }, [])

  return <Spinner />
}
