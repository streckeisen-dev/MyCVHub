<template>
  <loading-spinner />
</template>

<script setup lang="ts">
import accountApi from '@/api/AccountApi'
import router from '@/router'
import ToastService from '@/services/ToastService'
import { useI18n } from 'vue-i18n'
import LoadingSpinner from '@/components/LoadingSpinner.vue'
import LoginStateService from '@/services/LoginStateService'
import { AccountStatus } from '@/dto/AccountStatusDto'
import { RestError } from '@/api/RestError'

const { t } = useI18n({
  useScope: 'global'
})

try {
  await accountApi.verifyLogin()
  await accountApi.loadAccountStatus()
  if (LoginStateService.getAccountStatus() === AccountStatus.INCOMPLETE) {
    await router.push({ name: 'oauth-signup' })
  } else {
    await router.push({ name: 'account' })
  }
} catch (e) {
  const error = (e as RestError).errorDto
  const errorDetails = error?.message || t('error.genericMessage')
  ToastService.error(t('account.login.oauth.error'), errorDetails)
  await router.push({ name: 'login' })
}
</script>
