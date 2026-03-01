import { CoverLetterStyleDto } from '@/types/coverletter/CoverLetterStyleDto.ts'
import {
  extractErrorIfResponseIsNotOk,
  fetchFromApi,
  getJSONIfResponseIsOk
} from '@/api/ApiHelper.ts'
import { CoverLetterGenerationRequestDto } from '@/types/coverletter/CoverLetterGenerationRequestDto.ts'

async function getStyles(locale: string): Promise<CoverLetterStyleDto[]> {
  const response = await fetchFromApi('/cover-letter/styles', locale)
  return getJSONIfResponseIsOk<CoverLetterStyleDto[]>(response)
}

async function getCoverLetter(
  generationRequest: CoverLetterGenerationRequestDto,
  locale: string
): Promise<Blob | MediaSource> {
  const response = await fetchFromApi('/cover-letter/generate', locale, {
    method: 'POST',
    body: JSON.stringify(generationRequest)
  })
  await extractErrorIfResponseIsNotOk(response)
  return response.blob()
}

export default {
  getStyles,
  getCoverLetter
}
