import { CVStyleDto } from '@/types/cv/CVStyleDto.ts'
import { extractErrorIfResponseIsNotOk, fetchFromApi, getJSONIfResponseIsOk } from '@/api/ApiHelper.ts'
import { CvConfigurationRequestDto } from '@/types/cv/CvConfigurationRequestDto.ts'

async function getCVStyles(locale: string): Promise<CVStyleDto[]> {
  const response = await fetchFromApi('/cv/styles', locale)
  return getJSONIfResponseIsOk<CVStyleDto[]>(response)
}

async function getCV(
  generationRequest: CvConfigurationRequestDto,
  locale: string
): Promise<Blob | MediaSource> {
  const response = await fetchFromApi('/cv/generate', locale, {
    method: 'POST',
    body: JSON.stringify(generationRequest)
  })
  await extractErrorIfResponseIsNotOk(response)
  return response.blob()
}

export default {
  getCVStyles,
  getCV
}
