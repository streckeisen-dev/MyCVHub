<template>
  <v-dialog
    :model-value="true"
    @update:model-value="cancel"
  >
    <v-sheet class="work-experience-sheet">
      <h2 v-if="isEdit">{{ t('workExperience.editor.edit') }}</h2>
      <h2 v-else>{{ t('workExperience.editor.add') }}</h2>

      <v-form @submit.prevent>
        <v-text-field
          :label="t('fields.jobTitle')"
          v-model="formState.jobTitle"
          :error-messages="jobTitleErrors"
        />
        <v-text-field
          :label="t('fields.location')"
          v-model="formState.location"
          :error-messages="locationErrors"
        />
        <v-text-field
          :label="t('fields.company')"
          v-model="formState.company"
          :error-messages="companyErrors"
        />
        <v-date-input
          :label="t('fields.positionStart')"
          v-model="formState.positionStart"
          :error-messages="positionStartErrors"
        />
        <v-date-input
          :label="t('fields.positionEnd')"
          v-model="formState.positionEnd"
          clearable
          @click:clear="() => (formState.positionEnd = undefined)"
          :error-messages="positionEndErrors"
        />
        <v-textarea
          :label="t('fields.description')"
          v-model="formState.description"
          :error-messages="descriptionErrors"
        />

        <form-buttons
          @save="save"
          @cancel="cancel"
          :is-saving="isSaving"
        />
      </v-form>
    </v-sheet>
  </v-dialog>
</template>

<script setup lang="ts">
import { type ComputedRef, reactive, ref } from 'vue'
import type { WorkExperienceDto } from '@/dto/WorkExperienceDto'
import { VDateInput } from 'vuetify/labs/components'
import useVuelidate from '@vuelidate/core'
import profileApi from '@/api/ProfileApi'
import { convertDateToString, convertStringToDate } from '@/services/DateHelper'
import { type ErrorMessages, getErrorMessages } from '@/services/FormHelper'
import type { WorkExperienceUpdateDto } from '@/dto/WorkExperienceUpdateDto'
import { useI18n } from 'vue-i18n'
import { required, withI18nMessage } from '@/validation/validators'
import { helpers } from '@vuelidate/validators'
import FormButtons from '@/components/FormButtons.vue'
import ToastService from '@/services/ToastService'
import { RestError } from '@/api/RestError'

const { t } = useI18n({
  useScope: 'global'
})

const props = defineProps<{
  value: WorkExperienceDto | undefined
  isEdit: boolean
}>()

const emit = defineEmits(['saveNew', 'saveEdit', 'cancel'])

const isSaving = ref(false)

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

const positionEndIsAfterStart = withI18nMessage(() => {
  if (formState.positionEnd && formState.positionStart) {
    return formState.positionEnd > formState.positionStart
  }
  return true
})

const rules = {
  jobTitle: {
    required
  },
  location: {
    required
  },
  company: {
    required
  },
  positionStart: {
    required
  },
  positionEnd: {
    dateIsBeforeValidator: helpers.withParams(
      { earlierDate: t('fields.positionStart'), laterDate: t('fields.positionEnd') },
      positionEndIsAfterStart
    )
  },
  description: {
    required
  }
}

const form = useVuelidate<FormState>(rules, formState)

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

  const workExperience: WorkExperienceUpdateDto = {
    id: props.isEdit ? props.value!!.id : undefined,
    jobTitle: formState.jobTitle,
    company: formState.company,
    location: formState.location,
    positionStart: convertDateToString(formState.positionStart),
    positionEnd: convertDateToString(formState.positionEnd),
    description: formState.description
  }

  isSaving.value = true
  try {
    const savedExperience = await profileApi.saveWorkExperience(workExperience)
    if (props.isEdit) {
      emit('saveEdit', savedExperience)
    } else {
      emit('saveNew', savedExperience)
    }
    errorMessages.value = {}
  } catch (e) {
    const error = (e as RestError).errorDto
    errorMessages.value = error?.errors || {}
    if (Object.keys(errorMessages.value).length === 0) {
      const errorMessage = props.isEdit
        ? t('workExperience.editor.editError')
        : t('workExperience.editor.addError')
      const errorDetails = error?.message || t('error.genericMessage')
      ToastService.error(errorMessage, errorDetails)
    }
  } finally {
    isSaving.value = false
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
