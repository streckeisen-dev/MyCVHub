import { fetchFromApi, getJSONIfResponseIsOk } from '@/api/ApiHelper'

async function testConnection(): Promise<TestResponse> {
  try {
    const response = await fetchFromApi('/test')
    return getJSONIfResponseIsOk<TestResponse>(response)
  } catch (error) {
    return Promise.reject(error)
  }
}

export default {
  testConnection
}

export type TestResponse = {
  message: String
}
