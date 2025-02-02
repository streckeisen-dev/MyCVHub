<template>
  <v-dialog
    :model-value="true"
    @update:model-value="cancel"
  >
    <v-sheet class="project-sheet">
      <h2 v-if="isEdit">{{ t('project.editor.edit') }}</h2>
      <h2 v-else>{{ t('project.editor.add') }}</h2>

      <v-form @submit.prevent>
        <v-text-field
          :label="t('fields.name')"
          v-model="formState.name"
          :error-messages="nameErrors"
        />
        <v-text-field
          :label="t('fields.role')"
          v-model="formState.role"
          :error-messages="roleErrors"
        />
        <v-date-input
          :label="t('fields.projectStart')"
          v-model="formState.projectStart"
          :error-messages="projectStartErrors"
        />
        <v-date-input
          :label="t('fields.projectEnd')"
          v-model="formState.projectEnd"
          clearable
          @click:clear="() => (formState.projectEnd = undefined)"
          :error-messages="projectEndErrors"
        />
        <v-textarea
          :label="t('fields.description')"
          v-model="formState.description"
          :error-messages="descriptionErrors"
        />

        <h3>{{ t('fields.links') }}</h3>
        <project-link-editor v-model="formState.links" />

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
import { VDateInput } from 'vuetify/labs/components'
import useVuelidate from '@vuelidate/core'
import profileApi from '@/api/ProfileApi'
import { convertDateToString, convertStringToDate } from '@/services/DateHelper'
import { type ErrorMessages, getErrorMessages } from '@/services/FormHelper'
import type { ErrorDto } from '@/dto/ErrorDto'
import { useI18n } from 'vue-i18n'
import { required, withI18nMessage } from '@/validation/validators'
import { helpers } from '@vuelidate/validators'
import FormButtons from '@/components/FormButtons.vue'
import ToastService from '@/services/ToastService'
import { ProjectLink } from '@/dto/ProjectLink'
import { ProjectDto } from '@/dto/ProjectDto'
import { ProjectUpdateDto } from '@/dto/ProjectUpdateDto'
import ProjectLinkEditor from '@/views/profile/components/project/ProjectLinkEditor.vue'

const { t } = useI18n({
  useScope: 'global'
})

const props = defineProps<{
  value: ProjectDto | undefined
  isEdit: boolean
}>()

const emit = defineEmits(['saveNew', 'saveEdit', 'cancel'])

const isSaving = ref(false)

type FormState = {
  name?: string
  role?: string
  projectStart?: Date
  projectEnd?: Date
  description?: string
  links: Array<ProjectLink>
}

const formState = reactive<FormState>({
  name: props.value?.name,
  role: props.value?.role,
  projectStart: convertStringToDate(props.value?.projectStart),
  projectEnd: convertStringToDate(props.value?.projectEnd),
  description: props.value?.description,
  links: props.value?.links || []
})

const projectEndIsAfterStart = withI18nMessage(() => {
  if (formState.projectEnd && formState.projectStart) {
    return formState.projectEnd > formState.projectStart
  }
  return true
})

const rules = {
  name: {
    required
  },
  role: {
    required
  },
  projectStart: {
    required
  },
  projectEnd: {
    dateIsBeforeValidator: helpers.withParams(
      { earlierDate: t('fields.projectStart'), laterDate: t('fields.projectEnd') },
      projectEndIsAfterStart
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

const nameErrors = getErrors('name')
const roleErrors = getErrors('role')
const projectStartErrors = getErrors('projectStart')
const projectEndErrors = getErrors('projectEnd')
const descriptionErrors = getErrors('description')

async function save() {
  const isValid = await form.value.$validate()
  if (!isValid) {
    return
  }

  const project: ProjectUpdateDto = {
    id: props.isEdit ? props.value!!.id : undefined,
    name: formState.name,
    role: formState.role,
    projectStart: convertDateToString(formState.projectStart),
    projectEnd: convertDateToString(formState.projectEnd),
    description: formState.description,
    links: formState.links
  }

  isSaving.value = true
  try {
    const savedProject = await profileApi.saveProject(project)
    if (props.isEdit) {
      emit('saveEdit', savedProject)
    } else {
      emit('saveNew', savedProject)
    }
    errorMessages.value = {}
  } catch (e) {
    const error = e as ErrorDto
    errorMessages.value = error?.errors || {}
    if (Object.keys(errorMessages.value).length === 0) {
      const errorMessage = props.isEdit
        ? t('project.editor.editError')
        : t('project.editor.addError')
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
.project-sheet {
  padding: 30px;

  h2 {
    margin-bottom: 10px;
  }
}
</style>
