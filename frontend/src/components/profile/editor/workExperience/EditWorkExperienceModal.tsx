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
import { useTranslation } from 'react-i18next'
import { h3 } from '@/styles/primitives.ts'
import { ChangeEvent, FormEvent, ReactNode, useState } from 'react'
import { WorkExperienceDto } from '@/types/profile/workExperience/WorkExperienceDto.ts'
import { WorkExperienceUpdateDto } from '@/types/profile/workExperience/WorkExperienceUpdateDto.ts'
import { toDateString } from '@/helpers/DateHelper.ts'
import { FormButtons } from '@/components/FormButtons.tsx'
import { CalendarDate, getLocalTimeZone, today } from '@internationalized/date'
import { DateInput } from '@/components/input/DateInput.tsx'
import { ErrorMessages } from '@/types/ErrorMessages.ts'
import ProfileApi from '@/api/ProfileApi.ts'
import { RestError } from '@/types/RestError.ts'
import { extractFormErrors } from '@/helpers/FormHelper.ts'

const EMPTY_WORK_EXPERIENCE: WorkExperienceFormData = {
  id: undefined,
  company: '',
  jobTitle: '',
  description: '',
  positionStart: null,
  positionEnd: null,
  location: ''
}

export interface WorkExperienceFormData {
  id: number | undefined
  company: string
  jobTitle: string
  description: string
  positionStart: CalendarDate | null
  positionEnd: CalendarDate | null
  location: string
}

export type EditWorkExperienceModalProps = Readonly<
  Omit<ModalProps, 'children'> & {
    onSaved: (update: WorkExperienceDto) => void
    initialValue: WorkExperienceFormData | undefined
  }
>

export function EditWorkExperienceModal(props: EditWorkExperienceModalProps): ReactNode {
  const { t, i18n } = useTranslation()
  const { initialValue, onSaved, onClose, ...modalProps } = props
  const [data, setData] = useState<WorkExperienceFormData>(initialValue ?? EMPTY_WORK_EXPERIENCE)
  const [isSaving, setIsSaving] = useState(false)
  const [errorMessages, setErrorMessages] = useState<ErrorMessages>({})

  function handleChange(e: ChangeEvent<HTMLInputElement>) {
    const name = e.currentTarget.name
    const value = e.currentTarget.value
    updateData(name, value)
  }

  function updateData(name: string, value: unknown) {
    setData((prev) => {
      return {
        ...prev,
        [name]: value
      }
    })
    setErrorMessages((prev: ErrorMessages) => {
      return {
        ...prev,
        [name]: undefined
      } as ErrorMessages
    })
  }

  async function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()

    const update: WorkExperienceUpdateDto = {
      ...data,
      positionStart: toDateString(data.positionStart),
      positionEnd: toDateString(data.positionEnd)
    }

    setIsSaving(true)
    try {
      const updated = await ProfileApi.saveWorkExperience(update, i18n.language)
      if (onClose) {
        onClose()
      }
      onSaved(updated)
    } catch (e) {
      const error = (e as RestError).errorDto
      const errorMessage = initialValue
        ? t('workExperience.editor.editError')
        : t('workExperience.editor.addError')
      extractFormErrors(error, errorMessage, setErrorMessages, t)
    } finally {
      setIsSaving(false)
    }
  }

  return (
    <Modal className="w-full p-5" onClose={onClose} {...modalProps} backdrop="blur" size="5xl">
      <ModalContent>
        {(onClose) => (
          <>
            <ModalHeader>
              <h3 className={h3()}>
                {initialValue ? t('workExperience.editor.edit') : t('workExperience.editor.add')}
              </h3>
            </ModalHeader>
            <ModalBody>
              <Form onSubmit={handleSubmit} className="flex flex-col gap-6">
                <Input
                  name="jobTitle"
                  label={t('fields.jobTitle')}
                  value={data.jobTitle}
                  onChange={handleChange}
                  isRequired
                  isInvalid={errorMessages.jobTitle != null}
                  errorMessage={errorMessages.jobTitle}
                />

                <Input
                  name="location"
                  label={t('fields.location')}
                  value={data.location}
                  onChange={handleChange}
                  isRequired
                  isInvalid={errorMessages.location != null}
                  errorMessage={errorMessages.location}
                />

                <Input
                  name="company"
                  label={t('fields.company')}
                  value={data.company}
                  onChange={handleChange}
                  isRequired
                  isInvalid={errorMessages.company != null}
                  errorMessage={errorMessages.company}
                />

                <DateInput
                  name="positionStart"
                  label={t('fields.positionStart')}
                  value={data.positionStart}
                  onChange={(val) => updateData('positionStart', val)}
                  maxValue={today(getLocalTimeZone())}
                  isRequired
                  isInvalid={errorMessages.positionStart != null}
                  errorMessage={errorMessages.positionStart}
                />

                <DateInput
                  className="col-span-11"
                  name="positionEnd"
                  label={t('fields.positionEnd')}
                  value={data.positionEnd}
                  onChange={(val) => updateData('positionEnd', val)}
                  maxValue={today(getLocalTimeZone())}
                  isClearable
                  onClear={() => updateData('positionEnd', null)}
                  isInvalid={errorMessages.positionEnd != null}
                  errorMessage={errorMessages.positionEnd}
                />

                <Textarea
                  name="description"
                  label={t('fields.description')}
                  value={data.description}
                  onChange={handleChange}
                  isInvalid={errorMessages.description != null}
                  errorMessage={errorMessages.description}
                  isRequired
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
