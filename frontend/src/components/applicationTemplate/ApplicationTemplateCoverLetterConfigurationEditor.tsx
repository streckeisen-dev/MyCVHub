import { Textarea, Input } from '@/components/ui/Fields.tsx'
import { ChangeEvent, ReactNode, useMemo } from 'react'
import { CoverLetterStyleDto } from '@/types/coverletter/CoverLetterStyleDto.ts'
import { CoverLetterGallery } from '@/components/download/coverletter/CoverLetterGallery.tsx'
import { LanguageInput } from '@/components/input/LanguageInput.tsx'
import { Key } from '@react-types/shared'
import {
  ApplicationDocument,
  ApplicationDocumentsEditor
} from '@/components/applicationTemplate/ApplicationDocumentsEditor.tsx'

import { useTranslation } from 'react-i18next'
import { ErrorMessages } from '@/types/ErrorMessages.ts'
import { SwitchInput } from '@/components/input/SwitchInput.tsx'

export interface ApplicationTemplateCoverLetterData {
  style: string | undefined
  language: string | undefined
  mirrorProfileImage: boolean
  content: string
  closing: string
  documents: ApplicationDocument[]
}

export type ApplicationTemplateCoverLetterConfigurationEditorProps = Readonly<{
  styles: CoverLetterStyleDto[]
  config: ApplicationTemplateCoverLetterData
  onChange: (data: ApplicationTemplateCoverLetterData) => void
  errorMessages: ErrorMessages
}>

export function ApplicationTemplateCoverLetterConfigurationEditor(
  props: ApplicationTemplateCoverLetterConfigurationEditorProps
): ReactNode {
  const { styles, config, onChange, errorMessages } = props
  const { t } = useTranslation()

  const errors = useMemo(() => {
    return errorMessages
  }, [errorMessages])

  function clearError(key: string) {
    Object.keys(errors)
      .filter((errorKey) => errorKey.endsWith(key))
      .forEach((errorKey) => (errors[errorKey] = undefined))
  }

  function handleStyleSelected(key: string) {
    onChange({
      ...config,
      style: key
    })
    clearError('style')
  }

  function handleLanguageChange(value: Key | null) {
    onChange({
      ...config,
      language: (value as string) ?? undefined
    })
    clearError('language')
  }

  function handleMirrorProfileImageChange(_: string, value: boolean) {
    onChange({
      ...config,
      mirrorProfileImage: value
    })
  }

  function handleChange(e: ChangeEvent<HTMLInputElement>) {
    const name = e.currentTarget.name
    const value = e.currentTarget.value
    onChange({
      ...config,
      [name]: value
    })
    clearError(name)
  }

  function handleDocumentChange(documents: ApplicationDocument[]) {
    onChange({
      ...config,
      documents
    })
    clearError('documents')
  }

  return (
    <div className="flex w-full max-w-5xl flex-col gap-6">
      <div className="flex flex-col gap-2">
        <CoverLetterGallery
          styles={styles}
          compact
          selectedStyle={config.style}
          onSelect={handleStyleSelected}
        />
        {errors.style && <p className="text-danger text-sm">{errors.style}</p>}
      </div>

      <div className="flex w-full flex-col gap-5 rounded-lg border border-default-200 bg-surface p-5">
        <div className="max-w-md">
          <LanguageInput
            isRequired
            selectedKey={config.language ?? null}
            onSelectionChange={handleLanguageChange}
            errorMessage={errors.language}
          />
        </div>

        <SwitchInput
          name="mirrorProfileImage"
          onChange={handleMirrorProfileImageChange}
          isSelected={config.mirrorProfileImage}
        >
          {t('coverLetter.mirrorProfileImage')}
        </SwitchInput>

        <Textarea
          isRequired
          minRows={18}
          maxRows={50}
          name="content"
          label={t('coverLetter.content')}
          description={
            <p className="whitespace-break-spaces">{t('coverLetter.coverLetterHint')}</p>
          }
          value={config.content}
          onChange={handleChange}
          isInvalid={errors.content != null}
          errorMessage={errors.content}
        />

        <div className="max-w-3xl">
          <Input
            isRequired
            name="closing"
            label={t('fields.closing')}
            description={t('coverLetter.closingHint')}
            value={config.closing}
            onChange={handleChange}
            isInvalid={errors.closing != null}
            errorMessage={errors.closing}
          />
        </div>
      </div>

      <ApplicationDocumentsEditor
        documents={config.documents}
        onChange={handleDocumentChange}
        errorMessages={errorMessages}
      />
    </div>
  )
}
