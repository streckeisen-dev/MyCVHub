<template>
  <v-main>
    <profile-editor :profile="emptyProfile" :exists="false" />
  </v-main>
</template>

<script setup lang="ts">
import ProfileEditor from '@/views/profile/components/ProfileEditor.vue'
import type { ProfileDto } from '@/dto/ProfileDto'
import profileApi from '@/api/ProfileApi'
import router from '@/router'

const emptyProfile: ProfileDto = {
  alias: '',
  jobTitle: '',
  bio: '',
  isProfilePublic: true,
  isEmailPublic: false,
  isPhonePublic: false,
  isAddressPublic: false,
  workExperiences: [],
  education: [],
  skills: []
}

try {
  await profileApi.getProfile()
  router.push({ name: 'edit-profile' })
} catch (e) {
  console.log(e)
}
</script>

<style scoped></style>
