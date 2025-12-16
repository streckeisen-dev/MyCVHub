import { computed, type ComputedRef, type Ref } from 'vue'
import type { ErrorObject, Validation } from '@vuelidate/core'
import { KeyValueObject } from '@/model/KeyValueObject.ts'

export type ErrorMessages = KeyValueObject<string>

export function getErrorMessages(
  errorMessages: Ref<ErrorMessages>,
  form: Ref<Validation>,
  attributeName: string
): ComputedRef {
  return computed(() => {
    const backendError = errorMessages.value[attributeName]
    if (backendError) {
      return [backendError]
    }
    return form.value[attributeName].$errors.map((e: ErrorObject) => e.$message)
  })
}

export function getIndexedErrorMessages(
  errorMessages: Ref<ErrorMessages>,
  attributeName: string,
  index: number
): ComputedRef<ErrorMessages> {
  return computed(() => {
    const errors: ErrorMessages = {}
    const indexedErrorKey = `${attributeName}[${index}]`
    for (const errorMessagesKey in errorMessages.value) {
      if (errorMessagesKey.split('.')[0] === indexedErrorKey) {
        errors[errorMessagesKey.replace(`${indexedErrorKey}.`, '')] = errorMessages.value[errorMessagesKey]
      }
    }
    return errors
  })
}