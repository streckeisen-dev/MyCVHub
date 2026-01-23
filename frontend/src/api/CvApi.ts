import { CVStyleDto } from '@/types/cv/CVStyleDto.ts'
import { extractErrorIfResponseIsNotOk, fetchFromApi, getJSONIfResponseIsOk } from '@/api/ApiHelper.ts'
import { RestError } from '@/types/RestError.ts'
import { CvConfigurationRequestDto } from '@/types/cv/CvConfigurationRequestDto.ts'

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
  generationRequest: CvConfigurationRequestDto,
  locale: string
): Promise<Blob | MediaSource> {
  try {
    const response = await fetchFromApi('/cv/generate', locale, {
      method: 'POST',
      body: JSON.stringify(generationRequest)
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
