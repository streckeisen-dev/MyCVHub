import { ToastProvider } from '@heroui/react'
import { AuthorizationProvider } from '@/context/AuthorizationContext.tsx'
import { PropsWithChildren } from 'react'

export function Provider(props: Readonly<PropsWithChildren>) {
  const { children } = props

  return (
    <>
      <ToastProvider
        placement="top end"
      />
      <AuthorizationProvider>{children}</AuthorizationProvider>
    </>
  )
}
