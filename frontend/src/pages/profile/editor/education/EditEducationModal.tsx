import { ChangeEvent, FormEvent, ReactNode, useState } from 'react'
import {
  addToast,
  Form,
  Input,
  Modal,
  ModalBody,
  ModalContent,
  ModalHeader,
  ModalProps, Textarea
} from '@heroui/react'
import { EducationDto } from '@/types/EducationDto.ts'
import { useTranslation } from 'react-i18next'
import { FormButtons } from '@/components/FormButtons.tsx'
import ProfileApi from '@/api/ProfileApi.ts'
import { EducationUpdateDto } from '@/types/EducationUpdateDto.ts'
import { toDateString } from '@/helpers/DateHelper.ts'
import { ErrorMessages } from '@/types/ErrorMessages.ts'
import { RestError } from '@/types/RestError.ts'
import { DateInput } from '@/components/DateInput.tsx'
import { h3 } from '@/styles/primitives.ts'
import { getLocalTimeZone, today, CalendarDate } from '@internationalized/date'

const EMPTY_EDUCATION: EducationFormData = {
  id: undefined,
  institution: '',
  degreeName: '',
  description: '',
  location: '',
  educationStart: null,
  educationEnd: null
}

export interface EducationFormData {
  id: number | undefined
  institution: string
  location: string
  educationStart: CalendarDate | null
  educationEnd: CalendarDate | null
  degreeName: string
  description: string
}

export type EditEducationModalProps = Omit<ModalProps, 'children'> & {
  initialValue: EducationFormData | undefined,
  onSaved: (education: EducationDto) => void
}

export function EditEducationModal(props: EditEducationModalProps): ReactNode {
  const { t, i18n } = useTranslation()
  const { initialValue, onSaved, onClose, ...modalProps} = props
  const [data, setData] = useState<EducationFormData>(initialValue ?? EMPTY_EDUCATION)
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
    setIsSaving(true)

    const update: EducationUpdateDto = {
      ...data,
      educationStart: toDateString(data.educationStart),
      educationEnd: toDateString(data.educationEnd),
      description: data.description && data.description !== '' ? data.description : undefined
    }

    try {
      const updated = await ProfileApi.saveEducation(update, i18n.language)
      if (onClose) {
        onClose()
      }
      onSaved(updated)
    } catch (e) {
      const error = (e as RestError).errorDto
      const messages = error?.errors ?? {}
      setErrorMessages(messages)
      if (Object.keys(messages).length === 0) {
        const errorMessage = initialValue
          ? t('education.editor.editError')
          : t('education.editor.addError')
        addToast({
          title: errorMessage,
          description: error?.message ?? t('error.genericMessage'),
          color: 'danger'
        })
      }
    } finally {
      setIsSaving(false)
    } 
  }
  
  return <Modal className="w-full p-5" onClose={onClose} {...modalProps} >
    <ModalContent>
      {(onClose) => <>
        <ModalHeader>
          <h3 className={h3()}>{initialValue ? t('education.editor.edit') : t('education.editor.add')}</h3>
        </ModalHeader>
        <ModalBody>
          <Form onSubmit={handleSubmit} className="flex flex-col gap-6">
            <Input
              name="institution"
              label={t('fields.institution')}
              value={data.institution}
              onChange={handleChange}
              isInvalid={errorMessages.institution != null}
              errorMessage={errorMessages.institution}
              isRequired
            />

            <Input
              name="location"
              label={t('fields.location')}
              value={data.location}
              onChange={handleChange}
              isInvalid={errorMessages.location != null}
              errorMessage={errorMessages.location}
              isRequired
            />

            <Input
              name="degreeName"
              label={t('fields.degreeName')}
              value={data.degreeName}
              onChange={handleChange}
              isInvalid={errorMessages.degreeName != null}
              errorMessage={errorMessages.degreeName}
              isRequired
            />

            <DateInput
              name="educationStart"
              label={t('fields.educationStart')}
              value={data.educationStart}
              onChange={(val) => updateData('educationStart', val)}
              isInvalid={errorMessages.educationStart != null}
              errorMessage={errorMessages.educationStart}
              isRequired
              maxValue={today(getLocalTimeZone())}
            />

            <DateInput
              name="educationEnd"
              label={t('fields.educationEnd')}
              value={data.educationEnd}
              onChange={(val) => updateData('educationEnd', val)}
              isInvalid={errorMessages.educationEnd != null}
              errorMessage={errorMessages.educationEnd}
              isClearable
              onClear={() => updateData('educationEnd', null)}
              maxValue={today(getLocalTimeZone())}
            />

            <Textarea
              name="description"
              label={t('fields.description')}
              value={data.description}
              onChange={handleChange}
              isInvalid={errorMessages.description != null}
              errorMessage={errorMessages.description}
            />

            <FormButtons onCancel={onClose} isSaving={isSaving} />
          </Form>
        </ModalBody>
      </>}
    </ModalContent>   
  </Modal>
}