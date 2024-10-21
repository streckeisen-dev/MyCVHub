<template>
  <v-dialog :model-value="true" @update:model-value="cancel">
    <v-sheet class="skill-sheet">
      <h2 v-if="isEdit">{{ t('editSkill.edit') }}</h2>
      <h2 v-else>{{ t('editSkill.add') }}</h2>

      <v-form @submit.prevent>
        <v-text-field
          :label="t('fields.name')"
          v-model="formState.name"
          :error-messages="nameErrors"
        />
        <v-text-field
          :label="t('fields.type')"
          :hint="t('editSkill.typeHint')"
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

        <div class="form-action-buttons">
          <v-btn type="submit" :text="t('forms.save')" color="primary" @click="save" />
          <v-btn :text="t('forms.cancel')" @click="cancel" />
        </div>
      </v-form>
    </v-sheet>
  </v-dialog>
</template>

<script setup lang="ts">
import { type ComputedRef, reactive, ref } from 'vue'
import useVuelidate from '@vuelidate/core'
import profileApi from '@/api/ProfileApi'
import { type ErrorMessages, getErrorMessages } from '@/services/FormHelper'
import type { ErrorDto } from '@/dto/ErrorDto'
import type { SkillDto } from '@/dto/SkillDto'
import type { SkillUpdateDto } from '@/dto/SkillUpdateDto'
import { useI18n } from 'vue-i18n'
import { required, withI18nMessage } from '@/validation/validators'
import { helpers } from '@vuelidate/validators'

const { t } = useI18n()

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

  try {
    const savedSkill = await profileApi.saveSkill(skillUpdate)
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

<i18n>
{
  "de": {
    "editSkill": {
      "add": "Fähigkeit erstellen",
      "edit": "Fähigkeit bearbeiten",
      "typeHint": "Die Fähigkeiten werden in deinem Lebenslauf nach ihrem Typ gruppiert. Verwende etwas wie 'Programmiersprachen', 'Kommunikation' etc."
    }
  },
  "en": {
    "editSkill": {
      "add": "Add Skill",
      "edit": "Edit Skill",
      "typeHint": "Skills will be grouped by their type in your CV. Use something like 'Programming Languages', 'Communication', etc."
    }
  }
}
</i18n>

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
    margin: 15px 0;
  }
}
</style>
