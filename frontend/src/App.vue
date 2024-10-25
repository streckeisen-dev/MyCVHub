<template>
  <v-app id="mycv-app">
    <template v-if="showNavigation">
      <v-app-bar role="navigation" aria-label="Header navigation" class="navigation-bar">
        <template v-slot:prepend>
          <v-app-bar-nav-icon @click="isNavMenuOpen = !isNavMenuOpen"></v-app-bar-nav-icon>
        </template>
        <v-app-bar-title class="home-link-container">
          <router-link :to="{ name: accountApi.isUserLoggedIn() ? 'account' : 'home' }">
            <img
              v-if="isDarkMode"
              src="@/assets/mycvhub_logo_dark.png"
              alt="MyCVHub Logo"
              class="logo"
            />
            <img v-else src="@/assets/mycvhub_logo_light.png" alt="MyCVHub Logo" class="logo" />
          </router-link>
        </v-app-bar-title>
      </v-app-bar>

      <v-navigation-drawer id="side-nav" v-model="isNavMenuOpen" disable-resize-watcher>
        <v-list-item
          v-if="accountApi.isUserLoggedIn()"
          prepend-icon="mdi-account-circle"
          link
          :to="{ name: 'account' }"
          :title="t('account.title')"
        />
        <v-list-item
          v-else
          prepend-icon="mdi-account-circle"
          link
          :to="{ name: 'login' }"
          :title="t('account.login.action')"
        />

        <v-divider />

        <v-list-item
          v-if="!accountApi.isUserLoggedIn()"
          link
          prepend-icon="mdi-home"
          :to="{ name: 'home' }"
          :title="t('app.home')"
        />

        <v-list-item v-if="accountApi.isUserLoggedIn()" class="flex-wrap">
          <v-btn color="primary" :to="{ name: 'logout' }">{{ t('account.logout.action') }}</v-btn>
        </v-list-item>

        <v-list-item>
          <div class="language-selector">
            <a
              v-for="lang in i18n.global.availableLocales"
              :key="lang"
              @click="changeLocale(lang)"
              :class="localeClass(lang).value"
              >{{ lang.toUpperCase() }}</a
            >
          </div>
        </v-list-item>
      </v-navigation-drawer>
    </template>

    <suspense>
      <router-view class="router-view" />
    </suspense>

    <v-footer v-if="showNavigation">
      <div class="footer-content">
        <v-row justify="center">
          <v-col cols="12" class="text-center">&copy; 2024 MyCVHub. All Rights Reserved.</v-col>
        </v-row>
        <v-row justify="center" align="center" class="links">
          <router-link :to="{ name: 'privacy-policy' }">{{ t('privacy.title') }}</router-link>
          <v-btn href="https://github.com/lstreckeisen/my-cv" target="_blank" size="32">
            <v-icon slot="prepend" icon="mdi-github" size="32" />
          </v-btn>
        </v-row>
      </div>
    </v-footer>
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
import { computed, type ComputedRef, ref } from 'vue'
import { useLocale, useTheme } from 'vuetify'
import accountApi from '@/api/AccountApi'
import router from '@/router'
import { useI18n } from 'vue-i18n'
import i18n from '@/plugins/i18n'
import LanguageService from '@/services/LanguageService'

const { t } = useI18n()
const locale = useLocale().current

function localeClass(lang: string): ComputedRef {
  return computed(() => {
    return {
      'current-locale': locale.value === lang
    }
  })
}

function changeLocale(lang: string) {
  locale.value = lang
  LanguageService.setLanguage(lang)
}

const isNavMenuOpen = ref(false)

const showNavigation = ref(true)

const vuetifyTheme = useTheme()
const isDarkMode = computed(() => {
  return vuetifyTheme.global.current.value.dark
})
window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', ({ matches }) => {
  vuetifyTheme.global.name.value = matches ? 'dark' : 'light'
})

router.beforeEach(
  async (
    to: RouteLocationNormalized,
    from: RouteLocationNormalizedLoaded,
    next: NavigationGuardNext
  ) => {
    if (to.meta.authRequired && !accountApi.isUserLoggedIn()) {
      next({ name: 'login', query: { redirect: to.fullPath } })
    } else {
      showNavigation.value = !(to.meta.hideNavigation || false)
      isNavMenuOpen.value = false
      next()
    }
  }
)
</script>

<style lang="scss" scoped>
header.navigation-bar {
  border-bottom: 1px solid rgba(var(--v-border-color), var(--v-border-opacity));
}

#side-nav button {
  justify-self: center;
}

.home-link-container {
  margin-inline-start: 0 !important;

  .logo {
    vertical-align: middle;
    width: 220px;
  }
}

.language-selector {
  display: flex;
  gap: 5px;

  a {
    cursor: pointer;

    &.current-locale {
      text-decoration: underline;
    }
  }
}

footer {
  padding: 30px;
  flex: 0;
  flex-direction: column;

  .footer-content {
    width: 100%;

    div {
      padding: 0;
    }

    .links {
      gap: 15px;
    }
  }
}
</style>
