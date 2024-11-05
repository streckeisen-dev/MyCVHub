<template>
  <loading-spinner />
</template>

<script setup lang="ts">
import accountApi from '@/api/AccountApi'
import router from '@/router'
import ToastService from '@/services/ToastService'
import type { ErrorDto } from '@/dto/ErrorDto'
import { useI18n } from 'vue-i18n'
import LoadingSpinner from '@/components/LoadingSpinner.vue'

const { t } = useI18n({
  useScope: 'global'
})

try {
  await accountApi.verifyLogin()
  await router.push({ name: 'account' })
} catch (e) {
  const error = e as ErrorDto
  const errorDetails = error?.message || t('error.genericMessage')
  ToastService.error(t('account.login.oauth.error'), errorDetails)
  await router.push({ name: 'login' })
}
</script>
