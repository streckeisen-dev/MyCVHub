<template>
  <v-main>
    <profile-editor
      :profile="emptyProfile"
      :exists="false"
    />
  </v-main>
</template>

<script setup lang="ts">
import ProfileEditor from '@/views/profile/components/ProfileEditor.vue'
import type { ProfileDto } from '@/dto/ProfileDto'
import profileApi from '@/api/ProfileApi'
import router from '@/router'
import { RestError } from '@/api/RestError'

const emptyProfile: ProfileDto = {
  jobTitle: '',
  bio: '',
  isProfilePublic: false,
  isEmailPublic: false,
  isPhonePublic: false,
  isAddressPublic: false,
  hideDescriptions: true,
  profilePicture: '',
  workExperiences: [],
  education: [],
  skills: [],
  projects: [],
  theme: undefined
}

try {
  await profileApi.getProfile()
  router.push({ name: 'edit-profile' })
} catch (e) {
  const error = (e as RestError).errorDto
  if (error?.status === 401) {
    // prevent the form from being displayed in (unlikely) case that login state is true and cookies are expired
    await router.push({ name: 'login', query: { redirect: router.currentRoute.value.fullPath } })
  }
  // profile does not exist, therefore continue with creation
}
</script>
