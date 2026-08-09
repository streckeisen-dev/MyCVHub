import { Modal, ModalBody, ModalContent, ModalHeader, ModalProps } from '@/components/ui/Modal.tsx'
import { Autocomplete, AutocompleteItem, Input, Textarea } from '@/components/ui/Fields.tsx'
import { Button } from '@/components/ui/Button.tsx'
import { Form } from '@heroui/react'
import { ProjectDto } from '@/types/profile/project/ProjectDto.ts'
import { useTranslation } from 'react-i18next'
import { h3, h4 } from '@/styles/primitives.ts'
import { FormButtons } from '@/components/btn/FormButtons.tsx'
import { ChangeEvent, FormEvent, useState } from 'react'
import { ErrorMessages } from '@/types/ErrorMessages.ts'
import { DateInput } from '@/components/input/DateInput.tsx'
import { CalendarDate, getLocalTimeZone, today } from '@internationalized/date'
import ProfileApi from '@/api/ProfileApi.ts'
import { ProjectUpdateDto } from '@/types/profile/project/ProjectUpdateDto.ts'
import { toDateString } from '@/helpers/DateHelper.ts'
import { ProjectLinkUpdateDto } from '@/types/profile/project/ProjectLinkUpdateDto.ts'
import { ProjectLinkType } from '@/types/profile/project/ProjectLink.ts'
import { FaMinus, FaPlus } from 'react-icons/fa6'
import { v7 as uuid } from 'uuid'
import { RestError } from '@/types/RestError.ts'
import { extractFormErrors } from '@/helpers/FormHelper.ts'

const EMPTY_PROJECT: ProjectFormData = {
  id: undefined,
  name: '',
  role: '',
  projectStart: null,
  projectEnd: null,
  links: [],
  description: ''
}

export interface ProjectFormData {
  id: number | undefined
  name: string
  role: string
  description: string
  projectStart: CalendarDate | null
  projectEnd: CalendarDate | null
  links: ProjectLinkData[]
}

interface ProjectLinkData {
  uiId: string
  url: string
  displayName: string
  type: string
}

export type EditProjectModalProps = Omit<ModalProps, 'children'> & {
  initialValue: ProjectFormData | undefined
  onSaved: (project: ProjectDto) => void
}

export function EditProjectModal(props: EditProjectModalProps) {
  const { t, i18n } = useTranslation()
  const { initialValue, onSaved, onClose, ...modalProps } = props
  const [data, setData] = useState<ProjectFormData>(initialValue ?? EMPTY_PROJECT)
  const [isSaving, setIsSaving] = useState<boolean>(false)
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

    const update: ProjectUpdateDto = {
      ...data,
      projectStart: toDateString(data.projectStart),
      projectEnd: toDateString(data.projectEnd),
      links: data.links.map((link) => {
        const dto: ProjectLinkUpdateDto = {
          url: link.url,
          displayName: link.displayName,
          type: ProjectLinkType[link.type as keyof typeof ProjectLinkType]
        }
        return dto
      })
    }

    try {
      const updated = await ProfileApi.saveProject(update, i18n.language)
      if (onClose) {
        onClose()
      }
      onSaved(updated)
    } catch (e) {
      const error = (e as RestError).errorDto
      const errorMessage = initialValue
        ? t('project.editor.editError')
        : t('project.editor.addError')
      extractFormErrors(error, errorMessage, setErrorMessages, t)
    } finally {
      setIsSaving(false)
    }
  }

  function handleLinkChange(uiId: string, e: ChangeEvent<HTMLInputElement>) {
    const fieldName = e.currentTarget.name
    const value = e.currentTarget.value
    updateLinkValue(uiId, fieldName, value)
  }

  function handleLinkTypeChange(uiId: string, type: string) {
    updateLinkValue(uiId, 'type', type)
  }

  function updateLinkValue(uiId: string, name: string, value: unknown) {
    setData((prev) => {
      const index = prev.links.findIndex((link) => link.uiId === uiId)
      const toBeUpdated = prev.links[index]
      const updatedLinks = [...prev.links]
      updatedLinks[index] = {
        ...toBeUpdated,
        [name]: value
      }

      return {
        ...prev,
        links: updatedLinks
      }
    })

    const index = data.links.findIndex((link) => link.uiId === uiId)
    setErrorMessages((prev: ErrorMessages) => {
      return {
        ...prev,
        [`links[${index}].${name}`]: undefined
      } as ErrorMessages
    })
  }

  function addLink() {
    const empty: ProjectLinkData = {
      uiId: uuid(),
      displayName: '',
      url: '',
      type: ''
    }
    setData((prev) => {
      const updatedLinks = [...prev.links, empty]
      return {
        ...prev,
        links: updatedLinks
      }
    })
  }

  function removeLink(uiId: string) {
    setData((prev) => {
      const index = prev.links.findIndex((link) => link.uiId === uiId)
      const updatedLinks = prev.links.filter((_, i) => i !== index)

      return {
        ...prev,
        links: updatedLinks
      }
    })
  }

  return (
    <Modal className="w-full p-5 overflow-auto max-h-9/10" onClose={onClose} {...modalProps}>
      <ModalContent>
        {(onClose) => (
          <>
            <ModalHeader>
              <h3 className={h3()}>
                {initialValue ? t('project.editor.edit') : t('project.editor.add')}
              </h3>
            </ModalHeader>
            <ModalBody>
              <Form className="flex flex-col gap-6" onSubmit={handleSubmit}>
                <Input
                  name="name"
                  label={t('fields.name')}
                  value={data.name}
                  onChange={handleChange}
                  isInvalid={errorMessages.name != null}
                  errorMessage={errorMessages.name}
                  isRequired
                />

                <Input
                  name="role"
                  label={t('fields.role')}
                  value={data.role}
                  onChange={handleChange}
                  isInvalid={errorMessages.role != null}
                  errorMessage={errorMessages.role}
                  isRequired
                />

                <DateInput
                  name="projectStart"
                  label={t('fields.projectStart')}
                  value={data.projectStart}
                  onChange={(val) => updateData('projectStart', val)}
                  isInvalid={errorMessages.projectStart != null}
                  errorMessage={errorMessages.projectStart}
                  isRequired
                  maxValue={today(getLocalTimeZone())}
                />

                <DateInput
                  name="projectEnd"
                  label={t('fields.projectEnd')}
                  value={data.projectEnd}
                  onChange={(val) => updateData('projectEnd', val)}
                  isInvalid={errorMessages.projectEnd != null}
                  errorMessage={errorMessages.projectEnd}
                  isClearable
                  onClear={() => updateData('projectEnd', null)}
                  maxValue={today(getLocalTimeZone())}
                />

                <Textarea
                  name="description"
                  label={t('fields.description')}
                  value={data.description}
                  onChange={handleChange}
                  isInvalid={errorMessages.description != null}
                  errorMessage={errorMessages.description}
                  isRequired
                  rows={6}
                />

                <div className="flex flex-col gap-6">
                  <h4 className={h4()}>{t('fields.links')}</h4>
                  {data.links.map((link, linkIndex) => (
                    <div
                      className="grid grid-cols-1 items-start gap-x-3 gap-y-3 md:grid-cols-[minmax(0,2fr)_minmax(0,1.35fr)_minmax(0,1.25fr)_2.5rem]"
                      key={link.uiId}
                    >
                      <Input
                        name="displayName"
                        label={t('fields.displayName')}
                        value={link.displayName}
                        onChange={(e: ChangeEvent<HTMLInputElement>) =>
                          handleLinkChange(link.uiId, e)
                        }
                        isRequired
                        isInvalid={errorMessages[`links[${linkIndex}].displayName`] != null}
                        errorMessage={errorMessages[`links[${linkIndex}].displayName`]}
                      />

                      <Input
                        name="url"
                        label={t('fields.url')}
                        value={link.url}
                        onChange={(e: ChangeEvent<HTMLInputElement>) =>
                          handleLinkChange(link.uiId, e)
                        }
                        isRequired
                        isInvalid={errorMessages[`links[${linkIndex}].url`] != null}
                        errorMessage={errorMessages[`links[${linkIndex}].url`]}
                      />

                      <Autocomplete
                        name="type"
                        label={t('fields.type')}
                        selectedKey={link.type}
                        onSelectionChange={(key) => handleLinkTypeChange(link.uiId, key as string)}
                        isRequired
                        isInvalid={errorMessages[`links[${linkIndex}].type`] != null}
                        errorMessage={errorMessages[`links[${linkIndex}].type`]}
                      >
                        {Object.values(ProjectLinkType).map((val) => (
                          <AutocompleteItem key={val} id={val}>
                            {val}
                          </AutocompleteItem>
                        ))}
                      </Autocomplete>

                      <Button
                        variant="danger"
                        type="button"
                        className="h-10 min-w-10 justify-self-start rounded-full px-0 md:mt-[1.625rem] md:justify-self-center"
                        onPress={() => removeLink(link.uiId)}
                      >
                        <FaMinus size={18} />
                      </Button>
                    </div>
                  ))}
                  <div className="grid grid-cols-1 md:grid-cols-[minmax(0,2fr)_minmax(0,1.35fr)_minmax(0,1.25fr)_2.5rem]">
                    <Button
                      className="h-10 min-w-10 justify-self-start rounded-full px-0 md:col-start-4 md:justify-self-center"
                      variant="primary"
                      type="button"
                      onPress={addLink}
                    >
                      <FaPlus size={18} />
                    </Button>
                  </div>
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
