import { Navbar } from '@/components/nav/Navbar.tsx'
import { useTranslation } from 'react-i18next'
import { Link, Outlet } from 'react-router-dom'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'
import { Link as UiLink } from '@heroui/react'

export function DefaultLayout() {
  const { t } = useTranslation()
  return (
    <div className="relative flex flex-col h-screen">
      <Navbar />
      <main className="w-full mx-auto flex-grow pt-16 px-6 lg:px-15">
        <Outlet />
      </main>
      <footer className="w-full flex flex-col items-center justify-center py-3">
        <p>&copy; 2025 Streckeisen DevWorks</p>
        <div className="w-full flex justify-center items-center gap-3">
          <Link
            className="text-current"
            to={getRoutePath(RouteId.PrivacyPolicy)}
          >
            {t('privacy.title')}
          </Link>
          <UiLink
            isExternal
            className="flex items-center gap-1 text-current"
            href="https://heroui.com"
            title="heroui.com homepage"
          >
            Powered by HeroUI
          </UiLink>
        </div>
      </footer>
    </div>
  )
}
