import { describe, it, expect } from 'vitest'
import { getErrorMessages } from '../../src/services/FormHelper'
import { ComputedRef, Ref, ref } from 'vue'
import { BaseValidation, Validation } from '@vuelidate/core'

describe('getErrorMessages', () => {
  it('should return backend error over form error', () => {
    const backendErrors = ref({
      testField: 'Backend error'
    })
    const mockedForm = mockForm()
    mockedForm.value['testField'].$errors = [{ $message: 'Form error' }]

    const result = getErrorMessages(backendErrors, mockedForm, 'testField')

    compareErrors('Backend error', result)
  })

  it('should return form error when there is no backend error', () => {
    const backendErrors = ref({})
    const mockedForm = mockForm()
    mockedForm.value['testField'].$errors = [{ $message: 'Form error' }]

    const result = getErrorMessages(backendErrors, mockedForm, 'testField')

    compareErrors('Form error', result)
  })

  it('should return empty array when there are no errors', () => {
    const backendErrors = ref({})
    const mockedForm = mockForm()

    const result = getErrorMessages(backendErrors, mockedForm, 'testField')

    expect(result).not.null
    expect(result.value).toEqual([])
  })
})

function mockForm(): Ref<Validation> {
  const form = {
    $anyDirty: false,
    $commit(): void {},
    $dirty: false,
    $error: false,
    $errors: [],
    $externalResults: [],
    $invalid: false,
    $model: undefined,
    $path: '',
    $pending: false,
    $reset(): void {},
    $silentErrors: [],
    $touch(): void {},
    $validate(): Promise<boolean> {
      return Promise.resolve(false)
    },
    $getResultsForChild(key: string): BaseValidation | undefined {
      return undefined
    },
    $clearExternalResults(): void {},
    testField: {
      $errors: []
    }
  }
  return ref<Validation>(form)
}

function compareErrors(expected: string, actual: ComputedRef<Array<string>>) {
  expect(actual).not.null
  expect(actual.value).toEqual([expected])
}
