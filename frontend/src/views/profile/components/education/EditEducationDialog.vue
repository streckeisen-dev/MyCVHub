<template>
  <v-dialog :model-value="true" @update:model-value="cancel">
    <v-sheet class="education-sheet">
      <h2 v-if="isEdit">Edit Education Entry</h2>
      <h2 v-else>Add Education Entry</h2>

      <v-form @submit.prevent>
        <v-text-field
          label="Institution"
          v-model="formState.institution"
          :error-messages="institutionErrors"
        />
        <v-text-field
          label="Location"
          v-model="formState.location"
          :error-messages="locationErrors"
        />
        <v-text-field
          label="Degree Name"
          v-model="formState.degreeName"
          :error-messages="degreeNameErrors"
        />
        <v-date-input
          label="Education Start"
          v-model="formState.educationStart"
          :error-messages="educationStartErrors"
        />
        <v-date-input
          label="Education End"
          v-model="formState.educationEnd"
          clearable
          :error-messages="educationEndErrors"
        />
        <v-textarea
          label="Description"
          v-model="formState.description"
          :error-messages="descriptionErrors"
        />

        <div class="form-action-buttons">
          <v-btn type="submit" text="Save" color="primary" @click="save" />
          <v-btn text="Cancel" @click="cancel" />
        </div>
      </v-form>
    </v-sheet>
  </v-dialog>
</template>

<script setup lang="ts">
import { type ComputedRef, reactive, ref } from 'vue'
import { VDateInput } from 'vuetify/labs/components'
import { helpers, required } from '@vuelidate/validators'
import useVuelidate from '@vuelidate/core'
import profileApi from '@/api/ProfileApi'
import { convertDateToString, convertStringToDate } from '@/services/DateHelper'
import { type ErrorMessages, getErrorMessages } from '@/services/FormHelper'
import type { ErrorDto } from '@/dto/ErrorDto'
import type { EducationDto } from '@/dto/EducationDto'
import type { EducationUpdateDto } from '@/dto/EducationUpdateDto'

const props = defineProps<{
  value: EducationDto | undefined
  isEdit: boolean
}>()

const emit = defineEmits(['saveNew', 'saveEdit', 'cancel'])

type FormState = {
  institution?: string
  location?: string
  degreeName?: string
  educationStart?: Date
  educationEnd?: Date
  description?: string
}

const formState = reactive<FormState>({
  institution: props.value?.institution,
  location: props.value?.location,
  degreeName: props.value?.degreeName,
  educationStart: convertStringToDate(props.value?.educationStart),
  educationEnd: convertStringToDate(props.value?.educationEnd),
  description: props.value?.description
})

const educationEndIsAfterStart = () => {
  if (formState.educationEnd && formState.educationStart) {
    return formState.educationEnd > formState.educationStart
  }
  return true
}

const rules = {
  institution: {
    required: helpers.withMessage('Institution must not be blank', required)
  },
  location: {
    required: helpers.withMessage('Location must not be blank', required)
  },
  degreeName: {
    required: helpers.withMessage('Degree Name must not be blank', required)
  },
  educationStart: {
    required: helpers.withMessage('Education Start mut not be blank', required)
  },
  educationEnd: {
    educationEndIsAfterStart: helpers.withMessage(
      'Education End must be after Education Start',
      educationEndIsAfterStart
    )
  },
  description: {
    required: helpers.withMessage('Description must not be blank', required)
  }
}

const form = useVuelidate<FormState>(rules, formState)

const errorMessages = ref<ErrorMessages>({})

function getErrors(attributeName: string): ComputedRef {
  return getErrorMessages(errorMessages, form, attributeName)
}

const institutionErrors = getErrors('institution')
const locationErrors = getErrors('location')
const degreeNameErrors = getErrors('degreeName')
const educationStartErrors = getErrors('educationStart')
const educationEndErrors = getErrors('educationEnd')
const descriptionErrors = getErrors('description')

async function save() {
  const isValid = await form.value.$validate()
  if (!isValid) {
    return
  }

  const educationUpdate: EducationUpdateDto = {
    id: props.isEdit ? props.value!!.id : undefined,
    institution: formState.institution,
    degreeName: formState.degreeName,
    location: formState.location,
    educationStart: convertDateToString(formState.educationStart),
    educationEnd: convertDateToString(formState.educationEnd),
    description: formState.description
  }

  try {
    const savedEducation = await profileApi.saveEducation(educationUpdate)
    if (props.isEdit) {
      emit('saveEdit', savedEducation)
    } else {
      emit('saveNew', savedEducation)
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
.education-sheet {
  padding: 30px;

  h2 {
    margin-bottom: 10px;
  }

  .form-action-buttons {
    margin-top: 10px;
  }
}
</style>
