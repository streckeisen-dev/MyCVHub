import { fetchFromApi, getJSONIfResponseIsOk } from '@/api/ApiHelper.ts'
import { DashboardInfoDto } from '@/types/dashboard/DashboardInfoDto.ts'

async function getDashboardInfo(locale: string): Promise<DashboardInfoDto> {
  const response = await fetchFromApi('/dashboard', locale)
  return getJSONIfResponseIsOk<DashboardInfoDto>(response)
}

export default {
  getDashboardInfo
}
