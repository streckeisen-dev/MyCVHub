import { AccountStatus } from '@/dto/AccountStatusDto'

export const AUTH_STATE_KEY = 'my-cv-login-state'
export const ACCOUNT_STATUS_KEY = 'my-cv-account-status'

function successfulLogin() {
  localStorage.setItem(AUTH_STATE_KEY, 'true')
}

function loggedOut() {
  localStorage.removeItem(AUTH_STATE_KEY)
  localStorage.removeItem(ACCOUNT_STATUS_KEY)
}

function isLoggedIn(): boolean {
  return toBoolean(localStorage.getItem(AUTH_STATE_KEY)) || false
}

function getAccountStatus(): AccountStatus | undefined {
  return AccountStatus[localStorage.getItem(ACCOUNT_STATUS_KEY) as keyof typeof AccountStatus]
}

function setAccountStatus(accountStatus: AccountStatus) {
  localStorage.setItem(ACCOUNT_STATUS_KEY, String(accountStatus))
}

function toBoolean(s: string | undefined | null): boolean | undefined {
  if (s === 'true') {
    return true
  }
  if (s === 'false') {
    return false
  }
  return undefined
}

export default {
  successfulLogin,
  loggedOut,
  isLoggedIn,
  getAccountStatus,
  setAccountStatus
}
