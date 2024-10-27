import { computed, type ComputedRef, type Ref } from 'vue'
import type { ErrorObject, Validation } from '@vuelidate/core'

export type ErrorMessages = { [key: string]: string }

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
