<template>
  <v-main>
    <v-container>
      <v-row
        justify="center"
        align="center"
      >
        <v-col cols="12">
          <h2>{{ t('account.verification.pending.title') }}</h2>
        </v-col>
        <v-col cols="12">
          <p>{{ t('account.verification.pending.description') }}</p>
        </v-col>
        <v-col cols="12">
          <p>
            {{ t('account.verification.resend.description') }}
            <v-btn
              class="verification-resend-btn"
              :text="t('account.verification.resend.action')"
              variant="text"
              @click="generateToken"
              color="primary"
            />
          </p>
        </v-col>
      </v-row>
    </v-container>
  </v-main>
</template>

<script setup lang="ts">
import accountApi from '@/api/AccountApi'
import type { ErrorDto } from '@/dto/ErrorDto'
import { useI18n } from 'vue-i18n'
import ToastService from '@/services/ToastService'
import LoginStateService from '@/services/LoginStateService'
import router from '@/router'
import { AccountStatus } from '@/dto/AccountStatusDto'

const { t } = useI18n({
  useScope: 'global'
})

async function generateToken() {
  try {
    await accountApi.generateVerificationCode()
    ToastService.success(t('account.verification.resend.success'))
  } catch (e) {
    const error = e as ErrorDto
    const errorDetails = error?.message || t('error.genericMessage')
    ToastService.error(t('account.verification.resend.error'), errorDetails)
  }
}

if (LoginStateService.getAccountStatus() === AccountStatus.VERIFIED) {
  await router.push({ name: 'account' })
}
</script>

<style scoped lang="scss">
.verification-resend-btn {
  text-transform: none;
  font-size: 1rem;
}
</style>
