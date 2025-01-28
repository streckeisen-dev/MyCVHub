import { describe, it, expect } from 'vitest'
import LoginStateService, {
  ACCOUNT_STATUS_KEY,
  AUTH_STATE_KEY
} from '../../src/services/LoginStateService'
import { AccountStatus } from '../../src/dto/AccountStatusDto'

describe('isLoggedIn', () => {
  it('should return false if no state is set', () => {
    const result = LoginStateService.isLoggedIn()

    expect(result).toBe(false)
  })

  it('should return false if non-boolean value is set as state', () => {
    localStorage.setItem(AUTH_STATE_KEY, 'test')

    const result = LoginStateService.isLoggedIn()

    expect(result).toBe(false)
  })

  it('should return false if state is set to "false"', () => {
    localStorage.setItem(AUTH_STATE_KEY, 'false')

    const result = LoginStateService.isLoggedIn()

    expect(result).toBe(false)
  })

  it('should return true if state is set to "true"', () => {
    localStorage.setItem(AUTH_STATE_KEY, 'true')

    const result = LoginStateService.isLoggedIn()

    expect(result).toBe(true)
  })
})

describe('successfulLogin', () => {
  it('should set state to "true"', () => {
    LoginStateService.successfulLogin()

    const result = localStorage.getItem(AUTH_STATE_KEY)

    expect(result).toBe('true')
  })
})

describe('loggedOut', () => {
  it('should remove state', () => {
    localStorage.setItem(AUTH_STATE_KEY, 'true')
    localStorage.setItem(ACCOUNT_STATUS_KEY, 'test')

    LoginStateService.loggedOut()

    expect(localStorage.getItem(AUTH_STATE_KEY)).toBe(null)
    expect(localStorage.getItem(ACCOUNT_STATUS_KEY)).toBe(null)
  })
})

describe('getAccountStatus', () => {
  it('should return undefined if no status is set', () => {
    const result = LoginStateService.getAccountStatus()

    expect(result).toBe(undefined)
  })

  it('should return undefined if non-enum value is set as status', () => {
    localStorage.setItem(ACCOUNT_STATUS_KEY, 'test')

    const result = LoginStateService.getAccountStatus()

    expect(result).toBe(undefined)
  })

  it('should return account status if status is set', () => {
    localStorage.setItem(ACCOUNT_STATUS_KEY, 'INCOMPLETE')

    const result = LoginStateService.getAccountStatus()

    expect(result).toBe(AccountStatus.INCOMPLETE)
  })
})

describe('setAccountStatus', () => {
  it('should set account status', () => {
    LoginStateService.setAccountStatus(AccountStatus.INCOMPLETE)

    const result = localStorage.getItem(ACCOUNT_STATUS_KEY)

    expect(result).toBe('INCOMPLETE')
  })
})
