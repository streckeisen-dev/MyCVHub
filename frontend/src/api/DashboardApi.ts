import { fetchFromApi, getJSONIfResponseIsOk } from '@/api/ApiHelper.ts'
import { RestError } from '@/types/RestError.ts'
import { DashboardInfoDto } from '@/types/dashboard/DashboardInfoDto.ts'

async function getDashboardInfo(locale: string): Promise<DashboardInfoDto> {
  try {
    const response = await fetchFromApi('/dashboard', locale)
    return await getJSONIfResponseIsOk<DashboardInfoDto>(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to load dashboard info', error)
  }
}

export default {
  getDashboardInfo
}
