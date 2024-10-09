<template>
  <v-row justify="center" class="education-editor">
    <v-sheet class="editor-sheet" rounded color="primary">
      <v-col cols="12">
        <v-row justify="end">
          <v-btn text="Add education entry" color="btn-primary" @click="addEducation" />
        </v-row>
      </v-col>
      <education-container
        :values="education"
        actions
        @edit="editEducation"
        @delete="deleteEducation"
        />
    </v-sheet>
  </v-row>
  <edit-education-dialog
    v-if="showEditDialog"
    :value="educationToEdit"
    :is-edit="isEdit"
    @saveNew="onSaveNew"
    @saveEdit="onSaveEdit"
    @cancel="onEditCancel"
    />

  <notification
    v-if="deleteErrorMessage"
    title="Failed to delete education entry"
    :message="deleteErrorMessage"
  />
</template>

<script setup lang="ts">
import type { WorkExperienceDto } from '@/dto/WorkExperienceDto'
import { type PropType, ref } from 'vue'
import EditWorkExperienceDialog from '@/views/profile/components/work-experience/EditWorkExperienceDialog.vue'
import profileApi from '@/api/ProfileApi'
import type { ErrorDto } from '@/dto/ErrorDto'
import Notification from '@/components/Notification.vue'
import WorkExperienceContainer from '@/views/profile/components/work-experience/WorkExperienceContainer.vue'
import EducationContainer from '@/views/profile/components/education/EducationContainer.vue'
import EditEducationDialog from '@/views/profile/components/education/EditEducationDialog.vue'
import type { EducationDto } from '@/dto/EducationDto'

const education = defineModel({
  required: true,
  type: Object as PropType<Array<EducationDto>>
})

const showEditDialog = ref(false)
const educationToEdit = ref<EducationDto>()
const isEdit = ref<boolean>()
const deleteErrorMessage = ref<string>()

function addEducation() {
  isEdit.value = false
  showEditDialog.value = true
  educationToEdit.value = null
}

function editEducation(education: EducationDto) {
  isEdit.value = true
  showEditDialog.value = true
  educationToEdit.value = education
}

function onSaveNew(newEntry: WorkExperienceDto) {
  showEditDialog.value = false
  educationToEdit.value = null
  isEdit.value = null
  education.value.push(newEntry)
}

function onSaveEdit(updatedEntry: WorkExperienceDto) {
  showEditDialog.value = false
  educationToEdit.value = null
  isEdit.value = null
  const updateIndex = education.value.findIndex((e) => e.id === updatedEntry.id)
  education.value[updateIndex] = updatedEntry
}

function onEditCancel() {
  showEditDialog.value = false
  educationToEdit.value = null
  isEdit.value = null
}

async function deleteEducation(id: number) {
  try {
    await profileApi.deleteEducation(id)
    const index = education.value.findIndex((e) => e.id === id)
    education.value.splice(index, 1)
    deleteErrorMessage.value = null
  } catch (e) {
    const error = e as ErrorDto
    deleteErrorMessage.value = error.message
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
