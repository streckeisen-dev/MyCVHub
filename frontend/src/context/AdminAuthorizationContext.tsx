import AdminAuthApi from '@/api/AdminAuthApi.ts'
import { AdminRole } from '@/types/admin/AdminRole.ts'
import { createContext, PropsWithChildren, ReactNode, useCallback, useEffect, useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'

export interface AuthorizedAdmin {
  username: string
  role: AdminRole
  mustChangePassword: boolean
}

export type AdminUpdateFunction = () => void

export type AdminLogoutFunction = () => void

export interface AdminAuthorizationContextValue {
  admin: AuthorizedAdmin | undefined
  isLoadingAdmin: boolean
  handleAdminUpdate: AdminUpdateFunction
  handleAdminLogout: AdminLogoutFunction
}

export const AdminAuthorizationContext = createContext<AdminAuthorizationContextValue>({
  admin: undefined,
  isLoadingAdmin: true,
  handleAdminUpdate: () => { /* empty */ },
  handleAdminLogout: () => { /* empty */ }
})

export function AdminAuthorizationProvider(props: Readonly<PropsWithChildren>): ReactNode {
  const { i18n } = useTranslation()
  const { children } = props

  const [admin, setAdmin] = useState<AuthorizedAdmin>()
  const [isLoading, setIsLoading] = useState(true)

  const handleAdminUpdate = useCallback<AdminUpdateFunction>(() => {
    async function getAdminAuth() {
      setIsLoading(true)
      try {
        const auth = await AdminAuthApi.verifyLogin(i18n.language)
        setAdmin({
          username: auth.username,
          role: auth.role,
          mustChangePassword: auth.mustChangePassword
        })
      } catch (_ignore) {
        setAdmin(undefined)
      } finally {
        setIsLoading(false)
      }
    }
    getAdminAuth()
  }, [i18n.language])

  useEffect(() => {
    handleAdminUpdate()
  }, [handleAdminUpdate])

  const handleAdminLogout = useCallback<AdminLogoutFunction>(() => {
    setAdmin(undefined)
  }, [])

  const contextValue = useMemo<AdminAuthorizationContextValue>(() => {
    return {
      admin,
      isLoadingAdmin: isLoading,
      handleAdminUpdate,
      handleAdminLogout
    }
  }, [admin, handleAdminLogout, handleAdminUpdate, isLoading])

  return (
    <AdminAuthorizationContext value={contextValue}>
      {children}
    </AdminAuthorizationContext>
  )
}
