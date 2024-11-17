<template>
  <loading-spinner v-if="isLoading" />
  <v-main v-else>
    <v-empty-state :title="t('account.verification.error')" />
  </v-main>
</template>

<script setup lang="ts">
import LoadingSpinner from '@/components/LoadingSpinner.vue'
import accountApi from '@/api/AccountApi'
import type { ErrorDto } from '@/dto/ErrorDto'
import { useI18n } from 'vue-i18n'
import ToastService from '@/services/ToastService'
import { ref } from 'vue'
import router from '@/router'

const { t } = useI18n({
  useScope: 'global'
})

const props = defineProps<{
  accountId: number
  token: string
}>()

const isLoading = ref(true)

try {
  await accountApi.verifyAccount(props.accountId, props.token)
  await router.push({ name: 'account' })
  ToastService.success(t('account.verification.success'))
} catch (e) {
  const error = e as ErrorDto
  const errorDetails = error?.message || t('error.genericMessage')
  ToastService.error(t('account.verification.error'), errorDetails)
} finally {
  isLoading.value = false
}
</script>

<style scoped lang="scss"></style>
