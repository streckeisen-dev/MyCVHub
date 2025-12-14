import type { NavigateOptions } from 'react-router-dom'

import { HeroUIProvider } from '@heroui/system'
import { useHref, useNavigate } from 'react-router-dom'
import { ToastProvider } from '@heroui/react'
import { useTranslation } from 'react-i18next'
import { AuthorizationProvider } from '@/context/AuthorizationContext.tsx'

declare module '@react-types/shared' {
  interface RouterConfig {
    routerOptions: NavigateOptions;
  }
}

export function Provider({ children }: { children: React.ReactNode }) {
  const navigate = useNavigate()
  const {i18n} = useTranslation()

  return (
    <HeroUIProvider navigate={navigate} useHref={useHref} locale={i18n.language}>
      <ToastProvider
        placement="top-right"
        toastOffset={60}
        toastProps={{
          shouldShowTimeoutProgress: true,
          timeout: 0
        }}
      />
      <AuthorizationProvider>
        {children}
      </AuthorizationProvider>
    </HeroUIProvider>
  )
}
