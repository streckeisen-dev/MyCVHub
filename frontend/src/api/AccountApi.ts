import { AccountDto } from '@/types/account/AccountDto.ts'
import { AccountUpdateDto } from '@/types/account/AccountUpdateDto.ts'
import { AuthResponseDto } from '@/types/account/AuthResponseDto.ts'
import {
  extractErrorIfResponseIsNotOk,
  fetchFromApi,
  getJSONIfResponseIsOk
} from '@/api/ApiHelper.ts'
import { RestError } from '@/types/RestError.ts'
import { ChangePasswordRequestDto } from '@/types/account/ChangePasswordRequestDto.ts'
import { SignupRequestDto } from '@/types/account/SignUpRequestDto.ts'
import { AccountVerificationRequestDto } from '@/types/account/AccountVerificationRequestDto.ts'
import { OAuthSignUpRequestDto } from '@/types/account/OAuthSignUpRequestDto.ts'

async function login(username: string | undefined, password: string | undefined, locale: string): Promise<void> {
  try {
    const response = await fetchFromApi('/auth/login', locale, {
      method: 'POST',
      body: JSON.stringify({
        username: username,
        password: password
      })
    })
    return extractErrorIfResponseIsNotOk(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to login', error)
  }
}

async function signUp(signupRequest: SignupRequestDto, locale: string): Promise<void> {
  try {
    const response = await fetchFromApi('/auth/signup', locale, {
      method: 'POST',
      body: JSON.stringify(signupRequest)
    })
    return extractErrorIfResponseIsNotOk(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to signup', error)
  }
}

async function oauthSignUp(oAuthSignUpRequest: OAuthSignUpRequestDto, locale: string): Promise<void> {
  try {
    const response = await fetchFromApi('/oauth/signup', locale, {
      method: 'POST',
      body: JSON.stringify(oAuthSignUpRequest)
    })
    return extractErrorIfResponseIsNotOk(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to signup with OAuth', error)
  }
}

async function verifyLogin(locale: string): Promise<AuthResponseDto> {
  try {
    const response = await fetchFromApi('/auth/login/verify', locale)
    return await getJSONIfResponseIsOk<AuthResponseDto>(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to verify login state', error)
  }
}

async function getAccount(locale: string): Promise<AccountDto> {
  try {
    const response = await fetchFromApi('/account', locale)
    return await getJSONIfResponseIsOk<AccountDto>(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to load account', error)
  }
}

async function updateAccount(updateRequest: AccountUpdateDto, locale: string): Promise<AccountDto> {
  try {
    const response = await fetchFromApi('/account', locale, {
      method: 'POST',
      body: JSON.stringify(updateRequest)
    })
    return await getJSONIfResponseIsOk<AccountDto>(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to update account', error)
  }
}

async function changePassword(changePasswordRequest: ChangePasswordRequestDto, locale: string): Promise<void> {
  try {
    const response = await fetchFromApi('/auth/change-password', locale, {
      method: 'POST',
      body: JSON.stringify(changePasswordRequest)
    })
    await extractErrorIfResponseIsNotOk(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to change password', error)
  }
}

async function logout(locale: string): Promise<void> {
  try {
    const response = await fetchFromApi('/auth/logout', locale, {
      method: 'POST'
    })

    await extractErrorIfResponseIsNotOk(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to logout', error)
  }
}

async function deleteAccount(locale: string): Promise<void> {
  try {
    const response = await fetchFromApi('/account', locale, {
      method: 'DELETE'
    })
    await extractErrorIfResponseIsNotOk(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to delete account', error)
  }
}

async function generateVerificationCode(locale: string): Promise<void> {
  try {
    const response = await fetchFromApi('/account/verification/generate', locale, {
      method: 'POST'
    })
    await extractErrorIfResponseIsNotOk(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to generate verification code', error)
  }
}

async function verifyAccount(request: AccountVerificationRequestDto, locale: string): Promise<void> {
  try {
    const response = await fetchFromApi('/account/verification', locale, {
      method: 'POST',
      body: JSON.stringify(request)
    })
    await extractErrorIfResponseIsNotOk(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    throw new RestError('Failed to verify account', error)
  }
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