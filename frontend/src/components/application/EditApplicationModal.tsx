import { ChangeEvent, FormEvent, ReactNode, useState } from 'react'
import { Form, Input, Modal, ModalBody, ModalContent, ModalHeader, Textarea } from '@heroui/react'
import { useTranslation } from 'react-i18next'
import { h3 } from '@/styles/primitives.ts'
import { FormButtons } from '@/components/FormButtons.tsx'
import { ErrorMessages } from '@/types/ErrorMessages.ts'
import ApplicationApi from '@/api/ApplicationApi.ts'
import { ApplicationUpdateRequestDto } from '@/types/application/ApplicationUpdateRequestDto.ts'
import { RestError } from '@/types/RestError.ts'
import { extractFormErrors } from '@/helpers/FormHelper.ts'
import { ApplicationDetailsDto } from '@/types/application/ApplicationDetailsDto.ts'

const EMPTY_APPLICATION: ApplicationFormData = {
  id: undefined,
  jobTitle: '',
  company: '',
  source: '',
  description: ''
}

export interface ApplicationFormData {
  id: number | undefined
  jobTitle: string
  company: string
  source: string
  description: string
}

export type EditApplicationModalProps = Readonly<{
  initialValue?: ApplicationDetailsDto
  onClose: () => void
  onSave: (application: ApplicationDetailsDto) => void
}>

function toFormData(application: ApplicationDetailsDto): ApplicationFormData {
  return {
    ...application,
    source: application.source ?? '',
    description: application.description ?? ''
  }
}

export function EditApplicationModal(props: EditApplicationModalProps): ReactNode {
  const { t, i18n } = useTranslation()
  const { initialValue, onClose, onSave } = props

  const [data, setData] = useState<ApplicationFormData>(() =>
    initialValue ? toFormData(initialValue) : EMPTY_APPLICATION
  )
  const [isSaving, setIsSaving] = useState<boolean>(false)
  const [errorMessages, setErrorMessages] = useState<ErrorMessages>({})

  function handleChange(e: ChangeEvent<HTMLInputElement>) {
    const name = e.currentTarget.name
    const value = e.currentTarget.value
    setData((prev) => ({ ...prev, [name]: value }))
    setErrorMessages((prev) => {
      return {
        ...prev,
        [name]: undefined
      } as ErrorMessages
    })
  }

  async function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()

    setIsSaving(true)
    const update: ApplicationUpdateRequestDto = {
      id: data.id,
      jobTitle: data.jobTitle,
      company: data.company,
      source: data.source === '' ? undefined : data.source,
      description: data.description === '' ? undefined : data.description
    }
    try {
      const updated = await ApplicationApi.save(update, i18n.language)
      onClose()
      onSave(updated)
    } catch (e) {
      const error = (e as RestError).errorDto
      extractFormErrors(error, t('application.editor.saveError'), setErrorMessages, t)
    } finally {
      setIsSaving(false)
    }
  }

  return (
    <Modal isOpen={true} size="5xl" backdrop="blur" className="w-full p-6" onClose={onClose}>
      <ModalContent>
        {(onClose) => (
          <>
            <ModalHeader className="flex flex-col gap-1">
              <h3 className={h3()}>
                {initialValue ? t('application.editor.edit') : t('application.editor.add')}
              </h3>
            </ModalHeader>
            <ModalBody>
              <Form className="flex flex-col gap-6" onSubmit={handleSubmit}>
                <Input
                  name="jobTitle"
                  label={t('fields.jobTitle')}
                  isRequired
                  value={data.jobTitle}
                  onChange={handleChange}
                  isInvalid={errorMessages.jobTitle != null}
                  errorMessage={errorMessages.jobTitle}
                />

                <Input
                  name="company"
                  label={t('fields.company')}
                  isRequired
                  value={data.company}
                  onChange={handleChange}
                  isInvalid={errorMessages.company != null}
                  errorMessage={errorMessages.company}
                />

                <Input
                  name="source"
                  label={t('fields.source')}
                  value={data.source}
                  onChange={handleChange}
                  isInvalid={errorMessages.source != null}
                  errorMessage={errorMessages.source}
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
          </>
        )}
      </ModalContent>
    </Modal>
  )
}
