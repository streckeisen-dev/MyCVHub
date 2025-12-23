import { CVStyleDto } from '@/types/cv/CVStyleDto.ts'
import {
  extractErrorIfResponseIsNotOk,
  fetchFromApi,
  getJSONIfResponseIsOk
} from '@/api/ApiHelper.ts'
import { RestError } from '@/types/RestError.ts'
import { CVGenerationRequestDto } from '@/types/cv/CVGenerationRequestDto.ts'

async function getCVStyles(locale: string): Promise<CVStyleDto[]> {
  try {
    const response = await fetchFromApi('/cv/styles', locale)
    return await getJSONIfResponseIsOk<CVStyleDto[]>(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to load CV styles', error)
  }
}

async function getCV(
  style: string,
  generationRequest: CVGenerationRequestDto,
  locale: string
): Promise<Blob | MediaSource> {
  try {
    const response = await fetchFromApi(`/cv/generate?style=${style}`, locale, {
      method: 'POST',
      body: JSON.stringify(generationRequest)
    })
    await extractErrorIfResponseIsNotOk(response)
    const blob = await response.blob()
    return blob
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to generate CV', error)
  }
}

export default {
  getCVStyles,
  getCV
}
