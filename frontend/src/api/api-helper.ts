import type { ErrorDto } from '@/dto/ErrorDto'


async function fetchFromApi<T>(path: string, options?: RequestInit): Promise<Response> {
  const uri = `/api${path}`
  return fetch(uri, options)
}

async function getJSONIfResponseIsOk<T>(response: Response): Promise<T> {
  await throwErrorIfResponseNotOk(response)
  return response.json()
}

async function throwErrorIfResponseNotOk(response: Response): Promise<void> {
  if (!response.ok) {
    const error: ErrorDto = await response.json()
    throw new Error(error.errorMessage)
  }
}

export { fetchFromApi, getJSONIfResponseIsOk, throwErrorIfResponseNotOk }