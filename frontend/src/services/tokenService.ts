import { ref } from 'vue'

const accessToken = ref<string>()

export function setAccessToken(token: string) {
  accessToken.value = token
}

export function getAccessToken(): string | undefined {
  return accessToken.value
}

export function clearAccessToken() {
  accessToken.value = undefined
}

export function hasAccessToken(): boolean {
  return accessToken.value != null
}