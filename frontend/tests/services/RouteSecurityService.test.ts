import { afterEach, expect, describe, it, vi } from 'vitest'
import AccountApi from '../../src/api/AccountApi'
import LoginStateService, { ACCOUNT_STATUS_KEY } from '../../src/services/LoginStateService'
import RouteSecurityService from '../../src/services/RouteSecurityService'

describe('enforceRouteAccessPermissions', () => {
  afterEach(() => {
    vi.restoreAllMocks()
    localStorage.clear()
  })

  it('should load account status if account status is not loaded', async () => {
    const accountApiSpy = vi.spyOn(AccountApi, 'loadAccountStatus')
    vi.spyOn(LoginStateService, 'getAccountStatus').mockImplementation(() => 'VERIFIED')
    accountApiSpy.mockImplementation(() => {
      return Promise.resolve()
    })
    const nextMock = vi.fn()

    await RouteSecurityService.enforceRouteAccessPermissions(undefined, undefined, nextMock)

    expect(accountApiSpy).toHaveBeenCalledTimes(1)
    expect(nextMock).toHaveBeenCalledTimes(1)
    expect(nextMock).toHaveBeenCalledWith()
  })

  it('should not load account status if account status is verified', async () => {
    const accountApiSpy = vi.spyOn(AccountApi, 'loadAccountStatus')
    localStorage.setItem(ACCOUNT_STATUS_KEY, 'VERIFIED')
    accountApiSpy.mockImplementation(() => {
      return Promise.resolve()
    })
    const nextMock = vi.fn()

    await RouteSecurityService.enforceRouteAccessPermissions(undefined, 'VERIFIED', nextMock)

    expect(accountApiSpy).toHaveBeenCalledTimes(0)
    expect(nextMock).toHaveBeenCalledTimes(1)
    expect(nextMock).toHaveBeenCalledWith()
  })

  it('should redirect to account-verification-pending page if account status load fails', async () => {
    const accountApiSpy = vi.spyOn(AccountApi, 'loadAccountStatus')
    accountApiSpy.mockImplementation(() => {
      return Promise.reject()
    })
    const nextMock = vi.fn()

    await RouteSecurityService.enforceRouteAccessPermissions(undefined, undefined, nextMock)

    expect(accountApiSpy).toHaveBeenCalledTimes(1)
    expect(nextMock).toHaveBeenCalledTimes(1)
    expect(nextMock).toHaveBeenCalledWith({ name: 'account-verification-pending' })
  })

  it('should redirect to account-verification-pending page if account status load returns invalid result', async () => {
    const accountApiSpy = vi.spyOn(AccountApi, 'loadAccountStatus')
    vi.spyOn(LoginStateService, 'getAccountStatus').mockImplementation(() => undefined)
    accountApiSpy.mockImplementation(() => {
      return Promise.resolve()
    })
    const nextMock = vi.fn()

    await RouteSecurityService.enforceRouteAccessPermissions(undefined, undefined, nextMock)

    expect(accountApiSpy).toHaveBeenCalledTimes(1)
    expect(nextMock).toHaveBeenCalledTimes(1)
    expect(nextMock).toHaveBeenCalledWith({ name: 'account-verification-pending' })
  })

  it('should redirect to account-verification-pending page if route requires verified account and account is not verified', async () => {
    const accountApiSpy = vi.spyOn(AccountApi, 'loadAccountStatus')
    vi.spyOn(LoginStateService, 'getAccountStatus').mockImplementation(() => 'UNVERIFIED')
    accountApiSpy.mockImplementation(() => {
      return Promise.resolve()
    })
    const nextMock = vi.fn()

    await RouteSecurityService.enforceRouteAccessPermissions('VERIFIED', 'UNVERIFIED', nextMock)

    expect(nextMock).toHaveBeenCalledTimes(1)
    expect(nextMock).toHaveBeenCalledWith({ name: 'account-verification-pending' })
  })

  it('should let user access route if route requires verified account and account is verified', async () => {
    const accountApiSpy = vi.spyOn(AccountApi, 'loadAccountStatus')
    vi.spyOn(LoginStateService, 'getAccountStatus').mockImplementation(() => 'VERIFIED')
    accountApiSpy.mockImplementation(() => {
      return Promise.resolve()
    })
    const nextMock = vi.fn()

    await RouteSecurityService.enforceRouteAccessPermissions('VERIFIED', 'VERIFIED', nextMock)
    expect(nextMock).toHaveBeenCalledTimes(1)
    expect(nextMock).toHaveBeenCalledWith()
  })

  it('should redirect to oauth-signup page if route requires unverified account and account is incomplete', async () => {
    const accountApiSpy = vi.spyOn(AccountApi, 'loadAccountStatus')
    vi.spyOn(LoginStateService, 'getAccountStatus').mockImplementation(() => 'INCOMPLETE')
    accountApiSpy.mockImplementation(() => {
      return Promise.resolve()
    })
    const nextMock = vi.fn()

    await RouteSecurityService.enforceRouteAccessPermissions('UNVERIFIED', 'INCOMPLETE', nextMock)

    expect(nextMock).toHaveBeenCalledTimes(1)
    expect(nextMock).toHaveBeenCalledWith({ name: 'oauth-signup' })
  })

  it('should let user access route if route requires unverified account and account is unverified', async () => {
    vi.spyOn(AccountApi, 'loadAccountStatus').mockImplementation(() => {
      return Promise.resolve()
    })
    vi.spyOn(LoginStateService, 'getAccountStatus').mockImplementation(() => 'UNVERIFIED')
    const nextMock = vi.fn()

    await RouteSecurityService.enforceRouteAccessPermissions('UNVERIFIED', 'UNVERIFIED', nextMock)

    expect(nextMock).toHaveBeenCalledTimes(1)
    expect(nextMock).toHaveBeenCalledWith()
  })
})