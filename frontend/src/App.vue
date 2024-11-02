<template>
  <v-app id="mycv-app">
    <template v-if="showNavigation">
      <v-app-bar
        role="navigation"
        aria-label="Header navigation"
        class="navigation-bar"
      >
        <template v-slot:prepend>
          <v-app-bar-nav-icon @click="isNavMenuOpen = !isNavMenuOpen" />
        </template>
        <v-app-bar-title class="home-link-container">
          <router-link :to="{ name: accountApi.isUserLoggedIn() ? 'account' : 'home' }">
            <img
              v-if="isDarkMode"
              src="@/assets/mycvhub_logo_dark.png"
              alt="MyCVHub Logo"
              class="logo"
            />
            <img
              v-else
              src="@/assets/mycvhub_logo_light.png"
              alt="MyCVHub Logo"
              class="logo"
            />
          </router-link>
        </v-app-bar-title>

        <v-spacer v-if="vuetify.display.mdAndUp.value" />
        <div
          v-if="vuetify.display.mdAndUp.value"
          class="language-selector"
        >
          <v-menu class="language-selector-menu">
            <template v-slot:activator="{ props }">
              <v-btn
                prepend-icon="mdi-web"
                :text="languageDisplayName(locale)"
                v-bind="props"
                size="large"
                variant="flat"
                style="text-transform: none"
              />
            </template>
            <v-list>
              <v-list-item
                v-for="lang in i18n.global.availableLocales"
                :key="lang"
                @click="changeLocale(lang)"
                :class="localeClass(lang).value"
                :title="languageDisplayName(lang)"
              />
            </v-list>
          </v-menu>
        </div>
      </v-app-bar>

      <v-navigation-drawer
        id="side-nav"
        v-model="isNavMenuOpen"
        disable-resize-watcher
        disable-route-watcher
        temporary
      >
        <v-list>
          <template v-if="accountApi.isUserLoggedIn()">
            <v-list-item
              v-if="profileThumbnail"
              link
              :to="{ name: 'account' }"
              :title="t('account.title')"
            >
              <template v-slot:prepend>
                <v-img
                  class="profile-picture-thumbnail"
                  :src="profileThumbnail"
                  :lazy-src="ProfileApi.getDefaultProfilePictureThumbnail()"
                />
              </template>
            </v-list-item>
            <v-list-item
              v-else
              prepend-icon="mdi-account-circle"
              link
              :to="{ name: 'account' }"
              :title="t('account.title')"
            />
          </template>
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

          <v-list-item
            v-if="accountApi.isUserLoggedIn()"
            link
            prepend-icon="mdi-logout"
            :to="{ name: 'logout' }"
            :title="t('account.logout.action')"
          />

          <v-list-group
            v-if="vuetify.display.smAndDown.value"
            class="sidebar-language-switcher"
          >
            <template v-slot:activator="{ props }">
              <v-list-item
                v-bind="props"
                link
                prepend-icon="mdi-web"
                :title="languageDisplayName(locale)"
              />
            </template>
            <v-list-item
              v-for="lang in i18n.global.availableLocales"
              :key="lang"
              @click="changeLocale(lang)"
              :class="localeClass(lang).value"
              :title="languageDisplayName(lang)"
              prepend-icon="mdi-translate"
            />
          </v-list-group>
        </v-list>
      </v-navigation-drawer>
    </template>

    <suspense>
      <div class="router-view-wrapper">
        <router-view />
      </div>
    </suspense>

    <v-footer v-if="showNavigation">
      <div class="footer-content">
        <v-row justify="center">
          <v-col
            cols="12"
            class="text-center"
          >
            &copy; 2024 MyCVHub. All Rights Reserved.
          </v-col>
        </v-row>
        <v-row
          justify="center"
          align="center"
          class="links"
        >
          <router-link :to="{ name: 'privacy-policy' }">{{ t('privacy.title') }}</router-link>
          <v-btn
            href="https://github.com/lstreckeisen/my-cv"
            target="_blank"
            size="32"
            variant="flat"
          >
            <template v-slot:prepend>
              <v-icon
                icon="mdi-github"
                size="32"
              />
            </template>
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
import { computed, type ComputedRef, ref, watchEffect } from 'vue'
import { useLocale, useTheme } from 'vuetify'
import accountApi from '@/api/AccountApi'
import router from '@/router'
import { useI18n } from 'vue-i18n'
import i18n from '@/plugins/i18n'
import LanguageService from '@/services/LanguageService'
import profileApi from '@/api/ProfileApi'
import vuetify from '@/plugins/vuetify'
import ProfileApi from '@/api/ProfileApi'

const { t } = useI18n({
  useScope: 'global'
})
const locale = useLocale().current

function localeClass(lang: string): ComputedRef {
  return computed(() => {
    return {
      'current-locale': locale.value === lang
    }
  })
}

function changeLocale(lang: string) {
  isNavMenuOpen.value = false
  locale.value = lang
  LanguageService.setLanguage(lang)
}

function languageDisplayName(lang: string): string | undefined {
  return new Intl.DisplayNames([lang], { type: 'language' }).of(lang)
}

const isNavMenuOpen = ref(false)
const showNavigation = ref(true)
const profileThumbnail = ref<string>()

watchEffect(async () => {
  if (accountApi.isUserLoggedIn()) {
    try {
      profileThumbnail.value = (await profileApi.getThumbnail()).thumbnailUrl
    } catch (e) {
      profileThumbnail.value = undefined
    }
  } else {
    profileThumbnail.value = undefined
  }
})

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
    isNavMenuOpen.value = false
    if (to.meta.authRequired && !accountApi.isUserLoggedIn()) {
      next({ name: 'login', query: { redirect: to.fullPath } })
    } else {
      showNavigation.value = !(to.meta.hideNavigation || false)
      next()
    }
  }
)
</script>

<style lang="scss" scoped>
header.navigation-bar {
  border-bottom: 1px solid rgba(var(--v-border-color), var(--v-border-opacity));
}

#side-nav {
  div.v-list {
    padding-top: 2px;
  }

  button {
    justify-self: center;
  }

  .profile-picture-thumbnail {
    width: 40px;
    height: 40px;
    margin-right: 15px;
  }
}

.home-link-container {
  margin-inline-start: 0 !important;

  .logo {
    vertical-align: middle;
    width: 220px;
  }
}

.language-selector {
  margin-right: 5px;
}

.router-view-wrapper {
  display: flex;
  height: 100%;
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

<style lang="scss">
.language-selector-menu div.v-overlay__content {
  margin-left: 7px;
}
</style>
