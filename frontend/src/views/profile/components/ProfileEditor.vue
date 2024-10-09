<template>
  <v-container class="profile-editor">
    <v-row>
      <v-col cols="12">
        <h2>Profile</h2>
      </v-col>
    </v-row>
    <v-tabs v-model="activeTab" color="secondary">
      <v-tab value="general">General</v-tab>
      <template v-if="isCreated">
        <v-tab value="work">Work Experience</v-tab>
        <v-tab value="edu">Education</v-tab>
        <v-tab value="skills">Skills</v-tab>
      </template>
    </v-tabs>

    <v-tabs-window v-model="activeTab">
      <v-tabs-window-item value="general">
        <v-row justify="center">
          <v-sheet class="form-sheet" rounded color="primary">
            <v-form @submit.prevent>
              <v-text-field
                v-model="alias"
                label="Alias"
                hint="Name that will be used in the URL of your profile"
              />
              <v-text-field v-model="jobTitle" label="Job Title" />
              <v-textarea v-model="aboutMe" label="About Me" />
              <v-switch
                v-model="isProfilePublic"
                label="Public Profile"
                hint="If enabled, your profile will be publicly accessible"
                color="btn-primary"
              />
              <v-switch
                v-model="isEmailPublic"
                label="Public E-Mail"
                hint="If enabled, your E-Mail address will be shown in your profile"
                color="btn-primary"
              />
              <v-switch
                v-model="isPhonePublic"
                label="Public Phone"
                hint="If enabled, your phone number will be shown in your profile"
                color="btn-primary"
              />
              <v-switch
                v-model="isAddressPublic"
                label="Public Address"
                hint="If enabled, your address will be shown in your profile"
                color="btn-primary"
              />

              <v-btn
                type="submit"
                text="Save"
                color="btn-primary"
                @click="saveGeneralInformation"
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
      </template>
    </v-tabs-window>
  </v-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { ProfileDto } from '@/dto/ProfileDto'
import profileApi from '@/api/ProfileApi'
import WorkExperiencesEditor from '@/views/profile/components/work-experience/WorkExperiencesEditor.vue'
import EducationEditor from '@/views/profile/components/education/EducationEditor.vue'

const props = defineProps<{
  profile: ProfileDto,
  exists: boolean
}>()

const activeTab = ref('general')
const isCreated = ref(props.exists)
const alias = ref(props.profile.alias)
const jobTitle = ref(props.profile.jobTitle)
const aboutMe = ref(props.profile.aboutMe)
const isProfilePublic = ref(props.profile.isProfilePublic)
const isEmailPublic = ref(props.profile.isEmailPublic)
const isPhonePublic = ref(props.profile.isPhonePublic)
const isAddressPublic = ref(props.profile.isAddressPublic)
const workExperiences = ref(props.profile.workExperiences)
const education = ref(props.profile.education)

async function saveGeneralInformation() {
  try {
    await profileApi.updateGeneralInformation({
      alias: alias.value,
      jobTitle: jobTitle.value,
      aboutMe: aboutMe.value,
      isProfilePublic: isProfilePublic.value,
      isEmailPublic: isEmailPublic.value,
      isPhonePublic: isPhonePublic.value,
      isAddressPublic: isAddressPublic.value
    })
    isCreated.value = true
  } catch (e) {
    console.log(e)
  }
}
</script>

<style scoped>
h2 {
  margin-bottom: 10px;
}

.profile-editor {
  .form-sheet {
    margin-top: 10px;
    width: 100%;
    padding: 30px;
  }
}
</style>
