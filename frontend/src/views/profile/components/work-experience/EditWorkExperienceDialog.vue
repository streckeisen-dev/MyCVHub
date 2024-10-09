<template>
  <v-dialog :model-value="true" @update:model-value="cancel">
    <v-sheet class="work-experience-sheet">
      <h2 v-if="isEdit">Edit Work Experience</h2>
      <h2 v-else>Add Work Experience</h2>

      <v-form @submit.prevent>
        <v-text-field
          label="Job Title"
          v-model="formState.jobTitle"
          :error-messages="jobTitleErrors"
        />
        <v-text-field
          label="Location"
          v-model="formState.location"
          :error-messages="locationErrors"
        />
        <v-text-field
          label="Company"
          v-model="formState.company"
          :error-messages="companyErrors"
        />
        <v-date-input
          label="Position Start"
          v-model="formState.positionStart"
          :error-messages="positionStartErrors"
        />
        <v-date-input
          label="Position End"
          v-model="formState.positionEnd"
          clearable
          :error-messages="positionEndErrors"
        />
        <v-textarea
          label="Description"
          v-model="formState.description"
          :error-messages="descriptionErrors"
        />

        <v-btn type="submit" text="Save" color="btn-primary" @click="save" />
        <v-btn text="Cancel" @click="cancel" />
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

const props = defineProps<{
  value: WorkExperienceDto | undefined
  isEdit: boolean
}>()

const emit = defineEmits(['saveNew', 'saveEdit', 'cancel'])

type FormState = {
  jobTitle?: string
  location?: string
  company?: string
  positionStart?: Date
  positionEnd?: Date
  description?: string
}

const formState = reactive<FormState>({
  jobTitle: props.value?.jobTitle,
  location: props.value?.location,
  company: props.value?.company,
  positionStart: convertStringToDate(props.value?.positionStart),
  positionEnd: convertStringToDate(props.value?.positionEnd),
  description: props.value?.description
})

const positionEndIsAfterStart = () => {
  if (formState.positionEnd && formState.positionStart) {
    return formState.positionEnd > formState.positionStart
  }
  return true
}

const rules = {
  jobTitle: {
    required: helpers.withMessage('Job Title must not be blank', required)
  },
  location: {
    required: helpers.withMessage('Location must not be blank', required)
  },
  company: {
    required: helpers.withMessage('Company must not be blank', required)
  },
  positionStart: {
    required: helpers.withMessage('Position Start mut not be blank', required)
  },
  positionEnd: {
    positionEndIsAfterStart: helpers.withMessage('Position End must be after Position Start', positionEndIsAfterStart)
  },
  description: {
    required: helpers.withMessage('Description must not be blank', required)
  }
}

const form = useVuelidate(rules, formState)

const errorMessages = ref<ErrorMessages>({})
function getErrors(attributeName: string): ComputedRef {
  return getErrorMessages(errorMessages, form, attributeName)
}

const jobTitleErrors = getErrors('jobTitle')
const locationErrors = getErrors('location')
const companyErrors = getErrors('company')
const positionStartErrors = getErrors('positionStart')
const positionEndErrors = getErrors('positionEnd')
const descriptionErrors = getErrors('description')

async function save() {
  const isValid = await form.value.$validate()
  if (!isValid) {
    return
  }

  const workExperience: WorkExperienceDto = {
    id: props.isEdit ? props.value.id : null,
    jobTitle: formState.jobTitle,
    company: formState.company,
    location: formState.location,
    positionStart: convertDateToString(formState.positionStart),
    positionEnd: convertDateToString(formState.positionEnd),
    description: formState.description
  }

  try {
    const savedExperience = await profileApi.saveWorkExperience(workExperience)
    if (props.isEdit) {
      emit('saveEdit', savedExperience)
    } else {
      emit('saveNew', savedExperience)
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
.work-experience-sheet {
  padding: 30px;

  h2 {
    margin-bottom: 10px;
  }
}
</style>
