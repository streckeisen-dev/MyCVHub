import { Page } from '@/types/Page.ts'
import { ApplicationSearchDto } from '@/types/application/ApplicationSearchDto.ts'
import {
  extractErrorIfResponseIsNotOk,
  fetchFromApi,
  getJSONIfResponseIsOk
} from '@/api/ApiHelper.ts'
import { ApplicationUpdateRequestDto } from '@/types/application/ApplicationUpdateRequestDto.ts'
import { ApplicationStatusDto } from '@/types/application/ApplicationStatusDto.ts'
import { ApplicationDetailsDto } from '@/types/application/ApplicationDetailsDto.ts'
import { ApplicationTransitionRequestDto } from '@/types/application/ApplicationTransitionRequestDto.ts'
import { ApplicationSearchRequest } from '@/types/application/ApplicationSearchRequest.ts'

async function getApplication(id: number, locale: string): Promise<ApplicationDetailsDto> {
  const response = await fetchFromApi(`/application/${id}`, locale)
  return getJSONIfResponseIsOk<ApplicationDetailsDto>(response)
}

async function getApplicationStatuses(locale: string): Promise<ApplicationStatusDto[]> {
  const response = await fetchFromApi('/application/statuses', locale)
  return getJSONIfResponseIsOk<ApplicationStatusDto[]>(response)
}

async function search(
  searchRequest: ApplicationSearchRequest,
  locale: string,
  signal: AbortSignal
): Promise<Page<ApplicationSearchDto>> {
  const params = new URLSearchParams({
    page: searchRequest.page.toString(),
    pageSize: searchRequest.pageSize
  })
  if (searchRequest.searchTerm) {
    params.append('searchTerm', searchRequest.searchTerm)
  }
  if (searchRequest.status) {
    params.append('status', searchRequest.status)
  }
  if (searchRequest.includeArchived) {
    params.append('includeArchived', searchRequest.includeArchived.toString())
  }
  if (searchRequest.sort) {
    params.append('sort', searchRequest.sort.column as string)
    params.append('sortDirection', searchRequest.sort.direction)
  }
  const response = await fetchFromApi(`/application/search?${params.toString()}`, locale, {
    signal
  })
  return getJSONIfResponseIsOk<Page<ApplicationSearchDto>>(response)
}

async function save(
  updateRequest: ApplicationUpdateRequestDto,
  locale: string
): Promise<ApplicationDetailsDto> {
  const response = await fetchFromApi('/application', locale, {
    method: 'POST',
    body: JSON.stringify(updateRequest)
  })
  return getJSONIfResponseIsOk<ApplicationDetailsDto>(response)
}

async function transition(
  transitionId: number,
  request: ApplicationTransitionRequestDto,
  locale: string
): Promise<ApplicationDetailsDto> {
  const response = await fetchFromApi(`/application/transition/${transitionId}`, locale, {
    method: 'PUT',
    body: JSON.stringify(request)
  })
  return getJSONIfResponseIsOk<ApplicationDetailsDto>(response)
}

async function archive(id: number, locale: string): Promise<void> {
  const response = await fetchFromApi(`/application/${id}/archive`, locale, {
    method: 'PUT'
  })
  await extractErrorIfResponseIsNotOk(response)
}

async function deleteApplication(id: number, locale: string): Promise<void> {
  const response = await fetchFromApi(`/application/${id}`, locale, {
    method: 'DELETE'
  })
  await extractErrorIfResponseIsNotOk(response)
}

export default {
  getApplication,
  getApplicationStatuses,
  search,
  save,
  transition,
  archive,
  deleteApplication
}
