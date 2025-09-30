import { CVStyleDto } from '@/dto/CVStyleDto.ts'
import {
  extractErrorIfResponseIsNotOk,
  fetchFromApi,
  getJSONIfResponseIsOk
} from '@/api/ApiHelper.ts'
import { RestError } from '@/api/RestError.ts'

async function getCVStyles(): Promise<Array<CVStyleDto>> {
  try {
    const response = await fetchFromApi('/api/cv/styles')
    return await getJSONIfResponseIsOk<Array<CVStyleDto>>(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to load CV styles', error)
  }
}

async function getCV(style: string): Promise<Blob | MediaSource> {
  try {
    const response = await fetchFromApi(`/api/cv/generate?style=${style}`)
    await extractErrorIfResponseIsNotOk(response)
    return await response.blob()
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to generate CV', error)
  }
}

export default {
  getCVStyles,
  getCV
}
