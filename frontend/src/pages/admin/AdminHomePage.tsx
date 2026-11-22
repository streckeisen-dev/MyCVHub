import { AdminAuthorizationContext } from '@/context/AdminAuthorizationContext.tsx'
import { AdminRole } from '@/types/admin/AdminRole.ts'
import { use } from 'react'
import { AdminDashboardPage } from './AdminDashboardPage.tsx'
import { AdminAccountsPage } from './AdminAccountsPage.tsx'

export function AdminHomePage() {
  const { admin } = use(AdminAuthorizationContext)
  return admin?.role === AdminRole.SUPER_ADMIN ? <AdminAccountsPage /> : <AdminDashboardPage />
}
