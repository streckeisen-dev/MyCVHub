<template>
  <v-row
    justify="center"
    class="project-editor"
  >
    <v-sheet
      class="editor-sheet"
      rounded
    >
      <v-col cols="12">
        <v-row justify="end">
          <v-btn
            :text="t('project.editor.add')"
            @click="addProject"
            color="primary"
          />
        </v-row>
      </v-col>
      <project-container
        :values="projects"
        actions
        @edit="editProject"
        @delete="deleteProject"
      />
    </v-sheet>
  </v-row>
  <edit-project-dialog
    v-if="showEditDialog"
    :value="projectToEdit"
    :is-edit="isEdit!!"
    @saveNew="onSaveNew"
    @saveEdit="onSaveEdit"
    @cancel="onEditCancel"
  />
</template>

<script setup lang="ts">
import { type PropType, ref, toRaw } from 'vue'
import profileApi from '@/api/ProfileApi'
import ProjectContainer from '@/views/profile/components/project/ProjectContainer.vue'
import { useI18n } from 'vue-i18n'
import ToastService from '@/services/ToastService'
import { ProjectDto } from '@/dto/ProjectDto'
import EditProjectDialog from '@/views/profile/components/project/EditProjectDialog.vue'
import { RestError } from '@/api/RestError'

const { t } = useI18n({
  useScope: 'global'
})

const projects = defineModel({
  required: true,
  type: Object as PropType<Array<ProjectDto>>
})

const showEditDialog = ref(false)
const projectToEdit = ref<ProjectDto>()
const isEdit = ref<boolean>()

function addProject() {
  isEdit.value = false
  showEditDialog.value = true
  projectToEdit.value = undefined
}

function editProject(project: ProjectDto) {
  isEdit.value = true
  showEditDialog.value = true
  // deep copy to avoid changing project links of the original project directly
  projectToEdit.value = structuredClone(toRaw(project))
}

function onSaveNew(newEntry: ProjectDto) {
  showEditDialog.value = false
  projectToEdit.value = undefined
  projects.value.push(newEntry)
}

function onSaveEdit(updatedEntry: ProjectDto) {
  showEditDialog.value = false
  projectToEdit.value = undefined
  const updateIndex = projects.value.findIndex((e) => e.id === updatedEntry.id)
  projects.value[updateIndex] = updatedEntry
}

function onEditCancel() {
  showEditDialog.value = false
  projectToEdit.value = undefined
}

async function deleteProject(id: number) {
  try {
    await profileApi.deleteProject(id)
    const index = projects.value.findIndex((e) => e.id === id)
    projects.value.splice(index, 1)
  } catch (e) {
    const error = (e as RestError).errorDto
    const errorDetails = error?.message || t('error.genericMessage')
    ToastService.error(t('project.editor.deleteError'), errorDetails)
  }
}
</script>

<style scoped>
.editor-sheet {
  margin-top: 10px;
  width: 100%;
  padding: 30px;
}
</style>
