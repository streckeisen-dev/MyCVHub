import type { ErrorDto } from '@/dto/ErrorDto'
import LoginStateService from '@/services/LoginStateService'
import i18n from '@/plugins/i18n'
import router from '@/router'
import { RestError } from '@/api/RestError'
import { AuthResponseDto } from '@/dto/AuthResponseDto.ts'
import LanguageService from '@/services/LanguageService.ts'
import { LocaleInstance } from 'vuetify/framework'

async function fetchFromApi(
  path: string,
  options?: RequestInit,
  retry: boolean = true
): Promise<Response> {
  const uri = path.startsWith('/api') ? path : `/api${path}`
  const opt: RequestInit = {
    ...options
  }

  const headers = (options?.headers as Headers) || new Headers()
  headers.append('Accept-Language', i18n.global.locale.value)
  opt.headers = headers

  try {
    const response = await fetch(uri, opt)
    if (response.status === 401 && retry) {
      await refreshToken()
      return fetchFromApi(uri, opt, false)
    }
    return response
  } catch (error) {
    throw new RestError('Failed to execute call', error as ErrorDto)
  }
}

async function refreshToken(): Promise<void> {
  const refreshResponse = await fetch('/api/auth/refresh', {
    method: 'POST',
    credentials: 'same-origin'
  })
  try {
    return await processAuthResponse(refreshResponse, undefined)
  } catch (e) {
    await router.push({ name: 'login', query: { redirect: router.currentRoute.value.fullPath } })
    const error = (e as RestError).errorDto
    throw new RestError('Token refresh failed', error)
  }
}

async function getJSONIfResponseIsOk<T>(response: Response): Promise<T> {
  try {
    await extractErrorIfResponseIsNotOk(response)
    return response.json()
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to extract response body', error)
  }
}

async function extractErrorIfResponseIsNotOk(response: Response): Promise<void> {
  if (!response.ok) {
    try {
      const error: ErrorDto = await response.json()
      error.status = response.status
      return Promise.reject(new RestError('Call failed', error))
    } catch {
      throw new RestError('Failed to extract error')
    }
  }
}

async function processAuthResponse(
  response: Response,
  locale: LocaleInstance | undefined
): Promise<void> {
  try {
    if (response.status === 401) {
      const error = (await response.json()) as ErrorDto
      LoginStateService.loggedOut()
      return Promise.reject(new RestError('Unauthorized', error))
    }
    await extractErrorIfResponseIsNotOk(response)
    const authResponse = await getJSONIfResponseIsOk<AuthResponseDto>(response)
    if (authResponse.language && locale) {
      LanguageService.setLanguage(authResponse.language, locale, true)
    }
    LoginStateService.successfulLogin()
  } catch (error) {
    throw new RestError('Failed to execute call', error as ErrorDto)
  }
}

function commonHeaders(): Headers {
  const headers = new Headers()
  headers.append('Content-Type', 'application/json')
  headers.append('Accept-Language', i18n.global.locale.value)
  return headers
}

export {
  fetchFromApi,
  getJSONIfResponseIsOk,
  extractErrorIfResponseIsNotOk,
  refreshToken,
  processAuthResponse,
  commonHeaders
}
