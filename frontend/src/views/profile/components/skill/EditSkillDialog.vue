<template>
  <v-dialog
    :model-value="true"
    @update:model-value="cancel"
  >
    <v-sheet class="skill-sheet">
      <h2 v-if="isEdit">{{ t('skills.editor.edit') }}</h2>
      <h2 v-else>{{ t('skills.editor.add') }}</h2>

      <v-form @submit.prevent>
        <v-text-field
          :label="t('fields.name')"
          v-model="formState.name"
          :error-messages="nameErrors"
        />
        <v-text-field
          :label="t('fields.type')"
          :hint="t('skills.editor.typeHint')"
          v-model="formState.type"
          :error-messages="typeErrors"
        />
        <v-slider
          :label="t('fields.level')"
          v-model="formState.level"
          thumb-label="always"
          step="1"
          :error-messages="levelErrors"
          class="level-slider"
          color="primary"
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
import useVuelidate from '@vuelidate/core'
import profileApi from '@/api/ProfileApi'
import { type ErrorMessages, getErrorMessages } from '@/services/FormHelper'
import type { SkillDto } from '@/dto/SkillDto'
import type { SkillUpdateDto } from '@/dto/SkillUpdateDto'
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
  value: SkillDto | undefined
  isEdit: boolean
}>()

const emit = defineEmits(['saveNew', 'saveEdit', 'cancel'])

const isSaving = ref(false)

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

const levelWithinRange = withI18nMessage(
  () => formState.level == null || (formState.level >= 0 && formState.level <= 100)
)

const rules = {
  name: {
    required
  },
  type: {
    required
  },
  level: {
    required,
    numberWithinRange: helpers.withParams(
      { name: t('fields.level'), min: '0', max: '100' },
      levelWithinRange
    )
  }
}

const form = useVuelidate<FormState>(rules, formState)

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
    id: props.isEdit ? props.value!!.id : undefined,
    name: formState.name,
    type: formState.type,
    level: formState.level
  }

  isSaving.value = true
  try {
    const savedSkill = await profileApi.saveSkill(skillUpdate)
    if (props.isEdit) {
      emit('saveEdit', savedSkill)
    } else {
      emit('saveNew', savedSkill)
    }
    errorMessages.value = {}
  } catch (e) {
    const error = (e as RestError).errorDto
    errorMessages.value = error?.errors || {}
    if (Object.keys(errorMessages.value).length === 0) {
      const errorMessage = props.isEdit ? t('skills.editor.editError') : t('skills.editor.addError')
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
.skill-sheet {
  padding: 30px;

  h2 {
    margin-bottom: 10px;
  }

  .level-slider {
    margin: 15px 0;
  }
}
</style>
