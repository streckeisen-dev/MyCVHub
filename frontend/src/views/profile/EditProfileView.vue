<template>
  <v-main>
    <profile-editor
      v-if="profile"
      :profile="profile"
      :exists="true"
    />
    <loading-spinner v-else-if="isLoadingProfile" />
    <v-empty-state
      v-else
      :headline="t('error.genericMessageTitle')"
      :title="t('profile.loadingError')"
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

const { t } = useI18n({
  useScope: 'global'
})

const profile = ref<ProfileDto>()
const isLoadingProfile = ref(true)
const loadingError = ref<string>()

try {
  profile.value = await profileApi.getProfile()
} catch (e) {
  const error = e as ErrorDto
  loadingError.value = error?.message || t('error.genericMessage')
} finally {
  isLoadingProfile.value = false
}
</script>
