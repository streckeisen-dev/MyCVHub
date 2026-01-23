import { ApplicationTemplateDto } from '@/types/applicationTemplate/ApplicationTemplateDto.ts'
import {
  extractErrorIfResponseIsNotOk,
  fetchFromApi,
  getJSONIfResponseIsOk
} from '@/api/ApiHelper.ts'
import { RestError } from '@/types/RestError.ts'
import { ApplicationTemplateUpdateDto } from '@/types/applicationTemplate/ApplicationTemplateUpdateDto.ts'

async function getApplicationTemplates(locale: string): Promise<ApplicationTemplateDto[]> {
  try {
    const response = await fetchFromApi('/application-template', locale)
    return await getJSONIfResponseIsOk<ApplicationTemplateDto[]>(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to load application templates', error)
  }
}

async function getApplicationTemplate(id: number, locale: string): Promise<ApplicationTemplateDto> {
  try {
    const response = await fetchFromApi(`/application-template/${id}`, locale)
    return await getJSONIfResponseIsOk<ApplicationTemplateDto>(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to load application template', error)
  }
}

async function saveApplicationTemplate(request: ApplicationTemplateUpdateDto, locale: string): Promise<ApplicationTemplateDto> {
  try {
    const response = await fetchFromApi('/application-template', locale, {
      method: 'POST',
      body: JSON.stringify(request)
    })
    return await getJSONIfResponseIsOk<ApplicationTemplateDto>(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to save application template', error)
  }
}

async function deleteApplicationTemplate(id: number, locale: string): Promise<void> {
  try {
    const response = await fetchFromApi(`/application-template/${id}`, locale, {
      method: 'DELETE'
    })
    await extractErrorIfResponseIsNotOk(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to delete application template', error)
  }
}

export default {
  getApplicationTemplates,
  getApplicationTemplate,
  saveApplicationTemplate,
  deleteApplicationTemplate
}
