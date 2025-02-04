<template>
  <v-row
    justify="center"
    class="skills-editor"
  >
    <v-sheet
      class="editor-sheet"
      rounded
    >
      <v-col cols="12">
        <v-row justify="end">
          <v-btn
            :text="t('skills.editor.add')"
            color="primary"
            @click="addSkill"
          />
        </v-row>
      </v-col>
      <skills-container
        :values="skills"
        actions
        @edit="editSkill"
        @delete="deleteSkill"
      />
    </v-sheet>
  </v-row>
  <edit-skill-dialog
    v-if="showEditDialog"
    :value="skillToEdit"
    :is-edit="isEdit!!"
    @saveNew="onSaveNew"
    @saveEdit="onSaveEdit"
    @cancel="onEditCancel"
  />
</template>

<script setup lang="ts">
import { type PropType, ref } from 'vue'
import profileApi from '@/api/ProfileApi'
import SkillsContainer from '@/views/profile/components/skill/SkillsContainer.vue'
import EditSkillDialog from '@/views/profile/components/skill/EditSkillDialog.vue'
import type { SkillDto } from '@/dto/SkillDto'
import { useI18n } from 'vue-i18n'
import ToastService from '@/services/ToastService'
import { RestError } from '@/api/RestError'

const { t } = useI18n({
  useScope: 'global'
})

const skills = defineModel({
  required: true,
  type: Object as PropType<Array<SkillDto>>
})

const showEditDialog = ref(false)
const skillToEdit = ref<SkillDto>()
const isEdit = ref<boolean>()

function addSkill() {
  isEdit.value = false
  showEditDialog.value = true
  skillToEdit.value = undefined
}

function editSkill(skill: SkillDto) {
  isEdit.value = true
  showEditDialog.value = true
  skillToEdit.value = skill
}

function onSaveNew(newEntry: SkillDto) {
  showEditDialog.value = false
  skillToEdit.value = undefined
  skills.value.push(newEntry)
}

function onSaveEdit(updatedEntry: SkillDto) {
  showEditDialog.value = false
  skillToEdit.value = undefined
  const updateIndex = skills.value.findIndex((e) => e.id === updatedEntry.id)
  skills.value[updateIndex] = updatedEntry
}

function onEditCancel() {
  showEditDialog.value = false
  skillToEdit.value = undefined
}

async function deleteSkill(id: number) {
  try {
    await profileApi.deleteSkill(id)
    const index = skills.value.findIndex((e) => e.id === id)
    skills.value.splice(index, 1)
  } catch (e) {
    const error = (e as RestError).errorDto
    const errorDetails = error?.message || t('error.genericMessage')
    ToastService.error(t('skills.editor.deleteError'), errorDetails)
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
