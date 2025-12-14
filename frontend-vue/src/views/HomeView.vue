<template>
  <v-main>
    <v-container class="home-view">
      <v-row
        align="center"
        justify="center"
        class="headline"
      >
        <v-col
          cols="12"
          class="text-center"
        >
          <h1>{{ t('home.welcome.title') }}</h1>
          <p>{{ t('home.welcome.message') }}</p>
          <router-link :to="{ name: 'signup' }">
            <v-btn
              color="primary"
              large
              :to="{ name: 'login' }"
              :text="t('home.getStarted')"
            />
          </router-link>
        </v-col>
      </v-row>

      <v-row>
        <v-col>
          <h2 class="text-center">{{ t('home.features.title') }}</h2>
        </v-col>
      </v-row>
      <v-row justify="center">
        <v-col
          cols="12"
          sm="6"
          md="4"
          lg="3"
          v-for="(feature, index) in features"
          :key="index"
        >
          <v-card>
            <v-card-title>{{ t(feature.title) }}</v-card-title>
            <v-card-text>
              {{ t(feature.description) }}
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <v-row
        align="center"
        justify="center"
        class="call-to-action"
      >
        <v-col
          cols="12"
          md="8"
          class="text-center"
        >
          <h2>{{ t('home.callToAction.title') }}</h2>
          <router-link :to="{ name: 'signup' }">
            <v-btn
              color="primary"
              large
              >{{ t('home.callToAction.btn') }}</v-btn
            >
          </router-link>
        </v-col>
      </v-row>
    </v-container>
  </v-main>
</template>

<script setup lang="ts">
import accountApi from '@/api/AccountApi'
import router from '@/router'
import { useI18n } from 'vue-i18n'

const { t } = useI18n({
  useScope: 'global'
})

const features = [
  {
    title: 'home.features.create.title',
    description: 'home.features.create.description'
  },
  {
    title: 'home.features.share.title',
    description: 'home.features.share.description'
  } /*,
  {
    title: 'Track Applications',
    description: 'Keep track of your job applications and their status all in one place.'
  }*/
]

if (accountApi.isUserLoggedIn()) {
  await router.push({ name: 'account' })
}
</script>

<style lang="scss" scoped>
.home-view {
  display: flex;
  flex-direction: column;

  .headline {
    p {
      margin-bottom: 10px;
    }
  }

  .call-to-action h2 {
    margin-bottom: 10px;
  }
}
</style>
