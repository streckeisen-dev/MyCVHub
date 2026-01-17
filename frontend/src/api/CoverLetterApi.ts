import { CoverLetterStyleDto } from '@/types/coverletter/CoverLetterStyleDto.ts'
import {
  extractErrorIfResponseIsNotOk,
  fetchFromApi,
  getJSONIfResponseIsOk
} from '@/api/ApiHelper.ts'
import { RestError } from '@/types/RestError.ts'
import { CoverLetterGenerationRequestDto } from '@/types/coverletter/CoverLetterGenerationRequestDto.ts'

async function getStyles(locale: string): Promise<CoverLetterStyleDto[]> {
  try {
    const response = await fetchFromApi('/cover-letter/styles', locale)
    return await getJSONIfResponseIsOk<CoverLetterStyleDto[]>(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to load cover letter styles', error)
  }
}

async function getCoverLetter(
  generationRequest: CoverLetterGenerationRequestDto,
  locale: string
): Promise<Blob | MediaSource> {
  try {
    const response = await fetchFromApi('/cover-letter/generate', locale, {
      method: 'POST',
      body: JSON.stringify(generationRequest)
    })
    await extractErrorIfResponseIsNotOk(response)
    return await response.blob()
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to generate cover letter', error)
  }
}

export default {
  getStyles,
  getCoverLetter
}
