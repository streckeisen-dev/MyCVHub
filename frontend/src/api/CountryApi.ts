import { CountryDto } from '@/types/country/CountryDto.ts'
import { fetchFromApi, getJSONIfResponseIsOk } from '@/api/ApiHelper.ts'

async function getCountries(locale: string): Promise<CountryDto[]> {
  const response = await fetchFromApi('/public/countries', locale)
  return getJSONIfResponseIsOk<CountryDto[]>(response)
}

export default {
  getCountries
}
