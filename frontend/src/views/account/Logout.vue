<template>
  <v-empty-state headline="Goodbye" title="Logout" text="You are being logged out...">
  </v-empty-state>
</template>

<script setup lang="ts">
import accountApi from '@/api/AccountApi'
import router from '@/router'

if (accountApi.isUserLoggedIn()) {
  try {
    await accountApi.logout()
    setTimeout(async () => {
      await router.push({ name: 'home' })
    }, 2000)
  } catch (error) {
    console.error('Failed to logout', error)
    router.back()
  }
} else {
  await router.push({ name: 'home' })
}
</script>

<style scoped></style>
