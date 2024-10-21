<template>
  <v-main>
    <v-empty-state :headline="t('logout.headline')" :title="t('logout.title')" :text="t('logout.message')" />
    <notification v-if="logoutError" :title="t('logout.errorTitle')" :message="t('logout.errorMessage')" />
  </v-main>
</template>

<script setup lang="ts">
import accountApi from '@/api/AccountApi'
import router from '@/router'
import Notification from '@/components/Notification.vue'
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const logoutError = ref(false)

if (accountApi.isUserLoggedIn()) {
  try {
    await accountApi.logout()
    setTimeout(async () => {
      await router.push({ name: 'home' })
    }, 2000)
  } catch (error) {
    setTimeout(() => {
      router.back()
    }, 2000)
  }
} else {
  await router.push({ name: 'home' })
}
</script>

<i18n>
{
  "de": {
    "logout": {
      "headline": "Auf Wiedersehen",
      "title": "Abmelden",
      "message": "Du wirst abgemeldet...",
      "errorTitle": "Fehler beim Abmelden",
      "errorMessage": "Etwas ist beim Abmelden schiefgelaufen"
    }
  },
  "en": {
    "logout": {
      "headline": "Goodbye",
      "title": "Logout",
      "message": "You are being logged out...",
      "errorTitle": "Failed to logout",
      "errorMessage": "Something went wrong during your logout"
    }
  }
}
</i18n>
