import { AccountStatus } from '@/dto/AccountStatusDto'
import accountApi from '@/api/AccountApi'
import ToastService from '@/services/ToastService'
import vuetify from '@/plugins/vuetify'
import type { NavigationGuardNext } from 'vue-router'

async function enforceRouteAccessPermissions(
  requiredAccountStatus: AccountStatus | undefined,
  accountStatus: AccountStatus | undefined,
  next: NavigationGuardNext
) {
  if (shouldLoadAccountStatus(accountStatus)) {
    try {
      await accountApi.loadAccountStatus()
    } catch (error) {
      ToastService.error(vuetify.locale.t('account.verification.statusCheck.error'))
      next({ name: 'account-verification-pending' })
      return
    }
  }

  if (routeRequiresVerifiedAccountAndAccountIsNotVerified(requiredAccountStatus, accountStatus)) {
    next({ name: 'account-verification-pending' })
    return
  }

  if (routeRequiresUnverifiedAndAccountIsIncomplete(requiredAccountStatus, accountStatus)) {
    next({ name: 'oauth-signup' })
  }
}

function shouldLoadAccountStatus(accountStatus: AccountStatus | undefined): boolean {
  return (
    accountStatus == null ||
    accountStatus === AccountStatus.INCOMPLETE ||
    accountStatus === AccountStatus.UNVERIFIED
  )
}

function routeRequiresVerifiedAccountAndAccountIsNotVerified(
  requiredAccountStatus: AccountStatus | undefined,
  accountStatus: AccountStatus | undefined
): boolean {
  return (
    (requiredAccountStatus === AccountStatus.VERIFIED || requiredAccountStatus == null) &&
    accountStatus !== AccountStatus.VERIFIED
  )
}

function routeRequiresUnverifiedAndAccountIsIncomplete(
  requiredAccountStatus: AccountStatus | undefined,
  accountStatus: AccountStatus | undefined
): boolean {
  return (
    requiredAccountStatus === AccountStatus.UNVERIFIED && accountStatus === AccountStatus.INCOMPLETE
  )
}

export default {
  enforceRouteAccessPermissions
}
