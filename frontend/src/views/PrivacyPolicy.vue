<template>
  <v-main>
    <v-container fluid>
      <v-row justify="center">
        <v-col cols="12" md="8">
          <v-card>
            <v-card-title>
              <h1>{{ t('privacy.title') }}</h1>
            </v-card-title>
            <v-card-subtitle class="policy-subtitle">
              {{ t('privacy.description') }}
            </v-card-subtitle>
          </v-card>
        </v-col>
      </v-row>

      <v-row justify="center">
        <v-col cols="12" md="8">
          <v-card>
            <v-card-text class="policy-content">
              <div class="policy-section">
                <h2>1. {{ t('privacy.intro.title') }}</h2>
                <p>{{ t('privacy.intro.content') }}</p>
              </div>

              <div class="policy-section">
                <h2>2. {{ t('privacy.collection.title') }}</h2>
                <p>{{ t('privacy.collection.content') }}</p>
                <ul>
                  <li>
                    <strong>{{ t('privacy.collection.profileInfo.title') }}</strong>:
                    {{ t('privacy.collection.profileInfo.content') }}
                  </li>
                  <li>
                    <strong>{{ t('privacy.collection.profilePicture.title') }}</strong>:
                    {{ t('privacy.collection.profilePicture.content') }}
                  </li>
                </ul>
              </div>

              <div class="policy-section">
                <h2>3. {{ t('privacy.usage.title') }}</h2>
                <p>{{ t('privacy.usage.content') }}</p>
                <ul>
                  <li>{{ t('privacy.usage.usage1') }}</li>
                  <li>
                    {{ t('privacy.usage.usage2') }}
                  </li>
                </ul>
              </div>

              <div class="policy-section">
                <h2>4. {{ t('privacy.storage.title') }}</h2>
                <p>
                  <strong>{{ t('privacy.storage.userData.title') }}</strong>:
                  {{ t('privacy.storage.userData.content') }}
                </p>
                <p>
                  <strong>{{ t('privacy.storage.pictures.title') }}</strong>:
                  {{ t('privacy.storage.pictures.content') }}
                </p>
              </div>

              <div class="policy-section">
                <h2>5. {{ t('privacy.access.title') }}</h2>
                <p>{{ t('privacy.access.content') }}</p>
              </div>

              <div class="policy-section">
                <h2>6. {{ t('privacy.sharing.title') }}</h2>
                <p>{{ t('privacy.sharing.content') }}</p>
              </div>

              <div class="policy-section">
                <h2>7. {{ t('privacy.thirdParty.title') }}</h2>
                <p>{{ t('privacy.thirdParty.content') }}</p>
              </div>

              <div class="policy-section">
                <h2>8. {{ t('privacy.contact.title') }}</h2>
                <p>
                  {{ t('privacy.contact.content') }}
                  <a :href="`mailto:${contactEmail}`">{{ contactEmail }}</a
                  >.
                </p>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </v-main>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n({
  useScope: 'global'
})

const contactEmail = 'contact@mycvhub.ch'
</script>

<style lang="scss" scoped>
.policy-subtitle {
  padding-bottom: 10px;
}

.policy-content {
  padding: 30px;

  .policy-section {
    margin-bottom: 10px;

    ul {
      padding-left: 15px;
      padding-bottom: 5px;
    }

    h2 {
      margin-bottom: 5px;
    }
  }
}
</style>
