<template>
  <v-row class="work-experience-entry">
    <v-col
      class="job-title"
      cols="8"
      md="10"
      >{{ workExperience.jobTitle }}</v-col
    >
    <v-col
      class="location"
      cols="4"
      md="2"
      >{{ workExperience.location }}</v-col
    >
    <v-col
      class="employer"
      cols="8"
      md="10"
      >{{ workExperience.company }}</v-col
    >
    <v-col
      class="employment-range"
      cols="4"
      md="2"
    >
      {{ toShortDate(workExperience.positionStart, i18n) }} -
      {{ toShortDate(workExperience.positionEnd, i18n) }}
    </v-col>
    <v-col
      v-if="workExperience.description"
      class="description"
      cols="12"
    >
      <pre>{{ workExperience.description }}</pre>
    </v-col>
  </v-row>
</template>

<script setup lang="ts">
import type { PublicWorkExperienceDto } from '@/dto/PublicWorkExperienceDto'
import { toShortDate } from '@/services/DateHelper'
import type { WorkExperienceDto } from '@/dto/WorkExperienceDto'
import { useI18n } from 'vue-i18n'

defineProps<{
  workExperience: PublicWorkExperienceDto | WorkExperienceDto
}>()

const i18n = useI18n({
  useScope: 'global'
})
</script>

<style lang="scss" scoped>
.work-experience-entry {
  > .job-title,
  > .location {
    padding-top: 10px;
    padding-bottom: 0;
  }

  > .employer,
  > .employment-range {
    padding-top: 0;
    padding-bottom: 5px;
    font-weight: lighter;
  }

  > .job-title {
    font-size: 14pt;
    font-weight: bold;
  }

  > .description {
    padding-top: 5px;
    padding-bottom: 10px;

    pre {
      white-space: break-spaces;
    }
  }
}
</style>
