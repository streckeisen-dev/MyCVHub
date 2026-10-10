import { fetchFromAdminApi } from '@/api/AdminAuthApi.ts'
import { getJSONIfResponseIsOk } from '@/api/ApiHelper.ts'
import { AdminDashboardDto } from '@/types/admin/AdminDashboardDto.ts'

async function getDashboard(locale: string, range = '30d'): Promise<AdminDashboardDto> {
  const response = await fetchFromAdminApi(`/admin/dashboard?range=${encodeURIComponent(range)}`, locale)
  return getJSONIfResponseIsOk<AdminDashboardDto>(response)
}

export default {
  getDashboard
}
