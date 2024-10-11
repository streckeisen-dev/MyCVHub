<template>
  <v-app-bar role="navigation" aria-label="Header navigation">
    <v-app-bar-title>
      {{ displayName }}
    </v-app-bar-title>
    <template v-slot:append>
      <!-- TODO: load & display socials -->
      <v-btn icon="mdi-linkedin" href="https://www.linkedin.com/in/lukas-streckeisen" />
      <v-btn icon="mdi-github" href="https://github.com/lstreckeisen" />
    </template>
  </v-app-bar>

  <v-main>
    <v-container v-if="profile">
      <v-row>
        <v-col cols="12" md="6" class="person-column">
          <img :src="profileImageSrc" alt="Default Profile Picture" class="profile-picture" />
          <profile-section :title="displayName" h2>
            <p>{{ profile.jobTitle }}</p>
          </profile-section>

          <profile-section title="About Me">
            <p>{{ profile.aboutMe }}</p>
          </profile-section>

          <profile-section v-if="hasContactInformation" title="Contact">
            <template v-if="profile.street">
              <p>{{ displayName }}</p>
              <p>{{ profile.street }} {{ profile.houseNumber }}</p>
              <p>{{ profile.country }}-{{ profile.postcode }} {{ profile.city }}</p>
            </template>
            <p v-else>{{ profile.city }}, {{ profile.country }}</p>
            <p v-if="profile.email">
              <a :href="profileEmailLink">{{ profile.email }}</a>
            </p>
            <p v-if="profile.phone">
              <a :href="profilePhoneLink">{{ profile.phone }}</a>
            </p>
          </profile-section>
        </v-col>
        <v-col cols="12" md="6">
          <profile-section title="Work Experience" h2>
            <work-experience-container :values="profile.workExperiences" />
          </profile-section>
          <profile-section title="Education" h2>
            <education-container :values="profile.education" />
          </profile-section>
          <profile-section title="Skills" h2>
            <skills-container :values="profile.skills" />
          </profile-section>
        </v-col>
      </v-row>
    </v-container>
    <loading-spinner v-else-if="isProfileLoading" />
    <v-empty-state
      v-else
      headline="Not Found"
      title="We could not find the profile you are looking for."
      :text="loadingError"
    />
  </v-main>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import type { PublicProfileDto } from '@/dto/PublicProfileDto'
import profileApi from '@/api/ProfileApi'
import LoadingSpinner from '@/components/LoadingSpinner.vue'
import type { ErrorDto } from '@/dto/ErrorDto'
import ProfileSection from '@/views/profile/components/ProfileSection.vue'
import vuetify from '@/plugins/vuetify'
import type { PublicEducationDto } from '@/dto/PublicEducationDto'
import { type PublicSkillDto, SkillType } from '@/dto/PublicSkillDto'
import WorkExperienceContainer from '@/views/profile/components/work-experience/WorkExperienceContainer.vue'
import {
  type NavigationGuardNext,
  onBeforeRouteLeave,
  type RouteLocationNormalized,
  type RouteLocationNormalizedLoaded
} from 'vue-router'
import SkillsContainer from '@/views/profile/components/skill/SkillsContainer.vue'
import EducationContainer from '@/views/profile/components/education/EducationContainer.vue'

const eD = ref<PublicEducationDto>({
  institution: 'Institution',
  location: 'Location',
  degreeName: 'Degree Name',
  educationStart: '2020-09-12',
  educationEnd: null,
  description: '- def\n- deg'
})

const skill = ref<PublicSkillDto>({
  name: 'Java',
  level: 80,
  type: SkillType.ProgrammingLanguage
})

const props = defineProps<{ alias: string }>()

const originalTheme = vuetify.theme.global.name.value
vuetify.theme.global.name.value = 'profile'

const profile = ref<PublicProfileDto>()
const isProfileLoading = ref(false)
const loadingError = ref<string>()

const profileImageSrc = computed(() => {
  return new URL('@/assets/default_profile_picture.png', import.meta.url)
})
const displayName = computed(() => {
  if (profile.value) {
    return `${profile.value?.firstName} ${profile.value?.lastName}`
  }
  return ''
})

const hasContactInformation = computed(
  () => profile.value.street || profile.value.phone || profile.value.email
)
const profileEmailLink = computed(() => `mailto:${profile.value.email}`)
const profilePhoneLink = computed(() => `tel:${profile.value.phone}`)

isProfileLoading.value = true
try {
  profile.value = await profileApi.getPublicProfile(props.alias)

  // TODO: set theme colors according to profile
  // vuetify.theme.themes.value.profile.colors.background = '#ff0000'
} catch (e) {
  const error = e as ErrorDto
  loadingError.value = error.message
} finally {
  isProfileLoading.value = false
}

onBeforeRouteLeave(
  (to: RouteLocationNormalized, from: RouteLocationNormalizedLoaded, next: NavigationGuardNext) => {
    vuetify.theme.global.name.value = originalTheme
    next()
  }
)
</script>

<style scoped>
.profile-picture {
  max-width: min(100%, 512px);
  height: auto;
}
</style>
