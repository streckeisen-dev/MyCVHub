<template>
  <v-row
    justify="center"
    class="work-experiences-editor"
  >
    <v-sheet
      class="editor-sheet"
      rounded
    >
      <v-col cols="12">
        <v-row justify="end">
          <v-btn
            :text="t('workExperience.editor.add')"
            @click="addWorkExperience"
            color="primary"
          />
        </v-row>
      </v-col>
      <work-experience-container
        :values="workExperiences"
        actions
        @edit="editWorkExperience"
        @delete="deleteWorkExperience"
      />
    </v-sheet>
  </v-row>
  <edit-work-experience-dialog
    v-if="showEditDialog"
    :value="workExperienceToEdit"
    :is-edit="isEdit!!"
    @saveNew="onSaveNew"
    @saveEdit="onSaveEdit"
    @cancel="onEditCancel"
  />
</template>

<script setup lang="ts">
import type { WorkExperienceDto } from '@/dto/WorkExperienceDto'
import { type PropType, ref } from 'vue'
import EditWorkExperienceDialog from '@/views/profile/components/work-experience/EditWorkExperienceDialog.vue'
import profileApi from '@/api/ProfileApi'
import type { ErrorDto } from '@/dto/ErrorDto'
import WorkExperienceContainer from '@/views/profile/components/work-experience/WorkExperienceContainer.vue'
import { useI18n } from 'vue-i18n'
import ToastService from '@/services/ToastService'

const { t } = useI18n({
  useScope: 'global'
})

const workExperiences = defineModel({
  required: true,
  type: Object as PropType<Array<WorkExperienceDto>>
})

const showEditDialog = ref(false)
const workExperienceToEdit = ref<WorkExperienceDto>()
const isEdit = ref<boolean>()

function addWorkExperience() {
  isEdit.value = false
  showEditDialog.value = true
  workExperienceToEdit.value = undefined
}

function editWorkExperience(workExperience: WorkExperienceDto) {
  isEdit.value = true
  showEditDialog.value = true
  workExperienceToEdit.value = workExperience
}

function onSaveNew(newEntry: WorkExperienceDto) {
  showEditDialog.value = false
  workExperienceToEdit.value = undefined
  workExperiences.value.push(newEntry)
}

function onSaveEdit(updatedEntry: WorkExperienceDto) {
  showEditDialog.value = false
  workExperienceToEdit.value = undefined
  const updateIndex = workExperiences.value.findIndex((e) => e.id === updatedEntry.id)
  workExperiences.value[updateIndex] = updatedEntry
}

function onEditCancel() {
  showEditDialog.value = false
  workExperienceToEdit.value = undefined
}

async function deleteWorkExperience(id: number) {
  try {
    await profileApi.deleteWorkExperience(id)
    const index = workExperiences.value.findIndex((e) => e.id === id)
    workExperiences.value.splice(index, 1)
  } catch (e) {
    const error = e as ErrorDto
    const errorDetails = error.message || t('error.genericMessage')
    ToastService.error(t('workExperience.editor.deleteError'), errorDetails)
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
