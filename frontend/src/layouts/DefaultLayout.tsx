import { Navbar } from '@/components/nav/Navbar.tsx'
import { useTranslation } from 'react-i18next'
import { Link, Outlet } from 'react-router-dom'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'
import { ExternalLink } from '@/components/ExternalLink.tsx'

export function DefaultLayout() {
  const { t } = useTranslation()
  return (
    <div className="relative flex min-h-screen flex-col">
      <Navbar />
      <main className="mx-auto w-full grow px-4 pt-12 sm:px-6 lg:px-8">
        <Outlet />
      </main>
      <footer className="w-full flex flex-col items-center justify-center gap-1 px-4 py-3 text-sm sm:text-base">
        <p>&copy; 2025 Streckeisen DevWorks</p>
        <div className="w-full flex flex-wrap justify-center items-center gap-x-4 gap-y-1">
          <Link className="text-foreground" to={getRoutePath(RouteId.TermsOfService)}>
            {t('tos.title')}
          </Link>
          <Link className="text-foreground" to={getRoutePath(RouteId.PrivacyPolicy)}>
            {t('privacy.title')}
          </Link>
          <ExternalLink href="https://heroui.com" title="heroui.com homepage" color="foreground">
            Powered by HeroUI
          </ExternalLink>
        </div>
      </footer>
    </div>
  )
}
