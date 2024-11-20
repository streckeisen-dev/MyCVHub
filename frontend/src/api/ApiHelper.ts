import type { ErrorDto } from '@/dto/ErrorDto'
import LoginStateService from '@/services/LoginStateService'
import i18n from '@/plugins/i18n'
import router from '@/router'

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
    return Promise.reject(error)
  }
}

async function refreshToken(): Promise<void> {
  const refreshResponse = await fetch('/api/auth/refresh', {
    method: 'POST',
    credentials: 'same-origin'
  })
  try {
    return await processAuthResponse(refreshResponse)
  } catch (error) {
    await router.push({ name: 'login', query: { redirect: router.currentRoute.value.fullPath } })
    return Promise.reject(error)
  }
}

async function getJSONIfResponseIsOk<T>(response: Response): Promise<T> {
  try {
    await extractErrorIfResponseIsNotOk(response)
    return response.json()
  } catch (error) {
    return Promise.reject(error)
  }
}

async function extractErrorIfResponseIsNotOk(response: Response): Promise<void> {
  if (!response.ok) {
    try {
      const error: ErrorDto = await response.json()
      error.status = response.status
      return Promise.reject(error)
    } catch (error) {
      return Promise.reject()
    }
  }
  return Promise.resolve()
}

async function processAuthResponse(response: Response): Promise<void> {
  try {
    if (response.status === 401) {
      const error = await response.json() as ErrorDto
      LoginStateService.loggedOut()
      return Promise.reject(error || {})
    }
    await extractErrorIfResponseIsNotOk(response)
    LoginStateService.successfulLogin()
    return Promise.resolve()
  } catch (error) {
    return Promise.reject(error)
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
