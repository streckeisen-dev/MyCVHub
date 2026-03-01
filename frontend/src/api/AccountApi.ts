import { AccountDto } from '@/types/account/AccountDto.ts'
import { AccountUpdateDto } from '@/types/account/AccountUpdateDto.ts'
import { AuthResponseDto } from '@/types/account/AuthResponseDto.ts'
import {
  extractErrorIfResponseIsNotOk,
  fetchFromApi,
  getJSONIfResponseIsOk
} from '@/api/ApiHelper.ts'
import { ChangePasswordRequestDto } from '@/types/account/ChangePasswordRequestDto.ts'
import { SignupRequestDto } from '@/types/account/SignUpRequestDto.ts'
import { AccountVerificationRequestDto } from '@/types/account/AccountVerificationRequestDto.ts'
import { OAuthSignUpRequestDto } from '@/types/account/OAuthSignUpRequestDto.ts'

async function login(username: string | undefined, password: string | undefined, locale: string): Promise<void> {
  const response = await fetchFromApi('/auth/login', locale, {
    method: 'POST',
    body: JSON.stringify({
      username: username,
      password: password
    })
  })
  return extractErrorIfResponseIsNotOk(response)
}

async function signUp(signupRequest: SignupRequestDto, locale: string): Promise<void> {
  const response = await fetchFromApi('/auth/signup', locale, {
    method: 'POST',
    body: JSON.stringify(signupRequest)
  })
  return extractErrorIfResponseIsNotOk(response)
}

async function oauthSignUp(oAuthSignUpRequest: OAuthSignUpRequestDto, locale: string): Promise<void> {
  const response = await fetchFromApi('/oauth/signup', locale, {
    method: 'POST',
    body: JSON.stringify(oAuthSignUpRequest)
  })
  return extractErrorIfResponseIsNotOk(response)
}

async function verifyLogin(locale: string): Promise<AuthResponseDto> {
  const response = await fetchFromApi('/auth/login/verify', locale)
  return getJSONIfResponseIsOk<AuthResponseDto>(response)
}

async function getAccount(locale: string): Promise<AccountDto> {
  const response = await fetchFromApi('/account', locale)
  return getJSONIfResponseIsOk<AccountDto>(response)
}

async function updateAccount(updateRequest: AccountUpdateDto, locale: string): Promise<AccountDto> {
  const response = await fetchFromApi('/account', locale, {
    method: 'POST',
    body: JSON.stringify(updateRequest)
  })
  return getJSONIfResponseIsOk<AccountDto>(response)
}

async function changePassword(changePasswordRequest: ChangePasswordRequestDto, locale: string): Promise<void> {
  const response = await fetchFromApi('/auth/change-password', locale, {
    method: 'POST',
    body: JSON.stringify(changePasswordRequest)
  })
  await extractErrorIfResponseIsNotOk(response)
}

async function logout(locale: string): Promise<void> {
  const response = await fetchFromApi('/auth/logout', locale, {
    method: 'POST'
  })
  await extractErrorIfResponseIsNotOk(response)
}

async function deleteAccount(locale: string): Promise<void> {
  const response = await fetchFromApi('/account', locale, {
    method: 'DELETE'
  })
  await extractErrorIfResponseIsNotOk(response)
}

async function generateVerificationCode(locale: string): Promise<void> {
  const response = await fetchFromApi('/account/verification/generate', locale, {
    method: 'POST'
  })
  await extractErrorIfResponseIsNotOk(response)
}

async function verifyAccount(request: AccountVerificationRequestDto, locale: string): Promise<void> {
  const response = await fetchFromApi('/account/verification', locale, {
    method: 'POST',
    body: JSON.stringify(request)
  })
  await extractErrorIfResponseIsNotOk(response)
}

export default {
  login,
  signUp,
  oauthSignUp,
  verifyLogin,
  getAccount,
  updateAccount,
  changePassword,
  logout,
  deleteAccount,
  generateVerificationCode,
  verifyAccount
}
