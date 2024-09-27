<template>
  <v-app id="mycv-app">
    <v-app-bar role="navigation" aria-label="Header navigation" border>
      <template v-slot:prepend>
        <v-app-bar-nav-icon @click="isNavMenuOpen = !isNavMenuOpen"></v-app-bar-nav-icon>
      </template>
      <v-app-bar-title>
        <router-link :to="{ name: 'home' }">MyCV</router-link>
      </v-app-bar-title>
    </v-app-bar>

    <v-navigation-drawer id="side-nav" v-model="isNavMenuOpen" disable-resize-watcher>
      <v-list-item v-if="accountApi.isUserLoggedIn()" prepend-icon="mdi-account-circle" link>
        <router-link :to="{ name: 'account' }">Account</router-link>
      </v-list-item>
      <v-list-item v-else prepend-icon="mdi-account-circle" link>
        <router-link :to="{ name: 'login' }">Login / Sign up</router-link>
      </v-list-item>

      <v-divider />

      <v-list-item link prepend-icon="mdi-home">
        <router-link :to="{ name: 'home' }">Home</router-link>
      </v-list-item>

      <v-list-item v-if="accountApi.isUserLoggedIn()" class="flex-wrap">
        <v-btn color="btn-primary" :to="{ name: 'logout' }">Logout</v-btn>
      </v-list-item>
    </v-navigation-drawer>

    <v-main>
      <suspense v-if="!isTokenRefreshPending">
        <router-view />
      </suspense>
      <loading-spinner v-else />
    </v-main>

    <v-footer></v-footer>
  </v-app>
</template>

<script setup lang="ts">
import {
  type NavigationGuardNext,
  type RouteLocationNormalized,
  type RouteLocationNormalizedLoaded,
  RouterLink,
  RouterView
} from 'vue-router'
import { onMounted, ref } from 'vue'
import { useTheme } from 'vuetify'
import accountApi from '@/api/account-api'
import router from '@/router'
import { refreshToken } from '@/api/api-helper'
import LoadingSpinner from '@/components/LoadingSpinner.vue'

const isNavMenuOpen = ref(false)
const isTokenRefreshPending = ref(true)

const vuetifyTheme = useTheme()
window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', ({ matches }) => {
  vuetifyTheme.global.name.value = matches ? 'dark' : 'light'
})

router.beforeEach(
  (to: RouteLocationNormalized, from: RouteLocationNormalizedLoaded, next: NavigationGuardNext) => {
    next()
    isNavMenuOpen.value = false
  }
)

onMounted(async () => {
  try {
    await refreshToken()
  } catch (error) {
    if (router.currentRoute.value.meta.authRequired) {
      await router.push({ name: 'login' })
    }
  } finally {
    isTokenRefreshPending.value = false
  }
})
</script>

<style scoped>
#side-nav button {
  justify-self: center;
}
</style>
