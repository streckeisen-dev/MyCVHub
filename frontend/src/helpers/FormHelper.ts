import { SetStateAction } from 'react'
import { ErrorMessages } from '@/types/ErrorMessages.ts'
import { ErrorDto } from '@/types/ErrorDto.ts'
import { addErrorToast } from '@/helpers/ToastHelper.ts'
import { TFunction } from 'i18next'

export function extractFormErrors(
  error: ErrorDto | undefined,
  errorMessage: string,
  setErrorMessages: (value: SetStateAction<ErrorMessages>) => void,
  t: TFunction
): void {
  const messages = error?.errors ?? {}
  setErrorMessages(messages)
  if (Object.keys(messages).length === 0) {
    addErrorToast(errorMessage, error?.message ?? t('error.genericMessage'))
  }
}

export function extractNestedErrors(errorMessages: ErrorMessages, prefix: string): ErrorMessages {
  return Object.keys(errorMessages)
    .filter((key) => key.startsWith(prefix))
    .reduce(
      (prev, key) => ({ ...prev, [key.replace(prefix, '')]: errorMessages[key] }),
      {}
    )
}