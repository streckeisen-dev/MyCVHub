import type { CountryDto } from '@/dto/CountryDto'
import { fetchFromApi, getJSONIfResponseIsOk } from '@/api/ApiHelper'
import { RestError } from '@/api/RestError'

async function getCountries(): Promise<Array<CountryDto>> {
  try {
    const response = await fetchFromApi('/public/countries')
    return await getJSONIfResponseIsOk<Array<CountryDto>>(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to load countries', error)
  }
}

export default {
  getCountries
}
