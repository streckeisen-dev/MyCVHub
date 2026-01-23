import { Page } from '@/types/Page.ts'
import { ApplicationSearchDto } from '@/types/application/ApplicationSearchDto.ts'
import {
  extractErrorIfResponseIsNotOk,
  fetchFromApi,
  getJSONIfResponseIsOk
} from '@/api/ApiHelper.ts'
import { RestError } from '@/types/RestError.ts'
import { ApplicationUpdateRequestDto } from '@/types/application/ApplicationUpdateRequestDto.ts'
import { ApplicationStatusDto } from '@/types/application/ApplicationStatusDto.ts'
import { ApplicationDetailsDto } from '@/types/application/ApplicationDetailsDto.ts'
import { ApplicationTransitionRequestDto } from '@/types/application/ApplicationTransitionRequestDto.ts'
import { ApplicationSearchRequest } from '@/types/application/ApplicationSearchRequest.ts'

async function getApplication(id: number, locale: string): Promise<ApplicationDetailsDto> {
  try {
    const response = await fetchFromApi(`/application/${id}`, locale)
    return await getJSONIfResponseIsOk<ApplicationDetailsDto>(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to load application', error)
  }
}

async function getApplicationStatuses(locale: string): Promise<ApplicationStatusDto[]> {
  try {
    const response = await fetchFromApi('/application/statuses', locale)
    return await getJSONIfResponseIsOk<ApplicationStatusDto[]>(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to load application statuses', error)
  }
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
  try {
    const response = await fetchFromApi(`/application/search?${params.toString()}`, locale, {
      signal
    })
    return await getJSONIfResponseIsOk<Page<ApplicationSearchDto>>(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to load applications', error)
  }
}

async function save(
  updateRequest: ApplicationUpdateRequestDto,
  locale: string
): Promise<ApplicationDetailsDto> {
  try {
    const response = await fetchFromApi('/application', locale, {
      method: 'POST',
      body: JSON.stringify(updateRequest)
    })
    return await getJSONIfResponseIsOk<ApplicationDetailsDto>(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to save application', error)
  }
}

async function transition(
  transitionId: number,
  request: ApplicationTransitionRequestDto,
  locale: string
): Promise<ApplicationDetailsDto> {
  try {
    const response = await fetchFromApi(`/application/transition/${transitionId}`, locale, {
      method: 'PUT',
      body: JSON.stringify(request)
    })
    return await getJSONIfResponseIsOk<ApplicationDetailsDto>(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to transition application', error)
  }
}

async function archive(id: number, locale: string): Promise<void> {
  try {
    const response = await fetchFromApi(`/application/${id}/archive`, locale, {
      method: 'PUT'
    })
    await extractErrorIfResponseIsNotOk(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to archive application', error)
  }
}

async function deleteApplication(id: number, locale: string): Promise<void> {
  try {
    const response = await fetchFromApi(`/application/${id}`, locale, {
      method: 'DELETE'
    })
    await extractErrorIfResponseIsNotOk(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to delete application', error)
  }
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
