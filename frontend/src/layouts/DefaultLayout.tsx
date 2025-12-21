import { Navbar } from '@/components/nav/Navbar.tsx'
import { useTranslation } from 'react-i18next'
import { Link, Outlet } from 'react-router-dom'
import { getRoutePath, RouteId } from '@/config/RouteTree.tsx'
import { ExternalLink } from '@/components/ExternalLink.tsx'

export function DefaultLayout() {
  const { t } = useTranslation()
  return (
    <div className="relative flex flex-col h-screen">
      <Navbar />
      <main className="w-full mx-auto grow pt-16 px-6 lg:px-15">
        <Outlet />
      </main>
      <footer className="w-full flex flex-col items-center justify-center py-3">
        <p>&copy; 2025 Streckeisen DevWorks</p>
        <div className="w-full flex justify-center items-center gap-3">
          <Link className="text-foreground" to={getRoutePath(RouteId.TermsOfService)}>
            {t('tos.title')}
          </Link>
          <Link className="text-foreground" to={getRoutePath(RouteId.PrivacyPolicy)}>
            {t('privacy.title')}
          </Link>
          <ExternalLink
            href="https://heroui.com"
            title="heroui.com homepage"
            color="foreground"
          >
            Powered by HeroUI
          </ExternalLink>
        </div>
      </footer>
    </div>
  )
}
