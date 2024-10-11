<template>
  <v-dialog :model-value="true" @update:model-value="cancel">
    <v-sheet class="skill-sheet">
      <h2 v-if="isEdit">Edit Skill</h2>
      <h2 v-else>Add Skill</h2>

      <v-form @submit.prevent>
        <v-text-field
          label="Name"
          v-model="formState.name"
          :error-messages="nameErrors"
        />
        <v-text-field
          label="Type"
          hint="Skills will be grouped by their type in your CV. Use something like 'Programming Languages', 'Communication', etc."
          v-model="formState.type"
          :error-messages="typeErrors"
        />
        <v-slider
          label="Level"
          v-model="formState.level"
          thumb-label="always"
          step="1"
          :error-messages="levelErrors"
          class="level-slider"
          color="btn-primary"
        />

        <div class="form-action-buttons">
          <v-btn type="submit" text="Save" color="btn-primary" @click="save" />
          <v-btn text="Cancel" @click="cancel" />
        </div>
      </v-form>
    </v-sheet>
  </v-dialog>
</template>

<script setup lang="ts">
import { type ComputedRef, reactive, ref } from 'vue'
import type { WorkExperienceDto } from '@/dto/WorkExperienceDto'
import { VDateInput } from 'vuetify/labs/components'
import { helpers, required } from '@vuelidate/validators'
import useVuelidate from '@vuelidate/core'
import profileApi from '@/api/ProfileApi'
import { convertDateToString, convertStringToDate } from '@/services/DateHelper'
import { type ErrorMessages, getErrorMessages } from '@/services/FormHelper'
import type { ErrorDto } from '@/dto/ErrorDto'
import type { SkillDto } from '@/dto/SkillDto'
import type { SkillUpdateDto } from '@/dto/SkillUpdateDto'

const props = defineProps<{
  value: SkillDto | undefined
  isEdit: boolean
}>()

const emit = defineEmits(['saveNew', 'saveEdit', 'cancel'])

type FormState = {
  name?: string
  type?: string
  level?: number
}

const formState = reactive<FormState>({
  name: props.value?.name,
  type: props.value?.type,
  level: props.value?.level
})

const levelWithinRange = () => formState.level >= 0 && formState.level <= 100

const rules = {
  name: {
    required: helpers.withMessage('Name must not be blank', required)
  },
  type: {
    required: helpers.withMessage('Type must not be blank', required)
  },
  level: {
    required: helpers.withMessage('Level must not be blank', required),
    levelWithinRange: helpers.withMessage('Level must be between 0 and 100', levelWithinRange)
  },
}

const form = useVuelidate(rules, formState)

const errorMessages = ref<ErrorMessages>({})

function getErrors(attributeName: string): ComputedRef {
  return getErrorMessages(errorMessages, form, attributeName)
}

const nameErrors = getErrors('name')
const typeErrors = getErrors('type')
const levelErrors = getErrors('level')

async function save() {
  const isValid = await form.value.$validate()
  if (!isValid) {
    return
  }

  const skillUpdate: SkillUpdateDto = {
    id: props.isEdit ? props.value.id : null,
    name: formState.name,
    type: formState.type,
    level: formState.level
  }

  try {
    const savedSkill = await profileApi.saveSkill(skillUpdate)
    console.log(savedSkill)
    if (props.isEdit) {
      emit('saveEdit', savedSkill)
    } else {
      emit('saveNew', savedSkill)
    }
    errorMessages.value = {}
  } catch (e) {
    const error = e as ErrorDto
    errorMessages.value = error.errors || {}
  }
}

function cancel() {
  emit('cancel')
}
</script>

<style lang="scss" scoped>
.skill-sheet {
  padding: 30px;

  h2 {
    margin-bottom: 10px;
  }

  .form-action-buttons {
    margin-top: 10px;
  }

  .level-slider {
    margin: 15px 0
  }
}
</style>
