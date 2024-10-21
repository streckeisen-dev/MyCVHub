<template>
  <v-container class="profile-editor">
    <v-row>
      <v-col cols="12" class="back-button">
        <router-link :to="{ name: 'account' }">
          <v-btn icon="mdi-arrow-left" />
          <span>{{ t('profileEditor.back') }}</span>
        </router-link>
      </v-col>
      <v-col cols="12">
        <h2>{{ t('profile.title') }}</h2>
      </v-col>
    </v-row>
    <v-tabs v-model="activeTab" color="secondary">
      <v-tab value="general">{{ t('profileEditor.general') }}</v-tab>
      <template v-if="isCreated">
        <v-tab value="work">{{ t('profile.workExperiences') }}</v-tab>
        <v-tab value="edu">{{ t('profile.education') }}</v-tab>
        <v-tab value="skills">{{ t('profile.skills') }}</v-tab>
      </template>
    </v-tabs>

    <v-tabs-window v-model="activeTab">
      <v-tabs-window-item value="general">
        <v-row justify="center">
          <v-sheet class="form-sheet" rounded>
            <v-form @submit.prevent>
              <v-row class="form-flex">
                <v-col cols="12" md="4">
                  <v-img
                    :src="profilePicture"
                    :lazy-src="defaultProfilePicture"
                    class="profile-picture"
                  >
                    <template #placeholder>
                      <v-row class="fill-height" justify="center" align="center">
                        <v-progress-circular indeterminate />
                      </v-row>
                    </template>
                  </v-img>
                  <v-file-input
                    v-model="formState.profilePicture"
                    :label="t('fields.profilePicture')"
                    :hint="t('profileEditor.pictureHint')"
                    prepend-icon="mdi-camera"
                    accept=".png, .jpeg, .jpg"
                    :error-messages="profilePictureErrors"
                  />
                </v-col>

                <v-col cols="12" md="8">
                  <v-text-field
                    v-model="formState.alias"
                    :label="t('fields.alias')"
                    :hint="t('profileEditor.aliasHint')"
                    :error-messages="aliasErrors"
                  />
                  <v-text-field
                    v-model="formState.jobTitle"
                    :label="t('fields.jobTitle')"
                    :error-messages="jobTitleErrors"
                  />
                  <v-textarea
                    v-model="formState.bio"
                    :label="t('fields.bio')"
                    :error-messages="bioErrors"
                  />
                  <v-switch
                    v-model="formState.isProfilePublic"
                    :label="t('profileEditor.publicProfile')"
                    :hint="t('profileEditor.publicProfileHint')"
                    color="primary"
                  />
                  <v-switch
                    v-model="formState.isEmailPublic"
                    :disabled="!formState.isProfilePublic"
                    :label="t('profileEditor.publicEmail')"
                    :hint="t('profileEditor.publicEmailHint')"
                    color="primary"
                  />
                  <v-switch
                    v-model="formState.isPhonePublic"
                    :disabled="!formState.isProfilePublic"
                    :label="t('profileEditor.publicPhone')"
                    :hint="t('profileEditor.publicPhoneHint')"
                    color="primary"
                  />
                  <v-switch
                    v-model="formState.isAddressPublic"
                    :disabled="!formState.isProfilePublic"
                    :label="t('profileEditor.publicAddress')"
                    :hint="t('profileEditor.publicAddressHint')"
                    color="primary"
                  />
                  <v-switch
                    v-model="formState.hideDescriptions"
                    :disabled="!formState.isProfilePublic"
                    :label="t('profileEditor.hideDescriptions')"
                    :hint="t('profileEditor.hideDescriptionsHint')"
                    color="primary"
                  />
                </v-col>
              </v-row>

              <v-btn
                type="submit"
                :text="t('forms.save')"
                color="primary"
                @click.prevent="saveGeneralInformation"
              />
            </v-form>
          </v-sheet>
        </v-row>
      </v-tabs-window-item>
      <template v-if="isCreated">
        <v-tabs-window-item value="work">
          <work-experiences-editor v-model="workExperiences" />
        </v-tabs-window-item>
        <v-tabs-window-item value="edu">
          <education-editor v-model="education" />
        </v-tabs-window-item>
        <v-tabs-window-item value="skills">
          <skills-editor v-model="skills" />
        </v-tabs-window-item>
      </template>
    </v-tabs-window>
  </v-container>
  <notification
    v-if="savingError"
    :title="t('profileEditor.saveErrorTitle')"
    :message="`${t('profileEditor.saveErrorMessage')}. ${savingError}`"
  />
</template>

<script setup lang="ts">
import { computed, type ComputedRef, reactive, ref } from 'vue'
import type { ProfileDto } from '@/dto/ProfileDto'
import profileApi from '@/api/ProfileApi'
import ProfileApi from '@/api/ProfileApi'
import WorkExperiencesEditor from '@/views/profile/components/work-experience/WorkExperiencesEditor.vue'
import EducationEditor from '@/views/profile/components/education/EducationEditor.vue'
import SkillsEditor from '@/views/profile/components/skill/SkillsEditor.vue'
import { helpers, required } from '@vuelidate/validators'
import useVuelidate from '@vuelidate/core'
import { type ErrorMessages, getErrorMessages } from '@/services/FormHelper'
import type { ErrorDto } from '@/dto/ErrorDto'
import router from '@/router'
import Notification from '@/components/Notification.vue'
import round from 'lodash/round'
import type { ProfileUpdateRequestDto } from '@/dto/ProfileUpdateRequestDto'
import { useI18n } from 'vue-i18n'
import i18n from '@/plugins/i18n'
import profileLocale from '@/locales/ProfileLocale'
import { withI18nMessage } from '@/validation/validators'

const { t, mergeLocaleMessage } = useI18n()
i18n.global.availableLocales.forEach((lang) => mergeLocaleMessage(lang, profileLocale[lang]))

const props = defineProps<{
  profile: ProfileDto
  exists: boolean
}>()

const profilePictureMaxSize = 2097152
const activeTab = ref('general')
const isCreated = ref(props.exists)
const workExperiences = ref(props.profile.workExperiences)
const education = ref(props.profile.education)
const skills = ref(props.profile.skills)
const savingError = ref<string>()
const profilePictureUrl = ref(props.profile.profilePicture)

const defaultProfilePicture = ProfileApi.getDefaultProfilePicture()
const profilePicture = computed(() => {
  return profilePictureUrl.value || defaultProfilePicture
})

type FormState = {
  profilePicture?: File
  alias?: string
  jobTitle?: string
  bio?: string
  isProfilePublic?: boolean
  isEmailPublic?: boolean
  isPhonePublic?: boolean
  isAddressPublic?: boolean
  hideDescriptions?: boolean
}

const formState = reactive<FormState>({
  profilePicture: undefined,
  alias: props.profile.alias,
  jobTitle: props.profile.jobTitle,
  bio: props.profile.bio,
  isProfilePublic: props.profile.isProfilePublic,
  isEmailPublic: props.profile.isEmailPublic,
  isPhonePublic: props.profile.isPhonePublic,
  isAddressPublic: props.profile.isAddressPublic,
  hideDescriptions: props.profile.hideDescriptions
})

const fileSizeValidator = withI18nMessage(
  () =>
    formState.profilePicture == undefined || formState.profilePicture?.size <= profilePictureMaxSize
)
const profilePictureSizeValidator = helpers.withParams(
  { maxSize: round(profilePictureMaxSize * Math.pow(2, -20), 1) },
  fileSizeValidator
)

const rules = {
  profilePicture: isCreated.value
    ? {
        fileSizeValidator: profilePictureSizeValidator
      }
    : {
        required,
        fileSizeValidator: profilePictureSizeValidator
      },
  alias: {
    required
  },
  jobTitle: {
    required
  },
  bio: {}
}

const form = useVuelidate<FormState>(rules, formState)
const errorMessages = ref<ErrorMessages>({})

function getErrors(attributeName: string): ComputedRef {
  return getErrorMessages(errorMessages, form, attributeName)
}

const profilePictureErrors = getErrors('profilePicture')
const aliasErrors = getErrors('alias')
const jobTitleErrors = getErrors('jobTitle')
const bioErrors = getErrors('bio')

async function saveGeneralInformation() {
  const isValid = await form.value.$validate()
  if (!isValid) {
    return
  }

  try {
    const profileUpdate: ProfileUpdateRequestDto = {
      profilePicture: formState.profilePicture,
      alias: formState.alias,
      jobTitle: formState.jobTitle,
      bio: formState.bio,
      isProfilePublic: formState.isProfilePublic,
      isEmailPublic: formState.isEmailPublic,
      isPhonePublic: formState.isPhonePublic,
      isAddressPublic: formState.isAddressPublic,
      hideDescriptions: formState.hideDescriptions
    }

    const savedProfile = await profileApi.updateGeneralInformation(profileUpdate)
    if (isCreated.value === false) {
      await router.push({ name: 'edit-profile' })
    } else {
      isCreated.value = true
      errorMessages.value = {}

      formState.alias = savedProfile.alias
      formState.jobTitle = savedProfile.jobTitle
      formState.bio = savedProfile.bio
      formState.isProfilePublic = savedProfile.isProfilePublic
      formState.isEmailPublic = savedProfile.isEmailPublic
      formState.isPhonePublic = savedProfile.isPhonePublic
      formState.isAddressPublic = savedProfile.isAddressPublic
      formState.hideDescriptions = savedProfile.hideDescriptions
      formState.profilePicture = undefined

      profilePictureUrl.value = savedProfile.profilePicture
    }
  } catch (e) {
    const error = e as ErrorDto
    if (error.errors) {
      errorMessages.value = error.errors
      savingError.value = undefined
    } else {
      errorMessages.value = {}
      savingError.value = error.message || ' '
    }
  }
}
</script>

<i18n>
{
  "de": {
    "profileEditor": {
      "back": "Zurück zum Account",
      "general": "Allgemein",
      "pictureHint": "Dein Profilbild darf nicht grösser als 2MB sein.",
      "aliasHint": "Name, der in der URL deines Profils verwendet werden soll",
      "publicProfile": "Öffentlicher Profilzugriff",
      "publicProfileHint": "Falls aktiviert, ist dein Profil öffentlich zugänglich",
      "publicEmail": "E-Mail im öffentlichen Profil anzeigen",
      "publicEmailHint": "Falls aktiviert, wird deine E-Mail-Adresse in deinem Profil angezeigt",
      "publicPhone": "Telefon im öffentlichen Profil anzeigen",
      "publicPhoneHint": "Falls aktiviert, wird deine Telefonnummer in deinem Profil angezeigt",
      "publicAddress": "Adresse im öffentlichen Profil anzeigen",
      "publicAddressHint": "Falls aktiviert, wird deine Adresse in deinem Profil angezeigt",
      "hideDescriptions": "Beschreibungen aus dem öffentlichen Profil ausblenden",
      "hideDescriptionsHint": "Falls aktiviert, werden detaillierte Beschreibungen deiner Lebenslaufeinträge nicht in deinem Profil angezeigt",
      "saveErrorTitle": "Das Profil konnte nicht gespeichert werden",
      "saveErrorMessage": "Beim Versuch, dein Profil zu speichern, ist ein Fehler aufgetreten"
    }
  },
  "en": {
    "profileEditor": {
      "back": "Back to account",
      "general": "General",
      "pictureHint": "Your profile picture must not exceed 2MB.",
      "aliasHint": "Name that will be used in the URL of your profile",
      "publicProfile": "Public Profile Access",
      "publicProfileHint": "If enabled, your profile will be publicly accessible",
      "publicEmail": "Show E-Mail in public profile",
      "publicEmailHint": "If enabled, your E-Mail address will be shown in your profile",
      "publicPhone": "Show phone in public profile",
      "publicPhoneHint": "If enabled, your phone number will be shown in your profile",
      "publicAddress": "Show address in public profile",
      "publicAddressHint": "If enabled, your address will be shown in your profile",
      "hideDescriptions": "Hide descriptions from public profile",
      "hideDescriptionsHint": "If enabled, detailed descriptions of your CV entries won't be shown in your profile",
      "saveErrorTitle": "Failed to save profile",
      "saveErrorMessage": "An error occurred while trying to save your profile"
    }
  }
}
</i18n>

<style lang="scss" scoped>
.back-button {
  padding-left: 0;
}

h2 {
  margin-bottom: 10px;
}

.profile-editor {
  .form-sheet {
    margin-top: 10px;
    width: 100%;
    padding: 30px;

    .form-flex {
      flex-direction: row-reverse;

      .profile-picture {
        max-width: min(100%, 400px);
        height: auto;
        margin-bottom: 10px;
        margin-left: 40px;
      }
    }
  }
}
</style>
