import { CountryDto } from '@/types/country/CountryDto.ts'
import { fetchFromApi, getJSONIfResponseIsOk } from '@/api/ApiHelper.ts'
import { RestError } from '@/types/RestError.ts'

async function getCountries(locale: string): Promise<CountryDto[]> {
  try {
    const response = await fetchFromApi('/public/countries', locale)
    return await getJSONIfResponseIsOk<CountryDto[]>(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to load countries', error)
  }
}

export default {
  getCountries
}