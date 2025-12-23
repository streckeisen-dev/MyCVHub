import { AccountStatus } from '@/dto/AccountStatusDto'
import accountApi from '@/api/AccountApi'
import ToastService from '@/services/ToastService'
import vuetify from '@/plugins/vuetify'
import type { NavigationGuardNext } from 'vue-router'
import LoginStateService from '@/services/LoginStateService.ts'

async function enforceRouteAccessPermissions(
  requiredAccountStatus: AccountStatus | undefined,
  accountStatus: AccountStatus | undefined,
  next: NavigationGuardNext
) {
  if (shouldLoadAccountStatus(accountStatus)) {
    try {
      await accountApi.loadAccountStatus()
    } catch {
      ToastService.error(vuetify.locale.t('account.verification.statusCheck.error'))
      next({ name: 'account-verification-pending' })
      return
    }
  }

  const updatedAccountStatus = LoginStateService.getAccountStatus()
  if (updatedAccountStatus == null) {
    ToastService.error(vuetify.locale.t('account.verification.statusCheck.error'))
    next({ name: 'account-verification-pending' })
    return
  }

  if (routeRequiresVerifiedAccount(requiredAccountStatus)) {
    if (updatedAccountStatus !== AccountStatus.VERIFIED) {
      next({ name: 'account-verification-pending' })
      return
    }

    next()
    return
  }

  if (requiredAccountStatus === AccountStatus.UNVERIFIED) {
    if (updatedAccountStatus == AccountStatus.INCOMPLETE) {
      next({ name: 'oauth-signup' })
      return
    }

    next()
    return
  }

  next()
}

function shouldLoadAccountStatus(accountStatus: AccountStatus | undefined): boolean {
  return (
    accountStatus == null ||
    accountStatus === AccountStatus.INCOMPLETE ||
    accountStatus === AccountStatus.UNVERIFIED
  )
}

function routeRequiresVerifiedAccount(requiredAccountStatus: AccountStatus | undefined): boolean {
  return requiredAccountStatus === AccountStatus.VERIFIED || requiredAccountStatus == null
}

export default {
  enforceRouteAccessPermissions
}
