<template>
  <v-main>
    <v-empty-state
      :headline="t('account.logout.headline')"
      :title="t('account.logout.action')"
      :text="t('account.logout.message')"
    />
  </v-main>
</template>

<script setup lang="ts">
import accountApi from '@/api/AccountApi'
import router from '@/router'
import { useI18n } from 'vue-i18n'
import ToastService from '@/services/ToastService'
import type { ErrorDto } from '@/dto/ErrorDto'

const { t } = useI18n({
  useScope: 'global'
})

if (accountApi.isUserLoggedIn()) {
  try {
    await accountApi.logout()
    setTimeout(async () => {
      await router.push({ name: 'home' })
    }, 2000)
  } catch (e) {
    const error = e as ErrorDto
    const errorDetails = error?.message || t('error.genericMessage')
    ToastService.error(t('account.logout.error'), errorDetails)
    setTimeout(() => {
      router.back()
    }, 2000)
  }
} else {
  await router.push({ name: 'home' })
}
</script>
