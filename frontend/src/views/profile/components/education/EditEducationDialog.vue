<template>
  <v-dialog
    :model-value="true"
    @update:model-value="cancel"
  >
    <v-sheet class="education-sheet">
      <h2 v-if="isEdit">{{ t('education.editor.edit') }}</h2>
      <h2 v-else>{{ t('education.editor.add') }}</h2>

      <v-form @submit.prevent>
        <v-text-field
          :label="t('fields.institution')"
          v-model="formState.institution"
          :error-messages="institutionErrors"
        />
        <v-text-field
          :label="t('fields.location')"
          v-model="formState.location"
          :error-messages="locationErrors"
        />
        <v-text-field
          :label="t('fields.degreeName')"
          v-model="formState.degreeName"
          :error-messages="degreeNameErrors"
        />
        <v-date-input
          :label="t('fields.educationStart')"
          v-model="formState.educationStart"
          :error-messages="educationStartErrors"
        />
        <v-date-input
          :label="t('fields.educationEnd')"
          v-model="formState.educationEnd"
          clearable
          @click:clear="() => (formState.educationEnd = undefined)"
          :error-messages="educationEndErrors"
        />
        <v-textarea
          :label="t('fields.description')"
          v-model="formState.description"
          :error-messages="descriptionErrors"
        />

        <form-buttons
          @save="save"
          @cancel="cancel"
        />
      </v-form>
    </v-sheet>
  </v-dialog>
</template>

<script setup lang="ts">
import { type ComputedRef, reactive, ref } from 'vue'
import { VDateInput } from 'vuetify/labs/components'
import useVuelidate from '@vuelidate/core'
import profileApi from '@/api/ProfileApi'
import { convertDateToString, convertStringToDate } from '@/services/DateHelper'
import { type ErrorMessages, getErrorMessages } from '@/services/FormHelper'
import type { ErrorDto } from '@/dto/ErrorDto'
import type { EducationDto } from '@/dto/EducationDto'
import type { EducationUpdateDto } from '@/dto/EducationUpdateDto'
import { useI18n } from 'vue-i18n'
import { required, withI18nMessage } from '@/validation/validators'
import { helpers } from '@vuelidate/validators'
import FormButtons from '@/components/FormButtons.vue'

const { t } = useI18n({
  useScope: 'global'
})

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

const educationEndIsAfterStart = withI18nMessage(() => {
  if (formState.educationEnd && formState.educationStart) {
    return formState.educationEnd > formState.educationStart
  }
  return true
})

const rules = {
  institution: {
    required
  },
  location: {
    required
  },
  degreeName: {
    required
  },
  educationStart: {
    required
  },
  educationEnd: {
    dateIsBeforeValidator: helpers.withParams(
      {
        earlierDate: t('fields.educationStart'),
        laterDate: t('fields.educationEnd')
      },
      educationEndIsAfterStart
    )
  },
  description: {}
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
}
</style>
