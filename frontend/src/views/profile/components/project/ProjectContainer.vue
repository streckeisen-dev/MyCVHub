<template>
  <v-row
    v-for="(project, index) in sortedProjects"
    :key="(project as ProjectDto).id || index"
    class="projects"
    justify="end"
  >
    <v-col
      cols="12"
      :md="actions ? 10 : 12"
    >
      <project-entry :project="project" />
    </v-col>
    <template v-if="actions">
      <v-col
        cols="3"
        sm="2"
        md="1"
        class="project-action"
      >
        <v-btn
          icon="mdi-pencil"
          color="primary"
          @click="editProject(project as ProjectDto)"
        />
      </v-col>
      <v-col
        cols="3"
        sm="2"
        md="1"
        class="project-action"
      >
        <v-btn
          icon="mdi-delete"
          color="red"
          @click="deleteProject((project as ProjectDto).id)"
        />
      </v-col>
    </template>
  </v-row>
  <v-col
    v-if="values.length === 0"
    cols="12"
  >{{ t('project.noEntries') }}</v-col
  >
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { compareDatesByYearAndMonth, convertStringToDate } from '@/services/DateHelper'
import { useI18n } from 'vue-i18n'
import { ProjectDto } from '@/dto/ProjectDto'
import { PublicProjectDto } from '@/dto/PublicProjectDto'
import ProjectEntry from '@/views/profile/components/project/ProjectEntry.vue'

const { t } = useI18n({
  useScope: 'global'
})

const props = defineProps<{
  values: Array<ProjectDto> | Array<PublicProjectDto>
  actions?: boolean
}>()

const emit = defineEmits(['edit', 'delete'])

const sortedProjects = computed(() => {
  const workExperiences = props.values
  return workExperiences.sort((a, b) => {
    const projectEndA = convertStringToDate(a.projectEnd)
    const projectEndB = convertStringToDate(b.projectEnd)
    const projectStartA = convertStringToDate(a.projectStart)!!
    const projectStartB = convertStringToDate(b.projectStart)!!

    const projectEndComparison = compareDatesByYearAndMonth(projectEndA, projectEndB)
    if (projectEndComparison === 0) {
      return compareDatesByYearAndMonth(projectStartA, projectStartB)
    }
    return projectEndComparison
  })
})

function editProject(project: ProjectDto) {
  emit('edit', project)
}

function deleteProject(id: number) {
  emit('delete', id)
}
</script>

<style lang="scss" scoped>
.projects {
  margin: 10px 0 0 0;

  .project-action {
    align-self: center;
  }
}
</style>
