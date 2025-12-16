<template>
  <loading-spinner />
</template>

<script setup lang="ts">
import router from '@/router'
import ToastService from '@/services/ToastService'
import { useI18n } from 'vue-i18n'
import LoadingSpinner from '@/components/LoadingSpinner.vue'

const { t } = useI18n({
  useScope: 'global'
})

ToastService.error(t('account.login.oauth.error'))
await router.push({ name: 'login' })
</script>
