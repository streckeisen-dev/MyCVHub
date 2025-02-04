import {
  commonHeaders,
  extractErrorIfResponseIsNotOk,
  fetchFromApi,
  getJSONIfResponseIsOk,
  processAuthResponse
} from '@/api/ApiHelper'
import type { AccountDto } from '@/dto/AccountDto'
import LoginStateService from '@/services/LoginStateService'
import type { SignupRequestDto } from '@/dto/SignUpRequestDto'
import type { AccountUpdateDto } from '@/dto/AccountUpdateDto'
import type { ChangePasswordRequestDto } from '@/dto/ChangePasswordRequestDto'
import { AccountStatus, type AccountStatusDto } from '@/dto/AccountStatusDto'
import type { OAuthSignUpRequestDto } from '@/dto/OAuthSignUpRequestDto'
import { RestError } from '@/api/RestError'

async function login(email: string, password: string): Promise<void> {
  try {
    const response = await fetch('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({
        username: email,
        password: password
      }),
      headers: commonHeaders()
    })
    return processAuthResponse(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    return Promise.reject(new RestError('Failed to login', error))
  }
}

async function verifyLogin(): Promise<void> {
  try {
    const response = await fetchFromApi('/auth/login/verify')
    return processAuthResponse(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    return Promise.reject(new RestError('Failed to verify login state', error))
  }
}

async function signUp(account: SignupRequestDto): Promise<void> {
  try {
    const response = await fetch('/api/auth/signup', {
      method: 'POST',
      body: JSON.stringify(account),
      headers: commonHeaders()
    })
    return processAuthResponse(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    return Promise.reject(new RestError('Failed to signup', error))
  }
}

async function oauthSignUp(oAuthSignUpRequest: OAuthSignUpRequestDto): Promise<void> {
  try {
    const response = await fetch('/api/oauth/signup', {
      method: 'POST',
      body: JSON.stringify(oAuthSignUpRequest),
      headers: commonHeaders()
    })
    return processAuthResponse(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    return Promise.reject(new RestError('Failed to signup with OAuth', error))
  }
}

async function changePassword(changePasswordRequest: ChangePasswordRequestDto): Promise<void> {
  try {
    const response = await fetchFromApi('/account/change-password', {
      method: 'POST',
      body: JSON.stringify(changePasswordRequest),
      headers: commonHeaders()
    })
    await extractErrorIfResponseIsNotOk(response)
  } catch (e) {
    const error = (e as RestError).errorDto
    return Promise.reject(new RestError('Failed to change password', error))
  }
}

async function update(accountUpdate: AccountUpdateDto): Promise<AccountDto> {
  try {
    const response = await fetchFromApi('/account', {
      method: 'POST',
      body: JSON.stringify(accountUpdate),
      headers: commonHeaders()
    })
    const updatedAccount = await getJSONIfResponseIsOk<AccountDto>(response)
    return Promise.resolve(updatedAccount)
  } catch (e) {
    const error = (e as RestError).errorDto
    return Promise.reject(new RestError('Failed to update account', error))
  }
}

async function getAccountInfo(): Promise<AccountDto> {
  try {
    const response = await fetchFromApi('/account')
    const account = await getJSONIfResponseIsOk<AccountDto>(response)
    return Promise.resolve(account)
  } catch (e) {
    const error = (e as RestError).errorDto
    return Promise.reject(new RestError('Failed to load account', error))
  }
}

function isUserLoggedIn(): boolean {
  return LoginStateService.isLoggedIn()
}

async function logout(): Promise<void> {
  try {
    const response = await fetch('/api/auth/logout', {
      method: 'POST'
    })

    if (!response.ok) {
      return Promise.reject(new RestError('Failed to perform logout'))
    }
    LoginStateService.loggedOut()
    return Promise.resolve()
  } catch (e) {
    const error = (e as RestError).errorDto
    return Promise.reject(new RestError('Failed to logout', error))
  }
}

async function loadAccountStatus(): Promise<void> {
  try {
    const response = await fetchFromApi('/account/status')
    const accountStatus = await getJSONIfResponseIsOk<AccountStatusDto>(response)
    LoginStateService.setAccountStatus(accountStatus.status)
    return Promise.resolve()
  } catch (e) {
    const error = (e as RestError).errorDto
    return Promise.reject(new RestError('Failed to load account status', error))
  }
}

async function generateVerificationCode(): Promise<void> {
  try {
    const response = await fetchFromApi('/account/verification/generate', {
      method: 'POST'
    })
    await extractErrorIfResponseIsNotOk(response)
    return Promise.resolve()
  } catch (e) {
    const error = (e as RestError).errorDto
    return Promise.reject(new RestError('Failed to generate verification code', error))
  }
}

async function verifyAccount(accountId: number, token: string): Promise<void> {
  try {
    const response = await fetchFromApi('/account/verification', {
      method: 'POST',
      body: JSON.stringify({
        id: accountId,
        token
      }),
      headers: commonHeaders()
    })
    await extractErrorIfResponseIsNotOk(response)
    if (isUserLoggedIn()) {
      LoginStateService.setAccountStatus(AccountStatus.VERIFIED)
    }
    return Promise.resolve()
  } catch (e) {
    const error = (e as RestError).errorDto
    return Promise.reject(new RestError('Failed to verify account', error))
  }
}

async function deleteAccount(): Promise<void> {
  try {
    const response = await fetchFromApi('/account', {
      method: 'DELETE',
      headers: commonHeaders()
    })
    await extractErrorIfResponseIsNotOk(response)
    return Promise.resolve()
  } catch (e) {
    const error = (e as RestError).errorDto
    return Promise.reject(new RestError('Failed to delete account', error))
  }
}

export default {
  login,
  verifyLogin,
  signUp,
  oauthSignUp,
  changePassword,
  update,
  isUserLoggedIn,
  logout,
  getAccountInfo,
  loadAccountStatus,
  generateVerificationCode,
  verifyAccount,
  deleteAccount
}
