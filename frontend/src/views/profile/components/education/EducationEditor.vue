<template>
  <v-row justify="center" class="education-editor">
    <v-sheet class="editor-sheet" rounded>
      <v-col cols="12">
        <v-row justify="end">
          <v-btn :text="t('education.editor.add')" color="primary" @click="addEducation" />
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
    :is-edit="isEdit!!"
    @saveNew="onSaveNew"
    @saveEdit="onSaveEdit"
    @cancel="onEditCancel"
  />

  <notification
    v-if="deleteErrorMessage"
    :title="t('education.editor.deleteError')"
    :message="deleteErrorMessage"
  />
</template>

<script setup lang="ts">
import { type PropType, ref } from 'vue'
import profileApi from '@/api/ProfileApi'
import type { ErrorDto } from '@/dto/ErrorDto'
import Notification from '@/components/Notification.vue'
import EducationContainer from '@/views/profile/components/education/EducationContainer.vue'
import EditEducationDialog from '@/views/profile/components/education/EditEducationDialog.vue'
import type { EducationDto } from '@/dto/EducationDto'
import { useI18n } from 'vue-i18n'

const { t } = useI18n({
  useScope: 'global'
})

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
  educationToEdit.value = undefined
}

function editEducation(education: EducationDto) {
  isEdit.value = true
  showEditDialog.value = true
  educationToEdit.value = education
}

function onSaveNew(newEntry: EducationDto) {
  showEditDialog.value = false
  educationToEdit.value = undefined
  isEdit.value = undefined
  education.value.push(newEntry)
}

function onSaveEdit(updatedEntry: EducationDto) {
  showEditDialog.value = false
  educationToEdit.value = undefined
  isEdit.value = undefined
  const updateIndex = education.value.findIndex((e) => e.id === updatedEntry.id)
  education.value[updateIndex] = updatedEntry
}

function onEditCancel() {
  showEditDialog.value = false
  educationToEdit.value = undefined
  isEdit.value = undefined
}

async function deleteEducation(id: number) {
  try {
    await profileApi.deleteEducation(id)
    const index = education.value.findIndex((e) => e.id === id)
    education.value.splice(index, 1)
    deleteErrorMessage.value = undefined
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
