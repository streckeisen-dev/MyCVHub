import AdminAuthApi from '@/api/AdminAuthApi.ts'
import { AdminAuthorizationContext } from '@/context/AdminAuthorizationContext.tsx'
import { AdminRole } from '@/types/admin/AdminRole.ts'
import { use } from 'react'
import { useTranslation } from 'react-i18next'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { FaChartLine, FaUsers, FaUserShield, FaArrowRightFromBracket, FaClipboardList } from 'react-icons/fa6'

export function AdminLayout() {
  const { t, i18n } = useTranslation()
  const { admin, handleAdminLogout } = use(AdminAuthorizationContext)
  const navigate = useNavigate()
  async function logout() {
    try {
      await AdminAuthApi.logout(i18n.language)
      handleAdminLogout()
      navigate('/admin/login', { replace: true })
    } catch {
      // Keep the session visible if the server could not clear its cookies.
    }
  }
  const showNavigation = admin && !admin.mustChangePassword
  const linkClass = ({ isActive }: { isActive: boolean }) => `flex min-h-10 items-center gap-2 border-b-2 px-3 text-sm font-medium ${isActive ? 'border-accent text-foreground' : 'border-transparent text-default-600 hover:text-foreground'}`
  return <div className="min-h-screen bg-background text-foreground">
    <header className="border-b border-default-200 bg-content1">
      <div className="mx-auto flex min-h-16 max-w-7xl flex-wrap items-center justify-between gap-3 px-4 py-3 sm:px-6">
        <span className="text-base font-semibold">MyCVHub <span className="text-default-500">/ {t('admin.layout.title')}</span></span>
        {admin && <div className="flex min-w-0 items-center gap-3"><span className="break-all text-sm text-default-600">{admin.username}</span><button type="button" className="flex min-h-10 items-center gap-2 rounded px-3 text-sm hover:bg-default-100" onClick={() => { void logout() }}><FaArrowRightFromBracket aria-hidden />{t('admin.logout')}</button></div>}
      </div>
      {showNavigation && <nav aria-label={t('admin.layout.navigation')} className="mx-auto flex max-w-7xl gap-1 overflow-x-auto px-4 sm:px-6">
        {admin.role === AdminRole.ADMIN ? <><NavLink to="/admin" end className={linkClass}><FaChartLine aria-hidden />{t('admin.dashboard.title')}</NavLink><NavLink to="/admin/users" className={linkClass}><FaUsers aria-hidden />{t('admin.users.title')}</NavLink><NavLink to="/admin/audit-log" className={linkClass}><FaClipboardList aria-hidden />{t('admin.audit.title')}</NavLink></> : <NavLink to="/admin" end className={linkClass}><FaUserShield aria-hidden />{t('admin.accounts.title')}</NavLink>}
      </nav>}
    </header>
    <Outlet />
  </div>
}
