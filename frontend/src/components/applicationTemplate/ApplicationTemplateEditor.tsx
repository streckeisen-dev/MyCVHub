import { FormEvent, Fragment, ReactNode, useState } from 'react'
import { Button, Form, Input } from '@heroui/react'
import { centerSection, h1 } from '@/styles/primitives.ts'
import { useTranslation } from 'react-i18next'
import { FormButtons } from '@/components/FormButtons.tsx'
import { ApplicationTemplateDto } from '@/types/applicationTemplate/ApplicationTemplateDto.ts'
import { ProfileDto } from '@/types/profile/ProfileDto.ts'
import { v7 as uuid } from 'uuid'
import { FaPlus, FaTrash } from 'react-icons/fa6'
import { ApplicationTemplateUpdateDto } from '@/types/applicationTemplate/ApplicationTemplateUpdateDto.ts'
import ApplicationTemplateApi from '@/api/ApplicationTemplateApi.ts'
import { ErrorMessages } from '@/types/ErrorMessages.ts'
import { extractFormErrors } from '@/helpers/FormHelper.ts'
import { RestError } from '@/types/RestError.ts'
import {
  CvConfigurationData,
  CvConfigurationEditor
} from '@/components/cv/CvConfigurationEditor.tsx'
import { CVStyleDto } from '@/types/cv/CVStyleDto.ts'

export type EditApplicationTemplateModalProps = Readonly<{
  onSave: (template: ApplicationTemplateDto) => void
  onCancel: () => void
  initialValue?: ApplicationTemplateDto
  profile: ProfileDto
  cvStyles: CVStyleDto[]
}>

interface ApplicationTemplateFormData {
  name: string
  cvConfig: CvConfigurationData
  documents: { id: string; name: string }[]
}

export function ApplicationTemplateEditor(props: EditApplicationTemplateModalProps): ReactNode {
  const { onSave, onCancel, initialValue, profile, cvStyles } = props
  const { t, i18n } = useTranslation()

  const [isSaving, setIsSaving] = useState<boolean>(false)
  const [data, setData] = useState<ApplicationTemplateFormData>({
    name: initialValue?.name ?? '',
    cvConfig: {
      cvStyle: initialValue?.cvConfiguration.cvStyle,
      cvContent:
        (initialValue?.cvConfiguration.includedCvContent && {
          workExperience: initialValue.cvConfiguration.includedCvContent.includedWorkExperience,
          education: initialValue.cvConfiguration.includedCvContent.includedEducation,
          projects: initialValue.cvConfiguration.includedCvContent.includedProjects,
          skills: initialValue.cvConfiguration.includedCvContent.includedSkills
        }) ??
        undefined,
      cvStyleOptions: initialValue?.cvConfiguration.cvStyleOptions
    },
    documents: initialValue?.documentChecklist?.map((name) => ({ id: uuid(), name })) ?? []
  })
  const [errorMessages, setErrorMessages] = useState<ErrorMessages>({})

  function handleNameChange(value: string) {
    setData((prev) => ({ ...prev, name: value }))
    clearError('name')
  }

  function handleCvConfigChange(value: CvConfigurationData) {
    setData((prev) => {
      return {
        ...prev,
        cvConfig: value
      }
    })
    clearError('cvConfiguration')
  }

  function handleDocumentChange(id: string, value: string) {
    setData((prev) => {
      return {
        ...prev,
        documents: [...prev.documents.filter((doc) => doc.id !== id), { id, name: value }]
      }
    })
    clearError('documentChecklist')
  }

  function clearError(field: string) {
    setErrorMessages((prev) => {
      return {
        ...prev,
        [field]: undefined
      } as ErrorMessages
    })
  }

  function handleAddDocument() {
    setData((prev) => {
      return {
        ...prev,
        documents: [...prev.documents, { id: uuid(), name: '' }]
      }
    })
  }

  function handleRemoveDocument(id: string) {
    setData((prev) => {
      return {
        ...prev,
        documents: prev.documents.filter((doc) => doc.id !== id)
      }
    })
  }

  async function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault()

    setIsSaving(true)
    const request: ApplicationTemplateUpdateDto = {
      id: initialValue?.id,
      name: data.name,
      cvConfiguration: {
        cvStyle: data.cvConfig.cvStyle,
        cvStyleOptions: data.cvConfig.cvStyleOptions,
        includedCvContent: data.cvConfig.cvContent && {
          includedWorkExperience: data.cvConfig.cvContent?.workExperience,
          includedEducation: data.cvConfig.cvContent?.education,
          includedProjects: data.cvConfig.cvContent?.projects,
          includedSkills: data.cvConfig.cvContent?.skills
        }
      },
      documentChecklist:
        data.documents.length > 0 ? data.documents.map((doc) => doc.name) : undefined
    }
    try {
      const saved = await ApplicationTemplateApi.saveApplicationTemplate(request, i18n.language)
      onSave(saved)
    } catch (e) {
      const error = (e as RestError).errorDto
      extractFormErrors(error, t('applicationTemplate.editor.saveError'), setErrorMessages, t)
    } finally {
      setIsSaving(false)
    }
  }

  return (
    <section className={centerSection()}>
      <h1 className={h1()}>
        {initialValue ? t('applicationTemplate.editor.edit') : t('applicationTemplate.editor.add')}
      </h1>
      <Form className="flex flex-col gap-6 mt-5" onSubmit={handleSubmit}>
        <Input
          isRequired
          label={t('fields.name')}
          value={data.name}
          onValueChange={handleNameChange}
          isInvalid={errorMessages.name != null}
          errorMessage={errorMessages.name}
        />

        <div>
          <CvConfigurationEditor
            profile={profile}
            cvStyles={cvStyles}
            config={data.cvConfig}
            onChange={handleCvConfigChange}
          />
          {errorMessages.cvConfiguration && (
            <p className="text-danger text-sm mt-1">{errorMessages.cvConfiguration}</p>
          )}
        </div>

        <div>
          <label className="text-default-500">{t('applicationTemplate.documentChecklist')}</label>
          <p className="text-default-400">{t('applicationTemplate.documentChecklistHint')}</p>
          <div className="grid grid-cols-12 gap-2 items-center mt-4">
            {data.documents.map((doc) => (
              <Fragment key={doc.id}>
                <Input
                  isRequired
                  className="col-span-10"
                  label={t('fields.documentName')}
                  value={doc.name}
                  onValueChange={(val) => handleDocumentChange(doc.id, val)}
                />
                <Button
                  className="col-span-2"
                  isIconOnly
                  color="danger"
                  onPress={() => handleRemoveDocument(doc.id)}
                  radius="full"
                >
                  <FaTrash />
                </Button>
              </Fragment>
            ))}
            <Button
              className="col-span-12"
              isIconOnly
              color="primary"
              onPress={handleAddDocument}
              radius="full"
            >
              <FaPlus />
            </Button>
          </div>
          {errorMessages.documentChecklist && (
            <p className="text-danger text-sm mt-1">{errorMessages.documentChecklist}</p>
          )}
        </div>

        <FormButtons onCancel={onCancel} isSaving={isSaving} />
      </Form>
    </section>
  )
}
