<template>
  <loading-spinner v-if="isLoading" />
  <v-main v-else>
    <v-empty-state :title="t('account.verification.error')" />
  </v-main>
</template>

<script setup lang="ts">
import LoadingSpinner from '@/components/LoadingSpinner.vue'
import accountApi from '@/api/AccountApi'
import { useI18n } from 'vue-i18n'
import ToastService from '@/services/ToastService'
import { ref } from 'vue'
import router from '@/router'
import { RestError } from '@/api/RestError'

const { t } = useI18n({
  useScope: 'global'
})

const props = defineProps<{
  accountId: string
  token: string
}>()

const isLoading = ref(true)

try {
  await accountApi.verifyAccount(Number.parseInt(props.accountId), props.token)
  await router.push({ name: 'account' })
  ToastService.success(t('account.verification.success'))
} catch (e) {
  const error = (e as RestError).errorDto
  const errorDetails = error?.message || t('error.genericMessage')
  ToastService.error(t('account.verification.error'), errorDetails)
} finally {
  isLoading.value = false
}
</script>
