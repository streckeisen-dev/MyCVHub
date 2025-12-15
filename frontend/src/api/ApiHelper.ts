import { RestError } from '@/types/RestError.ts'
import { ErrorDto } from '@/types/ErrorDto.ts'

export async function fetchFromApi(
   path: string,
   lang: string,
   options?: RequestInit,
   retry = true
 ): Promise<Response> {
   const uri = path.startsWith('/api') ? path : `/api${path}`
   const opt: RequestInit = {
     ...options
   }

   const headers = (options?.headers as Headers) || new Headers()
   commonHeaders(lang).forEach((val, key) => {
     if (headers.has(key)) return
     if (val !== '') {
       headers.append(key, val)
     }
   })
  opt.headers = new Headers()
  for (const key of headers.keys()) {
    const val = headers.get(key)
    if (val != null && val !== '') {
      opt.headers.append(key, val)
    }
  }

   try {
     const response = await fetch(uri, opt)
     if (response.status === 401 && retry) {
       await refreshToken()
       return fetchFromApi(uri, lang, opt, false)
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
     return await extractErrorIfResponseIsNotOk(refreshResponse)
   } catch (e) {
     const error = (e as RestError).errorDto
     throw new RestError('Token refresh failed', error)
   }
 }

 export async function getJSONIfResponseIsOk<T>(response: Response): Promise<T> {
   try {
     await extractErrorIfResponseIsNotOk(response)
     return response.json()
   } catch (e) {
     const error = (e as RestError).errorDto
     throw new RestError('Failed to extract response body', error)
   }
 }

 export async function extractErrorIfResponseIsNotOk(response: Response): Promise<void> {
   if (!response.ok) {
     let error: ErrorDto
     try {
       error = await response.json()
       error.status = response.status
     } catch {
       throw new RestError('Failed to extract error')
     }
     throw new RestError('Call failed', error)
   }
 }

 export function commonHeaders(locale: string): Headers {
   const headers = new Headers()
   headers.append('Content-Type', 'application/json')
   headers.append('Accept-Language', locale)
   return headers
 }