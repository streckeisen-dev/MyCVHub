import { fetchFromApi, getJSONIfResponseIsOk } from '@/api/api-helper'

export default {
  async testConnection(): Promise<TestResponse> {
    return await fetchFromApi('/test')
      .then(getJSONIfResponseIsOk<TestResponse>)
  }
}

export type TestResponse = {
  message: String
}