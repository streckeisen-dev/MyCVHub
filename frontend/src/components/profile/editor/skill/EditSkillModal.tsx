import {
  Autocomplete,
  AutocompleteItem,
  Form,
  Input,
  Modal,
  ModalBody,
  ModalContent,
  ModalHeader,
  ModalProps,
  Slider
} from '@heroui/react'
import { useTranslation } from 'react-i18next'
import { h3 } from '@/styles/primitives.ts'
import { FormButtons } from '@/components/FormButtons.tsx'
import { ChangeEvent, FormEvent, useState } from 'react'
import { ErrorMessages } from '@/types/ErrorMessages.ts'
import ProfileApi from '@/api/ProfileApi.ts'
import { SkillUpdateDto } from '@/types/profile/skill/SkillUpdateDto.ts'
import { SkillDto } from '@/types/profile/skill/SkillDto.ts'
import { RestError } from '@/types/RestError.ts'
import { extractFormErrors } from '@/helpers/FormHelper.ts'

const EMPTY_SKILL: SkillFormData = {
  id: undefined,
  name: '',
  type: '',
  level: 0
}

export interface SkillFormData {
  id: number | undefined
  name: string
  type: string
  level: number
}

export type EditSkillModalProps = Omit<ModalProps, 'children'> & {
  initialValue: SkillFormData | undefined
  existingTypes: string[]
  onSaved: (skill: SkillDto) => void
}

export function EditSkillModal(props: EditSkillModalProps) {
  const { t, i18n } = useTranslation()
  const { initialValue, existingTypes, onSaved, onClose, ...modalProps } = props
  const [isSaving, setIsSaving] = useState(false)
  const [data, setData] = useState<SkillFormData>(initialValue ?? EMPTY_SKILL)
  const [errorMessages, setErrorMessages] = useState<ErrorMessages>({})

  function handleChange(e: ChangeEvent<HTMLInputElement>) {
    const name = e.currentTarget.name
    const value = e.currentTarget.value
    updateData(name, value)
  }

  function updateData(name: string, value: unknown) {
    setData((prev) => ({ ...prev, [name]: value }))
    setErrorMessages((prev: ErrorMessages) => {
      return { ...prev, [name]: undefined } as ErrorMessages
    })
  }

  async function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()
    setIsSaving(true)

    const update: SkillUpdateDto = {
      ...data
    }
    try {
      const updated = await ProfileApi.saveSkill(update, i18n.language)
      onSaved(updated)
      if (onClose) {
        onClose()
      }
    } catch (e) {
      const error = (e as RestError).errorDto
      const errorMessage = initialValue ? t('skills.editor.editError') : t('skills.editor.addError')
      extractFormErrors(error, errorMessage, setErrorMessages, t)
    } finally {
      setIsSaving(false)
    }
  }

  return (
    <Modal className="w-full p-5" {...modalProps} backdrop="blur" size="5xl">
      <ModalContent>
        {(onClose) => (
          <>
            <ModalHeader>
              <h3 className={h3()}>
                {initialValue ? t('skills.editor.edit') : t('skills.editor.add')}
              </h3>
            </ModalHeader>
            <ModalBody>
              <Form className="flex flex-col gap-6" onSubmit={handleSubmit}>
                <Input
                  name="name"
                  label={t('fields.name')}
                  isRequired
                  value={data.name}
                  onChange={handleChange}
                  isInvalid={errorMessages.name != null}
                  errorMessage={errorMessages.name}
                />

                <Autocomplete
                  name="type"
                  label={t('fields.type')}
                  isRequired
                  inputValue={data.type}
                  onInputChange={(val) => updateData('type', val)}
                  allowsCustomValue
                  isInvalid={errorMessages.type != null}
                  errorMessage={errorMessages.type}
                >
                  {existingTypes.map((type) => (
                    <AutocompleteItem key={type}>{type}</AutocompleteItem>
                  ))}
                </Autocomplete>

                <div className="flex flex-col gap-2 w-full">
                  <Slider
                    name="level"
                    label={t('fields.level')}
                    minValue={0}
                    maxValue={100}
                    step={1}
                    value={data.level}
                    onChange={(val) => updateData('level', val)}
                  />
                  {errorMessages.level && (
                    <p className="text-danger text-small">{errorMessages.level}</p>
                  )}
                </div>

                <FormButtons onCancel={onClose} isSaving={isSaving} />
              </Form>
            </ModalBody>
          </>
        )}
      </ModalContent>
    </Modal>
  )
}
