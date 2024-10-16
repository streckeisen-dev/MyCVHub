<template>
  <v-main>
    <v-empty-state headline="Goodbye" title="Logout" text="You are being logged out..." />
    <notification v-if="logoutError" title="Failed to logout" message="Something went wrong during your logout" />
  </v-main>
</template>

<script setup lang="ts">
import accountApi from '@/api/AccountApi'
import router from '@/router'
import Notification from '@/components/Notification.vue'
import { ref } from 'vue'

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

<style scoped></style>
