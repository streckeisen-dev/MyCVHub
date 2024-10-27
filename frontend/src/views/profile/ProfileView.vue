<template>
	<v-app-bar
		role="navigation"
		aria-label="Header navigation">
		<v-app-bar-title>
			{{ displayName }}
		</v-app-bar-title>
		<template v-slot:append>
			<!-- TODO: load & display socials -->
			<!--<v-btn icon="mdi-linkedin" href="https://www.linkedin.com/in/lukas-streckeisen" />
      <v-btn icon="mdi-github" href="https://github.com/lstreckeisen" />-->
		</template>
	</v-app-bar>

	<v-main>
		<v-container v-if="profile">
			<v-row>
				<v-col
					cols="12"
					md="6"
					class="person-column">
					<v-img
						:src="profile.profilePicture"
						:lazy-src="defaultProfilePicture"
						class="profile-picture"
						:alt="t('fields.profilePicture')">
						<template #placeholder>
							<v-row
								class="fill-height"
								align="center"
								justify="center">
								<v-progress-circular indeterminate />
							</v-row>
						</template>
					</v-img>
					<profile-section
						:title="displayName"
						h2>
						<p>{{ profile.jobTitle }}</p>
					</profile-section>

					<profile-section
						v-if="profile.bio"
						:title="t('profile.aboutMe')">
						<p>{{ profile.bio }}</p>
					</profile-section>

					<profile-section
						v-if="hasContactInformation"
						:title="t('profile.contact')">
						<template v-if="profile.address">
							<p>{{ displayName }}</p>
							<p>{{ profile.address.street }} {{ profile.address.houseNumber }}</p>
							<p>
								{{ profile.address.country }}-{{ profile.address.postcode }}
								{{ profile.address.city }}
							</p>
						</template>
						<p v-if="profile.email">
							<a :href="profileEmailLink">{{ profile.email }}</a>
						</p>
						<p v-if="profile.phone">
							<a :href="profilePhoneLink">{{ profile.phone }}</a>
						</p>
					</profile-section>
				</v-col>
				<v-col
					cols="12"
					md="6">
					<profile-section
						:title="t('workExperience.title')"
						h2>
						<work-experience-container :values="profile.workExperiences" />
					</profile-section>
					<profile-section
						:title="t('education.title')"
						h2>
						<education-container :values="profile.education" />
					</profile-section>
					<profile-section
						:title="t('skills.title')"
						h2>
						<skills-container :values="profile.skills" />
					</profile-section>
				</v-col>
			</v-row>
		</v-container>
		<loading-spinner v-else-if="isProfileLoading" />
		<v-empty-state
			v-else
			:headline="t('profile.notFound.headline')"
			:title="t('profile.notFound.title')"
			:text="loadingError" />
	</v-main>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import type { PublicProfileDto } from '@/dto/PublicProfileDto'
import profileApi from '@/api/ProfileApi'
import ProfileApi from '@/api/ProfileApi'
import LoadingSpinner from '@/components/LoadingSpinner.vue'
import type { ErrorDto } from '@/dto/ErrorDto'
import ProfileSection from '@/views/profile/components/ProfileSection.vue'
import vuetify from '@/plugins/vuetify'
import WorkExperienceContainer from '@/views/profile/components/work-experience/WorkExperienceContainer.vue'
import {
	type NavigationGuardNext,
	onBeforeRouteLeave,
	type RouteLocationNormalized,
	type RouteLocationNormalizedLoaded
} from 'vue-router'
import SkillsContainer from '@/views/profile/components/skill/SkillsContainer.vue'
import EducationContainer from '@/views/profile/components/education/EducationContainer.vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n({
	useScope: 'global'
})

const props = defineProps<{ alias: string }>()
const originalTheme = vuetify.theme.global.name.value
vuetify.theme.global.name.value = 'profile'

const profile = ref<PublicProfileDto>()
const isProfileLoading = ref(false)
const loadingError = ref<string>()
const defaultProfilePicture = ProfileApi.getDefaultProfilePicture()

const displayName = computed(() => {
	if (profile.value) {
		return `${profile.value?.firstName} ${profile.value?.lastName}`
	}
	return ''
})

const hasContactInformation = computed(() =>
	profile.value ? profile.value.address || profile.value.phone || profile.value.email : false
)
const profileEmailLink = computed(() => `mailto:${profile.value!!.email}`)
const profilePhoneLink = computed(() => `tel:${profile.value!!.phone}`)

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
	max-width: min(100%, 400px);
	height: auto;
}
</style>
