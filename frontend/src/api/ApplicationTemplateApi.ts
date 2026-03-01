import { ApplicationTemplateDto } from '@/types/applicationTemplate/ApplicationTemplateDto.ts'
import {
  extractErrorIfResponseIsNotOk,
  fetchFromApi,
  getJSONIfResponseIsOk
} from '@/api/ApiHelper.ts'
import { ApplicationTemplateUpdateDto } from '@/types/applicationTemplate/ApplicationTemplateUpdateDto.ts'

async function getApplicationTemplates(locale: string): Promise<ApplicationTemplateDto[]> {
  const response = await fetchFromApi('/application-template', locale)
  return getJSONIfResponseIsOk<ApplicationTemplateDto[]>(response)
}

async function getApplicationTemplate(id: number, locale: string): Promise<ApplicationTemplateDto> {
  const response = await fetchFromApi(`/application-template/${id}`, locale)
  return getJSONIfResponseIsOk<ApplicationTemplateDto>(response)
}

async function saveApplicationTemplate(request: ApplicationTemplateUpdateDto, locale: string): Promise<ApplicationTemplateDto> {
  const response = await fetchFromApi('/application-template', locale, {
    method: 'POST',
    body: JSON.stringify(request)
  })
  return getJSONIfResponseIsOk<ApplicationTemplateDto>(response)
}

async function deleteApplicationTemplate(id: number, locale: string): Promise<void> {
  const response = await fetchFromApi(`/application-template/${id}`, locale, {
    method: 'DELETE'
  })
  await extractErrorIfResponseIsNotOk(response)
}

export default {
  getApplicationTemplates,
  getApplicationTemplate,
  saveApplicationTemplate,
  deleteApplicationTemplate
}
