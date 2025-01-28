import { afterEach, describe, expect, it, MockInstance, vi } from 'vitest'
import AccountApi from '../../src/api/AccountApi'
import LoginStateService from '../../src/services/LoginStateService'
import RouteSecurityService from '../../src/services/RouteSecurityService'
import { NavigationGuardNext } from 'vue-router'

interface RouteSecurityTestResources {
  loadAccountStatusSpy: MockInstance<() => Promise<void>>
  next: NavigationGuardNext
}

function prepareTestResources(
  shouldAccountStatusLoadSucceed: boolean,
  stateAccountStatus: string
): RouteSecurityTestResources {
  const loadAccountStatusSpy = mockLoadAccountStatus(shouldAccountStatusLoadSucceed)
  mockStateAccountStatus(stateAccountStatus)
  const nextMock = vi.fn()
  return {
    loadAccountStatusSpy: loadAccountStatusSpy,
    next: nextMock
  }
}

function mockStateAccountStatus(status: string) {
  vi.spyOn(LoginStateService, 'getAccountStatus').mockImplementation(() => status)
}

function mockLoadAccountStatus(shouldSucceed: boolean): MockInstance<() => Promise<void>> {
  const accountApiSpy = vi.spyOn(AccountApi, 'loadAccountStatus')
  accountApiSpy.mockImplementation(() => {
    if (shouldSucceed) {
      return Promise.resolve()
    }
    return Promise.reject()
  })
  return accountApiSpy
}

describe('enforceRouteAccessPermissions', () => {
  afterEach(() => {
    vi.restoreAllMocks()
    localStorage.clear()
  })

  it('should load account status if account status is not loaded', async () => {
    const testResources = prepareTestResources(true, 'VERIFIED')

    await RouteSecurityService.enforceRouteAccessPermissions(undefined, undefined, testResources.next)

    expect(testResources.loadAccountStatusSpy).toHaveBeenCalledTimes(1)
    expect(testResources.next).toHaveBeenCalledTimes(1)
    expect(testResources.next).toHaveBeenCalledWith()
  })

  it('should not load account status if account status is verified', async () => {
    const testResources = prepareTestResources(true, 'VERIFIED')

    await RouteSecurityService.enforceRouteAccessPermissions(undefined, 'VERIFIED', testResources.next)

    expect(testResources.loadAccountStatusSpy).toHaveBeenCalledTimes(0)
    expect(testResources.next).toHaveBeenCalledTimes(1)
    expect(testResources.next).toHaveBeenCalledWith()
  })

  it('should redirect to account-verification-pending page if account status load fails', async () => {
    const testResources = prepareTestResources(false, undefined)

    await RouteSecurityService.enforceRouteAccessPermissions(undefined, undefined, testResources.next)

    expect(testResources.loadAccountStatusSpy).toHaveBeenCalledTimes(1)
    expect(testResources.next).toHaveBeenCalledTimes(1)
    expect(testResources.next).toHaveBeenCalledWith({ name: 'account-verification-pending' })
  })

  it('should redirect to account-verification-pending page if account status load returns invalid result', async () => {
    const testResources = prepareTestResources(true, undefined)

    await RouteSecurityService.enforceRouteAccessPermissions(undefined, undefined, testResources.next)

    expect(testResources.loadAccountStatusSpy).toHaveBeenCalledTimes(1)
    expect(testResources.next).toHaveBeenCalledTimes(1)
    expect(testResources.next).toHaveBeenCalledWith({ name: 'account-verification-pending' })
  })

  it('should redirect to account-verification-pending page if route requires verified account and account is not verified', async () => {
    const testResources = prepareTestResources(true, 'UNVERIFIED')

    await RouteSecurityService.enforceRouteAccessPermissions('VERIFIED', 'UNVERIFIED', testResources.next)

    expect(testResources.next).toHaveBeenCalledTimes(1)
    expect(testResources.next).toHaveBeenCalledWith({ name: 'account-verification-pending' })
  })

  it('should let user access route if route requires verified account and account is verified', async () => {
    const testResources = prepareTestResources(true, 'VERIFIED')

    await RouteSecurityService.enforceRouteAccessPermissions('VERIFIED', 'VERIFIED', testResources.next)
    expect(testResources.next).toHaveBeenCalledTimes(1)
    expect(testResources.next).toHaveBeenCalledWith()
  })

  it('should redirect to oauth-signup page if route requires unverified account and account is incomplete', async () => {
    const testResources = prepareTestResources(true, 'INCOMPLETE')

    await RouteSecurityService.enforceRouteAccessPermissions('UNVERIFIED', 'INCOMPLETE', testResources.next)

    expect(testResources.next).toHaveBeenCalledTimes(1)
    expect(testResources.next).toHaveBeenCalledWith({ name: 'oauth-signup' })
  })

  it('should let user access route if route requires unverified account and account is unverified', async () => {
    const testResources = prepareTestResources(true, 'UNVERIFIED')

    await RouteSecurityService.enforceRouteAccessPermissions('UNVERIFIED', 'UNVERIFIED', testResources.next)

    expect(testResources.next).toHaveBeenCalledTimes(1)
    expect(testResources.next).toHaveBeenCalledWith()
  })
})
