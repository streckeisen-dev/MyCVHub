import type { ErrorDto } from '@/dto/ErrorDto'
import LoginStateService from '@/services/LoginStateService'

async function fetchFromApi<T>(
  path: string,
  options?: RequestInit,
  retry: boolean = true
): Promise<Response> {
  const uri = path.startsWith('/api') ? path : `/api${path}`
  const opt: RequestInit = {
    ...options
  }

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
  return await processAuthResponse(refreshResponse)
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
      return Promise.reject(error)
    } catch (error) {
      return Promise.reject()
    }
  }
  return Promise.resolve()
}

async function processAuthResponse(response: Response): Promise<void> {
  try {
    await extractErrorIfResponseIsNotOk(response)
    LoginStateService.successfulLogin()
    return Promise.resolve()
  } catch (error) {
    LoginStateService.loggedOut()
    return Promise.reject(error)
  }
}

const commonHeaders: { [key: string]: string } = {
  'Content-Type': 'application/json'
}

export {
  fetchFromApi,
  getJSONIfResponseIsOk,
  extractErrorIfResponseIsNotOk,
  refreshToken,
  processAuthResponse,
  commonHeaders
}
