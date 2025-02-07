<template>
  <v-row class="project-entry">
    <v-col
      class="project-name"
      cols="8"
      md="10"
    >
      {{ project.name }}
    </v-col>
    <v-col
      class="project-links"
      cols="4"
      md="2"
    >
      <project-link
        v-for="(link, index) in project.links"
        :link="link"
        :key="index"
      />
    </v-col>
    <v-col
      class="project-role"
      cols="8"
      md="10"
    >
      {{ project.role }}
    </v-col>
    <v-col
      class="project-duration"
      cols="4"
      md="2"
    >
      {{ toShortDate(project.projectStart, i18n) }} -
      {{ toShortDate(project.projectEnd, i18n) }}
    </v-col>
    <v-col
      v-if="project.description"
      class="description"
      cols="12"
    >
      <pre>{{ project.description }}</pre>
    </v-col>
  </v-row>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'
import { PublicProjectDto } from '@/dto/PublicProjectDto'
import { ProjectDto } from '@/dto/ProjectDto'
import ProjectLink from '@/views/profile/components/project/ProjectLink.vue'
import { toShortDate } from '@/services/DateHelper'

defineProps<{
  project: PublicProjectDto | ProjectDto
}>()

const i18n = useI18n({
  useScope: 'global'
})
</script>

<style lang="scss" scoped>
.project-entry {
  > .project-name,
  > .project-links {
    margin-top: 10px;
    padding: 0;
  }

  > .project-role,
  > .project-duration {
    padding-top: 0;
    padding-bottom: 5px;
    font-weight: lighter;
  }

  > .project-name {
    font-size: 14pt;
    font-weight: bold;
    padding: 7px;
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
