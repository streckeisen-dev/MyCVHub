import { ToastProvider } from '@heroui/react'
import { AuthorizationProvider } from '@/context/AuthorizationContext.tsx'
import { AdminAuthorizationProvider } from '@/context/AdminAuthorizationContext.tsx'
import { PropsWithChildren } from 'react'

export function Provider(props: Readonly<PropsWithChildren>) {
  const { children } = props

  return (
    <>
      <ToastProvider
        placement="top end"
      />
      <AuthorizationProvider>
        <AdminAuthorizationProvider>{children}</AdminAuthorizationProvider>
      </AuthorizationProvider>
    </>
  )
}
