import {
  extractErrorIfResponseIsNotOk,
  fetchFromApi,
  getJSONIfResponseIsOk
} from '@/api/ApiHelper.ts'
import { AdminAuthResponseDto } from '@/types/admin/AdminAuthResponseDto.ts'

const ADMIN_REFRESH_PATH = '/api/admin/auth/refresh'

function fetchFromAdminApi(path: string, locale: string, options?: RequestInit, retry = true): Promise<Response> {
  return fetchFromApi(path, locale, options, retry, ADMIN_REFRESH_PATH)
}

async function login(username: string | undefined, password: string | undefined, locale: string): Promise<void> {
  const response = await fetchFromAdminApi(
    '/admin/auth/login',
    locale,
    {
      method: 'POST',
      body: JSON.stringify({
        username: username,
        password: password
      })
    },
    false
  )
  return extractErrorIfResponseIsNotOk(response)
}

async function verifyLogin(locale: string): Promise<AdminAuthResponseDto> {
  const response = await fetchFromAdminApi('/admin/auth/login/verify', locale)
  return getJSONIfResponseIsOk<AdminAuthResponseDto>(response)
}

async function changePassword(password: string | undefined, confirmPassword: string | undefined, locale: string): Promise<void> {
  const response = await fetchFromAdminApi('/admin/auth/change-password', locale, {
    method: 'POST',
    body: JSON.stringify({
      password: password,
      confirmPassword: confirmPassword
    })
  })
  await extractErrorIfResponseIsNotOk(response)
}

async function logout(locale: string): Promise<void> {
  const response = await fetchFromAdminApi('/admin/auth/logout', locale, {
    method: 'POST'
  })
  await extractErrorIfResponseIsNotOk(response)
}

export { fetchFromAdminApi }

export default {
  login,
  verifyLogin,
  changePassword,
  logout
}
