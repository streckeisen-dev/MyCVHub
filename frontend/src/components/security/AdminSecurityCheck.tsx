import { AdminAuthorizationContext } from '@/context/AdminAuthorizationContext.tsx'
import { Spinner } from '@heroui/react'
import { PropsWithChildren, ReactNode, use } from 'react'
import { Navigate, useLocation } from 'react-router-dom'

export type AdminSecurityCheckProps = PropsWithChildren & {
  allowMustChangePassword?: boolean
}

export function AdminSecurityCheck(props: AdminSecurityCheckProps): ReactNode {
  const { allowMustChangePassword = false, children } = props
  const { admin, isLoadingAdmin } = use(AdminAuthorizationContext)
  const location = useLocation()

  if (isLoadingAdmin) {
    return (
      <div className="flex min-h-[18rem] w-full items-center justify-center">
        <Spinner />
      </div>
    )
  }

  if (!admin) {
    return (
      <Navigate
        to="/admin/login"
        replace
        state={{ from: location.pathname }}
      />
    )
  }

  if (admin.mustChangePassword && !allowMustChangePassword) {
    return <Navigate to="/admin/change-password" replace />
  }

  return children
}
