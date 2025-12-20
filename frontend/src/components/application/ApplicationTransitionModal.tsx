import {
  Form,
  Modal,
  ModalBody,
  ModalContent,
  ModalHeader,
  ModalProps,
  Textarea
} from '@heroui/react'
import { ApplicationTransitionDto } from '@/types/application/ApplicationTransitionDto.ts'
import { FormEvent, useRef, useState } from 'react'
import { FormButtons } from '@/components/FormButtons.tsx'
import { ErrorMessages } from '@/types/ErrorMessages.ts'
import { useTranslation } from 'react-i18next'
import { ApplicationDetailsDto } from '@/types/application/ApplicationDetailsDto.ts'
import ApplicationApi from '@/api/ApplicationApi.ts'
import { RestError } from '@/types/RestError.ts'
import { extractFormErrors } from '@/helpers/FormHelper.ts'
import { ApplicationTransitionRequestDto } from '@/types/application/ApplicationTransitionRequestDto.ts'

export type ApplicationTransitionModalProps = Readonly<
  Omit<ModalProps, 'children'> & {
    application: number
    transition: ApplicationTransitionDto
    onSave: (application: ApplicationDetailsDto) => void
  }
>

export function ApplicationTransitionModal(props: ApplicationTransitionModalProps) {
  const { transition, application, onSave, onClose, ...modalProps } = props
  const { t, i18n } = useTranslation()

  const [isSaving, setIsSaving] = useState<boolean>(false)
  const [errorMessages, setErrorMessages] = useState<ErrorMessages>({})
  const commentRef = useRef<HTMLTextAreaElement>(null)

  async function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    setIsSaving(true)
    const comment = commentRef.current?.value ?? undefined
    const request: ApplicationTransitionRequestDto = {
      applicationId: application,
      comment: comment === '' ? undefined : comment
    }
    try {
      const updated = await ApplicationApi.transition(transition.id, request, i18n.language)
      onSave(updated)
      if (onClose) {
        onClose()
      }
    } catch (e) {
      const error = (e as RestError).errorDto
      extractFormErrors(error, t('application.transitionError'), setErrorMessages, t)
    } finally {
      setIsSaving(false)
    }
  }

  return (
    <Modal isOpen={true} onClose={onClose} backdrop="blur" size="xl" className="w-full p-6" {...modalProps}>
      <ModalContent>
        {(onClose) => (
          <>
            <ModalHeader>{transition.label}</ModalHeader>
            <ModalBody>
              <Form onSubmit={handleSubmit} className="flex flex-col gap-6">
                <Textarea
                  ref={commentRef}
                  label={t('fields.comment')}
                  isInvalid={errorMessages.comment != null}
                  errorMessage={errorMessages.comment}
                />

                <FormButtons onCancel={onClose} isSaving={isSaving} />
              </Form>
            </ModalBody>
          </>
        )}
      </ModalContent>
    </Modal>
  )
}
