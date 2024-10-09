import type { CountryDto } from '@/dto/CountryDto'
import { fetchFromApi, getJSONIfResponseIsOk } from '@/api/ApiHelper'

async function getCountries(): Promise<Array<CountryDto>> {
  try {
    const response = await fetchFromApi("/public/countries")
    const countries = await getJSONIfResponseIsOk<Array<CountryDto>>(response)
    return Promise.resolve(countries)
  } catch (error) {
    return Promise.reject(error)
  }
}

export default {
  getCountries
}