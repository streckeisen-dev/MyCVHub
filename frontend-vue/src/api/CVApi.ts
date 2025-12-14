import { CVStyleDto } from '@/dto/CVStyleDto.ts'
import {
  commonHeaders,
  extractErrorIfResponseIsNotOk,
  fetchFromApi,
  getJSONIfResponseIsOk
} from '@/api/ApiHelper.ts'
import { RestError } from '@/api/RestError.ts'
import { CVGenerationRequestDto } from '@/dto/CVGenerationRequestDto.ts'

async function getCVStyles(): Promise<Array<CVStyleDto>> {
  try {
    const response = await fetchFromApi('/api/cv/styles')
    return await getJSONIfResponseIsOk<Array<CVStyleDto>>(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to load CV styles', error)
  }
}

async function getCV(style: string, generationRequest: CVGenerationRequestDto): Promise<Blob | MediaSource> {
  try {
    const response = await fetchFromApi(`/api/cv/generate?style=${style}`, {
      method: 'POST',
      body: JSON.stringify(generationRequest),
      headers: commonHeaders()
    })
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
