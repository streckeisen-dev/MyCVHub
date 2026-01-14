import {
  Form,
  Input,
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
import { ApplicationTransitionRequestDto } from '@/types/application/ApplicationTransitionRequestDto.ts'
import { CheckboxInput } from '@/components/input/CheckboxInput.tsx'
import { DateInput } from '@/components/input/DateInput.tsx'
import { CalendarDate } from '@internationalized/date'
import { toDateString } from '@/helpers/DateHelper.ts'
import { extractFormErrors } from '@/helpers/FormHelper.ts'
import { RestError } from '@/types/RestError.ts'
import ApplicationApi from '@/api/ApplicationApi.ts'

interface ScheduledWorkExperienceFormData {
  location: string
  positionStart: CalendarDate | null
  description: string
}

export type ApplicationTransitionModalProps = Readonly<
  Omit<ModalProps, 'children'> & {
    application: ApplicationDetailsDto
    transition: ApplicationTransitionDto
    onSave: (application: ApplicationDetailsDto) => void
  }
>

export function ApplicationTransitionModal(props: ApplicationTransitionModalProps) {
  const { transition, application, onSave, onClose, ...modalProps } = props
  const { t, i18n } = useTranslation()

  const [isSaving, setIsSaving] = useState<boolean>(false)
  const [errorMessages, setErrorMessages] = useState<ErrorMessages>({})
  const [hiredAutomationEnabled, setHiredAutomationEnabled] = useState<boolean>(false)
  const [scheduledWorkExperience, setScheduledWorkExperience] =
    useState<ScheduledWorkExperienceFormData>({
      location: '',
      positionStart: null,
      description: ''
    })
  const commentRef = useRef<HTMLTextAreaElement>(null)

  async function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    setIsSaving(true)
    const comment = commentRef.current?.value ?? undefined
    const request: ApplicationTransitionRequestDto = {
      applicationId: application.id,
      comment: comment === '' ? undefined : comment
    }

    if (transition.isHired && hiredAutomationEnabled) {
      request.scheduledWorkExperience = {
        jobTitle: application.jobTitle,
        company: application.company,
        location: scheduledWorkExperience.location,
        positionStart: toDateString(scheduledWorkExperience.positionStart),
        description: scheduledWorkExperience.description
      }
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

  function handleScheduledWorkExperienceChange(name: string, value: unknown) {
    setScheduledWorkExperience((prev) => ({ ...prev, [name]: value }))
    setErrorMessages((prev) => {
      return {
        ...prev,
        [name]: undefined
      } as ErrorMessages
    })
  }

  return (
    <Modal
      isOpen={true}
      onClose={onClose}
      backdrop="blur"
      size="xl"
      className="w-full p-6"
      {...modalProps}
    >
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

                {transition.isHired && (
                  <>
                    <CheckboxInput
                      label={t('application.hiredAutomation')}
                      isSelected={hiredAutomationEnabled}
                      onValueChange={(val) => setHiredAutomationEnabled(val)}
                    />

                    {hiredAutomationEnabled && (
                      <>
                        <Input
                          label={t('fields.location')}
                          isRequired
                          value={scheduledWorkExperience.location}
                          onValueChange={(val) =>
                            handleScheduledWorkExperienceChange('location', val)
                          }
                          isInvalid={errorMessages.location != null}
                          errorMessage={errorMessages.location}
                        />

                        <DateInput
                          label={t('fields.positionStart')}
                          isRequired
                          value={scheduledWorkExperience.positionStart}
                          onChange={(val) =>
                            handleScheduledWorkExperienceChange('positionStart', val)
                          }
                          isInvalid={errorMessages.positionStart != null}
                          errorMessage={errorMessages.positionStart}
                        />

                        <Textarea
                          label={t('fields.description')}
                          isRequired
                          value={scheduledWorkExperience.description}
                          onValueChange={(val) =>
                            handleScheduledWorkExperienceChange('description', val)
                          }
                          isInvalid={errorMessages.description != null}
                          errorMessage={errorMessages.description}
                        />
                      </>
                    )}
                  </>
                )}

                <FormButtons onCancel={onClose} isSaving={isSaving} />
              </Form>
            </ModalBody>
          </>
        )}
      </ModalContent>
    </Modal>
  )
}
