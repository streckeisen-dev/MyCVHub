import { useHref, useNavigate } from 'react-router-dom'

import { HeroUIProvider } from '@heroui/system'
import { ToastProvider } from '@heroui/react'
import { useTranslation } from 'react-i18next'
import { AuthorizationProvider } from '@/context/AuthorizationContext.tsx'
import { PropsWithChildren } from 'react'

export function Provider(props: Readonly<PropsWithChildren>) {
  const { children } = props
  const navigate = useNavigate()
  const { i18n } = useTranslation()

  return (
    <HeroUIProvider
      navigate={navigate}
      useHref={useHref}
      locale={i18n.language}
    >
      <ToastProvider
        placement="top-right"
        toastOffset={60}
        toastProps={{
          shouldShowTimeoutProgress: true,
          timeout: 0
        }}
      />
      <AuthorizationProvider>{children}</AuthorizationProvider>
    </HeroUIProvider>
  )
}
