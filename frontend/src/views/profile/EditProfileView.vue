<template>
  <v-main>
    <profile-editor v-if="profile" :profile="profile" :exists="true" />
    <loading-spinner v-else-if="isLoadingProfile" />
    <v-empty-state
      v-else
      :headline="t('error.genericMessage')"
      :title="t('editProfile.error')"
      :text="loadingError"
    />
  </v-main>
</template>

<script setup lang="ts">
import profileApi from '@/api/ProfileApi'
import ProfileEditor from '@/views/profile/components/ProfileEditor.vue'
import type { ProfileDto } from '@/dto/ProfileDto'
import { ref } from 'vue'
import type { ErrorDto } from '@/dto/ErrorDto'
import LoadingSpinner from '@/components/LoadingSpinner.vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const profile = ref<ProfileDto>()
const isLoadingProfile = ref(true)
const loadingError = ref<string>()

try {
  profile.value = await profileApi.getProfile()
} catch (e) {
  const error = e as ErrorDto
  loadingError.value = error.message
} finally {
  isLoadingProfile.value = false
}
</script>

<i18n>
{
  "de": {
    "editProfile": {
      "error": "Das Profil konnte nicht geladen werden"
    }
  },
  "en": {
    "editProfile": {
      "error": "Failed to load profile"
    }
  }
}
</i18n>
