import { FormEvent, ReactNode, useState } from 'react'
import { Form, Input } from '@heroui/react'
import { centerSection, h1 } from '@/styles/primitives.ts'
import { useTranslation } from 'react-i18next'
import { FormButtons } from '@/components/btn/FormButtons.tsx'
import { ApplicationTemplateDto } from '@/types/applicationTemplate/ApplicationTemplateDto.ts'
import { ProfileDto } from '@/types/profile/ProfileDto.ts'
import { v7 as uuid } from 'uuid'
import { ApplicationTemplateUpdateDto } from '@/types/applicationTemplate/ApplicationTemplateUpdateDto.ts'
import ApplicationTemplateApi from '@/api/ApplicationTemplateApi.ts'
import { ErrorMessages } from '@/types/ErrorMessages.ts'
import { extractFormErrors } from '@/helpers/FormHelper.ts'
import { RestError } from '@/types/RestError.ts'
import {
  CvConfigurationData,
  CvConfigurationEditor
} from '@/components/download/cv/CvConfigurationEditor.tsx'
import { CVStyleDto } from '@/types/cv/CVStyleDto.ts'
import {
  ApplicationDocument,
  ApplicationDocumentsEditor
} from '@/components/applicationTemplate/ApplicationDocumentsEditor.tsx'

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
  documents: ApplicationDocument[]
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
    documents: initialValue?.documents?.map((name) => ({ id: uuid(), name })) ?? []
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

  function handleDocumentChange(docs: ApplicationDocument[]) {
    setData((prev) => {
      return {
        ...prev,
        documents: docs
      }
    })
    clearError('documents')
  }

  function clearError(field: string) {
    setErrorMessages((prev) => {
      return {
        ...prev,
        [field]: undefined
      } as ErrorMessages
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
      documents: data.documents.length > 0 ? data.documents.map((doc) => doc.name) : undefined
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

        <ApplicationDocumentsEditor
          documents={data.documents}
          onChange={handleDocumentChange}
          errorMessages={errorMessages}
        />

        <FormButtons onCancel={onCancel} isSaving={isSaving} />
      </Form>
    </section>
  )
}
